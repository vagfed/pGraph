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
 * Created on Jul 3, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Parser {
	
	protected int 					MAX_TOPPROC = 6000;		// max number of process:pid entries allowed
	protected boolean				avoidTop = false;		// when true, skip top processes	
	protected boolean				avoidDisk = false;		// when true, skip disk data
	protected int					MAX_DISKS = 100;		// skip disk data if more than that
	protected boolean				firstRun = true;		// true only when Parser is run at first time
	
	private final static int		BUFSIZE = 10 * 1024 * 1024;
	
	
	protected boolean valid = false;			// true if valid data
	
	protected GregorianCalendar start=null;	// first sample date
	protected GregorianCalendar end=null; 	// last sample date
	
	protected int total_lines = 0;			// number of lines in file to be parsed
	protected int lines_read;				// lines of data read 
	protected int current_file_line_read;	// lines read in the current file
	
	protected ParserManager  manager = null;	// Manager for feedback

	
	protected String fileName=null;			// full path name of file to be parsed
	
	protected String cecName=null;			// CEC identifier, if known
	
	protected ViewerApplet applet = null;	// applet managing I/O to server


	// Performance data storage
	protected PerfData			perfData = new PerfData();
	
	protected boolean			multipleLPAR = false; 	// load only data meaningful for multiple LPAR
	
	public static String		AVG_CPU = "Avg-CPU";

	
	
	// Default constructor
	public Parser () {
	}
	

	
	/*
	 * Scan the data file to define the first and last sample time
	 * Do not read actual data, just update start & end variables.
	 */
	public abstract void scanTimeLimits();
	
	
	/*
	 * Parse data for only registered items.
	 * If firstParse, previously parsed data is deleted
	 * If lastParse, averages are computed and data will be available
	 */
	public abstract void parseData(boolean firstParse, boolean lastParse);
	
	
		

	/*
	 * End of input data: finish data object setup
	 */
	protected void endOfData() {	
		perfData.endOfData();																
	}	
		
	
	/**
	 * @param calendar
	 */
	public void setEnd(GregorianCalendar calendar) {
		end = calendar;
	}

	/**
	 * @param calendar
	 */
	public void setStart(GregorianCalendar calendar) {
		start = calendar;
	}

	/**
	 * @return
	 */
	public GregorianCalendar getEnd() {
		return end;
	}

	/**
	 * @return
	 */
	public GregorianCalendar getStart() {
		return start;
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param string
	 */
	public void setFileName(String string) {
		fileName = string;
	}
	   


	/**
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @return
	 */
	public String getCecName() {
		return cecName;
	}

	



	
	/*
	 * Return time slot related to given date
	 */
	protected int getSlot(GregorianCalendar gc) {
		if (gc.before(start) || gc.after(end))
			return -1;
		long a = start.getTime().getTime();
		long b = end.getTime().getTime();
		long x = gc.getTime().getTime();
		return (int)(1f*(x-a)/(b-a)*(DataSet.SLOTS-1));
	}


	/*
	 * To be overwritten by parsers that handle multiple files (eg TOPASCEC and CECPARSER)
	 */
	public String[] getParserNames() {
		return null;
	}



	/*
	 * To be overwritten by parsers that handle multiple files (eg TOPASCEC and TOPASCEC)
	 */
	public Parser getParser(int num) {
		return null; 
	}



	public PerfData getPerfData() {
		return perfData;
	}
	
	
	/*
	 * Provide a new BufferedReader from filename
	 */
	protected BufferedReader getReader() throws IOException {
		BufferedReader br;
		
		
		if (applet==null) {			
			if (fileName.endsWith(".gz"))
				br = new BufferedReader( 
						new InputStreamReader(
								new GZIPInputStream(
										new FileInputStream(fileName)
								)
						) ,
						BUFSIZE);
			else
				br = new BufferedReader(new FileReader(fileName),BUFSIZE);
		} else {
			if (fileName.endsWith(".gz"))
				br = new BufferedReader( 
						new InputStreamReader(
								new GZIPInputStream(
										applet.getInputStream(fileName)
								)
						) ,
						BUFSIZE);
			else
				br = new BufferedReader(
						new InputStreamReader(
								applet.getInputStream(fileName)
						) ,
						BUFSIZE);
		}
			
		return br;
	}


	public void setTotal_lines(int total_lines) {
		this.total_lines = total_lines;
	}


	public int getTotal_lines() {
		return total_lines;
	}


	
	
	protected String readLineAndShowProgress(BufferedReader br) throws IOException {
		int step;
		if (total_lines<100)
			step = 10;
		else
			step = total_lines / 100;
		
		String line = br.readLine();
		if (line==null)
			return null;
		lines_read++;
		current_file_line_read++;
		if ( manager!=null && lines_read % step == 0) {
			manager.showReadProgress((int)(100f*lines_read/total_lines));
		}	
		return line;
	}
	
	
	protected int readLineAndShowProgress(FileTokenizer ft) {
		int step;
		if (total_lines<100)
			step = 10;
		else
			step = total_lines / 100;
		
		int num = ft.readLine();
		if (num<0)
			return num;
		
		lines_read++;
		current_file_line_read++;
		if ( manager!=null && lines_read % step == 0) {
			manager.showReadProgress((int)(100f*lines_read/total_lines));
		}	
		return num;
	}



	public void setApplet(ViewerApplet applet) {
		this.applet = applet;
	}



	public void setMultipleLPAR(boolean multipleLPAR) {
		this.multipleLPAR = multipleLPAR;
	}



	public void setCpuWeight(float cpuWeight) {
		perfData.setCpuWeight(cpuWeight);
	}

	public boolean isAvoidDisk() {
		return avoidDisk;
	}



	public void setAvoidDisk(boolean avoidDisk) {
		this.avoidDisk = avoidDisk;
	}
	
	public void setMaxTopProcs(int v) {
		this.MAX_TOPPROC = v;
	}
	
	public void setMaxDisks(int v) {
		this.MAX_DISKS = v;
	}
	

}
