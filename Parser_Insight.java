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

public class Parser_Insight extends Parser {
	
	
	public Parser_Insight(ParserManager v) {
		super();
		manager = v;
	}

	@Override
	public void parseData(boolean firstParse, boolean lastParse) {
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of Insight data.");
			if (lastParse)
				endOfData();
		}
	}
	
	
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		BufferedReader	br;			// make buffered reads
		int				slot;		// time slot in data tables
		GregorianCalendar gc;		// last time read
				
	
		
		// Reset performance data if this is first file
		if (firstParse)
			perfData.setLimits(start, end);
		
		br = getReader();
		
		FileTokenizer ft = new FileTokenizer(br,fileName);
		ft.setSeparator(';');
		
		
		
		while ( ft.readLine()==13 ) {
			
			gc = getDate(ft.getStringToken(0));
			if (gc==null)
				continue;
			
			slot = getSlot(gc);
			
			/*
			sBuffer.Format(_T("%s;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d\n"),
					sTimeUTC,
					strCpuStat.nNumberOfCpu,
					strCpuStat.nLoadAvg1Min,
					strCpuStat.nLoadAvg5Min,
					strCpuStat.nLoadAvg15Min,
					strCpuStat.nInterruptsSec,
					strCpuStat.nSystemCallsSec,
					strCpuStat.nContextSwitchesSec,
					strCpuStat.nUserCpuTotal,
					strCpuStat.nSystemCpuTotal,
					strCpuStat.nIdleCpuTotal,
					strCpuStat.nIdleCpuTrue,
					strCpuStat.nWaitCpuTrue);
			*/
			
			perfData.add(slot, PerfData.TOT_CPU, ft.getFloatToken(1));
			perfData.add(slot, PerfData.CPU, PerfData.US, AVG_CPU, ft.getFloatToken(8));
			perfData.add(slot, PerfData.CPU, PerfData.SY, AVG_CPU, ft.getFloatToken(9));
			perfData.add(slot, PerfData.CPU, PerfData.ID, AVG_CPU, ft.getFloatToken(11));
			perfData.add(slot, PerfData.CPU, PerfData.WA, AVG_CPU, ft.getFloatToken(12));							
		}
		
		
		br.close();
		
		if (lastParse)
			perfData.endOfData();
		
		
	}


	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {}
		
		if (start!=null && end!=null)
			valid=true;
	}

	private void parseTimeLimits() throws Exception {
		BufferedReader	br;			// make buffered reads
		
		valid=false;
		
		br = getReader();

		
		FileTokenizer ft = new FileTokenizer(br,fileName);
		ft.setSeparator(';');
		
		int numTokens;
		GregorianCalendar curr;
		
		// Check if first line is an Insight line
		numTokens=ft.readLine();
		total_lines++;
		if (numTokens!=13)
			return;
		curr = getDate(ft.getStringToken(0));
		if (curr==null)
			return;
		
		start = curr;
		end = (GregorianCalendar)curr.clone();
		
		while ( (numTokens=ft.readLine())==13 ) {
			total_lines++;
			
			curr = getDate(ft.getStringToken(0));
			if (curr==null)
				continue;
			
			end = curr;								
		}

		br.close();
	}
	
	
	private GregorianCalendar getDate(String s) {
		if (s.length()!=14)
			return null;
		
		// 01234567890123
		// 20080617120648
		// YYYYMMDDhhmmss
		
		GregorianCalendar gc=null;
		
		try {
			gc = new GregorianCalendar (
					Integer.parseInt(s.substring(0,4)), 
					Integer.parseInt(s.substring(4,6))-1,
					Integer.parseInt(s.substring(6,8)),
					Integer.parseInt(s.substring(8,10)),
					Integer.parseInt(s.substring(10,12)),
					Integer.parseInt(s.substring(12))					);
		} catch (NumberFormatException nfe) {
			return null;
		}
		
		return gc;
	}
	
	
	
	
	
}
