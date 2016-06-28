/*
 * Created on Jul 3, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.io.BufferedReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
//import java.util.Vector;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Parser_Vmstat extends Parser {
	
	private int logicalCPU = -1;			// #LP in system 
	private float entitlement = -1;		// ent in system
	private long memory = -1;			// RAM in 4k pages
	private float logicalMemory = -1;		// Logical memory in GB
	
	

	/*
	 * Scan vmstat file to find time limits
	 */
	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {}
		
		if (start!=null && end!=null)
			valid=true;
	}
	
	
	public Parser_Vmstat(ParserManager v) {
		super();
		manager = v;
	}
	
	

	/* (non-Javadoc)
	 * @see pGraph.Parser#scanTimeLimits()
	 */
	private void parseTimeLimits() throws Exception {
		BufferedReader	br;			// make buffered reads
		String			time;		// time label
		int				day=1;		// conventional day. Increase after midnight
		
		final byte 	MAXWAIT = 20;		// Wait up to 20 lines for AAA before giving up.
		
		valid=false;
		
		br = getReader();
		
		////////////// NEW NEW NEW
		
		FileTokenizer ft = new FileTokenizer(br,fileName);
		ft.setSeparator(' ');
		
		int numTokens;
		GregorianCalendar curr;
		
		// Wait for "hr mi se" string to detect it is a vmstat input
		boolean found=false;
		while (!found && total_lines<MAXWAIT && (numTokens=ft.readLine())>=0) {
			total_lines++;
			if (numTokens >= 3 &&
					ft.getStringToken(numTokens-3).equals("hr") &&
					ft.getStringToken(numTokens-2).equals("mi") &&
					ft.getStringToken(numTokens-1).equals("se") )
				found = true;
		}
		if (!found)
			return;	// it is not a vmstat input	
		
		while ( (numTokens=ft.readLine())>=0 ) {
			total_lines++;
			time = ft.getStringToken(numTokens-1);
			if (time.length()==8 &&
					time.charAt(2)==':' &&
					time.charAt(5)==':') {
				try {
					curr = new GregorianCalendar(
							1970,
							Calendar.JANUARY,
							day,
							Integer.parseInt(time.substring(0,2)),
							Integer.parseInt(time.substring(3,5)),
							Integer.parseInt(time.substring((6)))	); 
				} catch (NumberFormatException nfe) {
					continue;
				}
				
				if (start==null) {
					start = curr;
					end = (GregorianCalendar)start.clone();
				} else {
					if (curr.before(end)) {
						curr.add(Calendar.DAY_OF_MONTH,1);
						day++;
					}
					end = curr;					
				}
			}
		}

		br.close();
		
		
		////////////// OLD OLD OLD
		
		
		/*
		
		// skip leading lines until dashes
		do {
			line = br.readLine();
			total_lines++;
		} while (line!=null && !line.startsWith("-"));
		
		// Get header line
		line = br.readLine();
		
		if (line.indexOf("hr mi se")<0)
			return;
		
		total_lines++;
			
		// Skip first line: averages from boot
		line = br.readLine();
		
		total_lines++;
		
		// Read first valid line and detect "first"
		line = br.readLine();
		total_lines++;
		time = line.substring(line.lastIndexOf(' ')+1);
		start = new GregorianCalendar(
			1970,
			Calendar.JANUARY,
			day,
			Integer.parseInt(time.substring(0,2)),
			Integer.parseInt(time.substring(3,5)),
			Integer.parseInt(time.substring((6)))	);
		
		// Look for last sample
		end = (GregorianCalendar)start.clone();
		GregorianCalendar curr = null;
		
		while (true) {
			line = br.readLine();

			if (line == null)
				break;
			
			total_lines++;
							
			// skip headers
			if (line.startsWith("kthr") || 
				line.startsWith("--") || 
				line.startsWith(" r") ||
				line.startsWith("System") )
				continue;
				
			// Get time. If truncated, skip line
			time = line.substring(line.lastIndexOf(' ')+1);
			if (time.length()!=8)
				continue;
			curr = new GregorianCalendar(
				1970,
				Calendar.JANUARY,
				day,
				Integer.parseInt(time.substring(0,2)),
				Integer.parseInt(time.substring(3,5)),
				Integer.parseInt(time.substring((6)))	);
				
			// If curr < end, midnight has passed
			if (curr.before(end)) {
				curr.add(Calendar.DAY_OF_MONTH,1);
				day++;
			}
				
			// update end
			end = curr;
		}

		// Close file
		br.close();
		
		//perfData.setTimeLimits(start, end);
		
		*/
	}
	
	/*
	 * Parse vmstat file and fill parser's data structures
	 */
	public void parseData(boolean firstParse, boolean lastParse) {
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of vmstat data.");
			if (lastParse)
				endOfData();
		}
	}

	/* (non-Javadoc)
	 * @see pGraph.Parser#parseData()
	 */
	@SuppressWarnings("unchecked")
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		int i;
		BufferedReader	br;			// make buffered reads
		int				slot;		// time slot in data tables
		GregorianCalendar gc;		// last time read
		int				day=1;		// conventional day. Increase after midnight
		int				hour=0; 
		String			s;
		
		float f1;
		
		
		byte	group[] = null;
		byte	type[]  = null;
				
	
		
		// Reset performance data if this is first file
		if (firstParse)
			perfData.setLimits(start, end);
		
		br = getReader();
		
		FileTokenizer ft = new FileTokenizer(br,fileName);
		ft.setSeparator(' ');
		
		int numTokens=0;
		boolean found=false;
		
		// Wait for first "System configuration" line
		found=false;
		while (!found && (numTokens=readLineAndShowProgress(ft))>=0) {
			if (numTokens >= 3 &&
					ft.getStringToken(0).equalsIgnoreCase("System") &&
					ft.getStringToken(1).equalsIgnoreCase("configuration:")  )
				found = true;
		}
		if (!found)
			return;	// it is not a vmstat input	
		updateSysCfg(ft, numTokens);
			
		// Wait for "hr mi se" string to detect available fields	
		found=false;
		while (!found && (numTokens=readLineAndShowProgress(ft))>=0) {
			if (numTokens >= 3 &&
					ft.getStringToken(numTokens-3).equals("hr") &&
					ft.getStringToken(numTokens-2).equals("mi") &&
					ft.getStringToken(numTokens-1).equals("se") )
				found = true;
		}
		if (!found)
			return;	// it is not a vmstat input	
		
		
		// Detect available fields
		group = new byte[numTokens-3];
		type  = new byte[numTokens-3];
		for (i=0; i<numTokens-3; i++) {
			group[i]=type[i]=-1;
		}
		
		for (i=0; i<numTokens-3; i++) {
			s = ft.getStringToken(i);
			if (s.equals("r")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.RUNQ;
			} else if (s.equals("b")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.SWQ;
			} else if (s.equals("avm")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.AVM;
			} else if (s.equals("fre")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.FRE;
			} else if (s.equals("pi")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.PI;
			} else if (s.equals("po")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.PO;
			} else if (s.equals("fr")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.FR;
			} else if (s.equals("sr")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.SR;
			} else if (s.equals("cy")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.CY;
			} else if (s.equals("sy")) {
				
				// vmstat has TWO "cy" labels!!! :-((
				s = ft.getStringToken(i-1);
				if (s.equals("in")) {		
					group[i] = PerfData.SYSTEM;
					type[i]  = PerfData.SYSC;
				} else if (s.equals("us")) {
					group[i] = PerfData.CPU;
					type[i]  = PerfData.SY;
				}				
			} else if (s.equals("cs")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.PSW;
			} else if (s.equals("us")) {
				group[i] = PerfData.CPU;
				type[i]  = PerfData.US;
			} else if (s.equals("wa")) {
				group[i] = PerfData.CPU;
				type[i]  = PerfData.WA;
			} else if (s.equals("id")) {
				group[i] = PerfData.CPU;
				type[i]  = PerfData.ID;
			} else if (s.equals("pc")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.PC;
			} else if (s.equals("ec")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.EC;
			} else if (s.equals("hpi")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.HYPPAG_IN;
			} else if (s.equals("hpit")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.HYPPAG_TIME;
			} else if (s.equals("pmem")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.PHYS_MEM;
			} else if (s.equals("loan")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.LOAN_MEM;
			} else if (s.equals("alp")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.USED_LP;
			} else if (s.equals("flp")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.FREE_LP;
			} else if (s.equals("p")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.WQPS;	
			} else if (s.equals("fi")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.FI;	
			} else if (s.equals("fo")) {
				group[i] = PerfData.SYSTEM;
				type[i]  = PerfData.FO;	
			}
		}
		
		
		// Now manage data and configuration change
		while ( (numTokens=readLineAndShowProgress(ft))>=0 ) {
			
			// Check if data
			if (numTokens == group.length+1 ) {
				
				// get time
				s = ft.getStringToken(numTokens-1);
				if (s.length()==8 &&
						s.charAt(2)==':' &&
						s.charAt(5)==':') {
					
					// Check if midnight has passed
					i=Integer.parseInt(s.substring(0,2)); // actual hour
					if ( i<hour ) 
						day++;	// midnight has passed
					hour=i;
					
					try {
						gc = new GregorianCalendar(
								1970,
								Calendar.JANUARY,
								day,
								Integer.parseInt(s.substring(0,2)),
								Integer.parseInt(s.substring(3,5)),
								Integer.parseInt(s.substring((6)))	); 
					} catch (NumberFormatException nfe) {
						System.out.println("Cannot parse time stamp in line " + lines_read + ". Skipping line.");
						continue;
					}
					slot = getSlot(gc);
				} else {
					System.out.println("Cannot parse time stamp in line " + lines_read + ". Skipping line.");
				continue;
				}
								
				
				for (i=0; i<numTokens-1; i++) {
					f1 = ft.getFloatToken(i);
					if (f1<0)
						continue;
					
					if (group[i]==PerfData.SYSTEM) {
						if ( type[i]==PerfData.AVM || type[i]==PerfData.FRE)
							f1 = f1 * 4 / 1024;
						perfData.add(slot, type[i], f1);
					} else if (group[i]==PerfData.CPU)
						perfData.add(slot, PerfData.CPU, type[i], AVG_CPU, f1);					
				}	
				
				if (logicalCPU>=0)		perfData.add(slot, PerfData.LP, logicalCPU);
				if (entitlement>=0)		perfData.add(slot, PerfData.ENT, entitlement);
				if (memory>=0)			perfData.add(slot, PerfData.RAM, memory*4/1024);
				if (logicalMemory>=0)	perfData.add(slot, PerfData.LOGICAL_MEM, logicalMemory);
				
				continue;
			}
			
			// Check if configuration change due to DLPAR, skip a line
			if (numTokens >= 3 &&
					ft.getStringToken(0).toLowerCase().equals("system") &&
					ft.getStringToken(1).toLowerCase().equals("configuration") &&
					ft.getStringToken(2).toLowerCase().equals("changed")  ) {
				readLineAndShowProgress(ft);
				continue;
			}
			
			// Update configuration when needed
			if (numTokens >= 3 &&
					ft.getStringToken(0).toLowerCase().equals("system") &&
					ft.getStringToken(1).toLowerCase().equals("configuration:") ) {
				updateSysCfg(ft, numTokens);
				continue;
			}
				
		}
		
		br.close();
		
		if (lastParse)
			perfData.endOfData();
		
		
	}
	
