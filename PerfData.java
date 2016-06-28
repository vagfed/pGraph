package pGraph;

import java.util.GregorianCalendar;
import java.util.Vector;


public class PerfData {
	
	/*
	 * Groups are "system", "cpu", "disks", "vpaths", etc.
	 * data[group] provide a matrix of [item][data sets]. The items are sorted and
	 * 	the names structure provides the keys. Look in names[group] for the item name: the
	 *  index in the names[group] is to be used in data[group].
	 */
	private static final int	ITEM_BLOCK	= 10;					// items allocated at the same time
	private DataSet		data[][][] = new DataSet[NUM_GROUPS][ITEM_BLOCK][];	// data[group][item][type]
	private String		names[][]= new String[NUM_GROUPS][ITEM_BLOCK];		// NOT sorted list  names[group]
	private int			valid_names[] = new int[NUM_GROUPS];
	private int			sorted_names[][] = new int[NUM_GROUPS][];
	
	private float		cpuWeight = 1;		// Weight to be applied to CPU usage
	
	private Vector<String>		lssrad_ref1 = null;
	private Vector<String>		lssrad_srad = null;
	private Vector<String>		lssrad_mem = null;
	private Vector<String>		lssrad_cpu = null;
	
	
	public static final byte			LPARSTATI	= 0;
	public static final byte			VMSTATV		= 1;
	public static final byte			VMSTATVEND	= 2;
	private static final byte			NUM_TEXT	= 3;
	
	@SuppressWarnings("unchecked")
	private Vector<String>		textLabel[] = new Vector[NUM_TEXT];
	@SuppressWarnings("unchecked")
	private Vector<String>		textValue[] = new Vector[NUM_TEXT];
	
	
	
	public PerfData() {
		int i;
		for (i=0; i<NUM_GROUPS; i++) {
			valid_names[i] = 0;
		}
	}
	
	
	public static final byte		SMT_UNKNOWN 		= 0;	// no idea about SMT
	public static final byte		SMT_SUPPOSED_ON		= 1;	// we suppose it is always on 
	public static final byte		SMT_SUPPOSED_OFF	= 2;	// we suppose it is always off
	public static final byte		SMT_PARSED			= 3;	// SMT status is recorded in file
	private byte 					smtStatus = SMT_UNKNOWN;
	
	private byte					smt_threads = 0;

	
	public byte getSmt_threads() {
		return smt_threads;
	}

	public void setSmt_threads(byte smt_threads) {
		this.smt_threads = smt_threads;
	}


	private GregorianCalendar	start=null;		// first slot date
	private GregorianCalendar	end=null;		// last slot date
	
	private static final String SYSTEM_ITEM = "S";	// idem for SYSTEM group
	

	public static final byte	SYSTEM			= 0;
	public static final byte	CPU				= 1;
	public static final byte	DISK			= 2;
	public static final byte	ESS				= 3;
	public static final byte	SCSI			= 4;	// SCSI adapter - DISK ONLY
	public static final byte	NETWORK			= 5;	// Network adapter
	public static final byte	WPAR			= 6;	// Workload PARtitions
	public static final byte	TOPPROC			= 7;	// Top processes
	public static final byte	TOPPROC_BY_NAME	= 8;	// Top processes
	public static final byte	FS				= 9;	// File system
	public static final byte	FCSTAT			= 10;	// FC adapter statistics
	public static final byte	DAC				= 11;	// dac statistics (same details as a disk)
	public static final byte	PROCPOOL		= 12;	// Processor Pool data
	public static final byte	SEA				= 13;	// Same structure of network adapters
	public static final byte	PCPU			= 14; 
	
	private static final byte	NUM_GROUPS	= 15;
	
	
	/*
	 * SYSTEM DATASET GROUP
	 */
	
	// Virtual CPU
	public static final byte	PC			= 0;	// Processor Consumed
	public static final byte	VP			= 1;	// Virtual Processors
	public static final byte	LP			= 2;	// Logical Processors
	public static final byte	POOL		= 3;	// Pool Size
	public static final byte	FREEPOOL	= 4;	// Free Pool
	public static final byte	ENT			= 5;	// Entitlement
	public static final byte	EC			= 6;	// Entitlement consumed
	
	// Memory
	public static final byte	AVM			= 7;	// Average virtual memory
	public static final byte	FRE			= 8;	// Free Memory MB
	public static final byte	PI			= 9;	// Paging IN 4KB
	public static final byte	PO			= 10;	// Paging OUT 4KB
	public static final byte	FI			= 11; 	// File page in / sec
	public static final byte	FO			= 12;	// File page out / sec
	public static final byte	FR			= 13;	// Pages freed
	public static final byte	CY			= 14;	// Clock cycles
	public static final byte	SR			= 15;	// pages searched
	public static final byte	RAM			= 16;	// Physical Memory MB
	public static final byte	NUMPERM		= 17;	// Permanent pages %
	public static final byte	MINPERM		= 18;	// Min perm pages %
	public static final byte	MAXPERM		= 19;	// Max perm pages %
	public static final byte	MINFREE		= 20;	// Minimum free pages
	public static final byte	MAXFREE		= 21;	// Maximum free pages
	public static final byte	NUMFREE		= 22;	// Number of free pages
	public static final byte	USEDMEM		= 23;	// MB really used: USEDMEM = RAM - FRE (used in CEC)
	public static final byte	NUMCLIENT	= 24;	// client pages
	public static final byte	MAXCLIENT	= 25;	// max allowed client pages
	
	// Kernel stats
	public static final byte	RUNQ		= 26;	// Run queue
	public static final byte	SWQ			= 27;	// Swap queue - Wait queue
	public static final byte	WQPS		= 28;	// #theads waiting on I/O per sec
	public static final byte	PSW			= 29;	// process switches
	public static final byte	SYSC		= 30;	// System calls
	public static final byte	FORK		= 31;	// Forks
	public static final byte	EXEC		= 32;	// Execs
	public static final byte	READ		= 33;	// Reads
	public static final byte	WRITE		= 34;	// Writes
	
	// Miscellanea
	public static final byte	TOT_CPU		= 35;	// Total number of CPUs in system or dedicated LPAR
	public static final byte	SRFR_RATIO	= 36;	// Computed: SR/FR
	public static final byte	DED			= 37;	// number of dedicated LPARs
	public static final byte	SHARED		= 38;	// number of uLPARs
	public static final byte	SHARED_DED	= 39;	// number of shared dedicated LPARs
	public static final byte	DED_PC		= 40;	// Computed: PC used in a dedicated LPAR/System
	public static final byte	GLOB_PC		= 41;	// Computed: PC + DED_PC
	
	// Asynchronous IO
	
	public static final byte	NUM_AIO		= 42;
	public static final byte	ACTIVE_AIO	= 43;
	public static final byte	CPU_AIO		= 44;
	
	// Logical Memory
	
	public static final byte	LOGICAL_MEM	= 45;	// GB
	public static final byte	PHYS_MEM	= 46;	// GB
	public static final byte	LOAN_MEM	= 47;	// GB
	public static final byte	HYPPAG_IN	= 48;
	public static final byte	HYPPAG_TIME	= 49;	// ms
	
	// Large Pages
	
	public static final byte	USED_LP		= 50;	// large pages currently in use
	public static final byte	FREE_LP		= 51;	// large pages in large page free list
	
	// Compressed memory
	
	public static final byte	TRUE_MEM	= 52;	// True memory assigned to LPAR MB
	public static final byte	COMP_POOL	= 53;	// Size of compressed pool MB
	public static final byte	UNC_POOL	= 54;	// Size of uncompressed pool MB
	public static final byte	EXP_MEM		= 55;	// Size of expanded memory MB
	public static final byte	CP_PI		= 56;	// Compressed pool page IN
	public static final byte	CP_PO		= 57;	// Compressed pool page OUT
	
	// Additional stats
	
	public static final byte	ENT_USED	= 58;	// Entitlement consumed: pc if pc<=ent, else ent
	public static final byte	PC_USED		= 59;	// Processor consumed & used = pc + (us%+sy%)
	public static final byte	FOLDED		= 60;	// Folded VPs
	public static final byte	VP_US		= 61;	// VP User%
	public static final byte	VP_SY		= 62;	// VP System%
	public static final byte	VP_WA		= 63;	// VP Wait%
	public static final byte	VP_ID		= 64;	// VP Idle%
	
