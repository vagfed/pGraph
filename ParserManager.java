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

import java.io.File;
import java.util.GregorianCalendar;



public class ParserManager implements Runnable {
	
	private Viewer 	caller 				= null;
	private File 	source 				= null;
	private Parser 	parser 				= null;
	private boolean singleHost			= true;
	private String 	singleHostFiles[] 	= null;
	private ViewerApplet applet			= null;
	private String	configurationFile	= null;
	private GlobalConfig globalConfig	= null;
	
	public static final byte	UNKNOWN		=  0;
	public static final byte 	NMON		=  1;
	public static final byte 	VMSTAT		=  2;
	public static final byte 	CECDIR		=  3;
	public static final byte 	XMTREND		=  6;
	public static final byte 	TOPASCEC	=  7;
	public static final byte	IOSTAT		=  8;
	public static final byte	LSLPARUTIL	=  9;
	public static final byte	CONFIGFILE	= 10;
	public static final byte	SAR			= 11;
	public static final byte	INSIGHT		= 12;
	public static final byte	COLLECTL	= 13;
	public static final byte	SNMP		= 14;
	
	
	private byte		 		filetype 	= UNKNOWN;	
	
	private byte	numThreads = 0;
	
	// Limits to be applied BEFORE having a parser
	GregorianCalendar			start = null;
	GregorianCalendar			end = null;
		
	public synchronized boolean running () {
		synchronized (this) {
			return numThreads!=0;
		}
	}
	
	public ParserManager(Viewer caller, GlobalConfig gc) {
		this.caller = caller;
		this.globalConfig = gc;
	}
	
	public void showReadProgress (int p) {
		caller.setProgressValue(p);
	}

	
	public void setSource(File f) {
		source = f;
		parser = null;
		configurationFile = null;
		singleHostFiles = null;
	}
	
	public void setLimits(GregorianCalendar start, GregorianCalendar end) {
		if (parser!=null) {
			parser.setStart(start);
			parser.setEnd(end);
		} else {
			
			// Store limits for next parsing (BE CAREFUL!!)
			this.start = start;
			this.end = end;
		}
	}
	
	
	private Parser loadConfigurationFile() {
		
		Parser_CEC parser;

		// Show progress bar as indeterminate bar 
		caller.setProgressMessage("Prescan configuration file: " + configurationFile);
		caller.setProgressIndeterminate(true);
		
		// Try as a CEC parser
		parser = new Parser_CEC(this);
		parser.setConfigurationFile(configurationFile);
		parser.setApplet(null);
		
		// Check if configuration file has provided limits
		start=parser.getStart();
		end=parser.getEnd();
		
		parser.scanTimeLimits();
		
		if (!parser.isValid()) {
			System.out.println("No valid data found.");			
			filetype = UNKNOWN;	
			return null;
		}		
				
		filetype = CONFIGFILE;
		return parser;
	}
	

	
	private Parser loadCECDirectory(File dir) {
		
		Parser parser;

		// Show progress bar as indeterminate bar 
		if (applet==null)
			caller.setProgressMessage("Prescan: " + dir.getPath());
		else
			caller.setProgressMessage("Prescan: multiple files, multiple hosts, from server");
		caller.setProgressIndeterminate(true);
		
		// Try as a CEC parser
		parser = new Parser_CEC(this);
		if (applet==null)
			parser.setFileName(dir.getPath());
		else
			parser.setFileName(null);
		parser.setApplet(applet);
		parser.scanTimeLimits();
		if (!parser.isValid()) {
			System.out.println("No valid data found.");			
			filetype = UNKNOWN;	
			return null;
		}		
				
		filetype = CECDIR;
		return parser;

	}
	
	
	public void setProgressMessage(String s) {
		if (caller==null)
			return;
		caller.setProgressMessage(s);
		
	}
	
	
	
