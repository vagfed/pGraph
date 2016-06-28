package pGraph;

import java.io.BufferedReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Parser_Sar extends Parser {
	
	
	/*
	 * 	Parser for "sar -A"
	 */
	
	
	private final static byte	UNKNOWN = 0;
	private final static byte	AIX 	= 1;
	private final static byte	HPUX	= 2;	
	private final static byte	SUNOS	= 3;
	
	private byte operatingSystem = UNKNOWN;

	
	private byte groupMatrix[][] = null;
	private byte typeMatrix[][] = null;
	
	private int 	numCpu = -1;
	
	private byte DEVICE = Byte.MAX_VALUE;
	
	GregorianCalendar headerGc = null;

	
	
	
	public Parser_Sar(ParserManager v) {
		super();
		manager = v;
	}

	public void parseData(boolean firstParse, boolean lastParse) {
		
		// If start and end are not know, abort
		if (start==null || end==null)
			return;
		
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of sar data (line "+current_file_line_read+")");
			if (lastParse)
				endOfData();
		}
	}
	
	
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {	
		
		// Reset performance data if this is first file
		if (firstParse) {
			perfData.setLimits(start, end);
			lines_read=0;
		}
		

		
		current_file_line_read=0;			// reset counter for current file
		
		GregorianCalendar	gc = start;
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		int				slot;
		
		br = getReader();
		line = readLineAndShowProgress(br);
		
		while (line!=null) {
			
			// Identify header structure
			line = parseHeaderLines(line, br);
			if (headerGc == null) {
				System.out.println(fileName + ": Warning, invalid date in sar data (line "+current_file_line_read+"). Skipping remaining lines.");
				return;
			}	
			
			gc = headerGc;	
			
			// Cycle on data blocks up to Average lines
			while (line!=null && !line.startsWith("Average")) {
				if ( line.length() <8 || line.charAt(2)!=':' | line.charAt(5)!=':')
					line = readLineAndShowProgress(br);		// skip empty lines
				else {
					gc = parseDate(line,gc);
					slot = getSlot(gc);
					line = parseDataLines(line, br, slot);
				}
					
			}

			// Skip "Average" lines
			while (line!=null && line.startsWith("Average"))
				line = readLineAndShowProgress(br);
		}
		
		

		
		if (lastParse) {
			perfData.endOfData();
			// Checkers TBD
		}
	}
	

	
	/*
	 * Parse header row(s) o detect data structure of real data row(s). It skips invalid lines.
	 * line		: last line read from data
	 * br		: BufferedReader from which data is read
	 * Returns the first line read that does not belong to the header
	 */
	private String parseHeaderLines(String line, BufferedReader br)  throws Exception {
		String 	tokens[];		// labels of data in this line
		int		row;			// current row inside groupMatrix & typeMatrix
		byte	prevGroupMatrix[][];
		byte	prevTypeMatrix[][];
		int		i;
		
		groupMatrix = typeMatrix = null;	// reset matrixes
		
		while (line!=null) {
			
			if (line.toLowerCase().startsWith("cores")) {
				// The number of physical CPUs is provided
				tokens = line.split("(\\s*=\\s*)");
				if (tokens.length != 2) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					line = readLineAndShowProgress(br);
					continue;
				}
				try {
					numCpu = Integer.parseInt(tokens[1]);
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
				}
				line = readLineAndShowProgress(br);
				continue;
			}
			
			if (line.startsWith("AIX") || 
					line.startsWith("HP-UX") || 
					line.startsWith("SunOS")) {	
				
				headerGc = scanDate(line);
				line = readLineAndShowProgress(br);
				continue;
			}
			
			
			
			if (line.startsWith("Average")) {
				System.out.println("\"Average\" right after header. broken stanza? " + current_file_line_read + ": " + line);
				return line;
			}
			
			// Skip empty lines
			if ( line.length() <8) {
				line = readLineAndShowProgress(br);
				continue;
			}
			
			// If groupMatrix==null skip all lines without a time stamp
			if (groupMatrix==null && ( 
					line.length()<8 || line.charAt(2)!=':' || line.charAt(5)!=':')) {
				line = readLineAndShowProgress(br);
				continue;
			}
			
			// If groupMatrix!=null stop at next line with a time stamp
			if ( groupMatrix!=null && 
					line.length()>8 && line.charAt(2)==':' && line.charAt(5)==':') {
				return line;
			}
			
			tokens = line.substring(8).trim().split("(\\s+)");
			if (tokens.length == 0) {
				System.out.println("Unexpected line. Skipping. " + current_file_line_read + ": " + line);
				line = readLineAndShowProgress(br);
				continue;
			}
			
			try {
				Float.parseFloat(tokens[tokens.length-1]);  // first may be a name of a device...
				return line;	// End of header! This is data!
			} catch (NumberFormatException nfe) {
				// While there is an exception, the line does not contain data!
			}
			
			if (groupMatrix == null) {
				// First header line
				groupMatrix = new byte[1][tokens.length];
				typeMatrix = new byte[1][tokens.length];
				row = 0;
			} else {
				prevGroupMatrix = groupMatrix;
				prevTypeMatrix = typeMatrix;
				groupMatrix = new byte[groupMatrix.length+1][];
				typeMatrix = new byte[typeMatrix.length+1][];
				for (i=0; i<prevGroupMatrix.length; i++) {
					groupMatrix[i] = prevGroupMatrix[i];
					typeMatrix[i] = prevTypeMatrix[i];
				}
				groupMatrix[prevGroupMatrix.length] = new byte[tokens.length];
				typeMatrix[prevTypeMatrix.length] = new byte[tokens.length];
				row = prevTypeMatrix.length;
			}
			
			// Fill with invalid tokens
			for (i=0; i<tokens.length; i++) {
				groupMatrix[row][i] = -1;
				typeMatrix[row][i] = -1;
			}
			
			for (i=0; i<tokens.length; i++) {
				
				// Special case: multiple lines of data related to multiple devices
				if (tokens[i].equals("device")) {
					groupMatrix[row][i] = DEVICE;
					typeMatrix[row][i] = DEVICE;
				}
				
				// One line of data
				if (tokens[i].equals("%usr")) {
					groupMatrix[row][i] = PerfData.CPU;
					typeMatrix[row][i] = PerfData.US;
				} 
				if (tokens[i].equals("%sys")) {
					groupMatrix[row][i] = PerfData.CPU;
					typeMatrix[row][i] = PerfData.SY;
				}
				if (tokens[i].equals("%wio")) {
					groupMatrix[row][i] = PerfData.CPU;
					typeMatrix[row][i] = PerfData.WA;
				}
				if (tokens[i].equals("%idle")) {
					groupMatrix[row][i] = PerfData.CPU;
					typeMatrix[row][i] = PerfData.ID;
				}
				
				if (tokens[i].equals("physc")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					if (tokens.length>i+1 && tokens[i+1].equals("%entc"))
						typeMatrix[row][i] = PerfData.PC;
					else
						typeMatrix[row][i] = PerfData.TOT_CPU;
				}
				if (tokens[i].equals("%entc")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.EC;
				}
				
				// Skip following data if dealing with multiple LPARs
				if (multipleLPAR) 
					continue;
				
				if (tokens[i].equals("scall/s")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.SYSC;
				}
				if (tokens[i].equals("sread/s")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.READ;
				}
				if (tokens[i].equals("swrit/s")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.WRITE;
				}
				if (tokens[i].equals("fork/s")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.FORK;
				}
				if (tokens[i].equals("exec/s")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.EXEC;
				}
				
				if (tokens[i].equals("%busy")) {
					groupMatrix[row][i] = PerfData.DISK;
					typeMatrix[row][i] = PerfData.DSK_BUSY;
				}
				/*
				if (tokens[i].equals("avque")) {
					groupMatrix[row][i] = PerfData.DISK;
					typeMatrix[row][i] = PerfData.DSK_AVG_WQ;
				}
				if (tokens[i].equals("avserv")) {
					groupMatrix[row][i] = PerfData.DISK;
					typeMatrix[row][i] = PerfData.DSK_AVG_T;
				}
				*/
				
				if (tokens[i].equals("runq-sz")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.RUNQ;
				}
				
				if (tokens[i].equals("swpq-sz")) {
					groupMatrix[row][i] = PerfData.SYSTEM;
					typeMatrix[row][i] = PerfData.SWQ;
				}
				
			}
			
			// Read new line and start over
			line = readLineAndShowProgress(br);
		}
		
		// End of file
		return null;
	}
	
	
	/*
	 * Parse data row(s) related to a single time interval, based on header schema.
	 * It reads exactly the number of rows in the header (or less if it reaches end-of-file)
	 * line		: last line read from data
	 * br		: BufferedReader from which data is read
	 * slot		: time slot where to put data samples
	 * Returns the first line NOT belonging to the time interval. Line may be blank.
	 */
	private String parseDataLines(String line, BufferedReader br, int slot) throws Exception {
		String tokens[];
		int i;
		int row;

		
		float f;
		
		// Blank time in first line
		line = "        " + line.substring(8); 
		
		row = 0;
		while (row < groupMatrix.length) {
			
			// Check if stanza is truncated
			if (line==null || 
					(line.length() >=5 && ( line.charAt(2)==':' || line.charAt(5)==':' )  ) ||
					line.startsWith("Average")) {
				return line;
			}
			
			line = line.trim();

			// AIX: Empty line: end of devices or just line with no valid values;
			if (line.length()==0 && operatingSystem==AIX) {	
				row++;
				line = readLineAndShowProgress(br);
				continue;
			}
			
			// Other Unix: end of time frame
			if (line.length()==0) {	
				return line;
			}
			
			tokens = line.split("(\\s+)");
			
			// Check if disk data has finished
			if (groupMatrix[row][0]==DEVICE) {
				if (tokens.length != groupMatrix[row].length)
					row++;		// it is no longer a DEVICE line!
				else {
					try {
						Float.parseFloat(tokens[0]);
						row++;		// it is no longer a DEVICE line!
					} catch (NumberFormatException nfe) {
						// Since there is an exception, the line is a DEVICE line!
					}
				}
			}
			
			// Check schema (sometimes AIX has fewer tokens then expected...)
			if (tokens.length > groupMatrix[row].length) {
				System.out.println("Unexpected line. Skipping. " + current_file_line_read + ": " + line);
				return line;
			}
			
			for (i=0; i<tokens.length; i++) {		
				
				// Skip unknown data
				if (groupMatrix[row][i]==-1)
					continue;
				
				if (groupMatrix[row][0]==DEVICE && i==0)
					continue;	// it's the name of the device
				
				try {
					f = Float.parseFloat(tokens[i]);
				} catch (NumberFormatException e) {
					System.out.println("Error parsing line " + current_file_line_read + ": " + line);
					return line;
				}
				

				switch (groupMatrix[row][i]) {
				case PerfData.DISK:		perfData.add(slot, groupMatrix[row][i], typeMatrix[row][i], tokens[0], f); break;
				case PerfData.SYSTEM:	perfData.add(slot, typeMatrix[row][i], f); break;
				case PerfData.CPU:		perfData.add(slot, PerfData.CPU, typeMatrix[row][i], AVG_CPU, f); break;
				
				default:				System.out.println("Unexpected line "+ current_file_line_read + ": " + line);
										return line;
				}
			
			}
			
			if (numCpu>0 && groupMatrix[row][0]==PerfData.CPU) {
				perfData.add(slot, PerfData.TOT_CPU, numCpu);
			}
			
			if (groupMatrix[row][0]!=DEVICE)
				row++;
			
			line = readLineAndShowProgress(br);
		}
		
		return line;
		
	}
	
	
	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {
			System.out.println("Error in scanning limits");
		}
		
		if (start!=null && end!=null)
			valid=true;
		
		if (operatingSystem == UNKNOWN)
			valid=false;
	}
	
	
	private GregorianCalendar scanDate (String line) {
		String s[] = line.split("(\\s+)|(/)");
		int month, day, year;
		
		try {
			month	 = Integer.parseInt(s[s.length-3]);
			day		 = Integer.parseInt(s[s.length-2]);
			year	 = Integer.parseInt(s[s.length-1]);
			
			
			if (year<1970) {			
				if (year >= 90)
					year += 1900;
				else
					year += 2000;
			}
		} catch (NumberFormatException nfe) {
			return null;
		}
		
		return new GregorianCalendar(year, month-1, day);		
	}
	
	
	private GregorianCalendar parseDate(String line, GregorianCalendar gc) {
		// 16:05:31  iget/s lookuppn/s dirblk/s
		// 01234567890
		int h,m,s;
		
		try {			
			h = Integer.parseInt(line.substring(0,2));
			m = Integer.parseInt(line.substring(3,5));
			s = Integer.parseInt(line.substring(6,8));
		} catch (NumberFormatException e) {
			return null;
		}
		
		GregorianCalendar result = new GregorianCalendar(gc.get(Calendar.YEAR),
															gc.get(Calendar.MONTH),
															gc.get(Calendar.DAY_OF_MONTH),
															h,m,s);
		// Check if midnight has passed
		if (result.before(gc))
			result.add(Calendar.DAY_OF_MONTH, 1);
		
		return result;
	}
	
	
	
	private void parseTimeLimits() throws Exception {
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		GregorianCalendar gc = null;

		br = getReader();
		
		// Get first not null line to understand if it is a SAR and from which Operating System
		// Skip cores information
		line = br.readLine();
		while (line!=null && 
				(line.length()==0 || line.toLowerCase().startsWith("cores")) ) {
			total_lines++;
			line = br.readLine();
		}
		if (line==null) {
			valid=false;
			return;		// no valid lines found
		}
		total_lines++;

		
		
		if (line.startsWith("AIX")) {	// AIX p550vio1 3 5 00CFDE9C4C00    03/20/08
			operatingSystem = AIX;			
		} else if (line.startsWith("HP-UX")) {	// HP-UX vibe B.11.11 U 9000/800    04/02/08
			operatingSystem = HPUX;			
		} else if (line.startsWith("SunOS")) {	// SunOS unknown 5.10 Generic_118855-33 i86pc    04/03/2008
			operatingSystem = SUNOS;			
		} else {
			// Unknown operating system
			valid=false;
			return;
		}
		gc = scanDate(line);
		if (gc == null) {
			// Unknown date format
			valid = false;
			return;
		}
		
		// Check all time stamp up to first "Average" line or end of file
		line = br.readLine();
		while (line!=null && !line.startsWith("Average")) {
			total_lines++;
			// 16:05:31  iget/s lookuppn/s dirblk/s
			// 01234567890
			if (line.length() > 8 && line.charAt(2)==':' && line.charAt(5)==':') {
				gc = parseDate(line,gc);
				if (gc == null) {
					System.out.println("Error in line " + total_lines + " " + line);
				} else {
					if (start == null || gc.before(start))
						start = gc;
					
					if (end == null || gc.after(end))
						end = gc;
				}
			}
			line = br.readLine();
		}
		
		if (start!=null && end!=null)
			valid=true;
		
		// Just count lines
		while (line!=null) {
			total_lines++;
			line = br.readLine();
		}
		
		return;
		
	}

}