	public static final byte	CPI			= 65;	// Cycles per instruction
	
	public static final byte	FAULTS		= 66;	// Page faults
	
	// Memory pages details
	
	public final static byte	NUMFRAMES4K		= 67;
	public final static byte	NUMFRAMES64K	= 68;
	public final static byte	NUMFRB4K		= 69;
	public final static byte	NUMFRB64K		= 70;
	public final static byte	PI4K			= 71;
	public final static byte	PO4K			= 72;
	public final static byte	PI64K			= 73;
	public final static byte	PO64K			= 74;
	public final static byte	NUMVPAGES4K		= 75;
	public final static byte	NUMVPAGES64K	= 76;
	public final static byte	FAULTS4K		= 77;
	public final static byte	FAULTS64K		= 78;
	
	
	private static final byte	NUM_SYSTEM	= 79;
	
	
	/*
	 * CPU DATASET GROUP
	 */
	
	// CPU usage
	public static final byte	US 			= 0;
	public static final byte	SY 			= 1;
	public static final byte	WA			= 2;
	public static final byte	ID			= 3;
	
	private static final byte 	NUM_CPU		= 4;
	
	
	
	/*
	 * DISK DATASET GROUP (used also by DAC)
	 */	
	// Basic statistics
	public final static byte 	DSK_BUSY  		= 0;	// %busy		: not if virtualTargetDisk
	public final static byte 	DSK_READKB 		= 1;	// KB/s read
	public final static byte 	DSK_WRITEKB		= 2;	// KB/s write
	public final static byte 	DSK_XFER  		= 3;	// transfers/sec
	public final static byte 	DSK_BSIZE 		= 4;	// block size
	
	// Extended statistics
	public final static byte 	DSK_RPS  		=  5;	// rps
	public final static byte 	DSK_AVG_R 		=  6;	// ms 
	public final static byte 	DSK_MIN_R 		=  7;	// ms	
	public final static byte 	DSK_MAX_R 		=  8;	// ms
	public final static byte 	DSK_TO_R 		=  9;	// read time-out per second : not if virtualTargetDisk
	public final static byte 	DSK_FAIL_R 		= 10;	// failed read per second : not if virtualTargetDisk	
	
	public final static byte 	DSK_WPS  		= 11;	// wps
	public final static byte 	DSK_AVG_W 		= 12;	// ms 
	public final static byte 	DSK_MIN_W 		= 13;	// ms	
	public final static byte 	DSK_MAX_W 		= 14;	// ms
	public final static byte 	DSK_TO_W 		= 15;	// write time-out per second : not if virtualTargetDisk
	public final static byte 	DSK_FAIL_W 		= 16;	// failed write per second : not if virtualTargetDisk
	
	public final static byte 	DSK_AVG_T 		= 17;	// ms
	public final static byte 	DSK_MIN_T 		= 18;	// ms	
	public final static byte 	DSK_MAX_T 		= 19;	// ms
	public final static byte 	DSK_AVG_WQ 		= 20;	// 
	public final static byte 	DSK_AVG_SQ 		= 21;	// 
	public final static byte 	DSK_FULLQ 		= 22;	// per second
	
	public final static byte	DSK_AVGSERV		= 23;
	public final static byte	DSK_AVGWAIT		= 24;
	
	public final static byte	DSK_AVG_RW		= 25;	// Computed: (DSK_RPS*DSK_AVG_R+DSK_WPS*DSK_AVG_W)/(DSK_RPS+DSK_WPS)
	public final static byte	DSK_RWPS		= 26;	// Computes: DSK_RPS+DSKWPS
	
	private static final byte 	NUM_DISK	= 27;
	
	
	/*
	 * ESS DATASET GROUP
	 */	
	public final static byte 	ESS_READKB 	= 0;	// KB/s read
	public final static byte 	ESS_WRITEKB	= 1;	// KB/s write
	public final static byte 	ESS_XFER  	= 2;	// transfers/sec
	public final static byte	ESS_BUSY	= 3;
	
	public final static byte	ESS_AVGSERV	= 4;
	public final static byte	ESS_AVGWAIT	= 5;
	
	private static final byte 	NUM_ESS		= 6;
	
	
	/*
	 * SCSI DATASET GROUP
	 */		
	// Basic statistics
	public final static byte 	SCSI_READKB 	=  0;	// KB/s read
	public final static byte 	SCSI_WRITEKB	=  1;	// KB/s write
	public final static byte 	SCSI_XFER  		=  2;	// transfers/sec
	
	// Extended statistics
	public final static byte 	SCSI_RPS  		=  3;	// rps
	public final static byte 	SCSI_AVG_R 		=  4;	// ms 
	public final static byte 	SCSI_MIN_R 		=  5;	// ms	
	public final static byte 	SCSI_MAX_R 		=  6;	// ms
	
	public final static byte 	SCSI_WPS  		=  7;	// wps
	public final static byte 	SCSI_AVG_W 		=  8;	// ms 
	public final static byte 	SCSI_MIN_W 		=  9;	// ms	
	public final static byte 	SCSI_MAX_W 		= 10;	// ms
	
	public final static byte 	SCSI_AVG_T 		= 11;	// ms
	public final static byte 	SCSI_MIN_T 		= 12;	// ms	
	public final static byte 	SCSI_MAX_T 		= 13;	// ms
	public final static byte 	SCSI_AVG_WQ 	= 14;	// 
	public final static byte 	SCSI_AVG_SQ 	= 15;	// 
	public final static byte 	SCSI_FULLQ 		= 16;	// per second
	
	private static final byte 	NUM_SCSI		= 17;
	
	
	/*
	 * NETWORK DATASET GROUP
	 */	
	public final static byte 	NET_READKB  	=  0;
	public final static byte 	NET_WRITEKB  	=  1;
	public final static byte 	NET_READS  		=  2;
	public final static byte 	NET_WRITES  	=  3;
	public final static byte 	NET_IERRORS  	=  4;
	public final static byte 	NET_OERRORS  	=  5;
	public final static byte 	NET_COLLISIONS  =  6;
	
	private static final byte 	NUM_NETWORK		=  7;
	
	
	/*
	 * WORKLOAD PARTITION
	 */
	public final static byte	WPAR_CPU		=  0;
	public final static byte	WPAR_MEM		=  1;
	public final static byte	WPAR_DISK		=  2;
	public final static byte	WPAR_PROC		=  3;
	
	private static final byte 	NUM_WPAR		=  4;
	
	
	/*
	 * TOP PROCESSES
	 */
	public final static byte	TOP_CPU			=  0;		// percentage of 1 CPU
	public final static byte	TOP_RESTEXT		=  1;		// KB
	public final static byte	TOP_RESDATA		=  2;		// KB
	
	private static final byte 	NUM_TOPPROC		=  3;
	
	
	/*
	 * TOP PROCESSES BY NAME
	 */
	public final static byte	TOP_CPU_BYNAME	=  0;		// percentage of 1 CPU
	public final static byte	TOP_RAM_BYNAME	=  1;		// percentage of RAM
	
	private static final byte 	NUM_TOPPROC_BYNAME =  2;	
	
	
	/*
	 * FILE SYSTEMS
	 */
	public final static byte	SPACEUSED		=  0;
	public final static byte	INODEUSED		=  1;
	
	private static final byte 	NUM_FS			=  2;
	
	
	/*
	 * FC STATS
	 */
	public final static byte	FCREAD			=  0;
	public final static byte	FCWRITE			=  1;
	public final static byte	FCXFERIN		=  2;
	public final static byte	FCXFEROUT		=  3;
	
	private static final byte 	NUM_FCSTAT			=  4;
	
	
	/*
	 * PROC POOL
	 */
	public final static byte	POOLSIZE		=  0;
	public final static byte	POOLUSED		=  1;
	public final static byte	ACTIVEPOOL		=  2;		// 1 when LPAR is in this pool
	
	private static final byte 	NUM_PROCPOOL			=  3;
	
	
	// PCPU usage
	public static final byte	P_TOT			= 0;
	
	private static final byte 	NUM_PCPU		= 1;
	
	

	
	
	
	public int getNumTopProc() {
		int num=0;
		
		for (int i=0; i<data[TOPPROC].length; i++)
			if (data[TOPPROC][i]!=null)
				num++;
		
		return num;
		//return data[TOPPROC].length;
	}
	