/*	
	@SuppressWarnings("unchecked")
	private void int_parseData_OLD(boolean firstParse, boolean lastParse) throws Exception {
		int i;
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		Vector			v;
		boolean			splpar=false;	// true if uLPAR
		boolean			ams=false;		// true if Active Memory Sharing
		int				slot;		// time slot in data tables
		GregorianCalendar gc;		// last time read
		int				day=1;		// conventional day. Increase after midnight
		int				hour=0; 
		String			s;
		
		float f1,f2,f3,f4,f5,f6,f7;
				
	
		
		// Reset performance data if this is first file
		if (firstParse)
			perfData.setLimits(start, end);
		
		br = getReader();
		
		// skip leading lines until dashes
		do {
			line = br.readLine();
			lines_read++;
			if (line.toLowerCase().startsWith("system configuration:"))
				updateSysCfg_OLD(line);
		} while (line!=null && !line.startsWith("-"));
		
		// Get header line
		line = readLineAndShowProgress(br);	

		if (line == null)
			return;
		
		v = splitLine(line);
		
		if (v.size() == 22)
			splpar = true;
		else if (v.size() == 20)
			splpar = false;
		else if (v.size() == 26) {
			splpar = true;
			ams = true;
		} else
			return;
			
		// Skip first line: averages from boot
		line = readLineAndShowProgress(br);	
		
		// Cycle on real data
		while (true) {
			line = readLineAndShowProgress(br);	
				
			if (line == null)
				break;
				
			// Skip blank lines
			if (line.length() < 10)
				continue;	
						
			// If it is just the header, skip it!
			if (line.startsWith("kthr") || line.startsWith("--") || line.startsWith(" r"))
				continue;
				
			// If configuration change, skip next line
			if (line.toLowerCase().startsWith("system configuration changed")) {
				line = readLineAndShowProgress(br);	
				continue;
			}
			
			// If sys cfg is shown, update it
			if (line.toLowerCase().startsWith("system configuration:")){
				updateSysCfg_OLD(line);
				continue;
			}

			v = splitLine(line);
			
			// Check if line is corrupted
			if ( 	(ams && v.size()!=24) ||
					(!ams && splpar && v.size()!=20) || 
					(!splpar && v.size()!=18) )
				continue;
			
			// Check if midnight has passed
			if (ams)
				s=(String)v.elementAt(23);
			else if (splpar)
				s=(String)v.elementAt(19);
			else
				s=(String)v.elementAt(17);
			i=Integer.parseInt(s.substring(0,2)); // actual hour
			if ( i<hour ) 
				day++;	// midnight has passed
			hour=i;
			
			try {
				gc = new GregorianCalendar(
							1970,
							Calendar.JANUARY,
							day,
							Integer.parseInt( s.substring(0,2) ),
							Integer.parseInt( s.substring(3,5) ),
							Integer.parseInt( s.substring(6)   )   );
			} catch (NumberFormatException e) {
				System.out.println("Error in line " + lines_read + " " + line);
				continue;
			}
			
			slot = getSlot(gc);
			
			try {
				f1 = Integer.parseInt((String)v.elementAt(13));
				f2 = Integer.parseInt((String)v.elementAt(14));
				f3 = Integer.parseInt((String)v.elementAt(16));
			} catch (NumberFormatException e) {
				System.out.println("Error parsing line " + lines_read + ": " + line);
				continue;
			}
							
			// Set data			
			perfData.add(slot, PerfData.CPU, PerfData.US, AVG_CPU, f1);
			perfData.add(slot, PerfData.CPU, PerfData.SY, AVG_CPU, f2);
			perfData.add(slot, PerfData.CPU, PerfData.WA, AVG_CPU, f3);
			perfData.add(slot, PerfData.CPU, PerfData.ID, AVG_CPU, 100f - f1 - f2 - f3);			
						
			// Write pc & ec
			if (splpar) {
				try {
					f1 = Float.parseFloat((String)v.elementAt(17));
					f2 = Float.parseFloat((String)v.elementAt(18));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + lines_read + ": " + line);
					continue;
				}
				perfData.add(slot, PerfData.PC, f1);
				perfData.add(slot, PerfData.LP, logicalCPU);
				perfData.add(slot, PerfData.ENT, entitlement);
				perfData.add(slot, PerfData.EC, f2);														
			}  
			
			try {
				f1 = Float.parseFloat((String)v.elementAt(2)) *4/1024;
				f2 = Float.parseFloat((String)v.elementAt(3)) *4/1024;
				f3 = Float.parseFloat((String)v.elementAt(5));
				f4 = Float.parseFloat((String)v.elementAt(6));
				f5 = Float.parseFloat((String)v.elementAt(7));
				f6 = Float.parseFloat((String)v.elementAt(8));
				f7 = Float.parseFloat((String)v.elementAt(9));
			} catch (NumberFormatException e) {
				System.out.println("Error parsing line " + lines_read + ": " + line);
				continue;
			}
			perfData.add(slot, PerfData.AVM, f1 );
			perfData.add(slot, PerfData.FRE, f2 );
			perfData.add(slot, PerfData.PI , f3 );
			perfData.add(slot, PerfData.PO , f4 );
			perfData.add(slot, PerfData.FR , f5 );
			perfData.add(slot, PerfData.SR , f6 );
			perfData.add(slot, PerfData.CY , f7 );
			perfData.add(slot, PerfData.RAM , memory *4/1024 );											
			
			try {
				f1 = Float.parseFloat((String)v.elementAt(0));
				f2 = Float.parseFloat((String)v.elementAt(1));
				f3 = Float.parseFloat((String)v.elementAt(12));
				f4 = Float.parseFloat((String)v.elementAt(11));
			} catch (NumberFormatException e) {
				System.out.println("Error parsing line " + lines_read + ": " + line);
				continue;
			}
			perfData.add(slot, PerfData.RUNQ , f1 );
			perfData.add(slot, PerfData.SWQ  , f2 );
			perfData.add(slot, PerfData.PSW  , f3 );			
			perfData.add(slot, PerfData.SYSC , f4 );
			
			if (ams) {
				try {
					f1 = Float.parseFloat((String)v.elementAt(19));
					f2 = Float.parseFloat((String)v.elementAt(20));
					f3 = Float.parseFloat((String)v.elementAt(21));
					f4 = Float.parseFloat((String)v.elementAt(22));
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + lines_read + ": " + line);
					continue;
				}
				perfData.add(slot, PerfData.HYPPAG_IN, f1);
				perfData.add(slot, PerfData.HYPPAG_TIME, f2);
				perfData.add(slot, PerfData.PHYS_MEM, f3);
				perfData.add(slot, PerfData.LOAN_MEM, f4);
				perfData.add(slot, PerfData.LOGICAL_MEM, logicalMemory);
			}
				
			
		}
		
		br.close();
		
		if (lastParse)
			perfData.endOfData();
		
		
	}
*/	
	

	
	/*
	 * Input line is a set of tokens delimited by a space character.
	 * Create a Vector containing the ordered set of String tokens
	 */
	
