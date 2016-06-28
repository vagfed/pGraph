/*
 * Created on Jul 16, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.io.BufferedReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;


/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Parser_Nmon extends Parser {
	
	private final static byte	AIX 	= 0;
	private final static byte	LINUX	= 1;
	
	private byte operatingSystem = AIX;
	private byte smt = 0;
	

	
	public Parser_Nmon(ParserManager v) {
		super();
		manager = v;
	}

	
	/*
	 * Scan nmon file to find time limits
	 */
	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {}
		
		if (start!=null && end!=null)
			valid=true;
	}

	/* (non-Javadoc)
	 * @see pGraph.Parser#scanTimeLimits()
	 */
	private void parseTimeLimits() throws Exception {
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		GregorianCalendar gc=null;	// last day read
		
		final byte 	MAXWAIT = 10;		// Wait up to 10 lines for AAA before giving up.
		boolean 	aaaFound = false;

		br = getReader();

		while (true) {
			line = br.readLine();

			if (line == null) {
				if (start!=null && end!=null)
					valid=true;
				break;
			}
			
			total_lines++;
			
			// Check if it is a valid nmon file
			if (!aaaFound && line.startsWith("AAA"))
				aaaFound = true;
			
			if (!aaaFound && total_lines>MAXWAIT)
				return;
				
			// Look only for time stamps
			if (!line.startsWith("ZZZZ"))
				continue;
				
			// 0123456789012345678901234567890
			// ZZZZ,T0001,12:01:37,16-JUL-2005
			
			// Check if enough data is present
			if (line.length() < 19)
				continue;
				
			gc = parseDate(line,gc);
			if (gc == null) {
				System.out.println("Error in line " + total_lines + " " + line);
				continue;
			}
				
			if (start == null || gc.before(start))
				start = gc;
			
			if (end == null || gc.after(end))
				end = gc;
		}
	}
	

	
	
	/*
	 * Parse nmon file and fill parser's data structures
	 */
	public void parseData(boolean firstParse, boolean lastParse) {
		
		// If start and end are not know, abort
		if (start==null || end==null)
			return;
		
		if (firstParse)
			avoidTop=false;
		
		
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of nmon data (line "+current_file_line_read+")");
			if (lastParse)
				endOfData();
		}
		
		firstRun=false;
	}

	/* (non-Javadoc)
	 * @see pGraph.Parser#parseData()
	 */
	@SuppressWarnings("unchecked")
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		int i,j;
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		 
		Vector			v;

		GregorianCalendar	gc=null;	// last read time
		
		String			timeLabel="";
		String			cpuName;
		int				slot=-1;
		
		String 			lastType = null;	// last data type read eg. DISKREAD, ESSREAD
		Vector			diskLabel = new Vector();	// List of disk labels
		Vector			essLabel = new Vector();	// List of ess labels
		Vector			scsiLabel = new Vector();
		Vector			networkLabel = new Vector();
		Vector			fsLabel = new Vector();
		Vector			wlmLabel = new Vector();
		Vector			fcLabel = new Vector();
		Vector			seaLabel = new Vector();
		
		boolean suppressFCwarning = false;
		boolean suppressJFSwarning = false;
		
		int diskRead=0;				// disks found in previous lines: only for multiple lines of same data
		int essRead=0;				// disks found in previous lines: only for multiple lines of same data
		
		float f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12;
		
		String diskName = null;
		
		String ref1,srad,mem,cpu;
		ref1=srad=mem=cpu=null;
		
		
		// Reset performance data if this is first file
		if (firstParse) {
			perfData.setLimits(start, end);
			lines_read=0;
		}
		
		current_file_line_read=0;	// reset counter for current file
		
		br = getReader();
		
		// Cycle on  data
		while (true) {
			line = readLineAndShowProgress(br);
				
			if (line == null)
				break;	
			
			// Skip empty lines
			if (line.length() == 0)
				continue;
			
			v = splitLine(line);
			
			// System.out.println(line);   // DEBUG
			
			// Check OS type
			if ( ((String)v.elementAt(0)).equals("AAA") 
					&& line.toLowerCase().contains("linux"))
				operatingSystem = LINUX;
					 
			// Manage time label: it MUST be before the other labels!!!!!
			if ( ((String)v.elementAt(0)).equals("ZZZZ") ) {
	
				gc = parseDate(v,gc);	
				if (gc == null) {
					System.out.println("Error in line " + current_file_line_read + " " + line);
					continue;
				}
				
				if (gc.before(start) || gc.after(end))
					timeLabel = null;
				else
					timeLabel = (String)v.elementAt(1);	// Store label Txxxx for reference
				slot = getSlot(gc);						// slot for current time
					
				// reset disk read count
				diskRead=0;
				essRead=0;
				
				continue;
			}
			
			/*
			if (skip)
				continue;
			*/
				
			// Get System information
			if ( ((String)v.elementAt(0)).equals("BBBP") && 
				 v.size()>=5 &&
				 ((String)v.elementAt(3)).indexOf("systemid")>=0 ){
				 	
				 	// Store the serial. Used to identify LPARs in the same CEC
				 	cecName=((String)v.elementAt(4)).substring(0,9);
				 	
				 	continue;
			}
			
			// Get affinity info
			if ( ((String)v.elementAt(0)).equals("BBBP") && 
				 v.size()>=3 &&
				 ((String)v.elementAt(2)).equals("lssrad") ){
				 	
				 	// First line is just empty
					if (v.size()==3) {
						ref1=srad=mem=cpu=null;
						continue;
					}
					
					// Just a header
					if ( ((String)v.elementAt(3)).startsWith("\"REF1"))
						continue;
					
					// New REF1
					if ( !((String)v.elementAt(3)).startsWith("\" ")) {
						// If we have a valid entry, add it!
						if (ref1!=null) {
							ref1=srad=mem=cpu=null;
						}
						ref1=((String)v.elementAt(3));
						ref1=ref1.substring(1, ref1.length()-1);
						continue;
					}
					
					// New data
					String ss[] = ((String)v.elementAt(3)).replaceAll("\"", "").trim().split(" {2,}");
					srad = ss[0];
					mem = ss[1];
					if (ss.length>=3)
						cpu = ss[2];
					else
						cpu=null;
					perfData.addLssrad(ref1, srad, mem, cpu);
					
					continue;
			}
			
			
			// lparstat -i info
			if ( ((String)v.elementAt(0)).equals("BBBP") && 
				 v.size()>=3 &&
				 ((String)v.elementAt(2)).equals("lparstat -i") ){
				
					// First line is just empty
					if (v.size()==3)
						continue;
				
					String ss[] = ((String)v.elementAt(3)).replaceAll("\"", "").split(":");
					perfData.addTextLabel(PerfData.LPARSTATI, ss[0].trim(), ss[1].trim());
				
					continue;
			}
			
			// vmstat -v info
			if ( ((String)v.elementAt(0)).equals("BBBP") && 
				 v.size()>=3 &&
				 ((String)v.elementAt(2)).equals("vmstat -v") ){
				
					// First line is just empty
					if (v.size()==3)
						continue;
					
					String ss[] = new String[2];
					String s = ((String)v.elementAt(3)).replaceAll("\"", "").trim();
					int pos = s.indexOf(' ');
					
					ss[0] = s.substring(pos+1);
					ss[1] = s.substring(0, pos);
				
					perfData.addTextLabel(PerfData.VMSTATV, ss[0], ss[1]);
				
					continue;
			}
			
			
			// ENDING vmstat -v info
			if ( ((String)v.elementAt(0)).equals("BBBP") && 
				 v.size()>=3 &&
				 ((String)v.elementAt(2)).equals("ending vmstat -v") ){
				
					// First line is just empty
					if (v.size()==3)
						continue;
					
					String ss[] = new String[2];
					String s = ((String)v.elementAt(3)).replaceAll("\"", "").trim();
					int pos = s.indexOf(' ');
					
					ss[0] = s.substring(pos+1);
					ss[1] = s.substring(0, pos);
				
					perfData.addTextLabel(PerfData.VMSTATVEND, ss[0], ss[1]);
				
					continue;
			}

			
			if ( ((String)v.elementAt(0)).equals("BBBL") && 
					 v.size()>=4 &&
					 ((String)v.elementAt(2)).equals("smt threads") ){
					 	
					 	// Store the serial. Used to identify LPARs in the same CEC
					 	smt=Byte.parseByte(((String)v.elementAt(3)));
					 	perfData.setSmt_threads(smt);
					 	
					 	continue;
				}
			
			// Try to figure out SMT status
			if ( ((String)v.elementAt(0)).equals("BBBP") &&
					v.size()==4 &&
					((String)v.elementAt(2)).equals("lparstat -i") ) {
				 
				if (((String)v.elementAt(3)).endsWith("Dedicated-SMT\""))
					perfData.setSmtStatus(PerfData.SMT_SUPPOSED_ON);
				if (((String)v.elementAt(3)).endsWith("Dedicated\""))
					perfData.setSmtStatus(PerfData.SMT_SUPPOSED_OFF);
			}
					
			
			
			// CHECK DISK, ESS, IOADAPT, NET, FC , SEA LABELS
			// Avoid if only CPU
			
			// If multiple LPAR we need diskAdapter and network data
			if ( !((String)v.elementAt(1)).startsWith("T") ) {
				
				if ( ((String)v.elementAt(0)).equals("IOADAPT") ) {				
					// Store labels in same order
					for (i=2; i<v.size(); i+=3) {
						j = ((String)v.elementAt(i)).indexOf("_");
						scsiLabel.add( ((String)v.elementAt(i)).substring(0,j) );
					}
					continue;
				}
				
				if ( ((String)v.elementAt(0)).equals("NET") ) {	
					// Store labels in same order
					for (i=2; i<2+(v.size()-2)/2; i++) {
						j = ((String)v.elementAt(i)).indexOf("-");
						networkLabel.add( ((String)v.elementAt(i)).substring(0,j) );
					}
					continue;
				}
				
			}
			
			if ( !multipleLPAR && !((String)v.elementAt(1)).startsWith("T") ) {
				
				if ( ((String)v.elementAt(0)).startsWith("DISKBUSY") ) {
					
					if (avoidDisk)
						continue;

					// Store labels in same order
					for (i=2; i<v.size(); i++)
						diskLabel.add(v.elementAt(i));
					
					if (firstRun && diskLabel.size() > MAX_DISKS) {
						avoidDisk = true;
						System.out.println("More than " + MAX_DISKS + " disks. Skipping disk data.");
						System.out.println("    - Deselect \"No disk data\" to include disk data and then press Zoom button.");
						System.out.println("    - Use pGraph.properties file and change MaxDisks variable.");
						continue;						
					}
					
					continue;
				}
				
				if ( ((String)v.elementAt(0)).equals("ESSREAD") ) {					
					// Store labels in same order
					for (i=2; i<v.size(); i++)
						essLabel.add(v.elementAt(i));
					continue;
				}			
				
				if ( ((String)v.elementAt(0)).equals("JFSFILE") ) {	
					// Store labels in same order
					for (i=2; i<v.size(); i++)
						fsLabel.add(v.elementAt(i));
					continue;
				}
				
				if ( ((String)v.elementAt(0)).equals("WLMCPU") ) {	
					// Store labels in same order
					for (i=2; i<v.size(); i++)
						wlmLabel.add(v.elementAt(i));
					continue;
				}
				
				if ( ((String)v.elementAt(0)).equals("FCREAD") ) {	
					// Store labels in same order
					for (i=2; i<v.size(); i++)
						fcLabel.add(v.elementAt(i));
					continue;
				}
				
				if ( ((String)v.elementAt(0)).equals("SEA") ) {	
					// Store labels in same order
					for (i=2; i<2+(v.size()-2)/2; i++) {
						j = ((String)v.elementAt(i)).indexOf("-");
						seaLabel.add( ((String)v.elementAt(i)).substring(0,j) );
					}
					continue;
				}
				
			}
		
			
			// STARTING FROM HERE ONLY DATA WITH VALID TIME STAMP IS READ
			
			if ( !((String)v.elementAt(1)).equals(timeLabel)  &&
				 !( v.size()>2 && ((String)v.elementAt(0)).equals("TOP") && ((String)v.elementAt(2)).equals(timeLabel) )
						 )
				continue;

				
			
			// Check CPU data
			if ( ((String)v.elementAt(0)).startsWith("CPU") &&
				 ((String)v.elementAt(0)).indexOf("_USE")<0 ) {
				// NOT CPU_EC_USE, NOT CPU_VP_USE
				
				if ( ((String)v.elementAt(0)).equals("CPU_ALL") )
					cpuName=AVG_CPU;
				else
					cpuName=(String)v.elementAt(0);
					
				// If inactive CPU (due to SMT-off or DLPAR) skip
				if (v.size() < 6)
					continue;
				
				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(2));
					f2 = Float.parseFloat((String)v.elementAt(3));
					f3 = Float.parseFloat((String)v.elementAt(4));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
								
				// Set data 		
				perfData.add(slot, PerfData.CPU, PerfData.US, cpuName, f1);
				perfData.add(slot, PerfData.CPU, PerfData.SY, cpuName, f2);
				perfData.add(slot, PerfData.CPU, PerfData.WA, cpuName, f3);
				perfData.add(slot, PerfData.CPU, PerfData.ID, cpuName, 100f - f1 - f2 - f3);	
				continue;		
			}
			
			
			// Check PCPU data
			if ( ((String)v.elementAt(0)).startsWith("PCPU") ) {
				
				if ( ((String)v.elementAt(0)).equals("PCPU_ALL") )
					cpuName=AVG_CPU;
				else
					cpuName=(String)v.elementAt(0);
					
				// If inactive CPU (due to SMT-off or DLPAR) skip
				if (v.size() < 6)
					continue;
				
				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(2));
					f2 = Float.parseFloat((String)v.elementAt(3));
					f3 = Float.parseFloat((String)v.elementAt(4));
					f4 = Float.parseFloat((String)v.elementAt(5));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
								
				// Set data 	
				perfData.add(slot, PerfData.PCPU, PerfData.P_TOT, cpuName, f1+f2+f3+f4);	
				
				/*
				perfData.add(slot, PerfData.PCPU, PerfData.P_US, cpuName, f1);
				perfData.add(slot, PerfData.PCPU, PerfData.P_SY, cpuName, f2);
				perfData.add(slot, PerfData.PCPU, PerfData.P_WA, cpuName, f3);
				perfData.add(slot, PerfData.PCPU, PerfData.P_ID, cpuName, f4);
				perfData.add(slot, PerfData.PCPU, PerfData.P_TOT, cpuName, f1+f2+f3+f4);
				*/
				continue;		
			}
			
			// Check MEM data
			if ( ((String)v.elementAt(0)).equals("MEM") ) {
				
				if (operatingSystem == AIX) {				
					// Parse data
					try {
						f1 = Float.parseFloat((String)v.elementAt(7));
						f2 = Float.parseFloat((String)v.elementAt(4));
						f3 = Float.parseFloat((String)v.elementAt(4))*1024/4;
						f4 = Float.parseFloat((String)v.elementAt(6));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line);
						continue;
					}
					
					// Set data 
					perfData.add(slot, PerfData.AVM, 	 f1 );
					perfData.add(slot, PerfData.FRE, 	 f2 );
					perfData.add(slot, PerfData.NUMFREE, f3 );
					perfData.add(slot, PerfData.RAM,	 f4 );
					
					if (v.size()<12)
						continue;
					// Parse compressed memory data, if any
					try {
						f1 = Float.parseFloat((String)v.elementAt(8));
						f2 = Float.parseFloat((String)v.elementAt(9));
						f3 = Float.parseFloat((String)v.elementAt(10));
						f4 = Float.parseFloat((String)v.elementAt(11));
					} catch (NumberFormatException e) {
						continue;
					}
					perfData.add(slot, PerfData.COMP_POOL, 	f1 );
					perfData.add(slot, PerfData.TRUE_MEM, 	f2 );
					perfData.add(slot, PerfData.EXP_MEM, 	f3 );	
					perfData.add(slot, PerfData.UNC_POOL, 	f4 );
		
					continue;
				} else if (operatingSystem == LINUX) {
					// Parse data
					try {
						f1 = Float.parseFloat((String)v.elementAt(2))/1024;
						f2 = Float.parseFloat((String)v.elementAt(6))/1024;
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line);
						continue;
					}
					
					// Set data 
					perfData.add(slot, PerfData.RAM, 	 f1 );
					perfData.add(slot, PerfData.FRE, 	 f2 );
				
					continue;
				} else
					continue;
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("MEMUSE") ) {

				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(2));
					f2 = Float.parseFloat((String)v.elementAt(3));
					f3 = Float.parseFloat((String)v.elementAt(4));
					f4 = Float.parseFloat((String)v.elementAt(5));
					f5 = Float.parseFloat((String)v.elementAt(6));
					if (v.size()>=9) {
						f6 = Float.parseFloat((String)v.elementAt(7));
						f7 = Float.parseFloat((String)v.elementAt(8));
					} else {
						f6 = -1;
						f7 = -1;
					}
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Set data				
				perfData.add(slot, PerfData.NUMPERM, f1 );
				perfData.add(slot, PerfData.MINPERM, f2 );
				perfData.add(slot, PerfData.MAXPERM, f3 );
				perfData.add(slot, PerfData.MINFREE, f4 );
				perfData.add(slot, PerfData.MAXFREE, f5 );
				if (v.size()>=9) {
					perfData.add(slot, PerfData.NUMCLIENT, f6 );
					perfData.add(slot, PerfData.MAXCLIENT, f7 );
				}
	
				continue;
							 	
			}	
			
			// Linux only
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("VM") && 
					!((String)v.elementAt(2)).equals("Paging and Virtual Memory") ) {

				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(11));
					f2 = Float.parseFloat((String)v.elementAt(12));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Sanity check: linux provides strange values....
				if (f1>1e6) f1=-1;
				if (f2>1e6) f2=-1;
				
				// Set data				
				perfData.add(slot, PerfData.PI, f1 );
				perfData.add(slot, PerfData.PO, f2 );
	
				continue;
							 	
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("PAGE") ) {

				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(5));
					f2 = Float.parseFloat((String)v.elementAt(6));
					f3 = Float.parseFloat((String)v.elementAt(7));
					f4 = Float.parseFloat((String)v.elementAt(8));
					f5 = Float.parseFloat((String)v.elementAt(9));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Set data				
				perfData.add(slot, PerfData.PI, f1 );
				perfData.add(slot, PerfData.PO, f2 );
				perfData.add(slot, PerfData.FR, f3 );
				perfData.add(slot, PerfData.SR, f4 );
				perfData.add(slot, PerfData.CY, f5 );	
				
				if (v.size() <= 10)
					continue;
				
				// parse compressed pool paging
				try {
					f1 = Float.parseFloat((String)v.elementAt(10));
					f2 = Float.parseFloat((String)v.elementAt(11));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Set data				
				perfData.add(slot, PerfData.CP_PI, f1 );
				perfData.add(slot, PerfData.CP_PO, f2 );
	
				continue;
							 	
			}
			
			// Check LPAR data
			if ( ((String)v.elementAt(0)).equals("LPAR") ) {

				// Parse data
				try {
					if (operatingSystem == AIX) {
						f1 = Float.parseFloat((String)v.elementAt(2));
						f2 = Integer.parseInt((String)v.elementAt(3));
						f3 = Integer.parseInt((String)v.elementAt(4));
						f4 = Integer.parseInt((String)v.elementAt(5));
						f5 = Float.parseFloat((String)v.elementAt(6));
						f6 = Float.parseFloat((String)v.elementAt(8));
						
						if (v.size()>21) {
							f7 = Float.parseFloat((String)v.elementAt(21));	// FOLDED
						} else {
							f7 = -1;
						}
						f8 = Float.parseFloat((String)v.elementAt(17));	// VP_US
						f9 = Float.parseFloat((String)v.elementAt(18));	// VP_SY
						f10 = Float.parseFloat((String)v.elementAt(19));	// VP_WA
						f11 = Float.parseFloat((String)v.elementAt(20));	// VP_ID
						
					} else if (operatingSystem == LINUX){
						f1 = Float.parseFloat((String)v.elementAt(2));
						f2 = Integer.parseInt((String)v.elementAt(12));
						f3 = -1;
						f4 = -1;
						f5 = Float.parseFloat((String)v.elementAt(9));
						f6 = -1;	
						f7 = -1;
						f8 = f9 = f10 = f11 = -1;
					} else {
						f1 = f2 = f3 = f4 = f5 = f6 = f7 = f8 = f9 = f10 = f11 = -1;
					}
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Set data				
				if (f1<=192)		perfData.add(slot, PerfData.PC,   f1 );
				if (f2<=192)		perfData.add(slot, PerfData.VP,   f2 );
				if (f3<=1536)	perfData.add(slot, PerfData.LP,   f3 );
				if (f4<=192)		perfData.add(slot, PerfData.POOL, f4 );
				if (f5<=192)		perfData.add(slot, PerfData.ENT,  f5 );
				if (f6>0 && f6<=192)
					perfData.add(slot, PerfData.FREEPOOL, f6 );	
				if (f1>=0 && f1<=192 && f5>0 && f5<=192)
					perfData.add(slot, PerfData.EC, 100f*f1/f5 );
				if (f7<=192)		perfData.add(slot, PerfData.FOLDED,  f7 );
				
				if (f8<=100f)		perfData.add(slot, PerfData.VP_US,  f8 );
				if (f9<=100f)		perfData.add(slot, PerfData.VP_SY,  f9 );
				if (f10<=100f)		perfData.add(slot, PerfData.VP_WA,  f10 );
				if (f11<=100f)		perfData.add(slot, PerfData.VP_ID,  f11 );
			
				continue;		
			}
			
			// Check DISK data
			if ( !multipleLPAR && !avoidDisk && ((String)v.elementAt(0)).startsWith("DISK") ) {
				
				// If label is not the same as before, it is new set of data (DISKBUSY-DISKBUSY1-DISKREAD-DISKREAD1...)
				if ( lastType==null || !((String)v.elementAt(0)).startsWith(lastType) ) {
					diskRead = 0;
					lastType = (String)v.elementAt(0);
				} 
				
				if (diskRead+v.size()-2>diskLabel.size()) {
					System.out.println("Warning: new disks have been added since nmon started. Skippping disk data.");
					avoidDisk=true;
					continue;
				}
				
				//if ( operatingSystem != AIX)
				//	continue;
				
				byte type;
				
				if (((String)v.elementAt(0)).startsWith("DISKREADSERV"))
					type=PerfData.DSK_AVG_R;
				else if (((String)v.elementAt(0)).startsWith("DISKWRITESERV"))
					type=PerfData.DSK_AVG_W;
				else if (((String)v.elementAt(0)).startsWith("DISKBUSY"))
					type=PerfData.DSK_BUSY;
				else if (((String)v.elementAt(0)).startsWith("DISKREAD"))
					type=PerfData.DSK_READKB;
				else if (((String)v.elementAt(0)).startsWith("DISKWRITE"))
					type=PerfData.DSK_WRITEKB;
				else if (((String)v.elementAt(0)).startsWith("DISKXFER"))
					type=PerfData.DSK_XFER;
				else if (((String)v.elementAt(0)).startsWith("DISKBSIZE"))
					type=PerfData.DSK_BSIZE;
				else if (((String)v.elementAt(0)).startsWith("DISKSERV"))
					type=PerfData.DSK_AVGSERV;
				else if (((String)v.elementAt(0)).startsWith("DISKWAIT"))
					type=PerfData.DSK_AVGWAIT;
				else if (((String)v.elementAt(0)).startsWith("DISKRIO"))
					type=PerfData.DSK_RPS;
				else if (((String)v.elementAt(0)).startsWith("DISKWIO"))
					type=PerfData.DSK_WPS;
				else 
					continue;	// unknown line
					
				byte newType;
				
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line);
						break;
					}
					diskName = (String)diskLabel.elementAt(i-2+diskRead);
					
					// Treat EMC disks as ESS disks
					if (diskName.startsWith("hdiskpower")) {
						switch (type) {
							case PerfData.DSK_READKB:  	newType=PerfData.ESS_READKB;  	break;
							case PerfData.DSK_WRITEKB: 	newType=PerfData.ESS_WRITEKB; 	break;
							case PerfData.DSK_XFER:    	newType=PerfData.ESS_XFER;    	break;
							case PerfData.DSK_AVGSERV:  newType=PerfData.ESS_AVGSERV;   break;
							case PerfData.DSK_AVGWAIT:  newType=PerfData.ESS_AVGWAIT;   break;
							
							default: continue;
						}
						perfData.add(slot, PerfData.ESS, newType, diskName, f1);
					} else if (diskName.startsWith("dac"))
						perfData.add(slot, PerfData.DAC, type, diskName, f1);
					else
						perfData.add(slot, PerfData.DISK, type, diskName, f1);
				}
				
				diskRead += (v.size()-2);
					
				continue;
			}
			
			// Check ESS data
			if ( !multipleLPAR && ((String)v.elementAt(0)).startsWith("ESS") ) {
					
				// If label is not the same as before, it is new set of data
				if ( !((String)v.elementAt(0)).equals(lastType) )
					essRead = 0;
				lastType = (String)v.elementAt(0);					
				
				byte type;
				if (((String)v.elementAt(0)).equals("ESSREAD"))
					type=PerfData.ESS_READKB;
				else if (((String)v.elementAt(0)).equals("ESSWRITE"))
					type=PerfData.ESS_WRITEKB;
				else if (((String)v.elementAt(0)).equals("ESSXFER"))
					type=PerfData.ESS_XFER;
				else
					continue;	// unknown line

				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line);
						break;
					}
					perfData.add(slot, PerfData.ESS, type, (String)essLabel.elementAt(i-2+essRead), f1);
				}
								
				continue;
			}	
			
			

		   	// Check ADAPTER data (also for multiple LPAR!)
			if ( ((String)v.elementAt(0)).equals("IOADAPT") ) {
							
				for (i=2, j=0; i<v.size(); i+=3, j++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));		// read
						f2 = Float.parseFloat((String)v.elementAt(i+1));	// write
						f3 = Float.parseFloat((String)v.elementAt(i+2));	// xfer
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line);
						break;
					}
					
					// Sanity check: sometimes nmon provides huge invalid data
					if (f1 > 10000000 || f2 > 10000000 || f3>10000000)
						continue;

					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_READKB,  (String)scsiLabel.elementAt(j), f1);
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WRITEKB, (String)scsiLabel.elementAt(j), f2);
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_XFER,    (String)scsiLabel.elementAt(j), f3);

			   }   
						
			   continue;
		   }
			

		   
		   // Check NETWORK data (when multiple LPAR we just need KB/s!)
		   if ( ((String)v.elementAt(0)).startsWith("NET") ) {
			   
			   if ( ((String)v.elementAt(0)).equals("NET") ) {
				   for (i=2, j=0; i<2+(v.size()-2)/2; i++, j++) {
					   try {
						   f1 = Float.parseFloat((String)v.elementAt(i));		// read
						   f2 = Float.parseFloat((String)v.elementAt(i+networkLabel.size())); // write
						} catch (NumberFormatException e) {
							System.out.println("Error parsing line " + current_file_line_read + ": " + line);
							break;
						}
					   
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_READKB,  (String)networkLabel.elementAt(j), f1);
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_WRITEKB, (String)networkLabel.elementAt(j), f2);
				  }
				  continue;		   
			   }
			   
			   if ( !multipleLPAR && ((String)v.elementAt(0)).equals("NETPACKET") ) {
				   for (i=2, j=0; i<2+(v.size()-2)/2; i++, j++) {
					   try {
						   f1 = Float.parseFloat((String)v.elementAt(i));		// read
						   f2 = Float.parseFloat((String)v.elementAt(i+networkLabel.size())); // write
						} catch (NumberFormatException e) {
							System.out.println("Error parsing line " + current_file_line_read + ": " + line);
							break;
						}
					   
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_READS,  (String)networkLabel.elementAt(j), f1);
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_WRITES, (String)networkLabel.elementAt(j), f2);
				  }
				  continue;		   
			   }
			   
			   if ( !multipleLPAR && ((String)v.elementAt(0)).equals("NETERROR") ) {
				   for (i=2, j=0; i<2+(v.size()-2)/3; i++, j++) {
					   try {
						   f1 = Float.parseFloat((String)v.elementAt(i));		// ierror
						   f2 = Float.parseFloat((String)v.elementAt(i+networkLabel.size())); // oerror
						   f3 = Float.parseFloat((String)v.elementAt(i+networkLabel.size()*2)); // collisions
						} catch (NumberFormatException e) {
							System.out.println("Error parsing line " + current_file_line_read + ": " + line);
							break;
						}
					   
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_IERRORS,   (String)networkLabel.elementAt(j), f1);
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_OERRORS,   (String)networkLabel.elementAt(j), f2);
					   perfData.add(slot, PerfData.NETWORK, PerfData.NET_COLLISIONS,(String)networkLabel.elementAt(j), f3);
				  }
				  continue;		   
			   }										
			   
			   continue;
			}		   		
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("PROC") ) {

				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(2));
					f2 = Float.parseFloat((String)v.elementAt(3));
					f3 = Float.parseFloat((String)v.elementAt(4));
					f4 = Float.parseFloat((String)v.elementAt(5));
					f5 = Float.parseFloat((String)v.elementAt(6));
					f6 = Float.parseFloat((String)v.elementAt(7));
					f7 = Float.parseFloat((String)v.elementAt(8));
					f8 = Float.parseFloat((String)v.elementAt(9));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Set data				
				perfData.add(slot, PerfData.RUNQ, f1 );
				perfData.add(slot, PerfData.SWQ,  f2 );
				perfData.add(slot, PerfData.PSW,  f3 );
			
				perfData.add(slot, PerfData.SYSC,  f4 );
				perfData.add(slot, PerfData.READ,  f5 );
				perfData.add(slot, PerfData.WRITE, f6 );
				perfData.add(slot, PerfData.FORK,  f7 );
				perfData.add(slot, PerfData.EXEC,  f8 );
				
				continue;
							 	
			}
			
			
			if ( !multipleLPAR && !avoidTop && ((String)v.elementAt(0)).equals("TOP") ) {
				
				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(3));
					f2 = Float.parseFloat((String)v.elementAt(8));
					f3 = Float.parseFloat((String)v.elementAt(9));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				// Sanity check: sometimes huge numbers. Ready for 128way :-)
				if (f1<=12800)
					perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_CPU, 
								(String)v.elementAt(13)+":"+(String)v.elementAt(1), f1);
				else
					perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_CPU, 
							(String)v.elementAt(13)+":"+(String)v.elementAt(1), 0);
				perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_RESTEXT, 
							(String)v.elementAt(13)+":"+(String)v.elementAt(1), f2);
				perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_RESDATA, 
							(String)v.elementAt(13)+":"+(String)v.elementAt(1), f3);
				
				// If too many top process data, skip it completely
				if (perfData.getNumTopProc()>=MAX_TOPPROC) {
					avoidTop = true;
					perfData.invalidateTop();
					System.out.println("Too many TOP processes. Limit of "+MAX_TOPPROC+ " has been reached: skipping TOP data.");
					System.out.println("    - Select a smaller time period to show TOP information.");
					System.out.println("    - Use pGraph.properties file and change MaxTopProcs variable to higher value.");
					//System.out.println("Too many processes for TOP data handling: skipping TOP data.");
					//System.out.println("    Please select a smaller time period to show TOP information.");
				}
				
				continue;
			}
			
	
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("JFSFILE") ) {	
				if (v.size()-2 != fsLabel.size()) {
					if (!suppressJFSwarning) {
						System.out.println("JFSFILE line "+ current_file_line_read +" skipped: filesystem number has changed.");
						suppressJFSwarning = true;
					}
					continue;
				}
				for (i=2; i<v.size(); i++) {
					if (((String)v.elementAt(i)).contains("nan"))		// due to Linux problems: "nan" or "-nan"
						continue;
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)fsLabel.elementAt(i-2);
					perfData.add(slot, PerfData.FS, PerfData.SPACEUSED, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("JFSINODE") ) {
				if (v.size()-2 != fsLabel.size()) {
					if (!suppressJFSwarning) {
						System.out.println("JFSINODE line "+ current_file_line_read +" skipped: filesystem number has changed.");
						suppressJFSwarning = true;
					}
					continue;
				}
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)fsLabel.elementAt(i-2);
					perfData.add(slot, PerfData.FS, PerfData.INODEUSED, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("WLMCPU") ) {			
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)wlmLabel.elementAt(i-2);
					perfData.add(slot, PerfData.WPAR, PerfData.WPAR_CPU, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("WLMMEM") ) {			
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)wlmLabel.elementAt(i-2);
					perfData.add(slot, PerfData.WPAR, PerfData.WPAR_MEM, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("WLMBIO") ) {			
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)wlmLabel.elementAt(i-2);
					perfData.add(slot, PerfData.WPAR, PerfData.WPAR_DISK, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("PROCAIO") ) {							
				// Parse data
				try {
					f1 = Float.parseFloat((String)v.elementAt(2));
					f2 = Float.parseFloat((String)v.elementAt(3));
					f3 = Float.parseFloat((String)v.elementAt(4));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					continue;
				}
				
				perfData.add(slot, PerfData.NUM_AIO, 	f1);
				perfData.add(slot, PerfData.ACTIVE_AIO, f2);
				perfData.add(slot, PerfData.CPU_AIO, 	f3);
				
				continue;
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("FCREAD") ) {		
				if (fcLabel.size() != v.size()-2) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)fcLabel.elementAt(i-2);
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCREAD, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("FCWRITE") ) {	
				if (fcLabel.size() != v.size()-2) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)fcLabel.elementAt(i-2);
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCWRITE, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("FCXFERIN") ) {	
				if (fcLabel.size() != v.size()-2) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)fcLabel.elementAt(i-2);
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCXFERIN, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).equals("FCXFEROUT") ) {	
				if (fcLabel.size() != v.size()-2) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				for (i=2; i<v.size(); i++) {
					try {
						f1 = Float.parseFloat((String)v.elementAt(i));
					} catch (NumberFormatException e) {
						System.out.println("Error parsing line " + current_file_line_read + ": " + line+ " element #"+i);
						continue;
					}
					diskName = (String)fcLabel.elementAt(i-2);
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCXFEROUT, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && ((String)v.elementAt(0)).startsWith("SEA") ) {
				   
				   if ( ((String)v.elementAt(0)).equals("SEA") ) {
					   for (i=2, j=0; i<2+(v.size()-2)/2; i++, j++) {
						   try {
							   f1 = Float.parseFloat((String)v.elementAt(i));		// read
							   f2 = Float.parseFloat((String)v.elementAt(i+seaLabel.size())); // write
							} catch (NumberFormatException e) {
								System.out.println("Error parsing line " + current_file_line_read + ": " + line);
								break;
							}
						   
						   perfData.add(slot, PerfData.SEA, PerfData.NET_READKB,  (String)seaLabel.elementAt(j), f1);
						   perfData.add(slot, PerfData.SEA, PerfData.NET_WRITEKB, (String)seaLabel.elementAt(j), f2);
					  }
					  continue;		   
				   }
				   
				   if ( ((String)v.elementAt(0)).equals("SEAPACKET") ) {
					   for (i=2, j=0; i<2+(v.size()-2)/2; i++, j++) {
						   try {
							   f1 = Float.parseFloat((String)v.elementAt(i));		// read
							   f2 = Float.parseFloat((String)v.elementAt(i+seaLabel.size())); // write
							} catch (NumberFormatException e) {
								System.out.println("Error parsing line " + current_file_line_read + ": " + line);
								break;
							}
						   
						   perfData.add(slot, PerfData.SEA, PerfData.NET_READS,  (String)seaLabel.elementAt(j), f1);
						   perfData.add(slot, PerfData.SEA, PerfData.NET_WRITES, (String)seaLabel.elementAt(j), f2);
					  }
					  continue;		   
				   }										
				   
				   continue;
				}
			
			
		}
			
		br.close();
		
		if (lastParse) {
			perfData.endOfData();
			// Checkers TBD
		}
		
				
			
	}
	
	

	
	/*
	 * Input line is a set of tokens delimited by a comma character.
	 * Create a Vector containing the ordered set of String tokens
	 */
	@SuppressWarnings("unchecked")
	private Vector splitLine (String s) {
		Vector result = new Vector();
		int begin=0;		// first token's char 
		int end;			// last token's char
		
		// Get each token, except for last one
		while ( (end=s.indexOf(',',begin)) >= 0) {
			result.add(s.substring(begin,end));
			begin = end+1;
		}
		
		// Manage last token
		result.add(s.substring(begin));
		
		return result;
	}
	
	private GregorianCalendar parseDate(String line, GregorianCalendar last) {
		return parseDate(splitLine(line),last);
	}
	
	@SuppressWarnings("unchecked")
	private GregorianCalendar parseDate(Vector v, GregorianCalendar last) {
		int h,m,s;		// hour minute second
		int year,month,day;
		String str;
		
		String time;
		String date;
		
		
		
		// ZZZZ,T0001,12:01:37,16-JUL-2005
		time = (String)v.elementAt(2);
		
		try {			
			h = Integer.parseInt(time.substring(0,2));
			m = Integer.parseInt(time.substring(3,5));
			s = Integer.parseInt(time.substring(6));
		} catch (NumberFormatException e) {
			return null;
		}
			
		// Check if date is provided (nmon 10)
		if (v.size()==4) {
			date = (String)v.elementAt(3);
			day=Integer.parseInt(date.substring(0,2));
			year=Integer.parseInt(date.substring(7));
			str=date.substring(3,6);
				
			month=Calendar.JANUARY;
			if (str.equals("JAN")) month=Calendar.JANUARY; else
			if (str.equals("FEB")) month=Calendar.FEBRUARY; else
			if (str.equals("MAR")) month=Calendar.MARCH; else
			if (str.equals("APR")) month=Calendar.APRIL; else
			if (str.equals("MAY")) month=Calendar.MAY; else
			if (str.equals("JUN")) month=Calendar.JUNE; else
			if (str.equals("JUL")) month=Calendar.JULY; else
			if (str.equals("AUG")) month=Calendar.AUGUST; else
			if (str.equals("SEP")) month=Calendar.SEPTEMBER; else
			if (str.equals("OCT")) month=Calendar.OCTOBER; else
			if (str.equals("NOV")) month=Calendar.NOVEMBER; else
			if (str.equals("DEC")) month=Calendar.DECEMBER;
			
			return new GregorianCalendar(
							year,
							month,
							day,
							h,m,s);
		} else {
			// No date provided. Assume start in 1970-JAN-01
			// Check midnight passing
			
			if (last == null)
				return new GregorianCalendar(
					1970,
					Calendar.JANUARY,
					1,
					h,m,s);
					
			if (h < last.get(Calendar.HOUR_OF_DAY))
				return new GregorianCalendar(
					1970,
					Calendar.JANUARY,
					last.get(Calendar.DAY_OF_MONTH)+1,
					h,m,s);	
			else	
				return new GregorianCalendar(
					1970,
					Calendar.JANUARY,
					last.get(Calendar.DAY_OF_MONTH),
					h,m,s);					
		}	
	}


}