	public void invalidateTop() {
		data[TOPPROC] = new DataSet[ITEM_BLOCK][];
		names[TOPPROC] = new String[ITEM_BLOCK];
		valid_names[TOPPROC]=0;
	}
	
	
	
	private byte getGroupTypes(byte group) {
		switch (group) {
			case SYSTEM:			return NUM_SYSTEM;
			case CPU:				return NUM_CPU;
			case DISK:				return NUM_DISK;
			case ESS:				return NUM_ESS;
			case SCSI:				return NUM_SCSI;
			case NETWORK:			return NUM_NETWORK;
			case WPAR:				return NUM_WPAR;
			case TOPPROC:			return NUM_TOPPROC;
			case TOPPROC_BY_NAME:	return NUM_TOPPROC_BYNAME;
			case FS:				return NUM_FS;
			case FCSTAT:			return NUM_FCSTAT;
			case DAC:				return NUM_DISK;
			case PROCPOOL:			return NUM_PROCPOOL;
			case SEA:				return NUM_NETWORK;
			case PCPU:				return NUM_PCPU;
			
			default:		System.out.println("FATAL ERROR: getGroupTypes("+group+")");
							System.exit(1);
							return 0;						
		}
		
	}
	
	
	
	private String getSetName(byte group, byte type) {
		switch (group) {
			case SYSTEM:
				switch (type) {
					case PC:		return "Proc consumed";
					case VP:		return "Virt Proc";
					case LP:		return "Logical Proc";
					case POOL:		return "Proc Pool";
					case FREEPOOL:	return "Free Pool";
					case ENT:		return "Entitlement";
					case EC:		return "Ent% Consumed";
					
					case AVM:		return "Avg Virt Mem";
					case FRE:		return "Free RAM MB";
					case PI:		return "Paging IN 4KB";
					case PO:		return "Paging OUT 4KB";
					case FI:		return "File page-in / sec";
					case FO:		return "File page-out / sec";
					case FR:		return "Pages Freed";
					case CY:		return "Clock Cycles";
					case SR:		return "Pages Scanned";
					case RAM:		return "Total RAM MB";
					case NUMPERM:	return "Num% Perm Pages";
					case MINPERM:	return "Min% Perm Pages";
					case MAXPERM:	return "Max% Perm Pages";
					case NUMFREE:	return "Num Free Pages";
					case USEDMEM:	return "Used RAM MB";
					case MINFREE:	return "Min Free Pages";
					case MAXFREE:	return "Max Free Pages";
					case NUMCLIENT:	return "Num% Client Pages";
					case MAXCLIENT:	return "Max% Client Pages";
					case RUNQ:		return "RunQ";
					case SWQ:		return "WaitQ";
					case WQPS:		return "#threads waiting I/O per sec";
					case PSW:		return "Proc Switches";
					case SYSC:		return "Sys Calls";
					case FORK:		return "Forks";
					case EXEC:		return "Execs";
					case READ:		return "Reads";
					case WRITE:		return "Write";
					case TOT_CPU:	return "Total sys CPU";
					case SRFR_RATIO:return "sr/fr ratio";
					case DED:		return "#Dedicated";
					case SHARED:	return "#Shared";
					case SHARED_DED:return "#Shared dedicated";
					case DED_PC:	return "Hint on Proc Consumed";
					case GLOB_PC:	return "uLPAR + hint-dedicated";
					case NUM_AIO:	return "Total AIO Procs";
					case ACTIVE_AIO:return "Active AIO Procs";
					case CPU_AIO:	return "CPU used by AIO";
					
					case LOGICAL_MEM:	return "Logical Memory";
					case PHYS_MEM:		return "Physical Memory";
					case LOAN_MEM:		return "Loaned Memory";
					case HYPPAG_IN:		return "Hypervisor page-in";
					case HYPPAG_TIME:	return "Hypervisor page-in time";
					
					case USED_LP:		return "#large pages used";
					case FREE_LP:		return "#large pages free";
					
					case TRUE_MEM:		return "True Mem MB";
					case COMP_POOL:		return "Comp pool MB";
					case UNC_POOL:		return "Uncomp pool MB";
					case EXP_MEM:		return "Exp Mem MB";
					case CP_PI:			return "Comp Pool pgin";
					case CP_PO:			return "Comp Pool pgout";
					
					case ENT_USED:		return "Proc consumed within ent";
					case PC_USED:		return "Proc consumed & used";
					case FOLDED:		return "Folded VPs";
					case VP_US:			return "VP Us%";
					case VP_SY:			return "VP Sy%";
					case VP_WA:			return "VP Wa%";
					case VP_ID:			return "VP Id%";
					case CPI:			return "Cycles per Instruction";
					
					case FAULTS:		return "faults/s";
					
					case NUMFRAMES4K:	return "4KB pool in MB";
					case NUMFRAMES64K:	return "64KB pool in MB";
					case NUMFRB4K:		return "4KB free pool in MB";
					case NUMFRB64K:		return "64KB free pool in MB";
					case PI4K:			return "4K page in / sec";
					case PO4K:			return "4K page out / sec";
					case PI64K:			return "64K page in / sec";
					case PO64K:			return "64K page out / sec";
					case NUMVPAGES4K:	return "virtual pages 4K";
					case NUMVPAGES64K:	return "virtual pages 64K";
					case FAULTS4K:		return "pages faults 4K / sec";
					case FAULTS64K:		return "pages faults 4K / sec";
					
					default:		return "SYS-ERROR";
				}
				
			case CPU:
				switch (type) {
					case US:		return "Us%";
					case SY:		return "Sy%";
					case WA:		return "Wa%";
					case ID:		return "Id%";
					default:		return "CPU-ERROR";
				}
				
			case PCPU:
				switch (type) {
					case P_TOT:		return "Total";
					default:		return "PCPU-ERROR";
				}
				

				
			case DISK:
				switch (type) {
					case DSK_BUSY:		return "Busy%";
					case DSK_READKB: 	return "Read KB";
					case DSK_WRITEKB:	return "Write KB";
					case DSK_XFER:  	return "Transf/sec";
					case DSK_BSIZE:		return "Block size";
					
					// Extended statistics
					case DSK_RPS:  		return "Reads/s";
					case DSK_AVG_R: 	return "AvgRead ms";
					case DSK_MIN_R: 	return "MinRead ms";
					case DSK_MAX_R: 	return "MaxRead ms";
					case DSK_TO_R: 		return "read Timeouts/s";
					case DSK_FAIL_R: 	return "Failed Read Requests/s";
					
					case DSK_WPS:  		return "Write/s";
					case DSK_AVG_W: 	return "AvgWrite ms";
					case DSK_MIN_W: 	return "MinWrite ms";
					case DSK_MAX_W: 	return "MaxWrite ms";
					case DSK_TO_W: 		return "Write Timeouts/s";
					case DSK_FAIL_W: 	return "Failed Write Requests/s";
					
					case DSK_AVG_T: 	return "AvgTime ms";
					case DSK_MIN_T: 	return "MinTime ms";
					case DSK_MAX_T: 	return "MaxTime ms";
					case DSK_AVG_WQ: 	return "Avg WaitQ Size";
					case DSK_AVG_SQ: 	return "Avg ServiceQ Size"; 
					case DSK_FULLQ: 	return "Service Queue full per sec";
					
					case DSK_AVGSERV: 	return "Avg service time";
					case DSK_AVGWAIT: 	return "Service wait time";
					
					case DSK_AVG_RW:	return "AvgRW ms";
					case DSK_RWPS:		return "Read/s+Write/s";
				
					default:		return "DISK-ERROR";
				}
				
			case ESS:
				switch (type) {
					case ESS_READKB: 	return "Read KB";
					case ESS_WRITEKB:	return "Write KB";
					case ESS_XFER:  	return "Transf/sec";
					
					case ESS_AVGSERV: 	return "Avg service time";
					case ESS_AVGWAIT: 	return "Service wait time";
					
					default:		return "ESS-ERROR";
				}
				
			case SCSI:
				switch (type) {
					case SCSI_READKB: 	return "Dsk Read KB";
					case SCSI_WRITEKB:	return "Dsk Write KB";
					case SCSI_XFER:  	return "Dsk Transf/sec";
					
					// Extended statistics
					case SCSI_RPS:  	return "Dsk Reads/s";
					case SCSI_AVG_R: 	return "Dsk AvgRead ms";
					case SCSI_MIN_R: 	return "Dsk MinRead ms";
					case SCSI_MAX_R: 	return "Dsk MaxRead ms";
					
					case SCSI_WPS:  	return "Dsk Write/s";
					case SCSI_AVG_W: 	return "Dsk AvgWrite ms";
					case SCSI_MIN_W: 	return "Dsk MinWrite ms";
					case SCSI_MAX_W: 	return "Dsk MaxWrite ms";
					
					case SCSI_AVG_T: 	return "Dsk AvgTime ms";
					case SCSI_MIN_T: 	return "Dsk MinTime ms";
					case SCSI_MAX_T: 	return "Dsk MaxTime ms";
					case SCSI_AVG_WQ: 	return "Dsk Avg WaitQ Size";
					case SCSI_AVG_SQ: 	return "Dsk Avg ServiceQ Size"; 
					case SCSI_FULLQ: 	return "Dsk Service Queue full per sec";
				
					default:		return "SCSI-ERROR";					
				}
				
			case NETWORK:
				switch (type) {
					case NET_READKB: 	 	return "Read KB";
					case NET_WRITEKB:  		return "Write KB";
					case NET_READS:  		return "Reads/s";
					case NET_WRITES:  		return "Write/s";
					case NET_IERRORS:  		return "In errors";
					case NET_OERRORS:  		return "Out errors";
					case NET_COLLISIONS:  	return "Collisions";	
					
					default:		return "NETWORK-ERROR";	
				}
				
			case SEA:
				switch (type) {
					case NET_READKB: 	 	return "Read KB";
					case NET_WRITEKB:  		return "Write KB";
					case NET_READS:  		return "Reads/s";
					case NET_WRITES:  		return "Write/s";
					case NET_IERRORS:  		return "In errors";
					case NET_OERRORS:  		return "Out errors";
					case NET_COLLISIONS:  	return "Collisions";	
					
					default:		return "SEA-ERROR";	
				}
				
			case WPAR:
				switch (type) {
					case WPAR_CPU: 	 		return "CPU%";
					case WPAR_MEM:  		return "RAM%";
					case WPAR_DISK:  		return "IO Bandwidth%";	
					case WPAR_PROC:			return "Proc consumed";
					
					default:		return "WPAR-ERROR";	
				}
			
			case TOPPROC:
				switch (type) {
					case TOP_CPU: 	 		return "ONE-CPU%";
					case TOP_RESTEXT:  		return "ResText";
					case TOP_RESDATA:  		return "ResData";
					
					default:		return "TOPPROC-ERROR";	
			}
				
			case TOPPROC_BY_NAME:
				switch (type) {
				case TOP_CPU_BYNAME: 	 	return "ONE-CPU%";
				case TOP_RAM_BYNAME:  		return "MEM%";
				
				default:		return "TOPPROC-ERROR";	
			}
				
			case FS:
				switch (type) {
					case SPACEUSED: 	 	return "Space Used%";
					case INODEUSED:  		return "Inode Used%";
					
					default:		return "FS-ERROR";	
			}
				
			case FCSTAT:
				switch (type) {
					case FCREAD: 	 		return "FC Read KB/s";
					case FCWRITE:  			return "FC Write KB/s";
					case FCXFERIN: 	 		return "FC Transfer IN";
					case FCXFEROUT:  		return "FC Transfer OUT";
					
					default:		return "FC-ERROR";	
			}
				
			case DAC:
				switch (type) {
					case SCSI_READKB: 	return "Dsk Read KB";
					case SCSI_WRITEKB:	return "Dsk Write KB";
					case SCSI_XFER:  	return "Dsk Transf/sec";
					
					// Extended statistics
					case SCSI_RPS:  	return "Dsk Reads/s";
					case SCSI_AVG_R: 	return "Dsk AvgRead ms";
					case SCSI_MIN_R: 	return "Dsk MinRead ms";
					case SCSI_MAX_R: 	return "Dsk MaxRead ms";
					
					case SCSI_WPS:  	return "Dsk Write/s";
					case SCSI_AVG_W: 	return "Dsk AvgWrite ms";
					case SCSI_MIN_W: 	return "Dsk MinWrite ms";
					case SCSI_MAX_W: 	return "Dsk MaxWrite ms";
					
					case SCSI_AVG_T: 	return "Dsk AvgTime ms";
					case SCSI_MIN_T: 	return "Dsk MinTime ms";
					case SCSI_MAX_T: 	return "Dsk MaxTime ms";
					case SCSI_AVG_WQ: 	return "Dsk Avg WaitQ Size";
					case SCSI_AVG_SQ: 	return "Dsk Avg ServiceQ Size"; 
					case SCSI_FULLQ: 	return "Dsk Service Queue full per sec";
				
					default:		return "DAC-ERROR";					
				}
				
			case PROCPOOL:
				switch (type) {
					case POOLSIZE:		return "ProcPool size";
					case POOLUSED:		return "ProcPool used";
					case ACTIVEPOOL:	return "This pool is active";
					
					default:		return "PROCPOOL-ERROR";
				}
				
			default:
				return "GEN-ERROR";
		}
		
	}
	
	
	
