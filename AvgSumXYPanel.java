package pGraph;


public class AvgSumXYPanel extends XYPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3819142265041879154L;
	private final byte		BLOCK = 5;				// allocation blocks in sourceData
	private DataSet[][]		sourceData = null;		// set of data from which sums are computed
	private DataSet[]		weightData = null;		// set of data used as a weight
	private GenericPanel[]	sourcePanel = null;		// set of panels that trigger data sum
	private boolean[]		selected = null;		// where true, the corresponding panel is used to compute corresponding sums
	private int				numData = 0;			// number of valid entries in arrays

	//private boolean			smartComputing = false;	// true if linear interpolation of nearby valid data (only for CPU data with multiple files, not for lslparutil
													// false if exact data computation 
	
	
	public AvgSumXYPanel (PerfData perfData, GlobalConfig config) {
		super(config);
		this.perfData = perfData;
	}
	
	
	public void selectAll() {
		for (int i=0; i<numData; i++)
			selected[i] = true;
		computeData();
	}
	
	public void deselectAll() {
		for (int i=0; i<numData; i++)
			selected[i] = false;
		computeData();
	}
	
	/*
	 * If panel selection is changed, compute all data
	 */
	public void panelIsSelected(GenericPanel jp, boolean isSelected) {
		for (int i=0; i<numData; i++) {
			if (jp == sourcePanel[i]) {
				if (isSelected != selected[i]) {
					selected[i] = isSelected;
					computeData();
				}
				return;
			}
		}
	}
	
	
	public void zoom() {
		computeData();
		super.zoom();
	}
	
	
	/*
	 * Compute dataSet searching for valid data
	 
	private void computeDataSmart() {
		int i,j,slot;
		float sum;
		float v;
		
		// Compute data
		for (i=0; i<dataSet.length; i++) {
			
			if (dataSet[i]==null)
				continue;
			
			for (slot=0; slot<DataSet.SLOTS; slot++) {
				sum = -1;
				for (j=0; j<numData; j++) {
					if (selected[j] && sourceData[j][i]!=null) {
						v = getvalidData(sourceData[j][i],slot);
						if (v>=0) {
							if (sum>=0)
								sum += v;
							else
								sum = v;
						}
					}
				}
				if (sum>=0)
					dataSet[i].add(slot, sum);
			}
			dataSet[i].endOfData();
		}

	}
	
	*/
	
	
	private void computeDataExact () {
		int i,j,slot;
		float weightedSum, sum;
		float v,w;
		
		
		// Compute data
		for (i=0; i<dataSet.length; i++) {
			
			if (dataSet[i]==null)
				continue;
			
			for (slot=0; slot<DataSet.SLOTS; slot++) {
				sum = -1;
				weightedSum = -1;
				for (j=0; j<numData; j++) {
					if (selected[j] && sourceData[j][i]!=null && weightData[j]!=null) {
						v = sourceData[j][i].getValue(slot);
						w = weightData[j].getValue(slot);   
						if (v>=0 && w>=0) {
							if (weightedSum>=0)
								weightedSum += v*w;
							else
								weightedSum = v*w;
							if (sum>=0)
								sum += w;
							else
								sum = w;
						}
					}
				}
				if (sum>0)
					dataSet[i].add(slot, weightedSum/sum);
				//else if (sum==0)
				//	dataSet[i].add(slot, 0);
			}
			dataSet[i].endOfData();
		}

	}
	
	
	
	/*
	 * Compute dataSet values from sourceData
	 */
	public void computeData () {
		int i,j;
		
		// Check if this is first computation
		if (dataSet == null) {
			dataSet = new DataSet[sourceData[0].length];
			for (i=0; i<dataSet.length; i++) {
				// Look for the name, if any
				for (j=0; j<sourceData.length; j++)
					if (sourceData[j][i]!=null)
						break;
				if (j<sourceData.length)
					dataSet[i] = new DataSet(sourceData[0][i].getName());
				else
					dataSet[i]=null;	// no valid input data found
			}
		} else {
			for (i=0; i<dataSet.length; i++)
				if (dataSet[i]!=null)
					dataSet[i].reset();
		}
		
		// Compute data
		/*
		if (smartComputing)
			computeDataSmart();
		else
			computeDataExact();
		*/
		computeDataExact();
		
		computeMaxValue();
	}
	
	
	public void addPanel (DataSet ds[], DataSet we, GenericPanel jp) {		
		int i;
		
		if (sourceData == null) {
			// Very first dataSet: allocate structures
			sourceData = new DataSet[BLOCK][ds.length];	
			weightData = new DataSet[BLOCK];
			for (i=0; i<ds.length; i++) {
				sourceData[0][i] = ds[i];
			}
			weightData[0] = we;
			sourcePanel = new GenericPanel[BLOCK];
			sourcePanel[0] = jp;
			selected = new boolean[BLOCK];
			selected[0] = true; 		// by default a new added panel is selected	
			numData = 1;
			return;
		} 
		
		// Check if ds has the correct number of entries
		if (ds.length != sourceData[0].length) {
			System.out.println("SumXYPanel: unexpected number of DataSet values");
			return;
		}
		
		// Check if arrays are full
		if (numData == sourceData.length) {
			DataSet[][] 	oldSourceData = sourceData;
			DataSet[]		oldWeightData = weightData;
			GenericPanel[]	oldSourcePanel = sourcePanel;
			boolean[]		oldSelected = selected;
			int 			j;
			
			sourceData = new DataSet[oldSourceData.length + BLOCK][oldSourceData[0].length];
			weightData = new DataSet[oldSourceData.length + BLOCK];
			sourcePanel = new GenericPanel[oldSourceData.length + BLOCK];
			selected = new boolean[oldSourceData.length + BLOCK];
			for (i=0; i<oldSourceData.length; i++) {
				for (j=0; j<oldSourceData[0].length; j++)
					sourceData[i][j] = oldSourceData[i][j];
				weightData[i] = oldWeightData[i];
				sourcePanel[i] = oldSourcePanel[i];
				selected[i] = oldSelected[i];
			}
		}
		
		// Add new info
		for (i=0; i<ds.length; i++)
			sourceData[numData][i] = ds[i];
		weightData[numData] = we;
		sourcePanel[numData] = jp;
		selected[numData] = true; 		// panels are always added as selected
		
		numData++;
	}
	
	
/*	
	private float linear(int x, int x1, int x2, float y1, float y2) {
		float f;
		
		f = 1f*(y2-y1)/(x2-x1)*x+(1f*x2*y1-1f*x1*y2)/(x2-x1);
		
		return f;
		
	}
*/

	
	
/*	
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
*/
	
	
/*
	public void setSmartComputing(boolean smartComputing) {
		this.smartComputing = smartComputing;
	}
*/	
}