	private Parser loadSingleFile(String fileName) {
		
		Parser parser;
		
		singleHostFiles = new String[1];
		singleHostFiles[0]=fileName;

		
		// Show progress bar as indeterminate bar 
		caller.setProgressIndeterminate(true);
		
		// Try as NMON file
		parser = new Parser_Nmon_Tokenized(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if nmon: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {	
			filetype=NMON;
			return parser;
		} 
		

		// Try as VMSTAT file
		parser = new Parser_Vmstat(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if vmstat: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=VMSTAT;
			return parser;
		} 
			
		
		// Try as TOPASOUT file
		parser = new Parser_Topas(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if topasout: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			if ( ((Parser_Topas)parser).getType() == Parser_Topas.TOPASCEC )
				filetype=TOPASCEC;
			else
				filetype=XMTREND;
			return parser;
		} 
				
	
		// Try as IOSTAT file
		parser = new Parser_Iostat(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if iostat: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=IOSTAT;
			return parser;
		} 
					
		
		// Try as LSLPARUTIL file
		parser = new Parser_Lslparutil(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if lslparutil: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=LSLPARUTIL;
			return parser;
		} 
		
		// Try as SAR file
		parser = new Parser_Sar(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if sar: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=SAR;
			return parser;
		}
		
		// Try as INSIGHT file
		parser = new Parser_Insight(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if Insight: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=INSIGHT;
			return parser;
		}
		
		
		// Try as SNMP file
		parser = new Parser_Snmp(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if Snmp: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=SNMP;
			return parser;
		}
		
		// Try as COLLECTL file
		parser = new Parser_Collectl(this);
		parser.setFileName(fileName);
		parser.setApplet(applet);
		caller.setProgressMessage("Check if Snmp: " + fileName);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			filetype=COLLECTL;
			return parser;
		}

							
		filetype = UNKNOWN;	
		return null;
			
	}
	
	
	private Parser loadMultipleSourcesSingleHost(String[] fileNames) {
		int num=0;	// number of files, not directory
		int i,j;
		int total_lines = 0;
		
		// Check all files and detect start & end
		Parser 	p;
		num=0;
		GregorianCalendar start=null, end=null, gc;
		byte myFileType = UNKNOWN;
		
		for (i=0; i<fileNames.length; i++) {
			
			p = loadSingleFile(fileNames[i]);
			
			// Only some types can be loaded from multiple files
			if (filetype!=NMON && filetype!=LSLPARUTIL && 
					filetype!=XMTREND && filetype!=SAR && filetype!=INSIGHT && filetype!=COLLECTL)
				p=null;
			
			if (p==null)
				fileNames[i]=null;	// invalid file
			else if (myFileType==UNKNOWN) {
				myFileType=filetype;	// first valid type. Following MUST be the same
				start=p.getStart();
				end=p.getEnd();
				num++;
				total_lines += p.getTotal_lines();
			} else if (myFileType!=filetype)
				fileNames[i]=null;	// wrong file type
			else {
				gc=p.getStart();
				if (gc.before(start))
					start=gc;
				gc=p.getEnd();
				if (gc.after(end))
					end=gc;
				num++;
				total_lines += p.getTotal_lines();
			}			
		}
		
		if (num==0) {
			filetype = UNKNOWN;
			return null;
		}
		
		filetype = myFileType;
		
		// Create parser and set start & end
		switch (filetype) {
			case NMON:			parser = new Parser_Nmon_Tokenized(this); break;
			case XMTREND:		parser = new Parser_Topas(this); break;
			case LSLPARUTIL:	parser = new Parser_Lslparutil(this); break;
			case SAR:			parser = new Parser_Sar(this); break;
			case INSIGHT:		parser = new Parser_Insight(this); break;
			case COLLECTL:		parser = new Parser_Collectl(this); break;
			default:			return null; // must never happen!
		}
		parser.setFileName(fileNames[0]);
		parser.setApplet(applet);
		parser.setStart(start);
		parser.setEnd(end);
		parser.setTotal_lines(total_lines);
		
		singleHostFiles = new String[num];
		for (i=0, j=0; i<fileNames.length; i++)
			if (fileNames[i]!=null)
				singleHostFiles[j++]=fileNames[i];
		
		return parser;	
	}

	
	