	private DataSet getDataSet(byte group, byte type, String item) {
		int i, pos;
		String s[];
		DataSet ds[][];
		
		// Check if item already exists
		for (i=0; i<valid_names[group]; i++) {
			if (item.equals(names[group][i])) {
				if (data[group][i][type]==null) {
					data[group][i][type] = new DataSet(getSetName(group,type));
					
					// Disable abs min and abs max for some data types
					if (group==TOPPROC || group==TOPPROC_BY_NAME)
						data[group][i][type].setAbsLimitActive(false);
				}
				return data[group][i][type];
			}
		}
		
		// New item must be added
		
		pos = valid_names[group];	// position of new item
		
		// if there is no space left, expand data structures
		if (pos==names[group].length) {
			ds = data[group];
			data[group] = new DataSet[ds.length+ITEM_BLOCK][];
			s = names[group];
			names[group] = new String[s.length+ITEM_BLOCK];
			for (i=0; i<ds.length; i++) {
				data[group][i]=ds[i];
				names[group][i]=s[i];
			}
		}
		
		// Add new item
		data[group][pos] = new DataSet[getGroupTypes(group)];
		data[group][pos][type] = new DataSet(getSetName(group,type));
		
		// Disable abs min and abs max for some data types
		if (group==TOPPROC || group==TOPPROC_BY_NAME)
			data[group][pos][type].setAbsLimitActive(false);
		
		names[group][pos] = item;
		valid_names[group]++;
		return data[group][pos][type];
	}
	

	public  void add(int slot, byte type, float value) {
		add(slot,SYSTEM,type,SYSTEM_ITEM,value);
	}
	
