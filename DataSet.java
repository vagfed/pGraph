/*
 * Created on Apr 21, 2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;


/**
 * @author Federico Vagnini
 */
public class DataSet implements Cloneable {
	
	public static int SLOTS = 500;			// time slots
	
	protected boolean		endOfInputData = false; // end of data input 
	
	private float 			data[]=null;	// value in each slot
	private float			min, avg, max;	// limits in this data set
	private int 			counter[]=null;	// counter[slot]: number of samples in each slot
	private String			name=null;		// name of this data set
	
	private boolean			absLimitActive = true;
	private float			absMin[]=null;	// absolute minimum for each slot
	private float			absMax[]=null;	// absolute maximum for each data slot
	
	// Standard deviation data structures
	private float			m, s, devstd, numSamples;
	
	/*
	public final static byte MIN=0;
	public final static byte AVG=1;
	public final static byte MAX=2;
	*/	
	
	private float			weight = 1;		// weight to apply to data
	
	
	/*
	 * Initialization of global data.
	 * If allocateData=false, do not allocate data structure
	 */
	public DataSet(String name) {
		this.name 		= name;
		endOfInputData 	= false;
		min=Float.POSITIVE_INFINITY;
		avg=0;
		max=Float.NEGATIVE_INFINITY;	
		
		m=s=devstd=-1;
		numSamples = 0;
	}
 
	
	
	/*
	 * Clear all stored data: parser is going to reload it for the SAME object
	 * Name does NOT change!
	 */
	 
	public void reset() {
		endOfInputData 	= false;
		data			= null;
		min				= Float.MAX_VALUE;
		avg				= 0;
		max				= Float.MIN_VALUE;
		counter			= null;
		//name			= null;		NAME DOES NOT CHANGE!!!!!!!!
		m=s=devstd=-1;
		numSamples = 0;
		absMax			= null;
		absMin			= null;
	}

	
	