	private Parser loadDirectorySingleHost(File dir) {
		int num=0;	// number of files, not directory
		File child[];
		int i,j;
		String fileNames[] = null;
		//int total_lines = 0;


		child = dir.listFiles();		
		for (i=0; i<child.length; i++) {
			if (child[i].isDirectory())
				child[i]=null;
			else
				num++;
		}
		
		fileNames = new String[num];
		for (i=0,j=0; i<child.length; i++)
			if (child[i]!=null)
				fileNames[j++]=child[i].getPath();
		
		return loadMultipleSourcesSingleHost(fileNames);
/*		
		// Check all files and detect start & end
		Parser 	p;
		num=0;
		GregorianCalendar start=null, end=null, gc;
		byte myFileType = UNKNOWN;
		
		for (i=0; i<fileNames.length; i++) {
			
			p = loadSingleFile(fileNames[i]);
			
			// Only some types can be loaded from multiple files
			if (filetype!=NMON && filetype!=LSLPARUTIL && filetype!=XMTREND)
				p=null;
			
			if (p==null)
				fileNames[i]=null;	// invalid file
			else if (myFileType==UNKNOWN) {
				myFileType=filetype;	// first valid type. Following MUST be the same
				start=p.getStart();
				end=p.getEnd();
				num++;
				total_lines += p.getTotal_lines();
			} else if (myFileType!=filetype)
				fileNames[i]=null;	// wrong file type
			else {
				gc=p.getStart();
				if (gc.before(start))
					start=gc;
				gc=p.getEnd();
				if (gc.after(end))
					end=gc;
				num++;
				total_lines += p.getTotal_lines();
			}			
		}
		
		if (num==0) {
			filetype = UNKNOWN;
			return null;
		}
		
		filetype = myFileType;
		
		// Create parser and set start & end
		switch (filetype) {
			case NMON:			parser = new Parser_Nmon(this); break;
			case XMTREND:		parser = new Parser_Topas(this); break;
			case LSLPARUTIL:	parser = new Parser_Lslparutil(this); break;
			default:			return null; // must never happen!
		}
		parser.setFileName(fileNames[0]);
		parser.setStart(start);
		parser.setEnd(end);
		parser.setTotal_lines(total_lines);
		
		singleHostFiles = new String[num];
		for (i=0, j=0; i<fileNames.length; i++)
			if (fileNames[i]!=null)
				singleHostFiles[j++]=fileNames[i];
		
		return parser;	
*/		
	}
	
	
	private void parseData() {
		
		// Reset limits: limits must be applied BEFORE!
		start=end=null;
		
		if (parser==null) {
			caller.setProgressMessage("No valid file selected");
			return;
		}
		
		// Show progress bar & parse
		caller.setProgressIndeterminate(false);
		
		if ( (source!=null && source.isFile()) || 
				(singleHost && singleHostFiles.length==1) ) {
			if (applet==null)
				caller.setProgressMessage("Parsing: " + source.getPath());
			else
				caller.setProgressMessage("Parsing: " + singleHostFiles[0]);
			parser.parseData(true,true); 
		} else if (singleHost) {
			caller.setProgressMessage("Parsing: " + singleHostFiles[0]);
			parser.setFileName(singleHostFiles[0]);
			parser.parseData(true,false);
			
			for (int i=1; i<singleHostFiles.length-1; i++) {
				caller.setProgressMessage("Parsing: " + singleHostFiles[i]);
				parser.setFileName(singleHostFiles[i]);
				parser.parseData(false,false);
			}
			
			caller.setProgressMessage("Parsing: " + singleHostFiles[singleHostFiles.length-1]);
			parser.setFileName(singleHostFiles[singleHostFiles.length-1]);
			parser.parseData(false,true);
			
		} else if (configurationFile==null){
			if (applet==null)
				caller.setProgressMessage("Parsing: " + source.getPath());
			else
				caller.setProgressMessage("Parsing: multiple hosts");
			parser.parseData(true,true);
		} else {
			caller.setProgressMessage("Parsing from configuration file");
			parser.parseData(true,true);
		}
		
		showReadProgress(0);	
	}
	
	
	

	public void run() {
		
		synchronized(this) {
			if (numThreads!=0) {
				System.out.println("WARNING: avoided attempt to run two threads into ParserManager");
				return;
			}
			numThreads++;
		}
		
		// required to be sure that Viewer threads have finished painting it
		caller.repaint();
		
		
		if (parser==null) {
			
			if (applet != null) {
				String files[] = applet.getFiles();
				if (files.length==1)
					parser = loadSingleFile(files[0]);
				else if (singleHost)
					parser = loadMultipleSourcesSingleHost(files);
				else
					parser = loadCECDirectory(null);
			
				
			} else if (configurationFile == null){		
				// Create new parser and scan time limits
				if ( source.isFile())
					parser = loadSingleFile(source.getPath());
				else if (source.isDirectory() && singleHost)
					parser = loadDirectorySingleHost(source);
				else
					parser = loadCECDirectory(source);
			} else
				parser = loadConfigurationFile();
		}
		
		// In case limits has been selected before a parser was available
		if (start!=null && end!=null)
			setLimits(start,end);
		
		// Update parser variables
		parser.setMaxTopProcs(globalConfig.getMaxTopProcs());
		parser.setMaxDisks(globalConfig.getMaxDisks());
		
		parseData();
		
		if (configurationFile!=null)
			caller.parsingComplete(configurationFile, parser);
		else if (applet==null)		
			caller.parsingComplete(source.getPath(), parser);
		else if (singleHost)
			caller.parsingComplete(singleHostFiles[0], parser);
		else
			caller.parsingComplete("Multiple files provided by server", parser);
		
		synchronized(this) {
			numThreads--;
		}
		
	}

	public void setSingleHost(boolean singleHost) {
		this.singleHost = singleHost;
	}

	public byte getFiletype() {
		return filetype;
	}

	public void setApplet(ViewerApplet applet) {
		this.applet = applet;
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
		parser = null;
	}


}