	public void add(int slot, byte group, byte type, String item, float value) {
		// Sanity checks (if slot==-1 just avoid adding data, but create DataSet)
		if (slot >=DataSet.SLOTS || 
				group<0 || group>=NUM_GROUPS ||
				type<0 )
			return;
		
		if (value<0)
			return;
		
		if (group==SYSTEM)
			item=SYSTEM_ITEM;
		
		DataSet ds = getDataSet(group,type,item);
		
		// If slot == -1 data in tin time scope
		if (slot>=0)
			ds.add(slot, value);
	}
	
	
	/*
	 * Must be called only ONCE by slot
	 */
	public  void addBySlot(int slot, byte type, float value, float min, float max) {
		addBySlot(slot,SYSTEM,type,SYSTEM_ITEM,value,min,max);
	}
	
	
	/*
	 * Must be called only ONCE by slot
	 */
	public void addBySlot(int slot, byte group, byte type, String item, float value, float min, float max) {
		// Sanity checks (if slot==-1 just avoid adding data, but create DataSet)
		if (slot >=DataSet.SLOTS || 
				group<0 || group>=NUM_GROUPS ||
				type<0 )
			return;
		
		if (value<0)
			return;
		
		DataSet ds = getDataSet(group,type,item);
		
		// If slot == -1 data in tin time scope
		if (slot>=0)
			ds.addBySlot(slot, value, min, max);
	}
	
	
	
	
	public void setLimits(GregorianCalendar start, GregorianCalendar end) {
		// Reset all collected data
		int i,j,k;
		
		for (i=0; i<data.length; i++) 
			if (data[i]!=null) {
				for (j=0; j<data[i].length; j++)
					if (data[i][j]!=null) {
						for (k=0; k<data[i][j].length; k++)
							if (data[i][j][k]!=null)
								data[i][j][k].reset();
					}
					
			}
		
		lssrad_ref1 = null;
		lssrad_srad = null;
		lssrad_mem = null;
		lssrad_cpu = null;
		
		for (i=0; i<textLabel.length; i++)
			textLabel[i] = null;
		for (i=0; i<textValue.length; i++)
			textValue[i] = null;
			
		//data = new DataSet[NUM_GROUPS][][];
		//names= new String[NUM_GROUPS][];
		this.start=start;
		this.end=end;
	}
		
	
	
	
	/*
	 * Data parsing ended. Compute all average and close data entry.
	 */
	public void endOfData() {
		int i,j,k;
		DataSet ds1,ds2,ds3,ds4,ds5,ds6;
		
		// Apply weight to CPU data
		if (data[SYSTEM]!=null && data[SYSTEM][0]!=null) {
			if (data[SYSTEM][0][PC]!=null)
				data[SYSTEM][0][PC].setWeight(cpuWeight);
			if (data[SYSTEM][0][ENT]!=null)
				data[SYSTEM][0][ENT].setWeight(cpuWeight);
		}
		
		for (i=0; i<data.length; i++)
			for (j=0; data[i]!=null && j<data[i].length; j++)
				for (k=0; data[i][j]!=null && k<data[i][j].length; k++)
					if (data[i][j][k]!=null)
						data[i][j][k].endOfData();
		
		
		
		// Create PC_USED 
		if (data[SYSTEM][0]!=null && data[SYSTEM][0][PC]!=null && data[SYSTEM][0][ENT]!=null && data[CPU]!=null && data[CPU][0]!=null) {
			
			int avgID=-1;
			// Detect AVG_CPU label
			for (i=0; i<names[CPU].length; i++)
				if (names[CPU][i].equals(Parser.AVG_CPU)) {
					avgID=i;
					break;
				}
			
			if (avgID>=0) {		
				ds1 = getDataSet(SYSTEM,PC_USED,SYSTEM_ITEM);
				float pc, us, sy, ent;
				float min_pc, min_us, min_sy, min_ent;
				float max_pc, max_us, max_sy, max_ent;
				float used, min_used, max_used;
				for (i=0; i<DataSet.SLOTS; i++) {
					pc = data[SYSTEM][0][PC].getValue(i);
					ent = data[SYSTEM][0][ENT].getValue(i);
					us = data[CPU][avgID][US].getValue(i);
					sy = data[CPU][avgID][SY].getValue(i);
					min_pc = data[SYSTEM][0][PC].getAbsMin(i);
					min_ent = data[SYSTEM][0][ENT].getAbsMin(i);
					min_us = data[CPU][avgID][US].getAbsMin(i);
					min_sy = data[CPU][avgID][SY].getAbsMin(i);
					max_pc = data[SYSTEM][0][PC].getAbsMax(i);
					max_ent = data[SYSTEM][0][ENT].getAbsMax(i);
					max_us = data[CPU][avgID][US].getAbsMax(i);
					max_sy = data[CPU][avgID][SY].getAbsMax(i);
					if (pc<0 || ent<0 || us<0 || sy<0)
						continue;
					
					if (pc<=ent) {
						// Percentage is related to ent
						used = ent*(us+sy)/100;
						min_used = min_ent*(min_us+min_sy)/100;
						max_used = max_ent*(max_us+max_sy)/100;
					} else {
						// Percentage is related to pc
						used = pc*(us+sy)/100;
						min_used = min_pc*(min_us+min_sy)/100;
						max_used = max_pc*(max_us+max_sy)/100;
					}
					
					//ds1.addBySlot(i, pc*(us+sy)/100, min_pc*(min_us+min_sy)/100, max_pc*(max_us+max_sy)/100);
					ds1.addBySlot(i, used, min_used, max_used);
				}
				ds1.endOfData();
			}
		}
				
		// Create GLOB_PC 
		if (data[SYSTEM]!=null && data[SYSTEM][0]!=null &&
				data[SYSTEM][0][PC]!=null && data[SYSTEM][0][DED_PC]!=null) {
			ds1 = getDataSet(SYSTEM,GLOB_PC,SYSTEM_ITEM);
			float pc, ded_pc;
			float min_pc, min_ded_pc;
			float max_pc, max_ded_pc;
			for (i=0; i<DataSet.SLOTS; i++) {
				pc = data[SYSTEM][0][PC].getValue(i);
				ded_pc = data[SYSTEM][0][DED_PC].getValue(i);
				min_pc = data[SYSTEM][0][PC].getAbsMin(i);
				min_ded_pc = data[SYSTEM][0][DED_PC].getAbsMin(i);
				max_pc = data[SYSTEM][0][PC].getAbsMax(i);
				max_ded_pc = data[SYSTEM][0][DED_PC].getAbsMax(i);
				if (pc<0 || ded_pc<0)
					continue;
				ds1.addBySlot(i, pc+ded_pc, min_pc+min_ded_pc, max_pc+max_ded_pc);
			}
			ds1.endOfData();
			
		}
		
		// Create SRFR_RATIO computed data sets
		if (data[SYSTEM]!=null && data[SYSTEM][0]!=null &&
				data[SYSTEM][0][SR]!=null && data[SYSTEM][0][FR]!=null) {
			ds1 = getDataSet(SYSTEM,SRFR_RATIO,SYSTEM_ITEM);
			float sr, fr;
			for (i=0; i<DataSet.SLOTS; i++) {
				sr = data[SYSTEM][0][SR].getValue(i);
				fr = data[SYSTEM][0][FR].getValue(i);
				if (fr<0 || sr<0)
					continue;
				if (fr==0)
					ds1.addBySlot(i, 0);
				else
					ds1.addBySlot(i, sr/fr);
			}
			ds1.endOfData();
		}
		
		// Create USEDRAM computed data sets
		if (data[SYSTEM]!=null && data[SYSTEM][0]!=null &&
				data[SYSTEM][0][RAM]!=null && data[SYSTEM][0][FRE]!=null) {
			ds1 = getDataSet(SYSTEM,USEDMEM,SYSTEM_ITEM);
			float ram, fre;
			float min_ram, min_fre;
			float max_ram, max_fre;
			for (i=0; i<DataSet.SLOTS; i++) {
				fre = data[SYSTEM][0][FRE].getValue(i);
				ram = data[SYSTEM][0][RAM].getValue(i);
				min_fre = data[SYSTEM][0][FRE].getAbsMin(i);
				min_ram = data[SYSTEM][0][RAM].getAbsMin(i);
				max_fre = data[SYSTEM][0][FRE].getAbsMax(i);
				max_ram = data[SYSTEM][0][RAM].getAbsMax(i);
				if (fre<0 || ram<0)
					continue;
				ds1.addBySlot(i, ram-fre, min_ram-max_fre, max_ram-min_fre);
			}
			ds1.endOfData();
		}
		
		// Create POOLUSED data for LPAR only
		if (data[PROCPOOL]!=null) {
			for (i=0; i<data[PROCPOOL].length; i++) {
				if (data[PROCPOOL][i]!=null && data[PROCPOOL][i][ACTIVEPOOL]!=null) {
					// We are in a LPAR that has been in this ProcPool
					ds1 = getDataSet(PROCPOOL, POOLUSED, names[PROCPOOL][i]);
					for (j=0; j<DataSet.SLOTS; j++) {
						if (data[PROCPOOL][i][ACTIVEPOOL].getValue(j)>=0)
							ds1.addBySlot(j, data[SYSTEM][0][PC].getValue(j), data[SYSTEM][0][PC].getAbsMin(j), data[SYSTEM][0][PC].getAbsMax(j));
					}
					ds1.endOfData();
				}
			}
		}
		
		// WPAR physical CPU usage
		if (data[WPAR]!=null && data[SYSTEM][0]!=null && data[SYSTEM][0][PC]!=null) {
			float cpu, pc;
			float min_cpu, min_pc;
			float max_cpu, max_pc;
			for (i=0; i<data[WPAR].length; i++) {
				if (data[WPAR][i]==null || data[WPAR][i][WPAR_CPU]==null)
					continue;
				ds1 = getDataSet(WPAR,WPAR_PROC,names[WPAR][i]);
				for (j=0; j<DataSet.SLOTS; j++) {
					cpu = data[WPAR][i][WPAR_CPU].getValue(j);
					pc  = data[SYSTEM][0][PC].getValue(j);
					min_cpu = data[WPAR][i][WPAR_CPU].getAbsMin(j);
					min_pc  = data[SYSTEM][0][PC].getAbsMin(j);
					max_cpu = data[WPAR][i][WPAR_CPU].getAbsMax(j);
					max_pc  = data[SYSTEM][0][PC].getAbsMax(j);
					if (cpu<0 || pc<0)
						continue;
					ds1.addBySlot(j, pc/100*cpu, min_pc/100*min_cpu, max_pc/100*max_cpu);
				}
				ds1.endOfData();
			}
		}
		
		// Global disk RW data: only if there are at least 2 disks!
		if (data[DISK]!=null && data[DISK][0]!=null && data[DISK][1]!=null) {
			float r,w,x,tr,tw,tx;
			float minR, minW, minX;
			float maxR, maxW, maxX;
			ds1 = getDataSet(DISK,DSK_READKB,"_Global_Disk");
			ds2 = getDataSet(DISK,DSK_WRITEKB,"_Global_Disk");
			ds3 = getDataSet(DISK,DSK_XFER,"_Global_Disk");
			for (i=0; i<DataSet.SLOTS; i++) {
				tr=tw=tx=-1;
				minR=minW=minX=-1;
				maxR=maxW=maxX=-1;
				for (j=0; j<data[DISK].length; j++) {
					if (data[DISK][j]==null)
						continue;
					if (data[DISK][j][DSK_READKB]==ds1 || 
							data[DISK][j][DSK_WRITEKB]==ds2 ||
							data[DISK][j][DSK_XFER]==ds3 )
						continue;
					if (data[DISK][j][DSK_READKB]!=null) {
						r = data[DISK][j][DSK_READKB].getValue(i);
						if (r>=0) {	if (tr<0) tr=r;	else tr+=r; }
						r = data[DISK][j][DSK_READKB].getAbsMin(i);
						if (r>=0) {	if (minR<0) minR=r;	else minR+=r; }
						r = data[DISK][j][DSK_READKB].getAbsMax(i);
						if (r>=0) {	if (maxR<0) maxR=r;	else maxR+=r; }
					}
					if (data[DISK][j][DSK_WRITEKB]!=null) {
						w = data[DISK][j][DSK_WRITEKB].getValue(i);
						if (w>=0) { if (tw<0) tw=w;	else tw+=w; }
						w = data[DISK][j][DSK_WRITEKB].getAbsMin(i);
						if (w>=0) { if (minW<0) minW=w;	else minW+=w; }
						w = data[DISK][j][DSK_WRITEKB].getAbsMax(i);
						if (w>=0) { if (maxW<0) maxW=w;	else maxW+=w; }
					}
					if (data[DISK][j][DSK_XFER]!=null) {
						x = data[DISK][j][DSK_XFER].getValue(i);
						if (x>=0) { if (tx<0) tx=x;	else tx+=x; }
						x = data[DISK][j][DSK_XFER].getAbsMin(i);
						if (x>=0) { if (minX<0) minX=x;	else minX+=x; }
						x = data[DISK][j][DSK_XFER].getAbsMax(i);
						if (x>=0) { if (maxX<0) maxX=x;	else maxX+=x; }
					}
				}
				ds1.addBySlot(i, tr, minR, maxR);
				ds2.addBySlot(i, tw, minW, maxW);
				ds3.addBySlot(i, tx, minX, maxX);
			}
			ds1.endOfData();
			ds2.endOfData();
			ds3.endOfData();
		}
		
		
		// Average RW service data: (DSK_RPS*DSK_AVG_R+DSK_WPS*DSK_AVG_W)/(DSK_RPS+DSK_WPS)
		if (data[DISK]!=null) {
			float rps,r,wps,w,rw;
			
			for (i=0; i<data[DISK].length; i++) {
				if (data[DISK][i]==null)
					continue;
				
				ds1 = data[DISK][i][DSK_RPS];
				ds2 = data[DISK][i][DSK_AVG_R];
				ds3 = data[DISK][i][DSK_WPS];
				ds4 = data[DISK][i][DSK_AVG_W];	
				if (ds1==null || ds2==null || ds3==null || ds4==null)
					continue;
				
				ds5 = getDataSet(DISK,DSK_AVG_RW,names[DISK][i]);
				ds6 = getDataSet(DISK,DSK_RWPS,names[DISK][i]);
				for (j=0; j<DataSet.SLOTS; j++) {
					rps = ds1.getValue(j);
					r   = ds2.getValue(j);
					wps = ds3.getValue(j);
					w   = ds4.getValue(j);
					
					if (rps<0 || r<0 || wps<0 || w<0)
						continue;
					
					if (rps+wps==0)
						rw = 0;
					else
						rw = (rps*r+wps*w)/(rps+wps);
					
					ds5.addBySlot(j, rw);
					ds6.addBySlot(j, rps+wps);
				}
				ds5.endOfData();
				ds6.endOfData();
			}
		}
		
		// Global disk ESS data: only if there are at least 2 disks!
		if (data[ESS]!=null && data[ESS][0]!=null && data[ESS][1]!=null) {
			float r,w,x,tr,tw,tx;
			float minR, minW, minX;
			float maxR, maxW, maxX;
			ds1 = getDataSet(ESS,ESS_READKB,"_Global_MPIO");
			ds2 = getDataSet(ESS,ESS_WRITEKB,"_Global_MPIO");
			ds3 = getDataSet(ESS,ESS_XFER,"_Global_MPIO");
			for (i=0; i<DataSet.SLOTS; i++) {
				tr=tw=tx=-1;
				minR=minW=minX=-1;
				maxR=maxW=maxX=-1;
				for (j=0; j<data[ESS].length; j++) {
					if (data[ESS][j]==null)
						continue;
					if (data[ESS][j][ESS_READKB]==ds1 ||
							data[ESS][j][ESS_WRITEKB]==ds2 ||
							data[ESS][j][ESS_XFER]==ds3)
						continue;
					r = data[ESS][j][ESS_READKB].getValue(i);
					w = data[ESS][j][ESS_WRITEKB].getValue(i);
					x = data[ESS][j][ESS_XFER].getValue(i);
					if (r>=0) {	if (tr<0) tr=r;	else tr+=r; }
					if (w>=0) {	if (tw<0) tw=w;	else tw+=w;	}
					if (x>=0) {	if (tx<0) tx=x;	else tx+=x;	}
					r = data[ESS][j][ESS_READKB].getAbsMin(i);
					w = data[ESS][j][ESS_WRITEKB].getAbsMin(i);
					x = data[ESS][j][ESS_XFER].getAbsMin(i);
					if (r>=0) {	if (minR<0) minR=r;	else minR+=r; }
					if (w>=0) {	if (minW<0) minW=w;	else minW+=w;	}
					if (x>=0) {	if (minX<0) minX=x;	else minX+=x;	}
					r = data[ESS][j][ESS_READKB].getAbsMax(i);
					w = data[ESS][j][ESS_WRITEKB].getAbsMax(i);
					x = data[ESS][j][ESS_XFER].getAbsMax(i);
					if (r>=0) {	if (maxR<0) maxR=r;	else maxR+=r; }
					if (w>=0) {	if (maxW<0) maxW=w;	else maxW+=w;	}
					if (x>=0) {	if (maxX<0) maxX=x;	else maxX+=x;	}
				}
				ds1.addBySlot(i, tr, minR, maxR);
				ds2.addBySlot(i, tw, minW, maxW);
				ds3.addBySlot(i, tx, minX, maxX);
			}
			ds1.endOfData();
			ds2.endOfData();
			ds3.endOfData();
		}
		
		// Global SCSI data: only if there are at least 2 adapters!
		//if (data[SCSI]!=null && data[SCSI][0]!=null && data[SCSI][1]!=null) {
		if (data[SCSI]!=null) {
			float r,w,x,tr,tw,tx;
			float minR, minW, minX;
			float maxR, maxW, maxX;
			ds1 = getDataSet(SCSI,SCSI_READKB,"_Global_Disk_Adapter");
			ds2 = getDataSet(SCSI,SCSI_WRITEKB,"_Global_Disk_Adapter");
			ds3 = getDataSet(SCSI,SCSI_XFER,"_Global_Disk_Adapter");
			for (i=0; i<DataSet.SLOTS; i++) {
				tr=tw=tx=-1;
				minR=minW=minX=-1;
				maxR=maxW=maxX=-1;
				for (j=0; j<data[SCSI].length; j++) {
					if (data[SCSI][j]==null)
						continue;
					if (data[SCSI][j][SCSI_READKB]==ds1 ||
							data[SCSI][j][SCSI_WRITEKB]==ds2 ||
							data[SCSI][j][SCSI_XFER]==ds3)
						continue;
					r = data[SCSI][j][SCSI_READKB].getValue(i);
					w = data[SCSI][j][SCSI_WRITEKB].getValue(i);
					x = data[SCSI][j][SCSI_XFER].getValue(i);
					if (r>=0) {	if (tr<0) tr=r;	else tr+=r; }
					if (w>=0) {	if (tw<0) tw=w;	else tw+=w;	}
					if (x>=0) {	if (tx<0) tx=x;	else tx+=x;	}
					r = data[SCSI][j][SCSI_READKB].getAbsMin(i);
					w = data[SCSI][j][SCSI_WRITEKB].getAbsMin(i);
					x = data[SCSI][j][SCSI_XFER].getAbsMin(i);
					if (r>=0) {	if (minR<0) minR=r;	else minR+=r; }
					if (w>=0) {	if (minW<0) minW=w;	else minW+=w;	}
					if (x>=0) {	if (minX<0) minX=x;	else minX+=x;	}
					r = data[SCSI][j][SCSI_READKB].getAbsMax(i);
					w = data[SCSI][j][SCSI_WRITEKB].getAbsMax(i);
					x = data[SCSI][j][SCSI_XFER].getAbsMax(i);
					if (r>=0) {	if (maxR<0) maxR=r;	else maxR+=r; }
					if (w>=0) {	if (maxW<0) maxW=w;	else maxW+=w;	}
					if (x>=0) {	if (maxX<0) maxX=x;	else maxX+=x;	}
				}
				ds1.addBySlot(i, tr, minR, maxR);
				ds2.addBySlot(i, tw, minW, maxW);
				ds3.addBySlot(i, tx, minX, maxX);
			}
			ds1.endOfData();
			ds2.endOfData();
			ds3.endOfData();
		}
		
		// Global FC data: only if there are at least 2 adapters!
		if (data[FCSTAT]!=null && data[FCSTAT][0]!=null && data[FCSTAT][1]!=null) {
			float r,w,xin,xout,tr,tw,txin,txout;
			float minR, minW, minXin, minXout;
			float maxR, maxW, maxXin, maxXout;
			ds1 = getDataSet(FCSTAT,FCREAD,"_Global_FC_Adapter");
			ds2 = getDataSet(FCSTAT,FCWRITE,"_Global_FC_Adapter");
			ds3 = getDataSet(FCSTAT,FCXFERIN,"_Global_FC_Adapter");
			ds4 = getDataSet(FCSTAT,FCXFEROUT,"_Global_FC_Adapter"); 
			for (i=0; i<DataSet.SLOTS; i++) {
				tr=tw=txin=txout=-1;
				minR=minW=minXin=minXout=-1;
				maxR=maxW=maxXin= maxXout=-1;
				for (j=0; j<data[FCSTAT].length; j++) {
					if (data[FCSTAT][j]==null)
						continue;
					if (data[FCSTAT][j][FCREAD]==ds1 ||
							data[FCSTAT][j][FCWRITE]==ds2 ||
							data[FCSTAT][j][FCXFERIN]==ds3 ||
							data[FCSTAT][j][FCXFEROUT]==ds4)
						continue;
					r = data[FCSTAT][j][FCREAD].getValue(i);
					w = data[FCSTAT][j][FCWRITE].getValue(i);
					xin = data[FCSTAT][j][FCXFERIN].getValue(i);
					xout = data[FCSTAT][j][FCXFEROUT].getValue(i);
					if (r>=0) {	if (tr<0) tr=r;	else tr+=r; }
					if (w>=0) {	if (tw<0) tw=w;	else tw+=w;	}
					if (xin>=0) {	if (txin<0) txin=xin;	else txin+=xin;	}
					if (xout>=0) {	if (txout<0) txout=xout;	else txout+=xout;	}
					r = data[FCSTAT][j][FCREAD].getAbsMin(i);
					w = data[FCSTAT][j][FCWRITE].getAbsMin(i);
					xin = data[FCSTAT][j][FCXFERIN].getAbsMin(i);
					xout = data[FCSTAT][j][FCXFEROUT].getAbsMin(i);
					if (r>=0) {	if (minR<0) minR=r;	else minR+=r; }
					if (w>=0) {	if (minW<0) minW=w;	else minW+=w;	}
					if (xin>=0) {	if (minXin<0) minXin=xin;	else minXin+=xin;	}
					if (xout>=0) {	if (minXout<0) minXout=xout;	else minXout+=xout;	}
					r = data[FCSTAT][j][FCREAD].getAbsMax(i);
					w = data[FCSTAT][j][FCWRITE].getAbsMax(i);
					xin = data[FCSTAT][j][FCXFERIN].getAbsMax(i);
					xout = data[FCSTAT][j][FCXFEROUT].getAbsMax(i);
					if (r>=0) {	if (maxR<0) maxR=r;	else maxR+=r; }
					if (w>=0) {	if (maxW<0) maxW=w;	else maxW+=w;	}
					if (xin>=0) {	if (maxXin<0) maxXin=xin;	else maxXin+=xin;	}
					if (xout>=0) {	if (maxXout<0) maxXout=xout;	else maxXout+=xout;	}
				}
				ds1.addBySlot(i, tr, minR, maxR);
				ds2.addBySlot(i, tw, minW, maxW);
				ds3.addBySlot(i, txin, minXin, maxXin);
				ds4.addBySlot(i, txout, minXout, maxXout);
			}
			ds1.endOfData();
			ds2.endOfData();
			ds3.endOfData();
			ds4.endOfData();
		}

		// Global NETWORK data: only if there are at least 2 adapters! AVOID LOOPBACK
		if (data[NETWORK]!=null && data[NETWORK][0]!=null && data[NETWORK][1]!=null && data[NETWORK][2]!=null) {
			float r,w,tr,tw;
			float minR, minW;
			float maxR, maxW;
			ds1 = getDataSet(NETWORK,NET_READKB,"_Global_Network");
			ds2 = getDataSet(NETWORK,NET_WRITEKB,"_Global_Network");
			for (i=0; i<DataSet.SLOTS; i++) {
				tr=tw=-1;
				minR=minW=-1;
				maxR=maxW=-1;
				for (j=0; j<data[NETWORK].length; j++) {
					if (data[NETWORK][j]==null || names[NETWORK][j].startsWith("lo"))
						continue;
					if (data[NETWORK][j][NET_READKB]==ds1 ||
							data[NETWORK][j][NET_WRITEKB]==ds2)
						continue;
					r = data[NETWORK][j][NET_READKB].getValue(i);
					w = data[NETWORK][j][NET_WRITEKB].getValue(i);
					if (r>=0) {	if (tr<0) tr=r;	else tr+=r; }
					if (w>=0) {	if (tw<0) tw=w;	else tw+=w;	}
					r = data[NETWORK][j][NET_READKB].getAbsMin(i);
					w = data[NETWORK][j][NET_WRITEKB].getAbsMin(i);
					if (r>=0) {	if (minR<0) minR=r;	else minR+=r; }
					if (w>=0) {	if (minW<0) minW=w;	else minW+=w;	}
					r = data[NETWORK][j][NET_READKB].getAbsMax(i);
					w = data[NETWORK][j][NET_WRITEKB].getAbsMax(i);
					if (r>=0) {	if (maxR<0) maxR=r;	else maxR+=r; }
					if (w>=0) {	if (maxW<0) maxW=w;	else maxW+=w;	}
				}
				ds1.addBySlot(i, tr, minR, maxR);
				ds2.addBySlot(i, tw, minW, maxW);
			}
			ds1.endOfData();
			ds2.endOfData();
		}	
		
		
		// Consolidate top processes by name
		String procName[];
		DataSet ds;
		float resdata, restext;
		float ram;
		
		for (i=0; i<data[TOPPROC].length; i++) {
			if (data[TOPPROC][i]==null)
				continue;
			procName = names[TOPPROC][i].split(":");
			for (j=0; j<DataSet.SLOTS; j++)
				add(j, PerfData.TOPPROC_BY_NAME, PerfData.TOP_CPU, 
						procName[0], data[TOPPROC][i][TOP_CPU].getValue(j));
			
			ds = getDataSet(PerfData.TOPPROC_BY_NAME, PerfData.TOP_RAM_BYNAME, procName[0]);		
			for (j=0; j<DataSet.SLOTS; j++) {
				resdata = data[TOPPROC][i][TOP_RESDATA].getValue(j)/1024;
				restext = data[TOPPROC][i][TOP_RESTEXT].getValue(j)/1024;
				ram = data[SYSTEM][0][RAM].getValue(j);
				if (resdata<0 || restext<0 || ram<0)
					continue;
				if (ds.getValue(j)<0)	// add Text only once!
					add(j, PerfData.TOPPROC_BY_NAME, PerfData.TOP_RAM_BYNAME, 
							procName[0], restext/ram*100);
				add(j, PerfData.TOPPROC_BY_NAME, PerfData.TOP_RAM_BYNAME, 
						procName[0], resdata/ram*100);
			}
		}
		
		for (i=0; i<data[TOPPROC_BY_NAME].length; i++) {
			if (data[TOPPROC_BY_NAME][i]==null)
				continue;
			for (j=0; j<NUM_TOPPROC_BYNAME; j++)
				data [TOPPROC_BY_NAME][i][j].endOfDataSpecial();
		}
		
		
		// Create DED_PC computed data set
		if (data[SYSTEM]!=null && 
				( data[SYSTEM][0]==null || data[SYSTEM][0][PC]==null ) &&   // no PC!!S
				data[CPU][0]!=null ) {
			
			int avgID=-1;
			// Detect AVG_CPU label
			for (i=0; i<names[CPU].length; i++)
				if (names[CPU][i].equals(Parser.AVG_CPU)) {
					avgID=i;
					break;
				}
			
			if (avgID>=0) {			
				ds1 = getDataSet(SYSTEM,DED_PC,SYSTEM_ITEM);
				ds1.setWeight(cpuWeight);
				float us, sy, ncpu;			
				
				for (i=0; i<DataSet.SLOTS; i++) {
					us = data[CPU][avgID][US].getValue(i);
					sy = data[CPU][avgID][SY].getValue(i);
					
					if (data[SYSTEM][0][TOT_CPU]!=null) {
						smtStatus = SMT_PARSED;
						ncpu = data[SYSTEM][0][TOT_CPU].getValue(i);
					} else {
						for (j=0, ncpu=0; j<names[CPU].length && names[CPU][j]!=null; j++)
							if (data[CPU][j][US].getValue(i)>=0)
								ncpu++;
						if (ncpu>1)
							ncpu--;		// if more than one CPU, one is average!
					}
					
					if (us<0 || sy<0 || ncpu<=0)
						continue;
					
					if (smtStatus == SMT_SUPPOSED_ON)
						ncpu /= 2;
					
					ds1.addBySlot(i, 1f*ncpu*(us+sy)/100);
				}
				ds1.endOfData();
			}
		}
		
		// Sort names for each group
		sortNames();
		
	}
	
	
	private void sortNames() {
		int i,j,k,x;
		
		// Sort all groups
		for (i=0; i<NUM_GROUPS; i++) {
			// create sorted list
			sorted_names[i] = new int[valid_names[i]];
			for (j=0; j<sorted_names[i].length;j++)
				sorted_names[i][j]=-1;
			
			// scan all names
			for (j=0; j<valid_names[i]; j++) {
				// scan all sorted list
				k=0;
				while ( sorted_names[i][k]>=0 &&
						compareNames(names[i][sorted_names[i][k]],names[i][j])<0)
					k++;
				if (sorted_names[i][k]>=0)
					for (x=valid_names[i]-1; x>k; x--)
						sorted_names[i][x]=sorted_names[i][x-1];
				sorted_names[i][k]=j;
			}
		}
	}
	
