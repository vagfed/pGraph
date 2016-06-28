package pGraph;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Parser_Lslparutil extends Parser {
	
	private static final int 	MAXLPAR 		= 1024;
	private static final int	MAXPROCPOOLS	= 256;
	private String	lparName[] = new String[MAXLPAR];
	private int		dataId[] = new int[MAXLPAR];
	private int		numLpar=0;							// number of valid lparName entries
	
	private PerfData lparPerfData[] = new PerfData[MAXLPAR];
	
	private BigDecimal	lparData[][] = new BigDecimal[MAXLPAR][NUM_LPARDATA];
	private static final byte	CAPPED_CYCLES			= 0;
	private static final byte	UNCAPPED_CYCLES			= 1;
	private static final byte	ENTITLED_CYCLES			= 2;
	private static final byte	PROC_UNITS				= 3;
	private static final byte	PROCS					= 4;
	private static final byte	SHARED_CYCLES			= 5;
	private static final byte	LPAR_TIME_CYCLES		= 6;
	
	// Shared memory partition only
	private static final byte	LOGICAL_MEM				= 7;
	private static final byte	PHYS_MEM				= 8;
	private static final byte	LOAN_MEM				= 9;
	
	// CPI computation
	private static final byte	RUN_LATCH_CYCLES		= 10;	// POWER8 --> total_instructions
	private static final byte	RUN_LATCH_INSTRUCTIONS	= 11;   // POWER8 --> total_instructions_execution_time
	
	private static final byte	NUM_LPARDATA			= 12;
	
	private GregorianCalendar lparTime[] = new GregorianCalendar[MAXLPAR];
	
	private BigDecimal	poolData[] = new BigDecimal[NUM_POOLDATA];
	private BigDecimal	procPoolData[][] = new BigDecimal[MAXPROCPOOLS][NUM_POOLDATA];
	private static final byte	UTILIZED_POOL_CYCLES 		= 0;
	private static final byte	TOTAL_POOL_CYCLES 			= 1;
	private static final byte	POOL_PROC_UNITS 			= 2;
	private static final byte	BORROWED_POOL_PROC_UNITS 	= 3;
	private static final byte	AVAILABLE_POOL_PROC_UNITS 	= 4;
	private static final byte	POOL_TIME_CYCLES			= 5;
	private static final byte	NUM_POOLDATA				= 6;
	
	private String		procPoolName[] = new String[MAXPROCPOOLS]; 
	
	private BigDecimal 	memPoolData[] = new BigDecimal[NUM_MEMPOOLDATA];
	private static final byte	PAGE_FAULTS					= 0;
	private static final byte	PAGE_IN_DELAY				= 1;
	private static final byte	POOL_MEM					= 3;
	private static final byte	LPAR_RUM_MEM				= 4;
	private static final byte	SYS_FIRMWARE_POOL_MEM		= 5;
	private static final byte	NUM_MEMPOOLDATA				= 6;
	
	
	private float numCPUinSystem;
	
	private BigDecimal cyclesPerSecond = null;
	
	private GregorianCalendar poolTime = null;			// last read time for global pool
	private GregorianCalendar procPoolTime[] = new GregorianCalendar[MAXPROCPOOLS];			// last read time for single proc pool
	private GregorianCalendar memPoolTime = null;			// last read time for global pool
	
	private Parser parser[] = null;
	
	private String	line;								// current line read
	private int		resource_type;
	private static final byte	POOL = 0;
	private static final byte	LPAR = 1;
	private static final byte	CEC  = 2;				// data about CEC CPU cfg
	private static final byte	PROCPOOL = 3;			// processor pool data
	private static final byte	MEMPOOL = 4;			// shared memory pool data
	private static final byte	INVALID = 5;
	private String	currLpar;
	private byte	currProcPool;
	private String	currProcPoolName;
	private BigDecimal	currData[] = new BigDecimal[max(NUM_LPARDATA,NUM_POOLDATA,NUM_MEMPOOLDATA)];
	private GregorianCalendar currTime=null;
	
	private byte currLparType;
	private byte lparType[] = new byte[MAXLPAR];
	private static final byte SHARED			= 0;
	private static final byte DEDICATED 		= 1;
	private static final byte SHARED_DED		= 2;
	private static final byte UNKNOWN			= 3;
	
	private String currLparProcPoolName;
	
	private static final BigDecimal zero = new BigDecimal(0);
	
	
	// Special constructor to be used ONLY internally!
	public Parser_Lslparutil (String name, PerfData data, GregorianCalendar start, GregorianCalendar end) {
		fileName = name;
		perfData = data;
		this.start=start;
		this.end=end;
		valid=true;
		
	}
	
	public Parser_Lslparutil(ParserManager v) {
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
			System.out.println(fileName + ": Warning, incomplete parsing of lslparutil data (line "+current_file_line_read+")");
			if (lastParse)
				endOfData();
		}
	}
	
	
	private void int_parseData(boolean firstParse, boolean lastParse)  throws Exception {
		BufferedReader	br;			// make buffered reads
		int				slot;
		int				i,j;
		int				lparId;
		float			f,f1;
		BigDecimal		bi1,bi2,bi3, bi4, bi5, bi6;
		
		
		// Reset performance data if this is first file
		if (firstParse) {
			lines_read=0;
			perfData.setLimits(start, end);
			for (i=0; i<MAXLPAR; i++) {
				if (lparPerfData[i]!=null)
					lparPerfData[i].setLimits(start, end);	// keep existing PerfData, but reset it!
			}
		}
		
		current_file_line_read = 0;
		
		br = getReader();
	
		
		// Cleanup
		//numLpar=0;
		for (i=0; i<MAXLPAR; i++) {
			lparTime[i]=null;
			lparType[i]=UNKNOWN;
			for (j=0; j<NUM_LPARDATA; j++)
				lparData[i][j]=null;
		}
		for (i=0; i<NUM_POOLDATA; i++) {
			poolData[i]=null;
			for (j=0; j<MAXPROCPOOLS; j++)
				procPoolData[j][i] = null;
		}
		poolTime=null;
		for (i=0; i<MAXPROCPOOLS; i++)
			procPoolTime[i]=null;

		
		
		// Cycle on  data
		while (true) {
			line = readLineAndShowProgress(br);
				
			if (line == null)
				break;	
				
			parseLine();
			
			if (resource_type==INVALID) 
				continue;

			if (resource_type==CEC) {
				slot = getSlot(currTime);			// slot for current time
				perfData.add(slot, PerfData.TOT_CPU, numCPUinSystem );				
				continue;
			}			
			
			
			if (resource_type==POOL) {
				
				// Skip computing pool if this is the first line
				if (poolData[UTILIZED_POOL_CYCLES]!=null && cyclesPerSecond!=null) {
					slot = getSlot(poolTime);
					
					bi1 = poolData[TOTAL_POOL_CYCLES].subtract(currData[TOTAL_POOL_CYCLES]);
					
					//bi2 = bi1.divide(cyclesPerSecond, new MathContext(5)).divide(new BigDecimal((poolTime.getTimeInMillis()-currTime.getTimeInMillis())/1000), new MathContext(5));
					bi4 = poolData[POOL_TIME_CYCLES].subtract(currData[POOL_TIME_CYCLES]);
					bi2 = bi1.divide(bi4, new MathContext(5));
					perfData.add(slot, PerfData.POOL, bi2.floatValue() );

					bi3 = poolData[UTILIZED_POOL_CYCLES].subtract(currData[UTILIZED_POOL_CYCLES]);
					bi4 = poolData[TOTAL_POOL_CYCLES].subtract(currData[TOTAL_POOL_CYCLES]);
					
					try {
						//  CURRENT ===>>  bi4 = bi2.subtract(bi3.divide(bi4, new MathContext(5)).multiply(bi2));
						
						bi1 = poolData[UTILIZED_POOL_CYCLES].subtract(currData[UTILIZED_POOL_CYCLES]);
						bi2 = poolData[POOL_TIME_CYCLES].subtract(currData[POOL_TIME_CYCLES]);
						bi3 = poolData[TOTAL_POOL_CYCLES].subtract(currData[TOTAL_POOL_CYCLES]);
						bi4 = bi3.divide(bi2, new MathContext(5)); // pool size
						bi5 = bi1.divide(bi2, new MathContext(5)); // used
						bi6 = bi4.subtract(bi5);
						//System.out.println(bi5.floatValue());
						perfData.add(slot, PerfData.FREEPOOL, bi6.floatValue());
						
						//System.out.println(currData[UTILIZED_POOL_CYCLES].floatValue());
					} catch (ArithmeticException e) {
						
					}
				}
				
				// Copy current data
				poolTime=currTime;
				for (i=0; i<NUM_POOLDATA; i++)
					poolData[i]=currData[i];
				
				continue;
			}
			
			if (resource_type==MEMPOOL) {
				
				// Skip computing pool if this is the first line
				if (memPoolData[PAGE_FAULTS]!=null) {
					slot = getSlot(memPoolTime);
					
					bi1 = memPoolData[PAGE_FAULTS].subtract(currData[PAGE_FAULTS]);
					perfData.add(slot, PerfData.HYPPAG_IN, bi1.floatValue());
					
					bi1 = memPoolData[PAGE_IN_DELAY].subtract(currData[PAGE_IN_DELAY]).divide(new BigDecimal(1000), new MathContext(2));
					perfData.add(slot, PerfData.HYPPAG_TIME, bi1.floatValue());
					
					bi1 = memPoolData[POOL_MEM];
					perfData.add(slot, PerfData.LOGICAL_MEM, bi1.floatValue());
					
					bi1 = memPoolData[LPAR_RUM_MEM].add(memPoolData[SYS_FIRMWARE_POOL_MEM]);
					perfData.add(slot, PerfData.PHYS_MEM, bi1.floatValue());
				}
				
				// Copy current data
				memPoolTime=currTime;
				for (i=0; i<NUM_POOLDATA; i++)
					memPoolData[i]=currData[i];
				
				continue;
			}
			
			if (resource_type==PROCPOOL) {
				
				// Skip computing pool if this is the first line
				if (procPoolData[currProcPool][UTILIZED_POOL_CYCLES]!=null && cyclesPerSecond!=null) {
					slot = getSlot(procPoolTime[currProcPool]);
					
					bi1 = procPoolData[currProcPool][TOTAL_POOL_CYCLES].subtract(currData[TOTAL_POOL_CYCLES]);
					
					//bi2 = bi1.divide(cyclesPerSecond, new MathContext(5)).divide(new BigDecimal((procPoolTime[currProcPool].getTimeInMillis()-currTime.getTimeInMillis())/1000), new MathContext(5));
					bi4 = procPoolData[currProcPool][POOL_TIME_CYCLES].subtract(currData[POOL_TIME_CYCLES]);
					bi2 = bi1.divide(bi4, new MathContext(5));
					perfData.add(slot, PerfData.PROCPOOL, PerfData.POOLSIZE, procPoolName[currProcPool], bi2.floatValue() );

					bi3 = procPoolData[currProcPool][UTILIZED_POOL_CYCLES].subtract(currData[UTILIZED_POOL_CYCLES]);
					
					// If no pool cycles are used, do not compare it with pool size (it may be zero!!)
					if (bi3.compareTo(new BigDecimal(0))==0)
						bi4 = bi3;
					else
						bi4 = bi3.divide(bi1, new MathContext(5)).multiply(bi2, new MathContext(5));
					perfData.add(slot, PerfData.PROCPOOL, PerfData.POOLUSED, procPoolName[currProcPool], bi4.floatValue() );
 
				}
			
				
				// Copy current data
				procPoolTime[currProcPool] = currTime;
				procPoolName[currProcPool] = currProcPoolName;
				for (i=0; i<NUM_POOLDATA; i++)
					procPoolData[currProcPool][i]=currData[i];
				
				continue;
			}
			
			if (resource_type==LPAR) {
				lparId = getLparData(currLpar);
				
				slot = getSlot(currTime);			// slot for current time
				
				lparPerfData[lparId].add(slot, PerfData.VP, currData[PROCS].floatValue() );
				
				switch (currLparType) {
					case DEDICATED:		lparPerfData[lparId].add(slot, PerfData.DED, 1 );
										break;
										
					case SHARED:		lparPerfData[lparId].add(slot, PerfData.ENT, currData[PROC_UNITS].floatValue() );
										break;
										
					case SHARED_DED:	lparPerfData[lparId].add(slot, PerfData.SHARED_DED, 1 );
										break;
					
					default:			System.out.println("Internal error in Parser_lslparutil: line "+current_file_line_read);
				}
				/*
				if (currData[PROC_UNITS]!=null) {
					lparPerfData[lparId].add(slot, PerfData.ENT, currData[PROC_UNITS].floatValue() );
				} else {
					lparPerfData[lparId].add(slot, PerfData.DED, 1 );
				}
				*/

				// Skip computing PC if this is the first line or type has changed in previous sample
				// Skip if previous values were lower than current
				// Skip if current value is zero
				//if (lparData[lparId][CAPPED_CYCLES]!=null) {
				if (lparType[lparId]!=UNKNOWN &&
						lparType[lparId]==currLparType && 
						lparData[lparId][ENTITLED_CYCLES].compareTo(currData[ENTITLED_CYCLES])>=0 ) {
					slot = getSlot(lparTime[lparId]);
					bi1 = lparData[lparId][CAPPED_CYCLES].subtract(currData[CAPPED_CYCLES]);
					bi2 = lparData[lparId][UNCAPPED_CYCLES].subtract(currData[UNCAPPED_CYCLES]);
					bi3 = lparData[lparId][ENTITLED_CYCLES].subtract(currData[ENTITLED_CYCLES]);
					if (lparData[lparId][SHARED_CYCLES]!=null && currData[SHARED_CYCLES]!=null)
						bi4 = lparData[lparId][SHARED_CYCLES].subtract(currData[SHARED_CYCLES]); 
					else
						bi4 = null;
					
					try {
						// If powered off, set PC to zero!
						if (currData[ENTITLED_CYCLES].compareTo(zero)==0)
							bi1=zero;
						else {		
							if (bi4==null)
								// NO shared-dedicated support
								bi1 = bi1.add(bi2).divide(bi3, new MathContext(5));
							else
								// SHARED-DED support (bi4 may be 0)
								bi1 = bi1.add(bi2).add(bi4).divide(bi3, new MathContext(5));
						}

						lparPerfData[lparId].add(slot, PerfData.EC, bi1.floatValue()*100);

						
						switch (currLparType) {
							case DEDICATED:		lparPerfData[lparId].add(slot, PerfData.PC, bi1.multiply(lparData[lparId][PROCS]).floatValue());
												break;
												
							case SHARED:		lparPerfData[lparId].add(slot, PerfData.PC, bi1.multiply(lparData[lparId][PROC_UNITS]).floatValue());
												break;
												
							case SHARED_DED:	lparPerfData[lparId].add(slot, PerfData.PC, bi1.multiply(lparData[lparId][PROCS]).floatValue());
												break;
							
							default:			System.out.println("Internal error in Parser_lslparutil: line "+current_file_line_read);
												slot = slot / 0; // Just invalidate line!
						}
						
						if (currLparProcPoolName !=null) {
							lparPerfData[lparId].add(slot, PerfData.PROCPOOL, PerfData.ACTIVEPOOL, currLparProcPoolName, 1);
						}
						
					} catch (ArithmeticException e) {
						// OK if LPAR is shutted down! (division by zero)
					}
				}
				
				// handle shared memory if any
				if (lparData[lparId][LOGICAL_MEM]!=null && 
						lparData[lparId][PHYS_MEM]!=null && 
						lparData[lparId][LOAN_MEM]!=null) {
					lparPerfData[lparId].add(slot, PerfData.LOGICAL_MEM, lparData[lparId][LOGICAL_MEM].divide(new BigDecimal(1024),new MathContext(5)).floatValue());
					lparPerfData[lparId].add(slot, PerfData.PHYS_MEM, 	 lparData[lparId][PHYS_MEM].divide(new BigDecimal(1024),new MathContext(5)).floatValue());
					lparPerfData[lparId].add(slot, PerfData.LOAN_MEM,    lparData[lparId][LOAN_MEM].divide(new BigDecimal(1024),new MathContext(5)).floatValue());
				}
				
				// handle CPI
				if (lparData[lparId][RUN_LATCH_CYCLES]!=null && lparData[lparId][RUN_LATCH_INSTRUCTIONS]!=null) {
					bi1 = lparData[lparId][RUN_LATCH_CYCLES].subtract(currData[RUN_LATCH_CYCLES]);
					bi2 = lparData[lparId][RUN_LATCH_INSTRUCTIONS].subtract(currData[RUN_LATCH_INSTRUCTIONS]);
					if (bi2.compareTo(zero)!=0) {
						bi3 = bi1.divide(bi2, new MathContext(5));
						lparPerfData[lparId].add(slot, PerfData.CPI, bi3.floatValue());
					}
				}
				
				// Copy current data
				lparTime[lparId]=currTime;
				lparType[lparId]=currLparType;
				for (i=0; i<NUM_LPARDATA; i++)
					lparData[lparId][i]=currData[i];
				
				
				
				continue;				
			}
			
			System.out.println("Skipped line: "+line);			
		}
		
		
		// If there are more file wait for the last
		if (!lastParse)
			return;
		
		
		// Finished reading data related to LPARs
		for (i=0; i<numLpar; i++)
			lparPerfData[i].endOfData();
		
		
		/*
		 * Create new Parsers, one for each LPAR
		 */		
		
		parser = new Parser_Lslparutil[numLpar];
		
		for (j=0; j<numLpar; j++) {
			parser[j] = new Parser_Lslparutil(lparName[j],lparPerfData[dataId[j]],start,end);
		}

		
		
		/*
		 * Create local perfData values from single PerfData related to LPARS
		 */

		
		int 		shared, dedicated, shared_ded;
		float		vp,ent,pc;
		float		min_vp,min_ent,min_pc;
		float		max_vp,max_ent,max_pc;
		float		ent_used;
		float		min_ent_used, max_ent_used;
		DataSet		ds=null;
		DataSet		ds1=null;
		
		for (i=0; i<DataSet.SLOTS; i++) {	

			shared=-1;
			dedicated=-1;
			shared_ded=-1;
			vp=ent=pc=-1;
			ent_used=-1;
			min_vp=min_ent=min_pc=-1;
			max_vp=max_ent=max_pc=-1;
			min_ent_used=max_ent_used=-1;
						
			for (j=0; j<numLpar; j++) {
				
				ds = parser[j].getPerfData().getData(PerfData.SYSTEM, 0, PerfData.DED);
				if (ds != null) {
					f = ds.getValue(i);
					if (f>0) {
						// It is a dedicated LPAR, at least in this slot
						if (dedicated>=0)
							dedicated++;
						else
							dedicated=1;
					}
					
				}
				
				ds = parser[j].getPerfData().getData(PerfData.SYSTEM, 0, PerfData.SHARED_DED);
				if (ds != null) {
					f = ds.getValue(i);
					if (f>0) {
						// It is a shared-dedicated LPAR, at least in this slot
						if (shared_ded>=0)
							shared_ded++;
						else
							shared_ded=1;
					}
					
				}				
				
				// May be an SPLPAR, at least in this slot				
				ds = parser[j].getPerfData().getData(PerfData.SYSTEM, 0, PerfData.ENT);
				if (ds != null ) {
					f = ds.getValue(i);
					if (f>0) {
						if (ent>=0)
							ent+=f;
						else
							ent=f;
						if (shared>=0)
							shared++;
						else
							shared=1;
					}
					f = ds.getAbsMin(i);
					if (f>0) {
						if (min_ent>=0)
							min_ent+=f;
						else
							min_ent=f;
					}
					f = ds.getAbsMax(i);
					if (f>0) {
						if (max_ent>=0)
							max_ent+=f;
						else
							max_ent=f;
					}
				}
				
				ds = parser[j].getPerfData().getData(PerfData.SYSTEM, 0, PerfData.VP);
				if (ds != null ) {
					f = ds.getValue(i);
					if (f>0) {
						if (vp>=0)
							vp+=f;
						else
							vp=f;
					}
					f = ds.getAbsMin(i);
					if (f>0) {
						if (min_vp>=0)
							min_vp+=f;
						else
							min_vp=f;
					}
					f = ds.getAbsMax(i);
					if (f>0) {
						if (max_vp>=0)
							max_vp+=f;
						else
							max_vp=f;
					}
				}

				ds = parser[j].getPerfData().getData(PerfData.SYSTEM, 0, PerfData.PC);
				if (ds != null ) {
					f = ds.getValue(i);					
					if (f>0) {
						if (pc>=0)
							pc+=f;
						else
							pc=f;
					}
					f = ds.getAbsMin(i);					
					if (f>0) {
						if (min_pc>=0)
							min_pc+=f;
						else
							min_pc=f;
					}
					f = ds.getAbsMax(i);					
					if (f>0) {
						if (max_pc>=0)
							max_pc+=f;
						else
							max_pc=f;
					}
					
					ds1 = parser[j].getPerfData().getData(PerfData.SYSTEM, 0, PerfData.ENT);
					if (ds1 != null ) {
						f1 = ds1.getValue(i);
						if (f1>0) {
							
							f = ds.getValue(i);	
							if (f>f1)
								f=f1;		// only get pc within ent limit !
							if (ent_used>=0)
								ent_used+=f;
							else
								ent_used=f;
							
							f = ds.getAbsMin(i);	
							if (f>f1)
								f=f1;		// only get pc within ent limit !
							if (min_ent_used>=0)
								min_ent_used+=f;
							else
								min_ent_used=f;
							
							f = ds.getAbsMax(i);	
							if (f>f1)
								f=f1;		// only get pc within ent limit !
							if (max_ent_used>=0)
								max_ent_used+=f;
							else
								max_ent_used=f;
						}
					}
				}
				
			}
			
			
			perfData.addBySlot(i, PerfData.VP,   		vp, min_vp, max_vp);
			perfData.addBySlot(i, PerfData.ENT,   	ent, min_ent, max_ent );
			perfData.addBySlot(i, PerfData.PC,   		pc, min_pc, max_pc );
			perfData.addBySlot(i, PerfData.ENT_USED,  ent_used, min_ent_used, max_ent_used );
			
			
			if (shared>=0 || dedicated>=0 || shared_ded>=0) {
				if (shared>=0)		
					perfData.add(i, PerfData.SHARED,   	shared );
				else
					perfData.add(i, PerfData.SHARED,   	0 );
				if (dedicated>=0)
					perfData.add(i, PerfData.DED,   	dedicated );
				else
					perfData.add(i, PerfData.DED,   	0 );
				if (shared_ded>=0)		
					perfData.add(i, PerfData.SHARED_DED,   	shared_ded );
				else
					perfData.add(i, PerfData.SHARED_DED,   	0 );
			}
			
		}	
		
		
		// Change Processor pool names from id to oldest human name
		int num=0;
		for (i=0; i<MAXPROCPOOLS; i++)
			if (procPoolName[i]!=null)
				num++;
		String names[] = new String[num];
		String alias[] = new String[num];
		for (i=0; i<MAXPROCPOOLS; i++)
			if (procPoolName[i]!=null) {
				names[i]=String.valueOf(i);
				alias[i]=procPoolName[i];
			}
		
		perfData.endOfData();	
	}
	
	
	private void parseLine() {
		int a,b,i;
		String mode;
		
		if (!line.startsWith("time=")) {
			resource_type=INVALID;
			return;
		}
		
		if (!line.contains("event_type=sample")) {
			resource_type=INVALID;
			return;
		}
		
		
		// 0         1         2         3
		// 0123456789012345678901234567890
		// time=05/16/2007 14:00:04,event_type=...
		
		// Check if enough data is present
		if (line.length() < 24){
			resource_type=INVALID;
			return;
		}
		
		// year, month, day, h, m , s
		currTime = new GregorianCalendar(
				Integer.parseInt(line.substring(11,15)),
				Calendar.JANUARY+Integer.parseInt(line.substring(5,7))-1,
				Integer.parseInt(line.substring(8,10)),
				Integer.parseInt(line.substring(16,18)),
				Integer.parseInt(line.substring(19,21)),
				Integer.parseInt(line.substring(22,24)));
		
		for (i=0; i<currData.length; i++)
			currData[i]=null;
		
		if (line.contains("configurable_sys_proc_units")) {
			// CEC data
			
			//                0123456789012345678901234567890
			a = line.indexOf("configurable_sys_proc_units=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				numCPUinSystem=Float.parseFloat(line.substring(a+28, b));
			else
				numCPUinSystem=Float.parseFloat(line.substring(a+28));
			
			//          	  0123456789012345678901234567890
			a = line.indexOf("proc_cycles_per_second=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				cyclesPerSecond=new BigDecimal(line.substring(a+23, b));
			else
				cyclesPerSecond=new BigDecimal(line.substring(a+23));
			
			resource_type=CEC;
			return;
			
		}
			
		if (line.contains("resource_type=pool")) {
			// POOL data
			//                0123456789012345678
			a = line.indexOf("total_pool_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[TOTAL_POOL_CYCLES]=new BigDecimal(line.substring(a+18, b));
			else
				currData[TOTAL_POOL_CYCLES]=new BigDecimal(line.substring(a+18));
			
			//                0123456789012345678901
			a = line.indexOf("utilized_pool_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[UTILIZED_POOL_CYCLES]=new BigDecimal(line.substring(a+21, b));
			else
				currData[UTILIZED_POOL_CYCLES]=new BigDecimal(line.substring(a+21));	
			
			//                012345678901234567890123456789			
			a = line.indexOf("configurable_pool_proc_units=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[POOL_PROC_UNITS]=new BigDecimal(line.substring(a+29, b));
			else
				currData[POOL_PROC_UNITS]=new BigDecimal(line.substring(a+29));		
			
			//                01234567890123456789012345
			a = line.indexOf("borrowed_pool_proc_units=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[BORROWED_POOL_PROC_UNITS]=new BigDecimal(line.substring(a+25, b));
			else
				currData[BORROWED_POOL_PROC_UNITS]=new BigDecimal(line.substring(a+25));		
			
			//          	  0123456789012345678901234567
			a = line.indexOf("curr_avail_pool_proc_units=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[AVAILABLE_POOL_PROC_UNITS]=new BigDecimal(line.substring(a+27, b));
			else
				currData[AVAILABLE_POOL_PROC_UNITS]=new BigDecimal(line.substring(a+27));	
			
			//    	  		  0123456789012
			a = line.indexOf("time_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[POOL_TIME_CYCLES]=new BigDecimal(line.substring(a+12, b));
			else
				currData[POOL_TIME_CYCLES]=new BigDecimal(line.substring(a+12));	
			
			resource_type=POOL;
			return;
		}
		
		if (line.contains("resource_type=lpar")) {
			// LPAR data
			
			// Start supposing it is a DEDICATED LPAR
			currLparType = DEDICATED;
			
			
			//                012345678901234
			a = line.indexOf("capped_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[CAPPED_CYCLES]=new BigDecimal(line.substring(a+14, b));
			else
				currData[CAPPED_CYCLES]=new BigDecimal(line.substring(a+14));
			
			//                01234567890123456
			a = line.indexOf("uncapped_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[UNCAPPED_CYCLES]=new BigDecimal(line.substring(a+16, b));
			else
				currData[UNCAPPED_CYCLES]=new BigDecimal(line.substring(a+16));
			
			//                01234567890123456
			a = line.indexOf("entitled_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[ENTITLED_CYCLES]=new BigDecimal(line.substring(a+16, b));
			else
				currData[ENTITLED_CYCLES]=new BigDecimal(line.substring(a+16));
			
			// If dedicated CPU, no curr_proc_units
			//                01234567890123456
			a = line.indexOf("curr_proc_units=");
			if (a>0) {			
				b = line.indexOf(',',a);
				if (b>0)
					currData[PROC_UNITS]=new BigDecimal(line.substring(a+16, b));
				else
					currData[PROC_UNITS]=new BigDecimal(line.substring(a+16));
				
				// It is a shared LPAR
				currLparType = SHARED;
				
			} else
				currData[PROC_UNITS]=null;
			
			//                012345678901
			a = line.indexOf("curr_procs=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[PROCS]=new BigDecimal(line.substring(a+11, b));
			else
				currData[PROCS]=new BigDecimal(line.substring(a+11));
			
			//                01234567890
			a = line.indexOf("lpar_name=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currLpar = line.substring(a+10, b);
			else
				currLpar = line.substring(a+10);
			
			//							1		  2 
			//                0123456789012345678901234567890
			a = line.indexOf("shared_cycles_while_active=");
			if (a<0) {
				// pre-POWER6
				currData[SHARED_CYCLES]=null;
			} else {
				b = line.indexOf(',',a);
				if (b>0)
					currData[SHARED_CYCLES]=new BigDecimal(line.substring(a+27, b));
				else
					currData[SHARED_CYCLES]=new BigDecimal(line.substring(a+27));
			}
			
			//				            1		  2
			//                01234567890123456789012345
			a = line.indexOf("curr_sharing_mode=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				mode = line.substring(a+18, b);
			else
				mode = line.substring(a+18);
			if (mode.equals("share_idle_procs_always"))
				currLparType = SHARED_DED;
			
			
			//	  		      0123456789012
			a = line.indexOf("time_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[LPAR_TIME_CYCLES]=new BigDecimal(line.substring(a+12, b));
			else
				currData[LPAR_TIME_CYCLES]=new BigDecimal(line.substring(a+12));
			
			
			//                          1		  2
			//                0123456789012345678901234567
			a = line.indexOf("curr_shared_proc_pool_name=");
			if (a<0) {
				// older HMC may hot have this field
				currLparProcPoolName = null;
			} else {
				b = line.indexOf(',',a);
				if (b>0)
					currLparProcPoolName = line.substring(a+27, b);
				else	
					currLparProcPoolName = line.substring(a+27);
			}
			
			//                0123456789
			a = line.indexOf("mem_mode=");
			if (a<0) {
				// older HMC may hot have this field
				// Do nothing
			} else {
				String s;
				b = line.indexOf(',',a);
				if (b>0)
					s = line.substring(a+9, b);
				else	
					s = line.substring(a+9);
				if (s.equals("shared")) {
					
					// This is a shared memory partition!
					
					//	  		      01234567890123
					a = line.indexOf("phys_run_mem=");
					if (a<0) {
						resource_type=INVALID;
						return;
					}
					b = line.indexOf(',',a);
					if (b>0)
						currData[PHYS_MEM]=new BigDecimal(line.substring(a+13, b));
					else
						currData[PHYS_MEM]=new BigDecimal(line.substring(a+13));
					
					//	  		      0123456789012345678901234
					a = line.indexOf("mem_overage_cooperation=");
					if (a<0) {
						resource_type=INVALID;
						return;
					}
					b = line.indexOf(',',a);
					if (b>0)
						s=line.substring(a+24, b);
					else
						s=line.substring(a+24);
					if (s.startsWith("-"))	// values are negative or zero
						currData[LOAN_MEM]=new BigDecimal(s.substring(1));
					else
						currData[LOAN_MEM]=new BigDecimal(s);

					
					//			      0123456789
					a = line.indexOf("curr_mem=");
					if (a<0) {
						resource_type=INVALID;
						return;
					}
					b = line.indexOf(',',a);
					if (b>0)
						currData[LOGICAL_MEM]=new BigDecimal(line.substring(a+9, b));
					else
						currData[LOGICAL_MEM]=new BigDecimal(line.substring(a+9));
				}
			}
			
			
			//          	  012345678901234567
			a = line.indexOf("run_latch_cycles=");		// Up to POWER7+
			if (a>0) {			
				b = line.indexOf(',',a);
				if (b>0)
					currData[RUN_LATCH_CYCLES]=new BigDecimal(line.substring(a+17, b));
				else
					currData[RUN_LATCH_CYCLES]=new BigDecimal(line.substring(a+17));
			} else
				currData[RUN_LATCH_CYCLES]=null;
			
			//    	  		  012345678901234567890123
			a = line.indexOf("run_latch_instructions=");   // Up to POWER7+
			if (a>0) {			
				b = line.indexOf(',',a);
				if (b>0)
					currData[RUN_LATCH_INSTRUCTIONS]=new BigDecimal(line.substring(a+23, b));
				else
					currData[RUN_LATCH_INSTRUCTIONS]=new BigDecimal(line.substring(a+23));
			} else
				currData[RUN_LATCH_INSTRUCTIONS]=null;
			
			
			//    	  		  01234567890123456789
			a = line.indexOf("total_instructions=");    // POWER8
			if (a>0) {			
				b = line.indexOf(',',a);
				if (b>0)
					currData[RUN_LATCH_CYCLES]=new BigDecimal(line.substring(a+19, b));
				else
					currData[RUN_LATCH_CYCLES]=new BigDecimal(line.substring(a+19));
			} else
				currData[RUN_LATCH_CYCLES]=null;
			
			//    	  		  01234567890123456789012345678901234
			a = line.indexOf("total_instructions_execution_time=");   // POWER8
			if (a>0) {			
				b = line.indexOf(',',a);
				if (b>0)
					currData[RUN_LATCH_INSTRUCTIONS]=new BigDecimal(line.substring(a+34, b));
				else
					currData[RUN_LATCH_INSTRUCTIONS]=new BigDecimal(line.substring(a+34));
			} else
				currData[RUN_LATCH_INSTRUCTIONS]=null;
			
			
			
			
			resource_type=LPAR;
			return;	
		}
		
		if (line.contains("resource_type=procpool")) {
			// PROCPOOL data
			
			//                012345678901234567890			
			a = line.indexOf("shared_proc_pool_id=");   
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currProcPool=Byte.parseByte(line.substring(a+20, b));
			else
				currProcPool=Byte.parseByte(line.substring(a+20));	

			
			//                01234567890123456789012			
			a = line.indexOf("shared_proc_pool_name=");   
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currProcPoolName=line.substring(a+22, b);
			else
				currProcPoolName=line.substring(a+22);

			
			//                0123456789012345678
			a = line.indexOf("total_pool_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[TOTAL_POOL_CYCLES]=new BigDecimal(line.substring(a+18, b)); 
			else
				currData[TOTAL_POOL_CYCLES]=new BigDecimal(line.substring(a+18));

			
			//                0123456789012345678901
			a = line.indexOf("utilized_pool_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[UTILIZED_POOL_CYCLES]=new BigDecimal(line.substring(a+21, b));
			else
				currData[UTILIZED_POOL_CYCLES]=new BigDecimal(line.substring(a+21));	
			
			//	  		      0123456789012
			a = line.indexOf("time_cycles=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[POOL_TIME_CYCLES]=new BigDecimal(line.substring(a+12, b));
			else
				currData[POOL_TIME_CYCLES]=new BigDecimal(line.substring(a+12));
			
			resource_type=PROCPOOL;
			return;
		}
		
		
		if (line.contains("resource_type=mempool")) {
			// MEMPOOL data
			
			//                0123456789012
			a = line.indexOf("page_faults=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[PAGE_FAULTS]=new BigDecimal(line.substring(a+12, b)); 
			else
				currData[PAGE_FAULTS]=new BigDecimal(line.substring(a+12));

			
			//                012345678901234
			a = line.indexOf("page_in_delay=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[PAGE_IN_DELAY]=new BigDecimal(line.substring(a+14, b));
			else
				currData[PAGE_IN_DELAY]=new BigDecimal(line.substring(a+14));	
			
			//	  		      012345678901234
			a = line.indexOf("curr_pool_mem=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[POOL_MEM]=new BigDecimal(line.substring(a+14, b));
			else
				currData[POOL_MEM]=new BigDecimal(line.substring(a+14));
			
			//		          01234567890123
			a = line.indexOf("lpar_run_mem=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[LPAR_RUM_MEM]=new BigDecimal(line.substring(a+13, b));
			else
				currData[LPAR_RUM_MEM]=new BigDecimal(line.substring(a+13));
			
			//	              01234567890123456789012
			a = line.indexOf("sys_firmware_pool_mem=");
			if (a<0) {
				resource_type=INVALID;
				return;
			}
			b = line.indexOf(',',a);
			if (b>0)
				currData[SYS_FIRMWARE_POOL_MEM]=new BigDecimal(line.substring(a+22, b));
			else
				currData[SYS_FIRMWARE_POOL_MEM]=new BigDecimal(line.substring(a+22));
			
			resource_type=MEMPOOL;
			return;
		}		
		
		resource_type=INVALID;
		return;
		
	}
	
	// Provide the id in lparData related to name. Create a new entry if needed.
	private int getLparData(String name) {		
		if (numLpar == 0) {
			addName(name,0);
			return dataId[0];
		}
		return getLparData(name, 0, numLpar);
	}
	
	private int getLparData(String name, int from, int to) {
		int comp, pos;

		if (from+1==to) {
			comp = name.compareTo(lparName[from]);
			if (comp==0) 
				return dataId[from];
			if (comp<0) {
				addName(name,from);
				return dataId[from];
			}
			addName(name,to);
			return dataId[to];
		}
		
		pos = (from+to)/2;
		comp = name.compareTo(lparName[pos]);
		
		if (comp==0) 
			return dataId[pos];
		
		if (comp<0)	
			return getLparData(name, from, pos);
		
		if (pos+1==to) {
			addName(name,to);
			return dataId[to];
		}
		
		return getLparData(name, pos+1, to);
	}
	
	private void addName(String name, int pos) {
		int i;
		for (i=numLpar; i>pos; i--) {
			lparName[i]=lparName[i-1];
			dataId[i]=dataId[i-1];
		}
		lparName[pos]=name;
		dataId[pos]=numLpar;
		lparPerfData[numLpar] = new PerfData();
		lparPerfData[numLpar].setLimits(start, end);
		numLpar++;
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
		String			line;		// single line of data
		GregorianCalendar gc=null;	// last day read
		final byte 	MAXWAIT = 20;		// Wait up to 20 lines before giving up.


		br = getReader();

		while (true) {
			line = br.readLine();

			if (line == null) {
				if (start!=null && end!=null)
					valid=true;
				break;
			}
			
			total_lines++;
			
			if (start==null && end==null && total_lines>MAXWAIT)
				return;
				
			// Look only for time stamps
			if (!line.startsWith("time="))
				continue;
			
			// 0         1         2         3
			// 012345678901234567890123456789012345
			// time=05/16/2007 14:00:04,event_type=...
			
			// Check if enough data is present
			if (line.length() < 35 ||
					!line.startsWith("time=") ||
					!line.contains(",event_type=") )
				continue;
			
			// year, month, day, h, m , s
			gc = new GregorianCalendar(
					Integer.parseInt(line.substring(11,15)),
					Calendar.JANUARY+Integer.parseInt(line.substring(5,7))-1,
					Integer.parseInt(line.substring(8,10)),
					Integer.parseInt(line.substring(16,18)),
					Integer.parseInt(line.substring(19,21)),
					Integer.parseInt(line.substring(22,24)));
				
			if (start == null || gc.before(start))
				start = gc;
			
			if (end == null || gc.after(end))
				end = gc;
		}
	}
	
	public Parser getParser(int num) {
		return parser[num]; 
	}
	
	
	public String[] getParserNames() {
		String result[] = new String[numLpar];
		for (int i=0; i<numLpar; i++)
			result[i]=lparName[i];
		return result;
	}
	
	private byte max(byte a, byte b, byte c) {
		if (a>b) {
			if (a>c)
				return a;
			return c;
		} else {
			if (b>c)
				return b;
			return c;
		}
		
	}

}
