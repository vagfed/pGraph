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

package pGraph;

import java.io.BufferedReader;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Parser_Iostat extends Parser {
	
	
	private int 	parsed_day;			// Conventional day: 1 JAN, incremented after midnight
	private int		last_parsed_hour;	// last hour read. Used to detect midnight passing
	private String	last_time = null;
	private GregorianCalendar last_gc = null;
	
	private boolean isIostat = false;	// true when a valid iostat line is found
	
	
	public Parser_Iostat(ParserManager v) {
		super();
		manager = v;
	}

	public void parseData(boolean firstParse, boolean lastParse) {
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of iostat data.");
			if (lastParse)
				endOfData();
		}

	}

	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {
			System.out.println(e);
		}
		
		if (start!=null && end!=null && isIostat)
			valid=true;

	}
	
	
	
	/*
	 * Check if the line is a valid iostat line. If so, set isIostat value
	 */
	private void checkIostat(String line) {
		if (isIostat)
			return;		// already checked!
		
		if (!line.contains("xfers"))
			return;
		if (line.contains("read") &&
				line.contains("write") &&
				line.contains("queue") &&
				line.contains("time")) 
			isIostat = true;		
	}

	

	private void parseTimeLimits() throws Exception {
		BufferedReader		br;			// make buffered reads
		String				line;		// single line of data
		GregorianCalendar 	curr = null;
		String				time = null;		// time as a string
		int					day=1;		// conventional day. Increase after midnight
		String				s;
		
		final byte 	MAXWAIT = 20;		// Wait up to 10 lines before giving up.
		
		valid=false;
		
		br = getReader();
		
		while (true) {		
		
			line = br.readLine();
			if (line == null)
				break;
			
			total_lines++;
			
			if (start==null && end==null && total_lines>MAXWAIT)
				return;
			
			checkIostat(line);
			
			if (!isDataLine(line))
				continue;
			
			s=line.substring(line.length()-"xx:xx:xx".length());
			if (time!=null && time.equals(s))
				continue;
			
			time=s;
			
			try {
				curr = new GregorianCalendar(
						1970,
						Calendar.JANUARY,
						day,
						Integer.parseInt(time.substring(0,2)),
						Integer.parseInt(time.substring(3,5)),
						Integer.parseInt(time.substring((6)))	);
			} catch (Exception e) {
				continue;
			}
			
			if (curr==null)
				continue;
				
			// Check if midnight has passed
			if (end!=null && curr.before(end)) {
				curr.add(Calendar.DAY_OF_MONTH,1);
				day++;
			}
			
			if (start==null)
				start=curr;
								
			if (end==null || end.before(curr))
				end=curr;				
			
		}

		// Close file
		br.close();		
	}
	

	
	

	


	

	
	
	
	
	float convertTimeValue(String s) {
		float factor = 1;
		
		if (s.endsWith("S"))
			factor = 1000;
		if (s.endsWith("H"))
			factor = 1000 * 3600;
		
		if (factor!=1)
			s=s.substring(0, s.length()-1);
		
		return factor * Float.parseFloat(s);
	}
	
	float convertKByteValue(String s) {
		float factor = 1;
		
		if (s.endsWith("K"))
			factor = 1000;
		if (s.endsWith("M"))
			factor = 1000000;
		if (s.endsWith("G"))
			factor = 1000000000;
		if (s.endsWith("T"))
			factor = 1000000000000f;
		
		if (factor!=1)
			s=s.substring(0, s.length()-1);
		
		return factor * Float.parseFloat(s) / 1024;
	}	

	



	
	
	private GregorianCalendar timeToGC(String time) {
		if (time.equals(last_time))
			return last_gc;
		
		
		GregorianCalendar result=null;
		
		try {
			result = new GregorianCalendar(
					1970,
					Calendar.JANUARY,
					parsed_day,
					Integer.parseInt(time.substring(0,2)),
					Integer.parseInt(time.substring(3,5)),
					Integer.parseInt(time.substring((6)))	);
		} catch (Exception e) {
			return null;
		}
		
		int hour = Integer.parseInt(time.substring(0,2));
		if (hour < last_parsed_hour) {
			// Midnight has passed
			parsed_day++;
			result.add(Calendar.DAY_OF_MONTH,1);
		}
		last_parsed_hour = hour;
		last_time = time;
		last_gc = result;
		return result;
	}
	
	
	private boolean isDataLine(String line) {		
		if (line==null)
			return false;
		if (line.length()<="xx:xx:xx".length())
			return false;
		
		// minus    87654321
		// .........xx:xx:xx
		if (line.charAt(line.length()-6)!=':' || line.charAt(line.length()-3)!=':')
			return false;
		
		return true;
	}
	
	/*
	 * Line Parser
	 */
	class DataParser {
		private String line=null;
		private final int	MAX_DATA = 50;			// max number of data fields
		private int begin[] = new int[MAX_DATA];	// begin of data label
		private int end[]   = new int[MAX_DATA];	// end of data label
		private int numData;						// number of labels
		
		public boolean parse(String s) {
			int i;
			boolean found;
			
			line = s;
			
			// Check if it is a valid line
			if (!line.matches("([#_\\-\\w\\d\\.]+\\s+)+\\d\\d:\\d\\d:\\d\\d"))
				return false;
			
			numData=0;
			found=false;
			for (i=0; i<line.length() && numData<MAX_DATA; i++) {
				if (line.charAt(i)!=' ') {
					if (!found) {
						found=true;
						begin[numData]=i;
					}
				} else {
					if (found) {
						found=false;
						end[numData++]=i-1;
					}
				}
			}
			if (numData==MAX_DATA) {
				System.out.println("DataParser: line with too many labels");
				return false;
			}
			end[numData++]=line.length()-1;
			
			return true;			
		}
	
		public String getLabel() {
			return line.substring(0, end[0]+1);
		}
		
		public String getTime() {
			return line.substring(begin[numData-1]);
		}
		
		public int getNumData() {
			return numData;
		}
		
		public float getValue(int pos) {
			if (pos>=numData)
				return -1;
			
			int i;
			float result=0;
			char c;
			boolean comma=false;
			float decimal=0.1f;
			
			for (i=begin[pos]; i<=end[pos]; i++) {
				c=line.charAt(i);
				if (c>='0' && c<='9') {
					if (!comma)
						result = result * 10 + (c-'0');
					else {
						result = result + decimal*(c-'0');
						decimal = decimal / 10;
					}
					continue;
				}
				if (c=='.') {
					comma=true;
					continue;
				}
				
				// We now manage suffix, mut it MUST be the last char!
				if (i!=end[pos])
					return -1;
				
				switch (c) {
					case 'K':	result = result * 1000f; break; 
					case 'M':	result = result * 1000000f; break; // Minutes are handled outside!
					case 'G':	result = result * 1000000000f; break; 
					case 'T':	result = result * 1000000000000f; break; 
					case 'S':	result = result * 1000f; break; 
					case 'H':	result = result * 3600000f; break; 
					default:	System.out.println("DataParser: unknown suffix"); return -1;
				}
			}
			
			return result;
		}
	}
	
	
	
	
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		BufferedReader	br;			// make buffered reads
		String			line;		// single line of data
		GregorianCalendar	gc;
		int num=0;
		int slot;

		
		// Reset performance data if this is first file
		if (firstParse) {
			perfData.setLimits(start, end);
			lines_read=0;
		}
		
		current_file_line_read = 0;
		
		parsed_day=1;			// We always begin with 01 JAN
		last_parsed_hour=0;		
		last_time=null;
		last_gc=null;
		
		br = getReader();
		
		line = readLineAndShowProgress(br);
		if (line==null)
			return;
		num++;
		
		DataParser dp = new DataParser();
		
		while (true) {		
			
			// Virtual client or server adapter data
			if (line.startsWith("Vadapter:")) {
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.startsWith("-------")) {
					System.out.println("Virtual adapter stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("avg  serv")) {
					System.out.println("Virtual adapter stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("sqsz qfull")) {
					System.out.println("Virtual adapter stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				
				// Virtual adapter data
				if (!dp.parse(line)) {
					System.out.println("Virtual adapter stanza interrupted in line "+num);
					continue;					
				}
				gc=timeToGC(dp.getTime());
				
				slot = getSlot(gc);
				
				if (dp.getNumData() == 21) {
					// Virtual client data
					
					// XFERS
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_XFER,   dp.getLabel(), dp.getValue(2) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_READKB, dp.getLabel(), dp.getValue(3)/1024 );   
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WRITEKB,dp.getLabel(), dp.getValue(4)/1024 );   
					
					// READ
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_RPS,    dp.getLabel(), dp.getValue(6) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_R,  dp.getLabel(), dp.getValue(7) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MIN_R,  dp.getLabel(), dp.getValue(8) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MAX_R,  dp.getLabel(), dp.getValue(9) );
					
					// WRITE
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WPS,    dp.getLabel(), dp.getValue(10) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_W,  dp.getLabel(), dp.getValue(11) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MIN_W,  dp.getLabel(), dp.getValue(12) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MAX_W,  dp.getLabel(), dp.getValue(13) );
					
					// QUEUE
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_T,  dp.getLabel(), dp.getValue(14) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MIN_T,  dp.getLabel(), dp.getValue(15) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MAX_T,  dp.getLabel(), dp.getValue(16) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_WQ, dp.getLabel(), dp.getValue(17) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_SQ, dp.getLabel(), dp.getValue(18) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_FULLQ,  dp.getLabel(), dp.getValue(19) );
				} else if (dp.getNumData() == 20) {
					// Virtual server data
					
					// XFERS
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_XFER,   dp.getLabel(), dp.getValue(2) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_READKB, dp.getLabel(), dp.getValue(3)/1024 ); 
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WRITEKB,dp.getLabel(), dp.getValue(4)/1024 ); 
					
					// READ
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_RPS,    dp.getLabel(), dp.getValue(5) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_R,  dp.getLabel(), dp.getValue(6) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MIN_R,  dp.getLabel(), dp.getValue(7) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MAX_R,  dp.getLabel(), dp.getValue(8) );
					
					// WRITE
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WPS,    dp.getLabel(), dp.getValue(9) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_W,  dp.getLabel(), dp.getValue(10) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MIN_W,  dp.getLabel(), dp.getValue(11) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MAX_W,  dp.getLabel(), dp.getValue(12) );
					
					// QUEUE
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_T,  dp.getLabel(), dp.getValue(13) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MIN_T,  dp.getLabel(), dp.getValue(14) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_MAX_T,  dp.getLabel(), dp.getValue(15) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_WQ, dp.getLabel(), dp.getValue(16) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_AVG_SQ, dp.getLabel(), dp.getValue(17) );
					perfData.add(slot, PerfData.SCSI, PerfData.SCSI_FULLQ,  dp.getLabel(), dp.getValue(18) );
				} else {
					System.out.println("Virtual adapter stanza interrupted in line "+num);
					continue;
				} 

				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				continue;	
			}
			
			
			// Virtual or physical disk data
			if (line.startsWith("Paths/Disk:") || line.startsWith("Disks:")) {
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.startsWith("-------")) {
					System.out.println("Disk stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("avg  serv")) {
					System.out.println("Disk stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("sqsz qfull")) {
					System.out.println("Disk stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				
				// Virtual/physical disk data
				while (dp.parse(line)) {	
					
					if (dp.getNumData()==25) {
						// normal hdisk
						
						gc=timeToGC(dp.getTime());					
						slot = getSlot(gc);
						
						// XFERS
						perfData.add(slot, PerfData.DISK, PerfData.DSK_BUSY,   dp.getLabel(), dp.getValue(1) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_XFER,   dp.getLabel(), dp.getValue(3) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_READKB, dp.getLabel(), dp.getValue(4)/1024 ); // Blocks
						perfData.add(slot, PerfData.DISK, PerfData.DSK_WRITEKB,dp.getLabel(), dp.getValue(5)/1024 ); // Blocks
						
						// READ
						perfData.add(slot, PerfData.DISK, PerfData.DSK_RPS,    dp.getLabel(), dp.getValue(6) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_R,  dp.getLabel(), dp.getValue(7) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_MIN_R,  dp.getLabel(), dp.getValue(8) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_MAX_R,  dp.getLabel(), dp.getValue(9) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_TO_R,   dp.getLabel(), dp.getValue(10) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_FAIL_R, dp.getLabel(), dp.getValue(11) );
						
						// WRITE
						perfData.add(slot, PerfData.DISK, PerfData.DSK_WPS,    dp.getLabel(), dp.getValue(12) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_W,  dp.getLabel(), dp.getValue(13) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_MIN_W,  dp.getLabel(), dp.getValue(14) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_MAX_W,  dp.getLabel(), dp.getValue(15) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_TO_W,   dp.getLabel(), dp.getValue(16) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_FAIL_W, dp.getLabel(), dp.getValue(17) );
						
						// QUEUE
						perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_T,  dp.getLabel(), dp.getValue(18) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_MIN_T,  dp.getLabel(), dp.getValue(19) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_MAX_T,  dp.getLabel(), dp.getValue(20) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_WQ, dp.getLabel(), dp.getValue(21) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_SQ, dp.getLabel(), dp.getValue(22) );
						perfData.add(slot, PerfData.DISK, PerfData.DSK_FULLQ,  dp.getLabel(), dp.getValue(23) );
					
					} else if (dp.getLabel().startsWith("hdiskpower") && dp.getNumData()==7) {
						// hdiskpower
						
						gc=timeToGC(dp.getTime());					
						slot = getSlot(gc);
						
						// XFERS
						perfData.add(slot, PerfData.ESS, PerfData.ESS_BUSY,   dp.getLabel(), dp.getValue(1) );
						perfData.add(slot, PerfData.ESS, PerfData.ESS_XFER,   dp.getLabel(), dp.getValue(3) );
						perfData.add(slot, PerfData.ESS, PerfData.ESS_READKB, dp.getLabel(), dp.getValue(4)/1024  );
						perfData.add(slot, PerfData.ESS, PerfData.ESS_WRITEKB,dp.getLabel(), dp.getValue(5)/1024 );
					} else if (dp.getLabel().startsWith("cd") && dp.getNumData()==7) {
						// It is just cdrom data: skip it!
						;
					} else {
						System.out.println("Disk stanza interrupted in line "+num);
						break;
					}
					
					line = readLineAndShowProgress(br);
					if (line==null)
						break;
					num++;			
				}
				if (line==null)
					break;
					
				continue;				
			}			
			
			
			// Physical adapter data
			if (line.startsWith("Adapter:")) {
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.startsWith("-------")) {
					System.out.println("Physical adapter stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("bread  bwrtn")) {
					System.out.println("Physical adapter stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				
				// Physical adapter data
				if (!dp.parse(line) || dp.getNumData()!=6) {
					System.out.println("Physical adapter stanza interrupted in line "+num);
					continue;					
				}
				gc=timeToGC(dp.getTime());
				
				slot = getSlot(gc);
				
				// XFERS
				perfData.add(slot, PerfData.SCSI, PerfData.SCSI_XFER,   dp.getLabel(), dp.getValue(2) );
				perfData.add(slot, PerfData.SCSI, PerfData.SCSI_READKB, dp.getLabel(), dp.getValue(3)/1024 );
				perfData.add(slot, PerfData.SCSI, PerfData.SCSI_WRITEKB,dp.getLabel(), dp.getValue(4)/1024 );
					
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				continue;				
			}			
			
			
			// Virtual target disk data
			if (line.startsWith("Vtargets/Disks:")) {
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.startsWith("-------")) {
					System.out.println("Disk stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("avg  serv")) {
					System.out.println("Disk stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				if (!line.endsWith("sqsz qfull")) {
					System.out.println("Disk stanza interrupted in line "+num);
					continue;
				}
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				
				// Virtual target disk data
				while (dp.parse(line)) {
					
					if (dp.getNumData()!=19) {
						System.out.println("Disk stanza interrupted in line "+num);
						break;
					}
					
					gc=timeToGC(dp.getTime());
					
					slot = getSlot(gc);
					
					// XFERS
					perfData.add(slot, PerfData.DISK, PerfData.DSK_XFER,   dp.getLabel(), dp.getValue(1) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_READKB, dp.getLabel(), dp.getValue(2)/1024 );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_WRITEKB,dp.getLabel(), dp.getValue(3)/1024 );
					
					// READ
					perfData.add(slot, PerfData.DISK, PerfData.DSK_RPS,    dp.getLabel(), dp.getValue(4) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_R,  dp.getLabel(), dp.getValue(5) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_MIN_R,  dp.getLabel(), dp.getValue(6) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_MAX_R,  dp.getLabel(), dp.getValue(7) );
					
					// WRITE
					perfData.add(slot, PerfData.DISK, PerfData.DSK_WPS,    dp.getLabel(), dp.getValue(8) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_W,  dp.getLabel(), dp.getValue(9) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_MIN_W,  dp.getLabel(), dp.getValue(10) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_MAX_W,  dp.getLabel(), dp.getValue(11) );
					
					// QUEUE
					perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_T,  dp.getLabel(), dp.getValue(12) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_MIN_T,  dp.getLabel(), dp.getValue(13) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_MAX_T,  dp.getLabel(), dp.getValue(14) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_WQ, dp.getLabel(), dp.getValue(15) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_AVG_SQ, dp.getLabel(), dp.getValue(16) );
					perfData.add(slot, PerfData.DISK, PerfData.DSK_FULLQ,  dp.getLabel(), dp.getValue(17) );					
					
					line = readLineAndShowProgress(br);
					if (line==null)
						break;
					num++;			
				}
				if (line==null)
					break;
					
				continue;				
			}	
			
			
			// Lines to be skipped
			if (line.startsWith("System configuration") ||
					line.startsWith("Disk history") ||
					line.startsWith("System configuration changed") ||
					line.length()==0) {
				
				line = readLineAndShowProgress(br);
				if (line==null)
					break;
				num++;
				continue;
			}
						
			// Unexpected line
			System.out.println("Unrecognized data. Skipping line "+num);
			
			line = readLineAndShowProgress(br);
			if (line==null)
				break;
			num++;
		}
		
		br.close();

		if (lastParse)
			perfData.endOfData();
		

	}
	
	
}