	private int compareNames(String a, String b) {
		if (a.length()==0)
			return -1;
		if (b.length()==0)
			return 1;
		
		if (a.charAt(0)=='_')
			return -1;
		
		if (b.charAt(0)=='_')
			return 1;
		
		if (a.startsWith("Avg"))
			return -1;
		
		if (b.startsWith("Avg"))
			return 1;
		
		
		int diff = a.length() - b.length();
		if (diff!=0)
			return diff;
		
		return a.compareTo(b);
	}
	
	
	public String[] getNames(byte group) {
		if (group<0 || group>=names.length)
			return null;
		
		if (valid_names[group]==0)
			return null;
		
		String result[] = new String[valid_names[group]];
		for (int i=0; i<valid_names[group]; i++)
			result[i]=names[group][sorted_names[group][i]];
		
		return result;
	}
	
	
	public DataSet getData(byte group, int item, byte type) {
		if ( data==null || data[group]==null || 
				item>=sorted_names[group].length || data[group][sorted_names[group][item]]==null )
			return null;
		return data[group][sorted_names[group][item]][type];
	}
	
	public DataSet getData(byte group, String item, byte type) {
		if ( data==null || data[group]==null )
			return null;		
		for (int i=0; i<names[group].length; i++)
			if (item.equals(names[group][i]))
				return data[group][i][type];		
		return null;
	}
	
	
	/*
	 * Remove a specific DataSet
	 */
	public void removeData(byte group, int item, byte type) {
		if ( data==null || data[group]==null || 
				sorted_names[group].length==0 || data[group][sorted_names[group][item]]==null )
			return;
		data[group][sorted_names[group][item]][type] = null;
	}


