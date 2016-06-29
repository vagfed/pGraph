/*
   Copyright 2016 Federico Vagnini (vagnini@it.ibm.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
public class Parser_Nmon_Tokenized extends Parser {
	
	private final static byte	AIX 	= 0;
	private final static byte	LINUX	= 1;
	
	private byte operatingSystem = AIX;
	private byte smt = 0;
	

	
	public Parser_Nmon_Tokenized(ParserManager v) {
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
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		int i,j;
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		 
		GregorianCalendar	gc=null;	// last read time
		
		String			timeLabel="";
		String			cpuName;
		int				slot=-1;
				
		NmonTokenizer 	nt = new NmonTokenizer();
		String			key;
		int				numDisks = 0;
		String			currentKeys[];
		
		boolean suppressFCwarning = false;
		boolean suppressJFSwarning = false;
		boolean suppressWLMwarning = false;
		boolean suppressNetWarnings = false;
		
		
		float f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11;
		
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
			
			// Manage time label: it MUST be before the other labels!!!!!
			if (line.startsWith("ZZZZ")) {
				String label, time, date;
				int from, to;
				
				// ZZZZ,T0001,15:10:17,21-OCT-2015
				from = line.indexOf(',') + 1;
				to = line.indexOf(',',from);
				label=line.substring(from, to);
				
				from = to + 1;
				to  = line.indexOf(',',from);
				time=line.substring(from, to);
				date=line.substring(to+1);
				
				gc = parseDate(time,date,gc); 
				if (gc == null) {
					System.out.println("Error in line " + current_file_line_read + " " + line);
					continue;
				}
				
				if (gc.before(start) || gc.after(end))
					timeLabel = null;
				else
					timeLabel = label;	// Store label Txxxx for reference
				slot = getSlot(gc);						// slot for current time
				
				continue;
			}
			
			// Skip lines starting with ERROR
			if (line.startsWith("ERROR:"))
				continue;
			
			nt.parseString(line,current_file_line_read);
			key = nt.getString(0);
			if (key==null)
				continue;
			
			// Check OS type
			if ( key.equals("AAA") && line.toLowerCase().contains("linux"))
				operatingSystem = LINUX;
			
			/*
			// Manage time label: it MUST be before the other labels!!!!!
			if ( key.equals("ZZZZ") ) {
	
				gc = parseDate(nt.getString(2),nt.getString(3),gc);
				if (gc == null) {
					System.out.println("Error in line " + current_file_line_read + " " + line);
					continue;
				}
				
				if (gc.before(start) || gc.after(end))
					timeLabel = null;
				else
					timeLabel = nt.getString(1);	// Store label Txxxx for reference
				slot = getSlot(gc);						// slot for current time
				
				continue;
			}
			*/
			

				
			// Get System information
			if ( key.equals("BBBP") && line.contains("systemid") ){
				 	
				 	// Store the serial. Used to identify LPARs in the same CEC
				 	cecName=nt.getString(4).substring(0,9);
				 	
				 	continue;
			}
			
			// Get affinity info
			if ( key.equals("BBBP") && nt.getString(2).equals("lssrad") ){
				 	
				 	// First line is just empty
					if (nt.getNumTokens()==3) {
						ref1=srad=mem=cpu=null;
						continue;
					}
					
					// Just a header
					if ( nt.getString(3).startsWith("\"REF1"))
						continue;
					
					// New REF1
					if ( !nt.getString(3).startsWith("\" ") ) {
						// If we have a valid entry, add it!
						if (ref1!=null) {
							ref1=srad=mem=cpu=null;
						}
						ref1=nt.getString(3);
						ref1=ref1.substring(1, ref1.length()-1);
						continue;
					}
					
					// New data
					String ss[] = nt.getString(3).replaceAll("\"", "").trim().split(" {2,}");
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
			if ( key.equals("BBBP") && nt.getString(2).equals("lparstat -i") ){
				
					// First line is just empty
					if (nt.getNumTokens()==3)
						continue;
				
					String ss[] = nt.getString(3).replaceAll("\"", "").split(":");
					perfData.addTextLabel(PerfData.LPARSTATI, ss[0].trim(), ss[1].trim());
				
					continue;
			}
			
			// vmstat -v info
			if ( key.equals("BBBP") && nt.getString(2).equals("vmstat -v") ){
				
					// First line is just empty
					if (nt.getNumTokens()==3)
						continue;
					
					String ss[] = new String[2];
					String s = nt.getString(3).replaceAll("\"", "").trim();
					int pos = s.indexOf(' ');
					
					ss[0] = s.substring(pos+1);
					ss[1] = s.substring(0, pos);
				
					perfData.addTextLabel(PerfData.VMSTATV, ss[0], ss[1]);
				
					continue;
			}
			
			
			// ENDING vmstat -v info
			if ( key.equals("BBBP") && nt.getString(2).equals("ending vmstat -v") ){
				
					// First line is just empty
					if (nt.getNumTokens()==3)
						continue;
					
					String ss[] = new String[2];
					String s = nt.getString(3).replaceAll("\"", "").trim();
					int pos = s.indexOf(' ');
					
					ss[0] = s.substring(pos+1);
					ss[1] = s.substring(0, pos);
				
					perfData.addTextLabel(PerfData.VMSTATVEND, ss[0], ss[1]);
				
					continue;
			}

			
			if ( key.equals("BBBL") && nt.getString(2).equals("smt threads") ){
					 	
					 	// Store the serial. Used to identify LPARs in the same CEC
					 	smt=Byte.parseByte(nt.getString(3));
					 	perfData.setSmt_threads(smt);
					 	
					 	continue;
				}
			
			// Try to figure out SMT status
			if ( key.equals("BBBP") && nt.getString(2).equals("lparstat -i") ) {
				 
				if (nt.getString(3).endsWith("Dedicated-SMT\""))
					perfData.setSmtStatus(PerfData.SMT_SUPPOSED_ON);
				if (nt.getString(3).endsWith("Dedicated\""))
					perfData.setSmtStatus(PerfData.SMT_SUPPOSED_OFF);
			}
					
			
			
			// CHECK DISK, ESS, IOADAPT, NET, FC , SEA LABELS
			// Avoid if only CPU
			
			
			
			
			// If multiple LPAR we need diskAdapter and network data
			if ( !isTimeStamp(nt.getString(1))) {
				
				if (!key.equals("AAA") && !key.startsWith("BBB") 
						&& !key.startsWith("CPU") && !key.startsWith("SCPU") 
						&& !key.startsWith("PCPU") && !key.equals("ZZZZ")
						&& !key.equals("TOP") )
					nt.updateKeys();
				
				if ( key.equals("TOP") && nt.getNumTokens()>2 && !isTimeStamp(nt.getString(2)) )
					nt.updateKeys();
				
			/*	
				if ( key.equals("IOADAPT") ) {			
					nt.updateKeys();
					continue;
				}
				
				if ( key.startsWith("NET") ) {	
					nt.updateKeys();
					continue;
				}
			*/
				
			}
			
			if ( !multipleLPAR && !isTimeStamp(nt.getString(1)) ) {
				
				if ( key.startsWith("DISKBUSY") ) {
										
					if (avoidDisk)
						continue;
					
					numDisks = numDisks + nt.getNumTokens() - 2 ;
					
					if (firstRun && numDisks > MAX_DISKS) {
						avoidDisk = true;
						System.out.println("More than " + MAX_DISKS + " disks. Skipping disk data.");
						System.out.println("    - Deselect \"No disk data\" to include disk data and then press Zoom button.");
						System.out.println("    - Use pGraph.properties file and change MaxDisks variable.");
						continue;						
					}
					
					continue;
				}				
			}
		
			
			// STARTING FROM HERE ONLY DATA WITH VALID TIME STAMP IS READ
			
			if ( !nt.getString(1).equals(timeLabel)  &&
				 !( nt.getNumTokens()>2 && key.equals("TOP") && nt.getString(2).equals(timeLabel) )
						 )
				continue;

				
			
			// Check CPU data
			if ( key.startsWith("CPU") &&
				 key.indexOf("_USE")<0 ) {
				// NOT CPU_EC_USE, NOT CPU_VP_USE
				
				if ( key.equals("CPU_ALL") )
					cpuName=AVG_CPU;
				else
					cpuName=key;
					
				// If inactive CPU (due to SMT-off or DLPAR) skip
				if (nt.getNumTokens() < 6)
					continue;
				
				// Parse data

				f1 = nt.getValue(2);
				f2 = nt.getValue(3);
				f3 = nt.getValue(4);
								
				// Set data 		
				perfData.add(slot, PerfData.CPU, PerfData.US, cpuName, f1);
				perfData.add(slot, PerfData.CPU, PerfData.SY, cpuName, f2);
				perfData.add(slot, PerfData.CPU, PerfData.WA, cpuName, f3);
				perfData.add(slot, PerfData.CPU, PerfData.ID, cpuName, 100f - f1 - f2 - f3);	
				continue;		
			}
			
			
			// Check PCPU data
			if ( key.startsWith("PCPU") ) {
				
				if ( key.equals("PCPU_ALL") )
					cpuName=AVG_CPU;
				else
					cpuName=key;
					
				// If inactive CPU (due to SMT-off or DLPAR) skip
				if (nt.getNumTokens() < 6)
					continue;
				
				// Parse data
				f1 = nt.getValue(2);
				f2 = nt.getValue(3);
				f3 = nt.getValue(4);
				f4 = nt.getValue(5);
								
				// Set data 	
				perfData.add(slot, PerfData.PCPU, PerfData.P_TOT, cpuName, f1+f2+f3+f4);	
				
				continue;		
			}
			
			// Check MEM data
			if ( key.equals("MEM") ) {
				
				if (operatingSystem == AIX) {				
					// Parse data
					f1 = nt.getValueFromKey("Virtual total(MB)");
					f2 = nt.getValueFromKey("Real free(MB)");
					f3 = f2 * 1024 / 4;
					f4 = nt.getValueFromKey("Real total(MB)");
					
					// Set data 
					perfData.add(slot, PerfData.AVM, 	 f1 );
					perfData.add(slot, PerfData.FRE, 	 f2 );
					perfData.add(slot, PerfData.NUMFREE, f3 );
					perfData.add(slot, PerfData.RAM,	 f4 );
					
					f1 = nt.getValueFromKey("Size of the Compressed pool(MB)");
					f2 = nt.getValueFromKey("Size of true memory(MB)");
					f3 = nt.getValueFromKey("Expanded memory size(MB)");
					f4 = nt.getValueFromKey("Size of the Uncompressed pool(MB)");
					
					perfData.add(slot, PerfData.COMP_POOL, 	f1 );
					perfData.add(slot, PerfData.TRUE_MEM, 	f2 );
					perfData.add(slot, PerfData.EXP_MEM, 	f3 );	
					perfData.add(slot, PerfData.UNC_POOL, 	f4 );
		
					continue;
				} else if (operatingSystem == LINUX) {
					// Parse data
					f1 = nt.getValueFromKey("memtotal");
					f2 = nt.getValueFromKey("memfree");
					
					// Set data 
					perfData.add(slot, PerfData.RAM, 	 f1 );
					perfData.add(slot, PerfData.FRE, 	 f2 );
				
					continue;
				} else
					continue;
			}
			
			if ( !multipleLPAR && key.equals("MEMUSE") ) {

				// Parse data
				f1 = nt.getValueFromKey("%numperm");
				f2 = nt.getValueFromKey("%minperm");
				f3 = nt.getValueFromKey("%maxperm");
				f4 = nt.getValueFromKey("minfree");
				f5 = nt.getValueFromKey("maxfree");
				f6 = nt.getValueFromKey("%numclient");
				f7 = nt.getValueFromKey("%maxclient");
				
				// Set data				
				perfData.add(slot, PerfData.NUMPERM, f1 );
				perfData.add(slot, PerfData.MINPERM, f2 );
				perfData.add(slot, PerfData.MAXPERM, f3 );
				perfData.add(slot, PerfData.MINFREE, f4 );
				perfData.add(slot, PerfData.MAXFREE, f5 );
				perfData.add(slot, PerfData.NUMCLIENT, f6 );
				perfData.add(slot, PerfData.MAXCLIENT, f7 );
				
	
				continue;
							 	
			}

			if ( !multipleLPAR && key.equals("MEMPAGES4KB") ) {

				// Parse data
				f1 = nt.getValueFromKey("numframes") * 4 / 1024;
				f2 = nt.getValueFromKey("numfrb") * 4 / 1024;
				f3 = nt.getValueFromKey("pgspgins");
				f4 = nt.getValueFromKey("pgspgouts");
				f5 = nt.getValueFromKey("numvpages") * 4 / 1024;
				f6 = nt.getValueFromKey("pgexct");
				
				
				// Set data				
				perfData.add(slot, PerfData.NUMFRAMES4K, f1 );
				perfData.add(slot, PerfData.NUMFRB4K, f2 );	
				perfData.add(slot, PerfData.PI4K, f3 );
				perfData.add(slot, PerfData.PO4K, f4 );
				perfData.add(slot, PerfData.NUMVPAGES4K, f5 );
				perfData.add(slot, PerfData.FAULTS4K, f6 );
				
	
				continue;
							 	
			}
			
			if ( !multipleLPAR && key.equals("MEMPAGES64KB") ) {

				// Parse data
				f1 = nt.getValueFromKey("numframes") * 64 / 1024;
				f2 = nt.getValueFromKey("numfrb") * 64 / 1024;
				f3 = nt.getValueFromKey("pgspgins");
				f4 = nt.getValueFromKey("pgspgouts");
				f5 = nt.getValueFromKey("numvpages") * 64 / 1024;
				f6 = nt.getValueFromKey("pgexct");
				
				// Set data				
				perfData.add(slot, PerfData.NUMFRAMES64K, f1 );
				perfData.add(slot, PerfData.NUMFRB64K, f2 );
				perfData.add(slot, PerfData.PI64K, f3 );
				perfData.add(slot, PerfData.PO64K, f4 );
				perfData.add(slot, PerfData.NUMVPAGES64K, f5 );
				perfData.add(slot, PerfData.FAULTS64K, f6 );
	
				continue;
							 	
			}
			
			
			// Linux only
			if ( !multipleLPAR && key.equals("VM") ) {

				// Parse data
				f1 = nt.getValueFromKey("pswpin");
				f2 = nt.getValueFromKey("pswpout");
				
				// Set data				
				perfData.add(slot, PerfData.PI, f1 );
				perfData.add(slot, PerfData.PO, f2 );
	
				continue;
							 	
			}
			
			if ( !multipleLPAR && key.equals("PAGE") ) {

				// Parse data
				f1 = nt.getValueFromKey("pgsin");
				f2 = nt.getValueFromKey("pgsout");
				f3 = nt.getValueFromKey("reclaims");
				f4 = nt.getValueFromKey("scans");
				f5 = nt.getValueFromKey("cycles");
				f6 = nt.getValueFromKey("faults");
				
				// Set data				
				perfData.add(slot, PerfData.PI, f1 );
				perfData.add(slot, PerfData.PO, f2 );
				perfData.add(slot, PerfData.FR, f3 );
				perfData.add(slot, PerfData.SR, f4 );
				perfData.add(slot, PerfData.CY, f5 );
				perfData.add(slot, PerfData.FAULTS, f6 );
				

				// parse compressed pool paging
				f1 = nt.getValueFromKey("Compressed pool pgins");
				f2 = nt.getValueFromKey("Compressed pool pgouts");
				
				// Set data				
				perfData.add(slot, PerfData.CP_PI, f1 );
				perfData.add(slot, PerfData.CP_PO, f2 );
	
				continue;
							 	
			}
			
			// Check LPAR data
			if ( key.equals("LPAR") ) {

				// Parse data
				if (operatingSystem == AIX) {
					f1 = nt.getValueFromKey("PhysicalCPU");
					f2 = nt.getValueFromKey("virtualCPUs");
					f3 = nt.getValueFromKey("logicalCPUs");
					f4 = nt.getValueFromKey("poolCPUs");
					f5 = nt.getValueFromKey("entitled");
					f6 = nt.getValueFromKey("PoolIdle");
					f7 = nt.getValueFromKey("Folded");
					f8 = nt.getValueFromKey("VP_User%");
					f9 = nt.getValueFromKey("VP_Sys%");
					f10= nt.getValueFromKey("VP_Wait%");
					f11= nt.getValueFromKey("VP_Idle%");
					
				} else if (operatingSystem == LINUX){
					
					// LPAR,Shared CPU LPAR Stats rhel7-postg-vm01,PhysicalCPU,capped,shared_processor_mode,system_potential_processors,system_active_processors,pool_capacity,MinEntCap,partition_entitled_capacity,partition_max_entitled_capacity,MinProcs,Logical CPU,partition_active_processors,partition_potential_processors,capacity_weight,unallocated_capacity_weight,BoundThrds,MinMem,unallocated_capacity,pool_idle_time,smt_mode
					
					f1 = nt.getValueFromKey("PhysicalCPU"); 
					f2 = nt.getValueFromKey("partition_active_processors"); 
					f3 = nt.getValueFromKey("Logical CPU");
					f4 = nt.getValueFromKey("pool_capacity")/100;
					f5 = nt.getValueFromKey("partition_entitled_capacity");
					f6 = -1;	
					f7 = -1;
					f8 = f9 = f10 = f11 = -1;
				} else {
					f1 = f2 = f3 = f4 = f5 = f6 = f7 = f8 = f9 = f10 = f11 = -1;
				}

				
				// Set data				
				if (f1<=512)		perfData.add(slot, PerfData.PC,   f1 );
				if (f2<=512)		perfData.add(slot, PerfData.VP,   f2 );
				if (f3<=4096)		perfData.add(slot, PerfData.LP,   f3 );
				if (f4<=512)		perfData.add(slot, PerfData.POOL, f4 );
				if (f5<=512)		perfData.add(slot, PerfData.ENT,  f5 );
				if (f6<=512)		perfData.add(slot, PerfData.FREEPOOL, f6 );	
				if (f1>=0 && f1<=512 && f5>0 && f5<=512)
					perfData.add(slot, PerfData.EC, 100f*f1/f5 );
				if (f7<=512)		perfData.add(slot, PerfData.FOLDED,  f7 );
				
				if (f8<=100f)		perfData.add(slot, PerfData.VP_US,  f8 );
				if (f9<=100f)		perfData.add(slot, PerfData.VP_SY,  f9 );
				if (f10<=100f)		perfData.add(slot, PerfData.VP_WA,  f10 );
				if (f11<=100f)		perfData.add(slot, PerfData.VP_ID,  f11 );
			
				continue;		
			}
			
			// Check DISK data
			if ( !multipleLPAR && !avoidDisk && key.startsWith("DISK") ) {
				
				// Check if number of disk changed
				if (nt.getNumTokens() != nt.getKeys().length) {
					System.out.println("Warning: number of disks changed since nmon started. Skippping disk data.");
					avoidDisk=true;
					continue;
				}

				
				byte type;
				
				if (key.startsWith("DISKREADSERV"))
					type=PerfData.DSK_AVG_R;
				else if (key.startsWith("DISKWRITESERV"))
					type=PerfData.DSK_AVG_W;
				else if (key.startsWith("DISKBUSY"))
					type=PerfData.DSK_BUSY;
				else if (key.startsWith("DISKREAD"))
					type=PerfData.DSK_READKB;
				else if (key.startsWith("DISKWRITE"))
					type=PerfData.DSK_WRITEKB;
				else if (key.startsWith("DISKXFER"))
					type=PerfData.DSK_XFER;
				else if (key.startsWith("DISKBSIZE"))
					type=PerfData.DSK_BSIZE;
				else if (key.startsWith("DISKSERV"))
					type=PerfData.DSK_AVGSERV;
				else if (key.startsWith("DISKWAIT"))
					type=PerfData.DSK_AVGWAIT;
				else if (key.startsWith("DISKRIO"))
					type=PerfData.DSK_RPS;
				else if (key.startsWith("DISKWIO"))
					type=PerfData.DSK_WPS;
				else 
					continue;	// unknown line
					
				byte newType;
				
				currentKeys = nt.getKeys();
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					
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
									
				continue;
			}
			
			// Check ESS data
			if ( !multipleLPAR && key.startsWith("ESS") ) {	
				
				byte type;
				if (key.equals("ESSREAD"))
					type=PerfData.ESS_READKB;
				else if (key.equals("ESSWRITE"))
					type=PerfData.ESS_WRITEKB;
				else if (key.equals("ESSXFER"))
					type=PerfData.ESS_XFER;
				else
					continue;	// unknown line

				currentKeys = nt.getKeys();
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.ESS, type, diskName, f1);
				}
								
				continue;
			}	
			
			

		   	// Check ADAPTER data (also for multiple LPAR!)
			if ( key.equals("IOADAPT") ) {
				
				for (i=2; i<nt.getNumTokens(); i+=3) {
					f1 = nt.getValue(i);	// read
					f2 = nt.getValue(i+1);	// write
					f3 = nt.getValue(i+2);	// xfer
					
					diskName = nt.getKeys()[i].split("_")[0];

					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_READKB,  diskName, f1);
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WRITEKB, diskName, f2);
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_XFER,    diskName, f3);

			   } 
				
			   continue;
		   }
			

		   
		   // Check NETWORK data (when multiple LPAR we just need KB/s!)
		   if ( key.startsWith("NET") ) {
			   
			   String split[];
			   String type;
			   String keys[];
			   
			   if ( key.equals("NET") ) {			   
				   for (i=2; i<nt.getNumTokens(); i++) {
					   keys=nt.getKeys();
					   if (nt.getNumTokens() != keys.length) {
						   if (!suppressNetWarnings) {
							   System.out.println("Network line " + current_file_line_read +": networks have chaged! Skipping network data!");
							   suppressNetWarnings=true;
						   }
						   break;
					   }
					   split = keys[i].split("-");
					   diskName = split[0];
					   for (j=1; j<split.length-2; j++)
						   diskName = diskName + "-" + split[j];
					   type = split[split.length-2];
					   
					   if (type.equals("read"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_READKB,  diskName, nt.getValue(i));
					   if (type.equals("write"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_WRITEKB,  diskName, nt.getValue(i));
				   }
				   continue;		   
			   }
			   
			   if ( !multipleLPAR && key.equals("NETPACKET") ) {
				   for (i=2; i<nt.getNumTokens(); i++) {
					   keys=nt.getKeys();
					   if (nt.getNumTokens() != keys.length) {
						   if (!suppressNetWarnings) {
							   System.out.println("Network line " + current_file_line_read +": networks have chaged! Skipping network data!");
							   suppressNetWarnings=true;
						   }
						   break;
					   }
					   split = keys[i].split("-");
					   diskName = split[0];
					   for (j=1; j<split.length-2; j++)
						   diskName = diskName + "-" + split[j];
					   type = split[split.length-1];
					   
					   if (type.equals("reads/s"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_READS,  diskName, nt.getValue(i));
					   if (type.equals("writes/s"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_WRITES,  diskName, nt.getValue(i));
				   }
				   continue;		   
			   }
			   
			   if ( !multipleLPAR && key.equals("NETERROR") ) {
				   for (i=2; i<nt.getNumTokens(); i++) {
					   keys=nt.getKeys();
					   if (nt.getNumTokens() != keys.length) {
						   if (!suppressNetWarnings) {
							   System.out.println("Network line " + current_file_line_read +": networks have chaged! Skipping network data!");
							   suppressNetWarnings=true;
						   }
						   break;
					   }
					   split = keys[i].split("-");
					   diskName = split[0];
					   for (j=1; j<split.length-2; j++)
						   diskName = diskName + "-" + split[j];
					   type = split[split.length-1];
					   
					   if (type.equals("ierrs"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_IERRORS,  diskName, nt.getValue(i));
					   if (type.equals("oerrs"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_OERRORS,  diskName, nt.getValue(i));
					   if (type.equals("collisions"))
						   perfData.add(slot, PerfData.NETWORK, PerfData.NET_COLLISIONS,  diskName, nt.getValue(i));
				   }
				   continue;		   
			   }										
			   
			   continue;
			}		   		
			
			if ( !multipleLPAR && key.equals("PROC") ) {

				// Parse data
				// PROC,Processes aix7nim,Runnable,Swap-in,pswitch,syscall,read,write,fork,exec,sem,msg,asleep_bufio,asleep_rawio,asleep_diocio
				f1 = nt.getValueFromKey("Runnable");
				f2 = nt.getValueFromKey("Swap-in");
				f3 = nt.getValueFromKey("pswitch");
				f4 = nt.getValueFromKey("syscall");
				f5 = nt.getValueFromKey("read");
				f6 = nt.getValueFromKey("write");
				f7 = nt.getValueFromKey("fork");
				f8 = nt.getValueFromKey("exec");
				
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
			
			
			if ( !multipleLPAR && !avoidTop && key.equals("TOP") ) {
				
				// Parse data
				// TOP,+PID,Time,%CPU,%Usr,%Sys,Threads,Size,ResText,ResData,CharIO,%RAM,Paging,Command,WLMclas
				f1 = nt.getValueFromKey("%CPU");
				f2 = nt.getValueFromKey("ResText");
				f3 = nt.getValueFromKey("ResData");
				
				// Sanity check: sometimes huge numbers. Ready for 512way :-)
				if (f1<=51200)
					perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_CPU, 
							nt.getStringFromKey("Command")+":" + nt.getStringFromKey("+PID"), f1);

				perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_RESTEXT, 
						nt.getStringFromKey("Command")+":"+nt.getStringFromKey("+PID"), f2);
				perfData.add(slot, PerfData.TOPPROC, PerfData.TOP_RESDATA, 
						nt.getStringFromKey("Command")+":"+nt.getStringFromKey("+PID"), f3);
				
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
			
	
			if ( !multipleLPAR && key.equals("JFSFILE") ) {	
				
				currentKeys = nt.getKeys();
				
				// JFSFILE,JFS Filespace %Used amlfcl61asp001,/,/home,/usr,/var,/tmp,/admin, ...
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressJFSwarning) {
						System.out.println("JFSFILE line "+ current_file_line_read +" skipped: filesystem number has changed.");
						suppressJFSwarning = true;
					}
					continue;
				}
								
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.FS, PerfData.SPACEUSED, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("JFSINODE") ) {
				
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressJFSwarning) {
						System.out.println("JFSINODE line "+ current_file_line_read +" skipped: filesystem number has changed.");
						suppressJFSwarning = true;
					}
					continue;
				}
								
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.FS, PerfData.INODEUSED, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("WLMCPU") ) {	
				
				// WLMCPU,CPU percent for 5 Classes aix7nim,Unclassified,Unmanaged,Default,Shared,System	
				currentKeys = nt.getKeys();
						
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressWLMwarning) {
						System.out.println("WLM line "+ current_file_line_read +" skipped: WLM class number has changed.");
						suppressWLMwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.WPAR, PerfData.WPAR_CPU, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("WLMMEM") ) {		
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressWLMwarning) {
						System.out.println("WLM line "+ current_file_line_read +" skipped: WLM class number has changed.");
						suppressWLMwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.WPAR, PerfData.WPAR_MEM, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("WLMBIO") ) {	
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressWLMwarning) {
						System.out.println("WLM line "+ current_file_line_read +" skipped: WLM class number has changed.");
						suppressWLMwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.WPAR, PerfData.WPAR_DISK, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("PROCAIO") ) {	
				// PROCAIO,Asynchronous I/O amlfcl61asp001,aioprocs,aiorunning,aiocp
				// Parse data
				f1 = nt.getValueFromKey("aioprocs");
				f2= nt.getValueFromKey("aiorunning");
				f3 = nt.getValueFromKey("aiocp");

				perfData.add(slot, PerfData.NUM_AIO, 	f1);
				perfData.add(slot, PerfData.ACTIVE_AIO, f2);
				perfData.add(slot, PerfData.CPU_AIO, 	f3);
				
				continue;
			}
			
			if ( !multipleLPAR && key.equals("FCREAD") ) {		
				//FCREAD,Fibre Channel Read KB/s,fcs0,fcs1,fcs2,fcs3
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCREAD, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("FCWRITE") ) {	
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCWRITE, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("FCXFERIN") ) {	
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCXFERIN, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.equals("FCXFEROUT") ) {	
				currentKeys = nt.getKeys();
				
				if (nt.getNumTokens() != currentKeys.length) {
					if (!suppressFCwarning) {
						System.out.println("Missing FC labels in FCxxxx. Skipping data");
						suppressFCwarning = true;
					}
					continue;
				}
				
				for (i=2; i<currentKeys.length; i++) {
					f1 = nt.getValue(i);
					diskName = currentKeys[i];
					perfData.add(slot, PerfData.FCSTAT, PerfData.FCXFEROUT, diskName, f1);
				}
			}
			
			if ( !multipleLPAR && key.startsWith("SEA") ) {
				
				
				// SEA,Shared Ethernet Adapter p8i-vios1,ent5-read-KB/s,ent5-write-KB/s
				// SEAPACKET,Shared Ethernet Adapter Packets p8i-vios1,ent5-reads/s,ent5-writes/s
				   
				String split[];
				String type;
				
				   
				if ( key.equals("SEA") ) {			   
					for (i=2; i<nt.getNumTokens(); i++) {
						split = nt.getKeys()[i].split("-");
						diskName = split[0];
						type = split[1];
					   
						if (type.equals("read"))
						   perfData.add(slot, PerfData.SEA, PerfData.NET_READKB,  diskName, nt.getValue(i));
						if (type.equals("write"))
						   perfData.add(slot, PerfData.SEA, PerfData.NET_READKB,  diskName, nt.getValue(i));
					}
					continue;		   
				}
				
				if ( key.equals("SEAPACKET") ) {			   
					for (i=2; i<nt.getNumTokens(); i++) {
						split = nt.getKeys()[i].split("-");
						diskName = split[0];
						type = split[1];
					   
						if (type.equals("reads/s"))
						   perfData.add(slot, PerfData.SEA, PerfData.NET_READS,  diskName, nt.getValue(i));
						if (type.equals("writes/s"))
						   perfData.add(slot, PerfData.SEA, PerfData.NET_WRITES,  diskName, nt.getValue(i));
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

	
	
	private GregorianCalendar parseDate(String time, String date, GregorianCalendar last) {
		int h,m,s;		// hour minute second
		int year,month,day;
		String str;
		
		
		
		
		try {			
			h = Integer.parseInt(time.substring(0,2));
			m = Integer.parseInt(time.substring(3,5));
			s = Integer.parseInt(time.substring(6));
		} catch (NumberFormatException e) {
			return null;
		}
			
		// Check if date is provided (nmon 10)
		if (date!=null) {
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
	
	
	/*
	 * Returns TRUE if s is a timestamp T[0-9]+
	 */
	private boolean isTimeStamp(String s) {
		if (s==null || s.length()<2 || !s.startsWith("T"))
			return false;
		
		s = s.substring(1);
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	
}