	//public DataSet clone(String newName) throws CloneNotSupportedException {
	public DataSet clone(String newName) {
		DataSet result;
		
		//get initial bit-by-bit copy
		try {
			result = (DataSet)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	  	
		result.name=newName;
		return result;
	}
	
	
	/*
	 * Return true if this data set contains valid data
	 */
	public boolean isValid() {
		if (data!=null && endOfInputData)
			return true;
		return false;
	}
	
	
	/*
	 * Add a new value for a given slot
	 */
	public  void add(int slot, float value) {
		
		// Sanity checks
		if ( slot<0 || slot>=SLOTS || endOfInputData)
			return;		
		if (value<0)
			return;
		
		// if this is the first data sample, allocate data structures
		if (data==null) {
			data = new float[SLOTS];
			counter = new int[SLOTS];
			
			if (absLimitActive) {
				absMax = new float[SLOTS];
				absMin = new float[SLOTS];
				for (int i=0; i<SLOTS; i++) {
					absMin[i]=Float.MAX_VALUE;
					absMax[i]=Float.MIN_VALUE;
				}
			}
		} 
		
		// Store data
		data[slot] += value;
		counter[slot]++;
		if (value<min) min = value;
		if (value>max) max = value;
		avg+=value;
		
		// Compute standard deviation
		if (numSamples == 0) {
			m = value;
			s = 0;
			numSamples = 1;
		} else {
			float prev_m = m;
			float prev_s = s;
			numSamples++;
			m = prev_m + ( value - prev_m ) / numSamples;
			s = prev_s + ( value - prev_m ) * ( value - m );
		}
		
		// check if abs limit has changed
		if (absLimitActive) {
			if (value<absMin[slot])
				absMin[slot]=value;
			if (value>absMax[slot])
				absMax[slot]=value;
		}
	}
	

	/*
	 * Add a new value by slot. 
	 * IT MUST BE CALLED ONLY ONCE FOR EACH SLOT !!!!!
	 */
	public  void addBySlot(int slot, float value) {
		
		// Sanity checks
		if ( slot<0 || slot>=SLOTS || endOfInputData)
			return;	
		if (value<0)
			return;
		
		// if this is the first data sample, allocate data structures
		if (data==null) {
			data = new float[SLOTS];
			counter = new int[SLOTS];
			
			if (absLimitActive) {
				absMax = new float[SLOTS];
				absMin = new float[SLOTS];
				for (int i=0; i<SLOTS; i++) {
					absMin[i]=Float.MAX_VALUE;
					absMax[i]=Float.MIN_VALUE;
				}
			}
		}		
			
		data[slot]=value;
		counter[slot]=1;	// ONLY ONE FOR EACH SLOT!!
		if (value<min) min = value;
		if (value>max) max = value;
		avg+=value;
		
		// Compute standard deviation
		if (numSamples == 0) {
			m = value;
			s = 0;
			numSamples = 1;
		} else {
			float prev_m = m;
			float prev_s = s;
			numSamples++;
			m = prev_m + ( value - prev_m ) / numSamples;
			s = prev_s + ( value - prev_m ) * ( value - m );
		}
	}	
	
	
	/*
	 * Add a new value by slot, with abs max and min 
	 * IT MUST BE CALLED ONLY ONCE FOR EACH SLOT !!!!!
	 */
	public  void addBySlot(int slot, float value, float vmin, float vmax) {
		
		// Sanity checks
		if ( slot<0 || slot>=SLOTS || endOfInputData)
			return;	
		if (value<0)
			return;
		
		addBySlot(slot, value);
		
		absMin[slot]=vmin;
		absMax[slot]=vmax;
		
		if (vmin<min) min = vmin;
		if (vmax>max) max = vmax;
		
	}
	
	
	/*
	 * Close data input and compute averages
	 */
	public void endOfData() {
		
		// If no data received, do nothing!
		if (data==null)
			return;
		
		int i;
		int num=0;	// Number of global samples
		
		// Average values in slots 
		for (i=0; i<data.length; i++) {
			if (counter[i]==0)
				data[i]=-1;
			else
				data[i] = data[i] * weight / counter[i];
			num += counter[i];
		}
		
		// Compute averages
		avg = avg * weight / num;
		
		// Apply weight to min & max
		min = min * weight;
		max = max * weight;
		
		// Compute standard deviation
		devstd = (float)Math.sqrt( s / (numSamples-1));
		
		// Check abs limits
		if (absLimitActive)
			for (i=0; i<data.length; i++)
				if (data[i]<0) {
					absMin[i]=-1;
					absMax[i]=-1;
				} else {
					absMin[i] = absMin[i] * weight;
					absMax[i] = absMax[i] * weight;
				}
		
		// Record the end of data input
		endOfInputData = true;
	}
	
	
	/*
	 * Special end of data: multiple adds on same slot are treated as a single add
	 */
	public void endOfDataSpecial() {
		
		// If no data received, do nothing!
		if (data==null)
			return;
		
		int i;
		int num=0;	// Number of global samples
		
		min=Float.MAX_VALUE;
		max=Float.MIN_VALUE; 
		
		
		// Recompute min-max-stdDev
		for (i=0; i<data.length; i++) {
			if (counter[i]==0)
				data[i]=-1;
			else {
				counter[i]=1;
				num++;
				if (data[i]<min)
					min = data[i];
				if (data[i]>max)
					max = data[i];
				
				if (num==1) {
					m = data[i];
					s = 0;
					numSamples = 1;
				} else {
					float prev_m = m;
					float prev_s = s;
					numSamples++;
					m = prev_m + ( data[i] - prev_m ) / numSamples;
					s = prev_s + ( data[i] - prev_m ) * ( data[i] - m );
				}
			}
		}
		
		// Compute averages
		avg /= num;
		
		// Compute standard deviation
		devstd = (float)Math.sqrt( s / (numSamples-1));
		
		// No absolute values!!!
		absLimitActive=false;
		
		// Record the end of data input
		endOfInputData = true;	
	}
	
	
	/*
	 * Truncate data at two decimal digits if Integere does not truncate values
	 */
	private float twoDigits(float f) {
		if (f<0)
			return -1;
		
		
		if (f > 1e6)
			return f; 
		
		return 1f*(int)(f*100)/100;
		
		/*
		if (f>99f*Integer.MAX_VALUE)
			return f;
		
		return 1f*(int)(f*100)/100;	
		*/
	}
	
	
	/*
	 * Returns the data contained in a given slot.
	 */
	public float getValue(int slot) {
		if (data==null)
			return -1;
		if (slot<0 || slot >= SLOTS)
			return -1;
		if (counter[slot]==0)
			return -1;
		
		return twoDigits(data[slot]);
	}
	
	public float getAbsMin(int slot) {
		if (absMin==null)
		return -1;
	if (slot<0 || slot >= SLOTS)
		return -1;
	if (counter[slot]==0)
		return -1;
	
	return twoDigits(absMin[slot]);
	}
	
	public float getAbsMax(int slot) {
		if (absMax==null)
		return -1;
	if (slot<0 || slot >= SLOTS)
		return -1;
	if (counter[slot]==0)
		return -1;
	
	return twoDigits(absMax[slot]);
	}
	
	

	/*
	 * Returns the data limit of a given type.
	 */	
	public float getMin() { return twoDigits(min); }
	public float getAvg() { return twoDigits(avg); }
	public float getMax() { return twoDigits(max); }
	public float getDevStd() { return twoDigits(devstd); }
	
	

	/*
	 * Provide the nearest slot that holds valid data
	 */	
	public int getNearestValidSlot(int slot) {	
		if (data==null)
			return -1;
		
		int pre  = getNearestValidSlotLeft(slot);
		int post = getNearestValidSlotRight(slot);
		
		if (post == SLOTS) return pre;
		if (pre < 0) return post;
		if (slot-pre < post-slot)
			return pre;
		return post;				
	}
	
	/*
	 * Provide the nearest slot that holds valid data on the left
	 * (before the slot)
	 */	
	public int getNearestValidSlotLeft(int slot) {
		if (data==null)
			return -1;
		
		if (slot<0)
			return -1;
			
		if (slot>=SLOTS)
			slot=SLOTS-1;
		
		int pre=slot;
		
		while (pre>=0 && counter[pre]==0)
			pre--;

		return pre;
	}	
	
	/*
	 * Provide the nearest slot that holds valid data on the right
	 * (after the slot)
	 */	
	public int getNearestValidSlotRight(int slot) {
		if (data==null)
			return -1;
		
		if (slot<0)
			slot=0;
		if (slot>=SLOTS)
			return SLOTS;
		
		int post=slot;

		while (post<SLOTS && counter[post]==0)
			post++;

		return post;
	}		



	/*
	 * Name of this data set
	 */
	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}



	public void setWeight(float weight) {
		this.weight = weight;
	}



	public boolean isAbsLimitActive() {
		return absLimitActive;
	}



	public void setAbsLimitActive(boolean absLimitActive) {
		this.absLimitActive = absLimitActive;
	}

}
