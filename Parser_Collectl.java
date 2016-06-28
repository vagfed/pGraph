package pGraph;

import java.io.BufferedReader;
import java.util.GregorianCalendar;

public class Parser_Collectl extends Parser {
	
	
	public Parser_Collectl(ParserManager v) {
		super();
		manager = v;
	}
	

	@Override
	public void parseData(boolean firstParse, boolean lastParse) {
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of collectl data.");
			if (lastParse)
				endOfData();
		}

	}
	
	
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		int i;
		BufferedReader	br;			// make buffered reads
		int				slot;		// time slot in data tables
		GregorianCalendar gc;		// last time read
		String			s, time, date;
		
		float f1;
		
		
		byte	group[] = null;
		byte	type[]  = null;
		String	item[]	= null;
		
		String	used = "";
		String	skipped = "";
				
	
		
		// Reset performance data if this is first file
		if (firstParse)
			perfData.setLimits(start, end);
		
		br = getReader();
		
		FileTokenizer ft = new FileTokenizer(br,fileName);
		ft.setSeparator(' ');
		
		int numTokens=0;
		boolean found=false;
		
		
		// Wait for "#Date Time ..." string
		found=false;
		while (!found && (numTokens=readLineAndShowProgress(ft))>=0) {
			if (numTokens >=2 &&
					ft.getStringToken(0).equals("#Date") &&
					ft.getStringToken(1).equals("Time")  )
				found = true;
		}
		if (!found)
			return;	// it is not a collectl input	
		
		
		// Detect available fields
		group = new byte[numTokens-2];
		type  = new byte[numTokens-2];
		item  = new String[numTokens-2];
		for (i=0; i<numTokens-2; i++) {
			group[i]=type[i]=-1;
			item[i]=null;
		}
		
		// Scan all tokens
		for (i=0; i<numTokens-2; i++) {
			s = ft.getStringToken(i+2);
			
			if (s.startsWith("[CPU")) {
				group[i]=PerfData.CPU;
				if (s.charAt(4)==']') {
					item[i]=Parser.AVG_CPU;
				} else {
					skipped = skipped + s + " ";
					group[i]=-1;
					type[i]=-1;
					item[i]=null;
					continue;
				}
				if (s.substring(5).equals("User%")) {
					used = used + s + " ";
					type[i]=PerfData.US;
					continue;
				}
				if (s.substring(5).equals("Sys%")) {
					used = used + s + " ";
					type[i]=PerfData.SY;
					continue;
				}
				if (s.substring(5).equals("Wait%")) {
					used = used + s + " ";
					type[i]=PerfData.WA;
					continue;
				}
				if (s.substring(5).equals("Idle%")) {
					used = used + s + " ";
					type[i]=PerfData.ID;
					continue;
				}
				skipped = skipped + s + " ";
				group[i]=-1;
				type[i]=-1;
				item[i]=null;
				continue;			
			}
			
			if (s.startsWith("[MEM")) {
				group[i]=PerfData.SYSTEM;
				if (s.substring(5).equals("Tot")) {
					used = used + s + " ";
					type[i]=PerfData.RAM;
					continue;
				}
				if (s.substring(5).equals("Free")) {
					used = used + s + " ";
					type[i]=PerfData.FRE;
					continue;
				}
				if (s.substring(5).equals("SwapIn")) {
					used = used + s + " ";
					type[i]=PerfData.PI;
					continue;
				}
				if (s.substring(5).equals("SwapOut")) {
					used = used + s + " ";
					type[i]=PerfData.PO;
					continue;
				}
				skipped = skipped + s + " ";
				group[i]=-1;
				type[i]=-1;
				item[i]=null;
				continue;				
			}
			
			if (s.startsWith("[NET")) {
				group[i]=PerfData.NETWORK;
				if (s.charAt(4)==']') {
					item[i]="All nets";
				} else {
					skipped = skipped + s + " ";
					group[i]=-1;
					type[i]=-1;
					item[i]=null;
					continue;
				}
				if (s.substring(5).equals("RxKBTot")) {
					used = used + s + " ";
					type[i]=PerfData.NET_READKB;
					continue;
				}
				if (s.substring(5).equals("TxKBTot")) {
					used = used + s + " ";
					type[i]=PerfData.NET_WRITEKB;
					continue;
				}
				skipped = skipped + s + " ";
				group[i]=-1;
				type[i]=-1;
				item[i]=null;
				continue;
			}
			
			if (s.startsWith("[DSK")) {
				group[i]=PerfData.DISK;
				if (s.charAt(4)==']') {
					item[i]="All disks";
				} else {
					skipped = skipped + s + " ";
					group[i]=-1;
					type[i]=-1;
					item[i]=null;
					continue;
				}
				if (s.substring(5).equals("ReadTot")) {
					used = used + s + " ";
					type[i]=PerfData.DSK_RPS;
					continue;
				}
				if (s.substring(5).equals("WriteTot")) {
					used = used + s + " ";
					type[i]=PerfData.DSK_WPS;
					continue;
				}
				if (s.substring(5).equals("ReadKBTot")) {
					used = used + s + " ";
					type[i]=PerfData.DSK_READKB;
					continue;
				}
				if (s.substring(5).equals("WriteKBTot")) {
					used = used + s + " ";
					type[i]=PerfData.DSK_WRITEKB;
					continue;
				}
				skipped = skipped + s + " ";
				group[i]=-1;
				type[i]=-1;
				item[i]=null;
				continue;
			}
			
			skipped = skipped + s + " ";
			group[i]=-1;
			type[i]=-1;
			item[i]=null;
			continue;			
		}
		
		System.out.println("DEBUG: Using fields:   "+used);
		System.out.println("DEBUG: Skipped fields: "+skipped);
			
			
		// Now manage data and configuration change
		while ( (numTokens=readLineAndShowProgress(ft))>=0 ) {
			
			// Check if number of tokens is correct
			if (numTokens != group.length+2) {
				System.out.println(fileName+"@"+lines_read+": Unexpected number of fields. Skipping line.");
				continue;
			}
			
			// Get Time
			date = ft.getStringToken(0);
			time = ft.getStringToken(1);
			try {
				gc = new GregorianCalendar(
						Integer.parseInt(date.substring(0,4)),		// Year
						Integer.parseInt(date.substring(4,6))-1,	// Month, zero based
						Integer.parseInt(date.substring(6)),		// Day
						Integer.parseInt(time.substring(0,2)),		// Hour
						Integer.parseInt(time.substring(3,5)),		// Minutes
						Integer.parseInt(time.substring((6)))	); 	// Seconds
				
			} catch (NumberFormatException nfe) {
				System.out.println("Warning: invalid time label in collectl data. Skipping line. "+fileName+"@"+lines_read);
				continue;
			}
			slot = getSlot(gc);
			
			// Add data
			for (i=0; i<numTokens-2; i++) {
				
				// Skip unknown fields
				if (group[i]==-1)
					continue;			
				
				f1 = ft.getFloatToken(i+2);
				if (f1<0)
					continue;
				
				if (group[i]==PerfData.SYSTEM ) {
					if (type[i]==PerfData.RAM)
						f1 = f1 /1024 ;
					if (type[i]==PerfData.FRE)
						f1 = f1 /1024 ;
				}
				
				perfData.add(slot, group[i], type[i], item[i], f1);				
			}				
		}
		
		br.close();
		
		if (lastParse)
			perfData.endOfData();
		
		
	}
	
	
	
	

	/*
	 * Scan collectl file to find time limits
	 */
	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {}
		
		if (start!=null && end!=null)
			valid=true;
	}
	
	private void parseTimeLimits() throws Exception {
		BufferedReader	br;			// make buffered reads
		String			date, time;		// time label
		
		valid=false;
		
		br = getReader();
		
		FileTokenizer ft = new FileTokenizer(br,fileName);
		ft.setSeparator(' ');
		
		int numTokens;
		GregorianCalendar curr;
		
		
		while ( (numTokens=ft.readLine())>=0 ) {
			total_lines++;
			
			// Check if corrupted line
			if (numTokens < 2)
				continue;
			
			date = ft.getStringToken(0);
			time = ft.getStringToken(1);
			
			// Skip comments
			if (date.startsWith("#"))
				continue;
			
			// Check format
			if (date.length()!=8 || time.length()!=8) {
				System.out.println("Warning: invalid time label in collectl data. Skipping line. "+fileName+"@"+lines_read);
				continue;
			}
			
			try {
				curr = new GregorianCalendar(
						Integer.parseInt(date.substring(0,4)),		// Year
						Integer.parseInt(date.substring(4,6))-1,	// Month, zero based
						Integer.parseInt(date.substring(6)),		// Day
						Integer.parseInt(time.substring(0,2)),		// Hour
						Integer.parseInt(time.substring(3,5)),		// Minutes
						Integer.parseInt(time.substring((6)))	); 	// Seconds
				
			} catch (NumberFormatException nfe) {
				System.out.println("Warning: invalid time label in collectl data. Skipping line. "+fileName+"@"+lines_read);
				continue;
			}
						
			if (start==null) {
				start = curr;
				end = (GregorianCalendar)start.clone();
			} else {
				end = curr;					
			}			
		}

		br.close();
	}

}