	public GregorianCalendar getEnd() {
		return end;
	}


	public GregorianCalendar getStart() {
		return start;
	}

	public byte getSmtStatus() {
		return smtStatus;
	}

	public void setSmtStatus(byte smtStatus) {
		this.smtStatus = smtStatus;
	}

	public void setCpuWeight(float cpuWeight) {
		this.cpuWeight = cpuWeight;
	}
	
	
	public int getNumLssrad() {
		if (lssrad_ref1==null)
			return 0;
		return lssrad_ref1.size();
	}
	
	public void addLssrad(String ref1, String srad, String mem, String cpu) {
		if (lssrad_ref1==null) {
			lssrad_ref1 = new Vector<String>();
			lssrad_srad = new Vector<String>();
			lssrad_mem = new Vector<String>();
			lssrad_cpu = new Vector<String>();		
		}
		
		lssrad_ref1.add(ref1);
		lssrad_srad.add(srad);
		lssrad_mem.add(mem);
		lssrad_cpu.add(cpu);	
	}
	
	public String getLssradRef1(int num) {
		if (lssrad_ref1==null || num>lssrad_ref1.size())
			return null;
		return lssrad_ref1.elementAt(num);
	}
	
	public String getLssradSrad(int num) {
		if (lssrad_srad==null || num>lssrad_srad.size())
			return null;
		return lssrad_srad.elementAt(num);
	}
	
