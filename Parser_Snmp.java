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
import java.util.GregorianCalendar;
import java.util.Vector;

public class Parser_Snmp extends Parser {
	
	private Parser			parser[] = null;	// containers of single LPAR data
	private float			cores[] = null;		// cores contained into system
	
	public Parser_Snmp(ParserManager v) {
		super();
		manager = v;
	}

	@Override
	public void parseData(boolean firstParse, boolean lastParse) {
		// TODO Auto-generated method stub
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of snmp data at line "+current_file_line_read);
			if (lastParse)
				endOfData();
		}

	}
	
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		BufferedReader		br;				// make buffered reads
		String				line=null;		// single line of data
		String				time=null;		// time as a string
		String				date=null;		// date as a string
		GregorianCalendar	gc=null;		// current time
		String				s;
		Vector				v;				
		int 				i;
		
		
		String 				tokens[];
		int					index;
		int 				slot;
		PerfData 			pd;
		float				us,sy,wa,id;
		

		
		
		if (firstParse) {
			lines_read=0;
			perfData.setLimits(start, end);
			if (parser != null) 
				for (i=0; i<parser.length; i++)
					parser[i].getPerfData().setLimits(start, end);
		}
		
		current_file_line_read = 0;
		
		br = getReader();
		
		
		try {
	
			while (true) {
				line = readLineAndShowProgress(br);
	
				if (line == null)
					break;
				
				// Only use CpuIdle for now....
				if (!line.contains("CpuIdle") && !line.contains("cores"))
					continue;
				
				// Split line in tokens
				tokens=line.trim().split("\\s+");
				
				if (tokens.length == 4 && tokens[1].equals("cores")) {
					index = getLPARIndex(tokens[0]);
					try {
						cores[index]=Float.parseFloat(tokens[3]);
					} catch (Exception e) {}
					continue;
				}
				
				if (tokens.length != 6 ) {
					// Invalid line: ignore
					continue;
				}
				
				/*
				 * Valid line
				 *          0          0         
				 *          0123456789 01234 
				 * wbrbgl01 2008/12/31 00:00 UCD-SNMP-MIB::ssCpuIdle.0 = 94
				 */		
					
				// Get time, skip if invalid format
				if (date==null || !time.equals(tokens[1]) || time==null || !time.equals(tokens[2])) {			
					// Get gc, skip if invalid format
					try {
						gc = new GregorianCalendar(
										Integer.parseInt(tokens[1].substring(0,4))   ,
										Integer.parseInt(tokens[1].substring(5,7))-1 ,
										Integer.parseInt(tokens[1].substring(8))   ,
										Integer.parseInt(tokens[2].substring(0,2))   ,
										Integer.parseInt(tokens[2].substring(3))   ,
										0													   );				
								
					} catch (Exception e) {
						continue;
					}
					
					date=tokens[1];
					time=tokens[2];
				}
				
				
				index = getLPARIndex(tokens[0]);
				slot = getSlot(gc);
				pd=parser[index].getPerfData();
				
				id=Float.parseFloat(tokens[5]);
				us=100f-id;
				wa=0f;
				sy=0f;
				
				pd.add(slot, PerfData.CPU, PerfData.ID, AVG_CPU, id );
				pd.add(slot, PerfData.CPU, PerfData.US, AVG_CPU, us );
				pd.add(slot, PerfData.CPU, PerfData.WA, AVG_CPU, wa );
				pd.add(slot, PerfData.CPU, PerfData.SY, AVG_CPU, sy );
				
				if (cores[index]>0)
					pd.add(slot, PerfData.TOT_CPU, cores[index]);

			}
			
		} catch (Exception e) {
			System.out.println("Error reading line: "+line);
			throw e;
		}

		
		// Close file
		br.close();

		
		// End of data of single parsers
		for (i=0; parser!=null && i<parser.length; i++) {
			parser[i].endOfData();		
		}
		
		
		if (lastParse)
			perfData.endOfData();							
	}
	
	
	private int getLPARIndex(String name) {
		if (name.equals(""))
			name="<Unknown>";
		
		// If this is the first LPAR, allocate parser structure
		if (parser==null) {
			parser = new Parser[1];
			parser[0] = new Parser_Topas(manager);
			parser[0].setFileName(name);
			parser[0].setApplet(applet);
			parser[0].setStart(start);
			parser[0].setEnd(end);
			parser[0].getPerfData().setLimits(start, end);
			
			cores = new float[1];
			cores[0]=-1;

			return 0;
		}
		
		// Check if a parser has already been defined and return it
		for (int i=0; i<parser.length; i++) {
			if (parser[i].getFileName().equals(name))
				return i;
		}
		
		// This is a new LPAR: allocate new object
		Parser old[] = parser;
		parser = new Parser_Topas[old.length+1];
		float  oldCores[] = cores;
		cores = new float[old.length+1];
		
		for (int i=0; i<old.length; i++) {
			parser[i]=old[i];
			cores[i]=oldCores[i];
		}
			
		parser[old.length] = new Parser_Topas(manager);
		parser[old.length].setFileName(name);
		parser[old.length].setApplet(applet);
		parser[old.length].setStart(start);
		parser[old.length].setEnd(end);
		parser[old.length].getPerfData().setLimits(start, end);
		
		cores[old.length]=-1;

		return old.length;		
	}	
	

	@Override
	public void scanTimeLimits() {
		// TODO Auto-generated method stub
		try {
			parseTimeLimits();
		} catch (Exception e) {}
		
		if (start!=null && end!=null)
			valid=true;
	}
	
	
	private void parseTimeLimits() throws Exception {
		BufferedReader		br;			// make buffered reads
		String				line;		// single line of data
		GregorianCalendar 	curr = null;
		String				date=null;		// date as a string
		String				time=null;		// time as a string
		String				tokens[];
		
		valid=false;
		
		br = getReader();
				
		while (true) {
			line = br.readLine();

			if (line == null)
				break;	
			
			total_lines++;
			
			// Split line in tokens
			tokens=line.trim().split("\\s+");
			if (tokens.length != 6 ) {
				// Invalid line: ignore
				continue;
			}
			
				
			/*
			 * Valid line
			 *          0          0         
			 *          0123456789 01234 
			 * wbrbgl01 2008/12/31 00:00 UCD-SNMP-MIB::ssCpuIdle.0 = 94
			 */		

				
			// Get time, skip if invalid format
			if (date==null || !time.equals(tokens[1]) || time==null || !time.equals(tokens[2])) {			
				// Get gc, skip if invalid format
				try {
					curr = new GregorianCalendar(
									Integer.parseInt(tokens[1].substring(0,4))   ,
									Integer.parseInt(tokens[1].substring(5,7))-1 ,
									Integer.parseInt(tokens[1].substring(8))   ,
									Integer.parseInt(tokens[2].substring(0,2))   ,
									Integer.parseInt(tokens[2].substring(3))   ,
									0													   );				
							
				} catch (Exception e) {
					continue;
				}
				
				date=tokens[1];
				time=tokens[2];
			}
			
			if (start==null)
				start=curr;
								
			if (end==null || end.before(curr))
				end=curr;
		}

		// Close file
		br.close();
	}
	
	public Parser getParser(int num) {
		return parser[num]; 
	}
	
	public String[] getParserNames() {
		if (parser == null)
			return null;
			
		String result[] = new String[parser.length];
		for (int i=0;  i<parser.length; i++)
			result[i]=parser[i].getFileName();
			
		return result;
	}

}
