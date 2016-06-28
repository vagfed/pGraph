/*
 * Created on Oct 13, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Parser_CEC extends Parser {
	
	private String fileName[] = null;	// Name of file
	private String directory = null;	// Name of directory
	private Parser parser[] = null;
	
	
	private boolean loadFromConfigurationFile = false;
	private float	fileNameWeight[] = null;
	private String 	singleHostDir[] = null;
	private float	singleHostDirWeight[] = null;
	private String	singleHostDirFiles[][] = null;
	private Parser	parserSingleHost[] = null;
	
	private static final byte	UNKNOWN		=0;
	private static final byte 	NMON		=1;
	private static final byte 	XMTREND		=2;
	private static final byte	SAR			=3;
	private static final byte	INSIGHT		=4;
	private byte		 		filetype 	= UNKNOWN;
	
	protected static final byte		VALUE 	= 0;
	protected static final byte		MIN		= 1;
	protected static final byte		MAX		= 2;
	
	private String line;				// current line read
	

	
	public Parser_CEC(ParserManager v) {
		super();
		manager = v;
	}
	
	

	
	public void scanTimeLimits() {
		int i,j;
		GregorianCalendar s,e;
		
		if (applet!=null && fileName==null)
			fileName = applet.getFiles();
		
		parser = new Parser[fileName.length];
		
		for (i=0; i<fileName.length; i++ ) {
				
			if (applet==null && !loadFromConfigurationFile)
				parser[i] = parseFileAndScanTimeLimits(directory+File.separator+fileName[i]);
			else
				parser[i] = parseFileAndScanTimeLimits(fileName[i]);

			if (parser[i]==null) {
				System.out.println("Skipping invalid file: "+fileName[i]);
				continue;
			}
			
			if (loadFromConfigurationFile && filetype==UNKNOWN) {
				System.out.println("Skipping invalid file: "+fileName[i]);
				continue;	// unsupported type
			}
			
			if (loadFromConfigurationFile)
				parser[i].setCpuWeight(fileNameWeight[i]);
			
			s=parser[i].getStart();
			e=parser[i].getEnd();
			
			if (start==null)
				start=s;
			else if (s.before(start))
				start=s;
			
			if (end==null)
				end=e;
			else if (e.after(end))
				end=e;
		}
		
		if (loadFromConfigurationFile) {
			// We must handle single host directories
			parserSingleHost = new Parser[singleHostDir.length];
			Parser p;
			int myFileType;
			int numLines;
			int num;
			String goodNames[];
			
			for (i=0; i<singleHostDir.length; i++) {
				myFileType = UNKNOWN;
				numLines = 0;
				num=0;
				for (j=0; j<singleHostDirFiles[i].length; j++) {
					
					p = parseFileAndScanTimeLimits(singleHostDirFiles[i][j]);
					
					// Only some types can be loaded from multiple files
					if (filetype!=NMON && filetype!=XMTREND && filetype!=SAR && filetype!=INSIGHT)
						p=null;
					
					if (p==null) {
						System.out.println("Skipping invalid file: "+singleHostDirFiles[i][j]);
						singleHostDirFiles[i][j]=null;	// invalid file 			
					} else if (myFileType==UNKNOWN) {
						myFileType=filetype;	// first valid type. Following MUST be the same						
					} else if (myFileType!=filetype) {
						System.out.println("Skipping valid file of inconsistent type in directory: "+singleHostDirFiles[i][j]);
						singleHostDirFiles[i][j]=null;	// wrong file type
					}

					if (singleHostDirFiles[i][j]!=null) {
						num++;
						s=p.getStart();
						e=p.getEnd();
						if (start==null)
							start=s;
						else if (s.before(start))
							start=s;
						
						if (end==null)
							end=e;
						else if (e.after(end))
							end=e;
						
						numLines += p.getTotal_lines();
					}					
				}
				
				if (myFileType==UNKNOWN) {
					// No valid files found
					singleHostDir[i]=null;
					singleHostDirFiles[i]=null;
					continue;
				}
				
				// Create parser
				switch (myFileType) {
					case NMON:			parserSingleHost[i] = new Parser_Nmon(manager); break;
					case XMTREND:		parserSingleHost[i] = new Parser_Topas(manager); break;
					case SAR:			parserSingleHost[i] = new Parser_Sar(manager); break;
					default:			continue; // must never happen!
				}
				parserSingleHost[i].setApplet(applet);
				parserSingleHost[i].setTotal_lines(numLines);
				parserSingleHost[i].setCpuWeight(singleHostDirWeight[i]);
				
				goodNames = new String[num];
				for (j=0; j<singleHostDirFiles[i].length; j++)
					if (singleHostDirFiles[i][j]!=null)
						goodNames[j]=singleHostDirFiles[i][j];
				singleHostDirFiles[i]=goodNames;
			}
		}
		
		// At least one file is valid
		if (start!=null && end!=null)
			valid=true;
	}
	
	

	/* (non-Javadoc)
	 * @see pGraph.Parser#parseData()
	 */
	public void parseData(boolean firstParse, boolean lastParse) {
		int i,j,k;
		
		// Sanity check: can not read multiple directory!
		if (! firstParse && !lastParse) {
			System.out.println("ERROR: wrong firstParse/lastParse in CECParser");
			return;
		}
		
		// Check if there is at least one valid file
		if (!valid)
			return;
		
		// Reset performance data if this is first file
		perfData.setLimits(start, end);

		
		DataSet		ds=null;
		
		
		for (i=0; fileName!=null && i<fileName.length; i++ ) {
			if (parser[i]==null)
				continue;
			parser[i].setStart(start);
			parser[i].setEnd(end);
			parser[i].setMultipleLPAR(true);
			manager.setProgressMessage("[Directory] Parsing: " + parser[i].getFileName());
			parser[i].parseData(true,true);	
		}
		
		if (loadFromConfigurationFile && singleHostDir!=null) {
			for (i=0; i<parserSingleHost.length; i++) {
				if (parserSingleHost[i]==null)
					continue;
				parserSingleHost[i].setStart(start);
				parserSingleHost[i].setEnd(end);
				parserSingleHost[i].setMultipleLPAR(true);
				
				parserSingleHost[i].setFileName(singleHostDirFiles[i][0]);
				manager.setProgressMessage("Parsing: " + singleHostDirFiles[i][0]);
				
				if (singleHostDirFiles[i].length==1)
					parserSingleHost[i].parseData(true,true);
				else {
					parserSingleHost[i].parseData(true,false);
					for (j=1; j<singleHostDirFiles[i].length-1; j++) {
						parserSingleHost[i].setFileName(singleHostDirFiles[i][j]);
						manager.setProgressMessage("Parsing: " + singleHostDirFiles[i][j]);
						parserSingleHost[i].parseData(false,false);
					}
					parserSingleHost[i].setFileName(singleHostDirFiles[i][singleHostDirFiles[i].length-1]);
					manager.setProgressMessage("Parsing: " + singleHostDirFiles[i][singleHostDirFiles[i].length-1]);
					parserSingleHost[i].parseData(false,true);
				}
				
				// Just to provide an unique name to this parser
				parserSingleHost[i].setFileName(singleHostDir[i]);
			}
		}
		
		boolean multipleCECs = false;
		cecName = null;
		// Check if all data is from same CEC	
		if (parser!=null) {
			i=0;
			while (i<parser.length && parser[i]==null)
				i++;
			if (i<parser.length)
				cecName = parser[i].getCecName();
			i++;
			while (i<parser.length && cecName!=null) {
				if (parser[i]!=null && !cecName.equals(parser[i].getCecName())) {
					cecName=null;
					multipleCECs=true;
				}
				i++;
			}		
		}
		if (!multipleCECs && parserSingleHost!=null) {
			if (cecName == null) {
				i=0;
				while (i<parserSingleHost.length && parserSingleHost[i]==null)
					i++;
				if (i<parserSingleHost.length)
					cecName = parserSingleHost[i].getCecName();
				i++;
			} else
				i=0;
			while (i<parserSingleHost.length && cecName!=null) {
				if (parserSingleHost[i]!=null && !cecName.equals(parserSingleHost[i].getCecName())) {
					cecName=null;
					multipleCECs=true;
				}
				i++;
			}
		}


		int 		shared, dedicated;
		PerfData	pd=null;
		
		final byte		VP 		= 0;
		final byte		LP 		= 1;
		final byte		POOL	= 2;
		final byte		PFREE	= 3;
		final byte		ENT		= 4;
		final byte		PC 		= 5;
		final byte		DED_PC	= 6;
		final byte		USEDMEM	= 7;
		final byte		LOGMEM	= 8;
		final byte		PHYMEM	= 9;
		final byte		HYPPAG	= 10;
		final byte		DISKR	= 11;
		final byte		DISKW	= 12;
		final byte		DISKX	= 13;
		final byte		NETR	= 14;
		final byte		NETW	= 15;
		final byte		NUM		= 16;
		
		float sumData[][] = new float[NUM][3];		// MIN, MAX, VALUE
		
		
			
		
		for (i=0; i<DataSet.SLOTS; i++) {	

			shared=0;
			dedicated=0;
			
			for (j=0; j<NUM; j++)
				for (k=0; k<3; k++)
					sumData[j][k]=-1;
			
						
			for (j=0; j<fileName.length; j++) {
				if (parser[j]==null)
					continue;
				
				pd = parser[j].getPerfData();
				
				ds = pd.getData(PerfData.SYSTEM, 0, PerfData.ENT);
				if (ds == null) {
					dedicated++;
					//continue;	// it is not a SPLPAR
				} else
					shared++;
				
				sumData[VP]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.VP),      i, sumData[VP]);
				sumData[LP]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.LP),      i, sumData[LP]);
				sumData[ENT]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.ENT),     i, sumData[ENT]);
				sumData[PC]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.PC),      i, sumData[PC]);
				sumData[DED_PC]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.DED_PC),  i, sumData[DED_PC]);

				
				// Pool data is meaningful only if LPAR all belong to same CEC
				if (cecName != null) {
					// use only first set of valid data (they should all be the same for each LPAR!)
					if (sumData[POOL][VALUE]<0)
						sumData[POOL]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.POOL),      i, sumData[POOL]);
					if (sumData[PFREE][VALUE]<0)
						sumData[PFREE]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.FREEPOOL),  i, sumData[PFREE]);
				}
				
				sumData[USEDMEM]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.USEDMEM),   i, sumData[USEDMEM]);
				sumData[PHYMEM]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.PHYS_MEM),  i, sumData[PHYMEM]);
				sumData[HYPPAG]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.HYPPAG_IN), i, sumData[HYPPAG]);
				
				ds=pd.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_READKB);
				if (ds==null)
					ds=pd.getData(PerfData.SCSI, 0, PerfData.DSK_READKB);
				sumData[DISKR]		= addWithValidData(ds, i, sumData[DISKR]);
				
				ds=pd.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_WRITEKB);
				if (ds==null)
					ds=pd.getData(PerfData.SCSI, 0, PerfData.DSK_WRITEKB);
				sumData[DISKW]		= addWithValidData(ds, i, sumData[DISKW]);

				ds=pd.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_XFER);
				if (ds==null)
					ds=pd.getData(PerfData.SCSI, 0, PerfData.DSK_XFER);
				sumData[DISKX]		= addWithValidData(ds, i, sumData[DISKX]);
				
				ds=pd.getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_READKB);
				if (ds==null) {
					// No global: 1 or 2 networks, one is loopback; otherwise no data at all!
					String s[] = pd.getNames(PerfData.NETWORK);
					if (s!=null && s.length==2) {
						if (s[0].startsWith("lo"))
							ds=pd.getData(PerfData.NETWORK, 1, PerfData.NET_READKB);
						else
							ds=pd.getData(PerfData.NETWORK, 0, PerfData.NET_READKB);
					}
				}
				sumData[NETR]		= addWithValidData(ds, i, sumData[NETR]);
				
				ds=pd.getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_WRITEKB);
				if (ds==null) {
					// No global: 1 or 2 networks, one is loopback
					String s[] = pd.getNames(PerfData.NETWORK);
					if (s!=null && s.length==2) {
						if (s[0].startsWith("lo"))
							ds=pd.getData(PerfData.NETWORK, 1, PerfData.NET_WRITEKB);
						else
							ds=pd.getData(PerfData.NETWORK, 0, PerfData.NET_WRITEKB);
					}
				}
				sumData[NETW]		= addWithValidData(ds, i, sumData[NETW]);				
			}
			
			for (j=0; parserSingleHost!=null && j<parserSingleHost.length; j++) {
				if (parserSingleHost[j]==null)
					continue;
				
				pd = parserSingleHost[j].getPerfData();
				
				ds = pd.getData(PerfData.SYSTEM, 0, PerfData.ENT);
				if (ds == null) {
					dedicated++;
					//continue;	// it is not a SPLPAR
				} else
					shared++;
				
				sumData[VP]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.VP),      i, sumData[VP]);
				sumData[LP]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.LP),      i, sumData[LP]);
				sumData[ENT]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.ENT),     i, sumData[ENT]);
				sumData[PC]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.PC),      i, sumData[PC]);
				sumData[DED_PC]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.DED_PC),  i, sumData[DED_PC]);
				
				// Pool data is meaningful only if LPAR all belong to same CEC
				if (cecName != null) {
					// use only first set of valid data (they should all be the same for each LPAR!)
					if (sumData[POOL][VALUE]<0)
						sumData[POOL]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.POOL),      i, sumData[POOL]);
					if (sumData[PFREE][VALUE]<0)
						sumData[PFREE]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.FREEPOOL),  i, sumData[PFREE]);
				}
				
				sumData[USEDMEM]	= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.USEDMEM),   i, sumData[USEDMEM]);
				sumData[PHYMEM]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.PHYS_MEM),  i, sumData[PHYMEM]);
				sumData[HYPPAG]		= addWithValidData(pd.getData(PerfData.SYSTEM, 0, PerfData.HYPPAG_IN), i, sumData[HYPPAG]);
				
				ds=pd.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_READKB);
				if (ds==null)
					ds=pd.getData(PerfData.SCSI, 0, PerfData.DSK_READKB);
				sumData[DISKR]		= addWithValidData(ds, i, sumData[DISKR]);
				
				ds=pd.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_WRITEKB);
				if (ds==null)
					ds=pd.getData(PerfData.SCSI, 0, PerfData.DSK_WRITEKB);
				sumData[DISKW]		= addWithValidData(ds, i, sumData[DISKW]);

				ds=pd.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_XFER);
				if (ds==null)
					ds=pd.getData(PerfData.SCSI, 0, PerfData.DSK_XFER);
				sumData[DISKX]		= addWithValidData(ds, i, sumData[DISKX]);
				
				ds=pd.getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_READKB);
				if (ds==null) {
					// No global: 1 or 2 networks, one is loopback
					String s[] = pd.getNames(PerfData.NETWORK);
					if (s!=null && s.length==2) {
						if (s[0].startsWith("lo"))
							ds=pd.getData(PerfData.NETWORK, 1, PerfData.NET_READKB);
						else
							ds=pd.getData(PerfData.NETWORK, 0, PerfData.NET_READKB);
					}
				}
				sumData[NETR]		= addWithValidData(ds, i, sumData[NETR]);
				
				ds=pd.getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_WRITEKB);
				if (ds==null) {
					// No global: 1 or 2 networks, one is loopback
					String s[] = pd.getNames(PerfData.NETWORK);
					if (s!=null && s.length==2) {
						if (s[0].startsWith("lo"))
							ds=pd.getData(PerfData.NETWORK, 1, PerfData.NET_WRITEKB);
						else
							ds=pd.getData(PerfData.NETWORK, 0, PerfData.NET_WRITEKB);
					}
				}
				sumData[NETW]		= addWithValidData(ds, i, sumData[NETW]);
			}
			
			perfData.addBySlot(i, PerfData.VP,   		sumData[VP][VALUE], sumData[VP][MIN], sumData[VP][MAX] );
			perfData.addBySlot(i, PerfData.LP,   		sumData[LP][VALUE], sumData[LP][MIN], sumData[LP][MAX] );
			perfData.addBySlot(i, PerfData.ENT,   		sumData[ENT][VALUE], sumData[ENT][MIN], sumData[ENT][MAX] );
			perfData.addBySlot(i, PerfData.PC,   		sumData[PC][VALUE], sumData[PC][MIN], sumData[PC][MAX] );
			perfData.addBySlot(i, PerfData.DED_PC,   	sumData[DED_PC][VALUE], sumData[DED_PC][MIN], sumData[DED_PC][MAX] );
			
			// Pool data is meaningful only if LPAR all belong to same CEC
			if (cecName != null) {		
				perfData.addBySlot(i, PerfData.POOL,     sumData[POOL][VALUE], sumData[POOL][MIN], sumData[POOL][MAX] );
				perfData.addBySlot(i, PerfData.FREEPOOL, sumData[PFREE][VALUE], sumData[PFREE][MIN], sumData[PFREE][MAX] );
			}
			
			perfData.addBySlot(i, PerfData.USEDMEM,   	sumData[USEDMEM][VALUE], sumData[USEDMEM][MIN], sumData[USEDMEM][MAX] );
			perfData.addBySlot(i, PerfData.LOGICAL_MEM, sumData[LOGMEM][VALUE], sumData[LOGMEM][MIN], sumData[LOGMEM][MAX] );
			perfData.addBySlot(i, PerfData.PHYS_MEM,    sumData[PHYMEM][VALUE], sumData[PHYMEM][MIN], sumData[PHYMEM][MAX] );
			perfData.addBySlot(i, PerfData.HYPPAG_IN,   sumData[HYPPAG][VALUE], sumData[HYPPAG][MIN], sumData[HYPPAG][MAX] );

			perfData.addBySlot(i, PerfData.SCSI, PerfData.SCSI_READKB,	"_Global_Disk_Adapter",  sumData[DISKR][VALUE], sumData[DISKR][MIN], sumData[DISKR][MAX] );
			perfData.addBySlot(i, PerfData.SCSI, PerfData.SCSI_WRITEKB,	"_Global_Disk_Adapter",  sumData[DISKW][VALUE], sumData[DISKW][MIN], sumData[DISKW][MAX] );
			perfData.addBySlot(i, PerfData.SCSI, PerfData.SCSI_XFER,	"_Global_Disk_Adapter",  sumData[DISKX][VALUE], sumData[DISKX][MIN], sumData[DISKX][MAX] );
			
			perfData.addBySlot(i, PerfData.NETWORK, PerfData.NET_READKB,	"_Global_Network",  sumData[NETR][VALUE], sumData[NETR][MIN], sumData[NETR][MAX] );
			perfData.addBySlot(i, PerfData.NETWORK, PerfData.NET_WRITEKB,	"_Global_Network",  sumData[NETW][VALUE], sumData[NETW][MIN], sumData[NETW][MAX] );
					
			perfData.add(i, PerfData.SHARED,   	shared );
			perfData.add(i, PerfData.DED,   	dedicated );					
		}
		
		perfData.endOfData();	
	}
	
	
	
	private float getvalidData(DataSet ds, int slot) {
		
		if (ds==null)
			return -1;
		
		int lslot, rslot;
		
		lslot = ds.getNearestValidSlotLeft(slot);
		rslot = ds.getNearestValidSlotRight(slot);

		if (lslot<0 || rslot<0 || 
			lslot>=DataSet.SLOTS || rslot>=DataSet.SLOTS)
			return -1;
		
		if (slot==lslot) {
			// This is a valid slot
			return ds.getValue(slot);																				
		} else {
			// Get a linear interpolation of left and right valid slots
			return linear( slot, lslot, rslot, ds.getValue(lslot), ds.getValue(rslot) );																																													
		}		
	}
	
	
	
	private float[] addWithValidData(DataSet ds, int slot, float[] data) {
		
		if (ds==null)
			return data;
		
		int lslot, rslot;
		
		lslot = ds.getNearestValidSlotLeft(slot);
		rslot = ds.getNearestValidSlotRight(slot);

		if (lslot<0 || rslot<0 || 
			lslot>=DataSet.SLOTS || rslot>=DataSet.SLOTS)
			return data;
		
		float min, max, value;
		
		if (slot==lslot) {
			// This is a valid slot
			min 	= 		ds.getAbsMin(slot);
			max 	= 		ds.getAbsMax(slot);
			value 	= 		ds.getValue(slot);																			
		} else {
			// Get a linear interpolation of left and right valid slots
			float lvalue, rvalue;
			
			lvalue=ds.getAbsMin(lslot); rvalue=ds.getAbsMin(rslot);
			min = linear( slot, lslot, rslot, lvalue, rvalue );
			
			lvalue=ds.getAbsMax(lslot); rvalue=ds.getAbsMax(rslot);
			max = linear( slot, lslot, rslot, lvalue, rvalue );
			
			lvalue=ds.getValue(lslot);   rvalue=ds.getValue(lslot);
			value = linear( slot, lslot, rslot, lvalue, rvalue );
		}
		
		if (value<0)
			return data;
		
		if (data[VALUE]>=0)
			data[VALUE] += value;
		else
			data[VALUE] = value;
		
		if (data[MIN]>=0)
			data[MIN] += min;
		else
			data[MIN] = min;
		
		if (data[MAX]>=0)
			data[MAX] += max;
		else
			data[MAX] = max;
		
			
		return data;																																															
	}




	
	/**
	 * Input: directory path to read
	 */
	public void setFileName(String string) {
		
		if (string==null)
			return;			// will happen if applet
		
		File dir, file;
		int num=0;	// number of files, not directory
		String s[];
		int i,j;
		
		directory = string;
		
		dir = new File(string);
		s = dir.list();
		
		for (i=0; i<s.length; i++) {
			file = new File(string,s[i]);
			if (file.isDirectory())
				s[i]=null;
			else
				num++;
		}
		
		fileName = new String[num];
		for (i=0,j=0; i<s.length; i++)
			if (s[i]!=null)
				fileName[j++]=s[i];
	}
	
	
	public void setConfigurationFile(String file) {
		if (file==null)
			return;
		
		try {
			readConfigurationFile(file);
		} catch (IOException ioe) {
			System.out.println("Error reading configuration file.");
			System.out.println("Offending line is: "+line);
			System.out.println("Remaining lines are ignored.");
			return;
		}
		
		loadFromConfigurationFile = true;
	}
	
	
	
	public Parser getParser(int num) {
		if (parser==null)
			return parserSingleHost[num]; 
		
		if (num<parser.length)
			return parser[num];
		else
			return parserSingleHost[num-parser.length];
	}
	
	
	private float linear(int x, int x1, int x2, float y1, float y2) {
		float f;
		
		f = 1f*(y2-y1)/(x2-x1)*x+(1f*x2*y1-1f*x1*y2)/(x2-x1);
		
		return f;
		
	}
	
	
	private Parser parseFileAndScanTimeLimits(String name) {
		Parser parser;
		
		// Try as NMON
		parser = new Parser_Nmon(manager);
		parser.setFileName(name);
		parser.setApplet(applet);
		manager.setProgressMessage("[Directory] Check if nmon: " + name);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			//System.out.println("nmon:      " + name);	
			filetype = NMON;
			return parser;
		}
		
		
		// Try as VMSTAT file
		parser = new Parser_Vmstat(manager);
		parser.setFileName(name);
		parser.setApplet(applet);
		manager.setProgressMessage("Check if vmstat: " + name);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			System.out.println("vmstat -t: " + name);
			return parser;
		}
		

		// Try as TOPASOUT file
		parser = new Parser_Topas(manager);
		parser.setFileName(name);
		parser.setApplet(applet);
		manager.setProgressMessage("[Directory] Check if topasout: " + name);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			//System.out.println("topasout:  " + name);
			if ( ((Parser_Topas)parser).getType() == Parser_Topas.TOPASCEC )
				filetype=UNKNOWN;
			else
				filetype=XMTREND;
			return parser;
		}
		
		
		// Try as SAR file
		parser = new Parser_Sar(manager);
		parser.setFileName(name);
		parser.setApplet(applet);
		manager.setProgressMessage("Check if sar: " + name);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			System.out.println("sar -t: " + name);
			filetype = SAR;
			return parser;
		}
		
		
		// Try as INSIGHT file
		parser = new Parser_Insight(manager);
		parser.setFileName(name);
		parser.setApplet(applet);
		manager.setProgressMessage("Check if Insight: " + name);
		parser.scanTimeLimits();
		
		if (parser.isValid()) {
			System.out.println("Insight: " + name);
			filetype = INSIGHT;
			return parser;
		}
		
				
		System.out.println("invalid:   " + name);	
		return null;			
	}
	
	
	public String[] getParserNames() {
		int i;
		
		if (parser == null)
			return null;
			
		String result[] = null;
		int ix;
		int num;
		
		if (parserSingleHost==null) 
			result = new String[parser.length];
		else
			result = new String[parser.length+parserSingleHost.length];
		 
		for ( i=0;  parser!=null && i<parser.length; i++) { 
			if (parser[i]!= null) {
				ix = parser[i].getFileName().lastIndexOf(File.separatorChar);
				result[i]=parser[i].getFileName().substring(ix+1);
			}
		}
		
		if (parser==null)
			num=0;
		else
			num=parser.length;
		for ( i=0;  parserSingleHost!=null && i<parserSingleHost.length; i++) { 
			if (parserSingleHost[i]!= null) {
				ix = parserSingleHost[i].getFileName().lastIndexOf(File.separatorChar);
				result[num+i]=parserSingleHost[i].getFileName().substring(ix+1);
			}
		}
			
		return result;
	}
	
	
	
	private String[] splitConfigurationLine(String line) {
		if (line==null)
			return null;
		
		if (line.startsWith("#"))
			return null;
		
		String tokens[] = line.split("(\\s*=\\s*)|(\\s*@\\s*)|(\\s+-\\s+)");
		int i=0;
		
		for (i=0; i<tokens.length; i++)
			tokens[i] = tokens[i].trim();
	
		for (i=0; i<tokens.length; i++)
			if (tokens[i]!=null && 
					tokens[i].startsWith("\"") &&
					tokens[i].endsWith("\""))
				tokens[i]=tokens[i].substring(1, tokens[i].length()-1);
		
		return tokens; 
	}
	
	
	@SuppressWarnings("unchecked")
	private void readConfigurationFile (String cfg)  throws IOException {
		BufferedReader br;
		
		try {
			br = new BufferedReader(new FileReader(cfg),1024*1024);
		} catch (FileNotFoundException fnfe) {
			System.out.println("Configuration file "+cfg+" not found.");
			return;
		}
		
		String base = null;
		String tokens[];
		File   f;
		float  w;
		
		Vector sh_dir 	= new Vector();
		Vector sh_dir_w = new Vector();
		Vector mh_dir 	= new Vector();
		Vector mh_dir_w = new Vector();
		Vector file 	= new Vector();
		Vector file_w 	= new Vector();
		
		
		// Reset time limits
		start = end = null;
		
		
		while ( (line=br.readLine())!=null ) {
			tokens = splitConfigurationLine(line);
			if (tokens==null) 
				continue;
			
			if (tokens[0].equalsIgnoreCase("ZOOM")) {
				if (tokens.length != 3) {
					System.out.println("Configuration file error, line skipped: "+line);
					continue;
				}
				start = getGC(tokens[1]);
				end   = getGC(tokens[2]);
				
				if (start==null || end==null || end.before(start)) {
					System.out.println("Configuration file error, line skipped: "+line);
					start=end=null;
				}
				continue;
			}
			
			if (tokens[0].equalsIgnoreCase("BASE")) {
				if (tokens.length != 2) {
					System.out.println("Configuration file error, line skipped: "+line);
					continue;
				}			
				f = new File(tokens[1]);
				if (!f.isAbsolute()) {
					System.out.println("BASE must be an absolute path! Line skipped: "+line);
					continue;
				}
				if (!f.exists() || !f.isDirectory()) {
					System.out.println("BASE does not exist or it is not a directory, line skipped: "+line);
					continue;
				}
				base = tokens[1];
				if (!base.endsWith(File.separator))
					base += File.separator;
				continue;
			}
			
			if (tokens[0].equalsIgnoreCase("SH_DIR")) {
				if (tokens.length<2 || tokens.length>3) {
					System.out.println("Configuration file error, line skipped: "+line);
					continue;
				}
				if (tokens.length==3) {
					try {
						w = Float.parseFloat(tokens[2]);
					} catch (NumberFormatException nfe) {
						System.out.println("Invalid float number, line skipped: "+line);
						continue;
					}
				} else
					w = 1;			
				f = new File(tokens[1]);
				if (f.isAbsolute()) {
					if (!f.exists() || !f.isDirectory()) {
							System.out.println("Invalid absolute directory, line skipped: "+line);
							continue;
					} else
						sh_dir.add(tokens[1]);
				} else {
					f = new File(base + tokens[1]);
					if (!f.exists() || !f.isDirectory()) {
						System.out.println("Invalid relative directory, line skipped: "+line);
						continue;
					} else
						sh_dir.add(base+tokens[1]);
				}
				sh_dir_w.add(new Float(w));
				continue;
			}
			
			if (tokens[0].equalsIgnoreCase("MH_DIR")) {
				if (tokens.length<2 || tokens.length>3) {
					System.out.println("Configuration file error, line skipped: "+line);
					continue;
				}
				if (tokens.length==3) {
					try {
						w = Float.parseFloat(tokens[2]);
					} catch (NumberFormatException nfe) {
						System.out.println("Invalid float number, line skipped: "+line);
						continue;
					}
				} else
					w = 1;			
				f = new File(tokens[1]);
				if (f.isAbsolute()) {
					if (!f.exists() || !f.isDirectory()) {
							System.out.println("Invalid absolute directory, line skipped: "+line);
							continue;
					} else
						mh_dir.add(tokens[1]);
				} else {
					f = new File(base + tokens[1]);
					if (!f.exists() || !f.isDirectory()) {
						System.out.println("Invalid relative directory, line skipped: "+line);
						continue;
					} else
						mh_dir.add(base+tokens[1]);
				}
				mh_dir_w.add(new Float(w));
				continue;
			}
			
			// It's a file!!!!
			if (tokens.length>2) {
				System.out.println("Invalid number of tokens, line skipped: "+line);
				continue;
			}
			if (tokens.length==2) {
				try {
					w = Float.parseFloat(tokens[1]);
				} catch (NumberFormatException nfe) {
					System.out.println("Invalid float number, line skipped: "+line);
					continue;
				}
			} else
				w = 1;
			f = new File(tokens[0]);
			if (f.isAbsolute()) {
				if (!f.exists() || f.isDirectory()) {
						System.out.println("Invalid absolute file, line skipped: "+line);
						continue;
				} else
					file.add(tokens[0]);
			} else {
				f = new File(base + tokens[0]);
				if (!f.exists() || f.isDirectory()) {
					System.out.println("Invalid relative file, line skipped: "+line);
					continue;
				} else
					file.add(base+tokens[0]);
			}
			file_w.add(new Float(w));	
		}
		
		int i,j,k;
		String s[];
		int num=0;
		
		for (i=0; i<mh_dir.size(); i++) {
			f = new File((String)mh_dir.elementAt(i));
			s = f.list();			
			for (j=0; j<s.length; j++) {
				f = new File((String)mh_dir.elementAt(i),s[j]);
				if (!f.isDirectory())
					num++;
			}		
		}
		
		fileName = new String[num + file.size()];
		fileNameWeight = new float[num + file.size()];
		num=0;
		for (i=0; i<mh_dir.size(); i++) {
			f = new File((String)mh_dir.elementAt(i));
			s = f.list();
			for (j=0; j<s.length; j++) {
				f = new File((String)mh_dir.elementAt(i),s[j]);
				if (!f.isDirectory()) {
					fileName[num]=(String)mh_dir.elementAt(i)+File.separator+s[j];
					fileNameWeight[num]=((Float)mh_dir_w.elementAt(i)).floatValue();
					// System.out.println(fileName[num]);
					num++;
				}
			}		
		}
		for (i=0; i<file.size(); i++) {
			fileName[num]=(String)file.elementAt(i);
			fileNameWeight[num]=((Float)file_w.elementAt(i)).floatValue();
			num++;
		}
		

		singleHostDir = new String[sh_dir.size()];
		singleHostDirWeight = new float[sh_dir_w.size()];
		singleHostDirFiles = new String[sh_dir.size()][];
		File child[];
		for (i=0; i<sh_dir.size(); i++) {
			singleHostDir[i] = (String)sh_dir.elementAt(i);
			singleHostDirWeight[i] = ((Float)sh_dir_w.elementAt(i)).floatValue();
			
			child = new File(singleHostDir[i]).listFiles();	
			num=0;
			for (j=0; j<child.length; j++) {
				if (child[j].isDirectory())
					child[j]=null;
				else
					num++;
			}	
			if (num==0)
				System.out.println("Warning! Empty single host dir: "+singleHostDir[i]);
			singleHostDirFiles[i] = new String[num];
			for (j=0,k=0; j<child.length; j++)
				if (child[j]!=null)
					singleHostDirFiles[i][k++]=child[j].getPath();
		}

	}
	
	private GregorianCalendar getGC(String string) {
		if (string.length()!="YYYYMMDDhhmmss".length())
			return null;
			
		int Y,M,D,h,m,s;
		
		try {
			Y = Integer.parseInt(string.substring(0,4),10);
			M = Integer.parseInt(string.substring(4,6),10);
			D = Integer.parseInt(string.substring(6,8),10);
			h = Integer.parseInt(string.substring(8,10),10);
			m = Integer.parseInt(string.substring(10,12),10);
			s = Integer.parseInt(string.substring(12),10);
		} catch (NumberFormatException nfe) {
			return null;
		}
		
		return new GregorianCalendar(Y,M-1,D,h,m,s);
	}

}