	public String getLssradMem(int num) {
		if (lssrad_mem==null || num>lssrad_mem.size())
			return null;
		return lssrad_mem.elementAt(num);
	}
	
	public String getLssradCpu(int num) {
		if (lssrad_cpu==null || num>lssrad_cpu.size())
			return null;
		return lssrad_cpu.elementAt(num);
	}
	
	public void addTextLabel(byte type, String label, String value) {
		if (type<0 || type>=NUM_TEXT)
			return;
		
		if (textLabel[type]==null) {
			textLabel[type] = new Vector<String>();
			textValue[type] = new Vector<String>();
		}
		textLabel[type].add(label);
		textValue[type].add(value);
	}
	
	public String getTextlabel(byte type, String label) {
		if (type<0 || type>=NUM_TEXT)
			return null;
		
		int n=0;
		while (n<textLabel[type].size() && !textLabel[type].elementAt(n).equals(label))
			n++;
		if (n<textLabel[type].size())
			return textValue[type].elementAt(n);
		
		return null;
	}
	
	public String[] getTextlabelNum(byte type, int num) {
		if (type<0 || type>=NUM_TEXT)
			return null;
		
		if (num<0 || num>=textLabel[type].size())
			return null;
		
		String result[] = new String[2];
		
		result[0] = textLabel[type].elementAt(num);
		result[1] = textValue[type].elementAt(num);
		
		return result;
	}

}