/*
	@SuppressWarnings("unchecked")
	private Vector splitLine (String s) {
		Vector result = new Vector();
		int begin=0;		// first token's char 
		int end;			// last token's char
		
		// Skip leading blanks
		while (begin<s.length() && s.charAt(begin) == ' ')
			begin++;
		
		// Get each token, except for last one
		while ( (end=s.indexOf(' ',begin)) >= 0) {
			result.add(s.substring(begin,end));
			begin = end+1;
			
			// skip blanks
			while (begin<s.length() && s.charAt(begin) == ' ')
				begin++;
		}
		
		// Manage last token
		if (begin<s.length())
			result.add(s.substring(begin));
		
		return result;
	}
*/

	
	private void updateSysCfg(FileTokenizer ft, int tokens) {
		
		String s;
		int equal;	// position of '=' in s
		String fieldName;
		
		for (int i=2; i<tokens; i++) {
			s = ft.getStringToken(i);
			equal = s.indexOf('=');
			if (equal<0)
				return;
			fieldName = s.substring(0, equal);
			
			if (fieldName.equals("lcpu"))
				logicalCPU=Integer.parseInt(s.substring(equal+1));
			else if (fieldName.equals("mem"))
				memory=Long.parseLong(s.substring(equal+1,s.length()-2))*1024/4;
			else if (fieldName.equals("ent"))
				entitlement=Float.parseFloat(s.substring(equal+1));
			else if (fieldName.equals("mmode"))
				; // do nothing
			else if (fieldName.equals("mpsz"))
				logicalMemory=Float.parseFloat(s.substring(equal+1,s.length()-2));
			
		}		
	}	

