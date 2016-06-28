/*
 * Created on Aug 13, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.io.BufferedReader;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Parser_Topas extends Parser {
	
	private long			delta=0;		// seconds from previous sample
	private byte			type=UNKNOWN;	// topasout type
	
	private static byte		UNKNOWN		= 0;
	public static byte		XMTREND		= 1;
	public static byte		TOPASCEC	= 2;
	
	private Parser			parser[] = null;	// containers of single LPAR data
	
	// Required for TOPASCEC to compute correctly VP and dedicated-CPU data
	private int				lpar_id = -1;		// last LPAR read by TOPASCEC
	private GregorianCalendar lpar_gc = null;	// last gc read by TOPASCEC
	private float			lpar_lp = -1;		// last LP read
	private float			lpar_smt = -1;		// last SMT status read
	private float			lpar_shared=-1;
	
	private GregorianCalendar	cec_gc=null;
	private float			cec_poolsize=-1;
	private float			cec_dedicated=-1;
	
	
	public Parser_Topas(ParserManager v) {
		super();
		manager = v;
	}
	
	

	/* (non-Javadoc)
	 * @see pGraph.Parser#scanTimeLimits()
	 */
	public void scanTimeLimits() {
		try {
			parseTimeLimits();
		} catch (Exception e) {}
		
		if (start!=null && end!=null && type!=UNKNOWN)
			valid=true;
	}
	
	
	private void parseTimeLimits() throws Exception {
		BufferedReader		br;			// make buffered reads
		String				line;		// single line of data
		GregorianCalendar 	curr = null;
		String				time=null;		// time as a string
		String				s;
		
		valid=false;
		final byte 	MAXWAIT = 10;		// Wait up to 10 lines for AAA before giving up.
		
		br = getReader();
				
		while (true) {
			line = br.readLine();

			if (line == null)
				break;	
			
			total_lines++;
			
			// Wait MAXWAIT lines to detech topas type
			if (type==UNKNOWN && total_lines>MAXWAIT) {
				start = null;
				end = null;
				br.close();
				return;
			}
							
			// skip invalid data
			if ( !line.startsWith("Time") ) {
				
				// try to detect data type
				if (line.indexOf("xmtrend recording")>=0)
					type = XMTREND;
				else if (line.indexOf("topas_cec recording")>=0)
					type = TOPASCEC;
				
				continue;
			}
				
			/*
			 * Valid line
			 * 0         1         2         3
			 * 0123456789012345678901234567890
			 * Time="2006/08/12 00:00:24", CPU/gluser=2.68
			 */		
			 
			// Check if time stamp is complete
			if (line.length()<26)
				continue;
				
			// Get time, skip if invalid format
			s = line.substring(6,25);
			if (time==null || !time.equals(s)) {			
				// Get gc, skip if invalid format
				try {
					curr = new GregorianCalendar(
									Integer.parseInt(line.substring( 6,10))   ,
									Integer.parseInt(line.substring(11,13))-1 ,
									Integer.parseInt(line.substring(14,16))   ,
									Integer.parseInt(line.substring(17,19))   ,
									Integer.parseInt(line.substring(20,22))   ,
									Integer.parseInt(line.substring(23,25))	   );				
							
				} catch (Exception e) {
					continue;
				}
				
				time=s;
			}
			
			if (start==null)
				start=curr;
								
			if (end==null || end.before(curr))
				end=curr;
		}
		
		// If type is not known, abort
		if (type == UNKNOWN) {
			start = null;
			end = null;
		}

		// Close file
		br.close();		
	}
	
	

	/* (non-Javadoc)
	 * @see pGraph.Parser#parseData()
	 */
	public void parseData(boolean firstParse, boolean lastParse) {
		try {
			int_parseData(firstParse, lastParse);
		} catch (Exception e) {
			System.out.println(fileName + ": Warning, incomplete parsing of topasout data at line "+current_file_line_read);
			if (lastParse)
				endOfData();
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void int_parseData(boolean firstParse, boolean lastParse) throws Exception {
		BufferedReader		br;				// make buffered reads
		String				line=null;		// single line of data
		String				time=null;		// time as a string
		GregorianCalendar	gc=null;		// current time
		String				s;
		Vector				v;				
		int 				i;
		
		
		// Sanity check: TOPASCEC can be parsed only once
		if (type==TOPASCEC && !firstParse && !firstParse) {
			System.out.println(fileName + " Error, topascec data in multiple file load.");
			return;
		}
		resetTopascecData();
		
		
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
								
				// skip invalid data
				if ( !line.startsWith("Time") )
					continue;
					
				/*
				 * Valid line
				 * 0         1         2         3
				 * 0123456789012345678901234567890
				 * Time="2006/08/12 00:00:24", CPU/gluser=2.68
				 */		
				 
				// Check if time stamp is complete
				if (line.length()<26)
					continue;
					
				// Detect current time and update gc if needed
				s = line.substring(6,25);
				if (time==null || !time.equals(s)) {			
					// Get gc, skip if invalid format
					try {
						gc = new GregorianCalendar(
										Integer.parseInt(line.substring( 6,10))   ,
										Integer.parseInt(line.substring(11,13))-1 ,
										Integer.parseInt(line.substring(14,16))   ,
										Integer.parseInt(line.substring(17,19))   ,
										Integer.parseInt(line.substring(20,22))   ,
										Integer.parseInt(line.substring(23,25))	   );				
								
					} catch (Exception e) {
						continue;
					}			
					
					// Compute delta if this is NOT the first sample
					if (time!=null) {
						/*
						 *         012345678901234567890
						 * time = "2006/08/12 00:00:24"
						 */
						GregorianCalendar pre = new GregorianCalendar(
														Integer.parseInt(time.substring( 0, 4))   ,
														Integer.parseInt(time.substring( 5, 7))-1 ,
														Integer.parseInt(time.substring( 8,10))   ,
														Integer.parseInt(time.substring(11,13))   ,
														Integer.parseInt(time.substring(14,16))   ,
														Integer.parseInt(time.substring(17))	   );
						delta = (gc.getTimeInMillis()-pre.getTimeInMillis())/1000;
					}
					
					time=s;
				}
				
				v=splitLine(line.substring(28));
				if (v==null)
					continue;
				
				if (v.size()<2)
					continue;
				
				// CPU
				if ( ((String)v.elementAt(0)).equals("CPU") ) {
	
					handle_CPU(gc,v);
					continue;
				}
				
				// LPAR
				if ( ((String)v.elementAt(0)).equals("LPAR") ) {
	
					handle_LPAR(gc,v);
					continue;
				}
				
				// Mem
				if ( ((String)v.elementAt(0)).equals("Mem") ) {
	
					handle_Mem(gc,v);
					continue;
				}
				
				// Disk
				if ( !multipleLPAR && ((String)v.elementAt(0)).equals("Disk") ) {
	
					handle_Disk(gc,v);
					continue;
				}
				
				// IP/NetIF
				if ( !multipleLPAR && ((String)v.elementAt(0)).equals("IP") &&
						((String)v.elementAt(1)).equals("NetIF") ) {
	
					handle_NetIF(gc,v);
					continue;
				}																
				
				// Proc
				if ( !multipleLPAR && ((String)v.elementAt(0)).equals("Proc") ) {
	
					handle_Proc(gc,v);
					continue;
				}
				
				// Syscall
				if ( !multipleLPAR && ((String)v.elementAt(0)).equals("Syscall") ) {
	
					handle_Syscall(gc,v);
					continue;
				}
				
				// host/CEC (before AIX 5.3 TL07) or CEC (from AIX 5.3 TL07)
				if ( !multipleLPAR && 
						( ((String)v.elementAt(0)).equals("CEC") || ((String)v.elementAt(1)).equals("CEC") )
						 ) {
	
					handle_CEC(gc,v);
					continue;
				}
				
				// host/LPAR
				if ( ((String)v.elementAt(1)).equals("LPAR") ) {
	
					handle_host_LPAR(gc,v);
					continue;
				}
				
				// WPAR
				if ( !multipleLPAR && ((String)v.elementAt(0)).equals("WLM") ) {
	
					handle_WLM(gc,v);
					continue;
				}
				
				// Filesystem
				if ( !multipleLPAR && v.size()==5 && ((String)v.elementAt(0)).equals("FS") ) {
	
					handle_FS(gc,v);
					continue;
				}
	
	
			}
			
		} catch (Exception e) {
			System.out.println("Error reading line: "+line);
			throw e;
		}
		
		// Finish LPAR setup for missing values
		if (type==TOPASCEC) {
			store_lpar_data();
			store_cec_data();
		}
		
		// Close file
		br.close();

		
		// End of data of single parsers (only if TOPASCEC)
		for (i=0; parser!=null && i<parser.length; i++) {
			parser[i].endOfData();		
		}
		
		
		if (lastParse)
			perfData.endOfData();
		
		// Sanity check: pool and dedicated data if fixed to zero, it was not available
		DataSet pool, tot_cpu, freepool;
		pool = perfData.getData(PerfData.SYSTEM, 0, PerfData.POOL);
		tot_cpu = perfData.getData(PerfData.SYSTEM, 0, PerfData.TOT_CPU);
		freepool = perfData.getData(PerfData.SYSTEM, 0, PerfData.FREEPOOL);
		if ( pool.getMin()==0 && pool.getMax()==0 && 
				tot_cpu.getMin()==0 && tot_cpu.getMax()==0 ) {
			perfData.removeData(PerfData.SYSTEM, 0, PerfData.POOL);
			perfData.removeData(PerfData.SYSTEM, 0, PerfData.TOT_CPU);
		}
		if (freepool.getMin()==0 && freepool.getMax()==0)
			perfData.removeData(PerfData.SYSTEM, 0, PerfData.FREEPOOL);

							
	}


	@SuppressWarnings("unchecked")
	private void handle_CEC(GregorianCalendar gc, Vector v) {
		String 	group, type;
		float	value;
		
		if ( ((String)v.elementAt(0)).equals("CEC") ) {
			// case of AIX 5.3 TL07 and later
			if (v.size()!=4)
				return;
			group = (String)v.elementAt(1);
			type  = (String)v.elementAt(2);
			value = ((Float)v.elementAt(3)).floatValue();
		} else {
			if (v.size()!=5)
				return;
			group = (String)v.elementAt(2);
			type  = (String)v.elementAt(3);
			value = ((Float)v.elementAt(4)).floatValue();
		}
		
		if (cec_gc!=null && gc.getTimeInMillis()!=cec_gc.getTimeInMillis()) {
			// new set of CEC data
			store_cec_data();
			cec_gc=null;
		}
		cec_gc=gc;
		
		
		int slot = getSlot(gc);
		
		if ( group.equals("Lpars") ) {
			
			if ( type.equals("shared") ) {		
				perfData.add(slot, PerfData.SHARED, value );
				return;
			}
			
			if ( type.equals("dedicated") ) {		
				perfData.add(slot, PerfData.DED, value );
				return;
			}			
						
		}
		
		
		if ( group.equals("CPU") ) {
			
			if ( type.equals("app") ) {	
				perfData.add(slot, PerfData.FREEPOOL, value );
				return;
			}
			
			if ( type.equals("poolsize") ) {	
				cec_poolsize=value;
				return;
			}
			
			if ( type.equals("dedicated") ) {	
				cec_dedicated=value;
				return;
			}
			
			if ( type.equals("shr_physb") ) {	
				perfData.add(slot, PerfData.PC, value );
				return;
			}							
		}		
							
	}
	
	
	@SuppressWarnings("unchecked")
	private void handle_host_LPAR(GregorianCalendar gc, Vector v) {
		if (v.size()<5)
			return;
			
			
		// We handle only CPU and Sys data
		if ( !((String)v.elementAt(2)).equals("CPU") &&
				!((String)v.elementAt(2)).equals("Sys") )
			return; 
			
			
		int index;
		index = getLPARIndex((String)v.elementAt(0));
		
		if (index!=lpar_id) {
			// It is a new host: compute previous host data
			store_lpar_data();
			lpar_id=index;
			lpar_gc=gc;
		}
		
		// Sometime LPAR name is missing
		boolean unknownLPAR = false;
		if ( ((String)v.elementAt(0)).equals(""))
			unknownLPAR=true;
		
		int slot = getSlot(gc);
		PerfData pd=parser[index].getPerfData();
		
		if ( ((String)v.elementAt(3)).equals("lcpu") ) {
			lpar_lp=((Float)v.elementAt(4)).floatValue();
			pd.add(slot, PerfData.LP, lpar_lp );
			return;
		}
		
		if ( !unknownLPAR &&
				((String)v.elementAt(3)).equals("user") ) {
			pd.add(slot, PerfData.CPU, PerfData.US, AVG_CPU, ((Float)v.elementAt(4)).floatValue() );
			return;
		}				
		
		if ( !unknownLPAR &&
				((String)v.elementAt(3)).equals("kern") ) {
			pd.add(slot, PerfData.CPU, PerfData.SY, AVG_CPU, ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
		if ( !unknownLPAR &&
				((String)v.elementAt(3)).equals("wait") ) {
			pd.add(slot, PerfData.CPU, PerfData.WA, AVG_CPU, ((Float)v.elementAt(4)).floatValue() );
			return;
		}		
		
		if ( !unknownLPAR &&
				((String)v.elementAt(3)).equals("idle") ) {
			pd.add(slot, PerfData.CPU, PerfData.ID, AVG_CPU, ((Float)v.elementAt(4)).floatValue() );
			return;
		}		
				
		if ( ((String)v.elementAt(3)).equals("physc") ) {
			pd.add(slot, PerfData.PC, ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
		if ( ((String)v.elementAt(3)).equals("entc") ) {
			pd.add(slot, PerfData.ENT, ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
		if ( ((String)v.elementAt(3)).equals("ent") ) {
			pd.add(slot, PerfData.EC, ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
		if ( ((String)v.elementAt(3)).equals("shared") ) {
			lpar_shared=((Float)v.elementAt(4)).floatValue();
			return;
		}
		
		if ( ((String)v.elementAt(3)).equals("smt") ) {
			lpar_smt=((Float)v.elementAt(4)).floatValue();
			return;
		}			
							
	}	
	
	
	@SuppressWarnings("unchecked")
	private void handle_WLM(GregorianCalendar gc, Vector v) {
		int slot = getSlot(gc);
		
		// WLM/SysWPAR2/CPU/consumed=0.20 ==> 5 tokens
		if (v.size() != 5)
			return;
		
		if ( ((String)v.elementAt(2)).equals("CPU") ) {
			perfData.add(slot, PerfData.WPAR, PerfData.WPAR_CPU, (String)v.elementAt(1), ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
		if ( ((String)v.elementAt(2)).equals("MEM") ) {
			perfData.add(slot, PerfData.WPAR, PerfData.WPAR_MEM, (String)v.elementAt(1), ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
		if ( ((String)v.elementAt(2)).equals("DISK") ) {
			perfData.add(slot, PerfData.WPAR, PerfData.WPAR_DISK, (String)v.elementAt(1), ((Float)v.elementAt(4)).floatValue() );
			return;
		}
		
	}


	@SuppressWarnings("unchecked")
	private void handle_CPU(GregorianCalendar gc, Vector v) {
		int slot = getSlot(gc);
		
		if ( ((String)v.elementAt(1)).equals("gluser") ) {
			perfData.add(slot, PerfData.CPU, PerfData.US, AVG_CPU, ((Float)v.elementAt(2)).floatValue() );
			return;
		}
				
		if ( ((String)v.elementAt(1)).equals("glkern") ) { 	
			perfData.add(slot, PerfData.CPU, PerfData.SY, AVG_CPU, ((Float)v.elementAt(2)).floatValue() );
			return;
		}
				
		if ( ((String)v.elementAt(1)).equals("glwait") ) { 	
			perfData.add(slot, PerfData.CPU, PerfData.WA, AVG_CPU, ((Float)v.elementAt(2)).floatValue() );
			return;
		}
				
		if ( ((String)v.elementAt(1)).equals("glidle") ) {
			perfData.add(slot, PerfData.CPU, PerfData.ID, AVG_CPU, ((Float)v.elementAt(2)).floatValue() );
			return;
		}	
				
		/*
		// Removed since LPAR/vcpu provides same information and dedicated can be better managed 
		if ( ((String)v.elementAt(1)).equals("numcpu") ) {
			perfData.add(slot, PerfData.VP, ((Float)v.elementAt(2)).floatValue() );
			return;
		}
		*/						
				
		if ( ((String)v.elementAt(1)).startsWith("cpu") ) {
			// processor id
			int num = Integer.parseInt( ((String)v.elementAt(1)).substring(3) ) +1;
					
			if ( ((String)v.elementAt(2)).equals("user") ) {
				perfData.add(slot, PerfData.CPU, PerfData.US, "CPU"+num, ((Float)v.elementAt(3)).floatValue() );
				return;
			}
					
			if ( ((String)v.elementAt(2)).equals("kern") ) {
				perfData.add(slot, PerfData.CPU, PerfData.SY, "CPU"+num, ((Float)v.elementAt(3)).floatValue() );
				return;
			}

			if ( ((String)v.elementAt(2)).equals("wait") ) {
				perfData.add(slot, PerfData.CPU, PerfData.WA, "CPU"+num, ((Float)v.elementAt(3)).floatValue() );
				return;
			}					
					
			if ( ((String)v.elementAt(2)).equals("idle") ) {
				perfData.add(slot, PerfData.CPU, PerfData.ID, "CPU"+num, ((Float)v.elementAt(3)).floatValue() );
				return;
			}
					
			return;														
		}		
	}
	
	
	@SuppressWarnings("unchecked")
	private void handle_LPAR(GregorianCalendar gc, Vector v) {
		int slot = getSlot(gc);
		
		if ( ((String)v.elementAt(1)).equals("pcpuinpool") ) {
			perfData.add(slot, PerfData.POOL, ((Float)v.elementAt(2)).floatValue() );
			return;
		}
			
		// Wait to know if dedicated or shared
		if ( ((String)v.elementAt(1)).equals("vcpu") ) {
			if (lpar_shared<0)
				return;		// do not know how to manage data
			if (lpar_shared>0)
				perfData.add(slot,PerfData.VP,((Float)v.elementAt(2)).floatValue());
			else
				perfData.add(slot,PerfData.TOT_CPU,((Float)v.elementAt(2)).floatValue());
			
			return;
		}
	
		
		if ( ((String)v.elementAt(1)).equals("lcpu") ) {
			perfData.add(slot, PerfData.LP, ((Float)v.elementAt(2)).floatValue() );
			return;
		}		
		
		if ( ((String)v.elementAt(1)).equals("app") ) {
			perfData.add(slot, PerfData.FREEPOOL, ((Float)v.elementAt(2)).floatValue() );
			return;
		}		
		
		if ( lpar_shared>0 && ((String)v.elementAt(1)).equals("ent") ) {
			perfData.add(slot, PerfData.ENT, ((Float)v.elementAt(2)).floatValue() );
			return;
		}	
		
		if ( lpar_shared>0 && ((String)v.elementAt(1)).equals("physc") ) {
			perfData.add(slot, PerfData.PC, ((Float)v.elementAt(2)).floatValue() );
			return;
		}	
		
		if ( lpar_shared>0 && ((String)v.elementAt(1)).equals("entc") ) {
			perfData.add(slot, PerfData.EC, ((Float)v.elementAt(2)).floatValue() );
			return;
		}
		
		if ( ((String)v.elementAt(1)).equals("shared") ) {
			lpar_shared = ((Float)v.elementAt(2)).floatValue();
			return;
		}
	}	
	
	
	@SuppressWarnings("unchecked")
	private void handle_Disk(GregorianCalendar gc, Vector v) {
		String	name	= (String)v.elementAt(1);
		int 	slot = getSlot(gc);
		
		if ( ((String)v.elementAt(2)).equals("busy") ) {
			if (name.startsWith("hdiskpower"))
				return;
			else if (name.startsWith("dac"))
				return;
			else
				perfData.add(slot, PerfData.DISK, PerfData.DSK_BUSY, name, ((Float)v.elementAt(3)).floatValue());
			return;
		}
		
		if ( ((String)v.elementAt(2)).equals("xfer") ) {
			if (name.startsWith("hdiskpower"))
				perfData.add(slot, PerfData.ESS, PerfData.ESS_XFER, name, ((Float)v.elementAt(3)).floatValue());
			else if (name.startsWith("dac"))
				perfData.add(slot, PerfData.DAC, PerfData.DSK_XFER, name, ((Float)v.elementAt(3)).floatValue());
			else
				perfData.add(slot, PerfData.DISK, PerfData.DSK_XFER, name, ((Float)v.elementAt(3)).floatValue());
			return;
		}
		
		if ( ((String)v.elementAt(2)).equals("rblk") && delta!=0 ) {
			if (name.startsWith("hdiskpower"))
				perfData.add(slot, PerfData.ESS, PerfData.ESS_READKB, name, ((Float)v.elementAt(3)).floatValue()/delta);
			else if (name.startsWith("dac"))
				perfData.add(slot, PerfData.DAC, PerfData.DSK_READKB, name, ((Float)v.elementAt(3)).floatValue()/delta);
			else
				perfData.add(slot, PerfData.DISK, PerfData.DSK_READKB, name, ((Float)v.elementAt(3)).floatValue()/delta);
			return;
		}				
		
		if ( ((String)v.elementAt(2)).equals("wblk") && delta!=0 ) {
			if (name.startsWith("hdiskpower"))
				perfData.add(slot, PerfData.ESS, PerfData.ESS_WRITEKB, name, ((Float)v.elementAt(3)).floatValue()/delta);
			else if (name.startsWith("dac"))
				perfData.add(slot, PerfData.DAC, PerfData.DSK_WRITEKB, name, ((Float)v.elementAt(3)).floatValue()/delta);
			else
				perfData.add(slot, PerfData.DISK, PerfData.DSK_WRITEKB, name, ((Float)v.elementAt(3)).floatValue()/delta);
			return;
		}		
		
		if ( ((String)v.elementAt(2)).equals("avgserv") ) {
			if (name.startsWith("hdiskpower"))
				perfData.add(slot, PerfData.ESS, PerfData.ESS_AVGSERV, name, ((Float)v.elementAt(3)).floatValue());
			else if (name.startsWith("dac"))
				perfData.add(slot, PerfData.DAC, PerfData.DSK_AVGSERV, name, ((Float)v.elementAt(3)).floatValue());
			else
				perfData.add(slot, PerfData.DISK, PerfData.DSK_AVGSERV, name, ((Float)v.elementAt(3)).floatValue());
			return;
		}
		
		if ( ((String)v.elementAt(2)).equals("avgwait") ) {
			if (name.startsWith("hdiskpower"))
				perfData.add(slot, PerfData.ESS, PerfData.ESS_AVGWAIT, name, ((Float)v.elementAt(3)).floatValue());
			else if (name.startsWith("dac"))
				perfData.add(slot, PerfData.DAC, PerfData.DSK_AVGWAIT, name, ((Float)v.elementAt(3)).floatValue());
			else
				perfData.add(slot, PerfData.DISK, PerfData.DSK_AVGWAIT, name, ((Float)v.elementAt(3)).floatValue());
			return;
		}
	}

	
	
	@SuppressWarnings("unchecked")
	private void handle_FS(GregorianCalendar gc, Vector v) {
		String	name	= (String)v.elementAt(1)+":"+(String)v.elementAt(2);
		int 	slot = getSlot(gc);
		
		// FS/rootvg/fslv04/%totused=59.58
		// FS/rootvg/fslv04/%nodesused=0.10
		
		if ( ((String)v.elementAt(3)).equals("%totused") ) {
			perfData.add(slot, PerfData.FS, PerfData.SPACEUSED, name, ((Float)v.elementAt(4)).floatValue());
			return;
		}
		
		if ( ((String)v.elementAt(3)).equals("%nodesused") ) {
			perfData.add(slot, PerfData.FS, PerfData.INODEUSED, name, ((Float)v.elementAt(4)).floatValue());
			return;
		}
	}
	


	@SuppressWarnings("unchecked")
	private void handle_NetIF(GregorianCalendar gc, Vector v) {
		String	name	= (String)v.elementAt(2);
		int 	slot 	= getSlot(gc);
		
		if ( ((String)v.elementAt(3)).equals("ioctet_kb") && delta!=0 ) {
			perfData.add(slot, PerfData.NETWORK, PerfData.NET_READKB, name, ((Float)v.elementAt(4)).floatValue()/delta);
			return;
		}
		
		if ( ((String)v.elementAt(3)).equals("ooctet_kb") && delta!=0 ) {
			perfData.add(slot, PerfData.NETWORK, PerfData.NET_WRITEKB, name, ((Float)v.elementAt(4)).floatValue()/delta);
			return;
		}		
		
		
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
		for (int i=0; i<old.length; i++)
			parser[i]=old[i];
			
		parser[old.length] = new Parser_Topas(manager);
		parser[old.length].setFileName(name);
		parser[old.length].setApplet(applet);
		parser[old.length].setStart(start);
		parser[old.length].setEnd(end);
		parser[old.length].getPerfData().setLimits(start, end);

		return old.length;		
	}		
	


	@SuppressWarnings("unchecked")
	private void handle_Mem(GregorianCalendar gc, Vector v) {
		int 	slot 	= getSlot(gc);
		
		if ( ((String)v.elementAt(1)).equals("Real") ) {
		
			if ( ((String)v.elementAt(2)).equals("numfrb") ) {
				perfData.add(slot, PerfData.FRE, ((Float)v.elementAt(3)).floatValue() *4/1024 );
				perfData.add(slot, PerfData.NUMFREE, ((Float)v.elementAt(3)).floatValue() );
				return;
			}
			
			if ( ((String)v.elementAt(2)).equals("size") ) {
				perfData.add(slot, PerfData.RAM, ((Float)v.elementAt(3)).floatValue() *4/1024 );
				return;
			}	
			
			if ( ((String)v.elementAt(2)).equals("%noncomp") ) {
				perfData.add(slot, PerfData.NUMPERM, ((Float)v.elementAt(3)).floatValue() );
				return;
			}					
		}
		
		
		if ( ((String)v.elementAt(1)).equals("Virt") ) {
			
			if ( ((String)v.elementAt(2)).equals("pgspgin") ) {
				perfData.add(slot, PerfData.PI, ((Float)v.elementAt(3)).floatValue() );
				return;
			}
			
			if ( ((String)v.elementAt(2)).equals("pgspgout") ) {
				perfData.add(slot, PerfData.PO, ((Float)v.elementAt(3)).floatValue() );
				return;
			}		
			
			if ( ((String)v.elementAt(2)).equals("steal") ) {
				perfData.add(slot, PerfData.FR, ((Float)v.elementAt(3)).floatValue() );
				return;
			}									
			
		}		
	}
	
	
	@SuppressWarnings("unchecked")
	private void handle_Proc(GregorianCalendar gc, Vector v) {
		int 	slot 	= getSlot(gc);
		
		if ( ((String)v.elementAt(1)).equals("runque") ) 
			perfData.add(slot, PerfData.RUNQ, ((Float)v.elementAt(2)).floatValue() );
			
		if ( ((String)v.elementAt(1)).equals("swpque") ) 
			perfData.add(slot, PerfData.SWQ, ((Float)v.elementAt(2)).floatValue() );
									
		if ( ((String)v.elementAt(1)).equals("pswitch") ) 
			perfData.add(slot, PerfData.PSW, ((Float)v.elementAt(2)).floatValue() );			
	}	

	
	@SuppressWarnings("unchecked")
	private void handle_Syscall(GregorianCalendar gc, Vector v) {
		int 	slot 	= getSlot(gc);
		
		if ( ((String)v.elementAt(1)).equals("total") ) 
			perfData.add(slot, PerfData.SYSC, ((Float)v.elementAt(2)).floatValue() );
			
		if ( ((String)v.elementAt(1)).equals("fork") ) 
			perfData.add(slot, PerfData.FORK, ((Float)v.elementAt(2)).floatValue() );
			
		if ( ((String)v.elementAt(1)).equals("exec") ) 
			perfData.add(slot, PerfData.EXEC, ((Float)v.elementAt(2)).floatValue() );
			
		if ( ((String)v.elementAt(1)).equals("read") ) 
			perfData.add(slot, PerfData.READ, ((Float)v.elementAt(2)).floatValue() );							
								
		if ( ((String)v.elementAt(1)).equals("write") ) 
			perfData.add(slot, PerfData.WRITE, ((Float)v.elementAt(2)).floatValue() );					
	}	
		
	
	/*
	 * Input line is a set of tokens delimited by '/' character and finally '=' character.
	 * Create a Vector containing the ordered set of String tokens.
	 */
	@SuppressWarnings("unchecked")
	private Vector splitLine (String s) {
		Vector result = new Vector();
		int begin=0;		// first token's char 
		int end;			// last token's char
		
		// Example: "CPU/cpu1/user=0.55"
		
		
		// Get all tokens that end with '/'
		while ( (end=s.indexOf('/',begin)) >= 0) {
			result.add(s.substring(begin,end));
			begin = end+1;
		}
		
		// Manage variable name and value
		end=s.indexOf('=',begin);
		if (end<0 || end==begin)
			return null;	// invalid line: it has no value or no label
		result.add(s.substring(begin,end));
		result.add(new Float(s.substring(end+1)));

		
		return result;
	}
	
	
	
	

	/**
	 * @return
	 */
	public byte getType() {
		return type;
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
	
	
	private void store_lpar_data() {
		if (lpar_id<0)
			return;
		
		int slot = getSlot(lpar_gc);
		PerfData pd=parser[lpar_id].getPerfData();
		
		// Decide how many VP depending on LP and SMT
		// If dedicated LPAR, put in PC the number of VP
		
		if (lpar_shared!=0) {
			// SPLPAR
			if (lpar_smt!=0)
				pd.add(slot, PerfData.VP, lpar_lp/2 );
			else
				pd.add(slot, PerfData.VP, lpar_lp );
		} else {
			// Use PC to store CPU usage
			if (lpar_smt!=0)
				pd.add(slot, PerfData.PC, lpar_lp/2 );
			else
				pd.add(slot, PerfData.PC, lpar_lp );
		}				
	}
	
	private void store_cec_data() {
		if (cec_gc==null)
			return;
		
		int slot = getSlot(cec_gc);
		perfData.add(slot, PerfData.POOL, cec_poolsize );
		perfData.add(slot, PerfData.TOT_CPU, cec_dedicated+cec_poolsize );
	}
	
	private void resetTopascecData() {
		lpar_id = -1;		
		lpar_gc = null;	
		lpar_lp = -1;		
		lpar_smt = -1;		

		lpar_shared=-1;
		
		cec_gc=null;
		cec_poolsize=-1;
		cec_dedicated=-1;		
	}

}