/*	
	private void updateSysCfg_OLD(String s) {
		int begin = s.indexOf('=')+1;
		int end   = s.indexOf(' ',begin);
		logicalCPU=Integer.parseInt(s.substring(begin,end));
		
		begin = s.indexOf('=',end)+1;
		end   = s.indexOf(' ',begin);
		if (end>0)
			memory=Long.parseLong(s.substring(begin,end-2))*1024/4;
		else {
			memory=Long.parseLong(s.substring(begin,s.length()-2))*1024/4;
			return;
		}
		
		begin = s.indexOf('=',end)+1;
		if (begin<0)
			return;
		end   = s.indexOf(' ',begin);
		if (end>0)
			entitlement=Float.parseFloat(s.substring(begin,end-2));
		else {
			entitlement=Float.parseFloat(s.substring(begin));
			return;
		}
		
		// Look for AMS
		begin = s.indexOf('=',end)+1;
		if (begin<0)
			return;
		end   = s.indexOf(' ',begin);
		if (end>0 && s.substring(begin,end).equals("shared")) {
			begin = s.indexOf('=',end)+1;
			if (begin<0)
				return;
			end   = s.indexOf(' ',begin);
			if (end>0)
				logicalMemory=Float.parseFloat(s.substring(begin,end-2));
			else {
				logicalMemory=Float.parseFloat(s.substring(begin,s.length()-2));
				return;
			}
		}
		
		
	}	
*/
}
