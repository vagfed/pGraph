package pGraph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public class StackPanel extends GenericPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8927812739647083606L;										
								
	//private Color theColors[] = new Color[255];
	private Color theColors[] = null;
	
	//private String	lparNames[] = null;
	private boolean fixedMax = false;
	private DataSet maxData = null;
	private int selectedSet = -1;				// set of data pointed by mouse
	

	
	
	private int 	lastPolyX[] = 	new int 	[4*(DataSet.SLOTS)];
	private float 	lastPolyY[] = 	new float 	[4*(DataSet.SLOTS)];
	private int 	currPolyX[] = 	new int 	[4*(DataSet.SLOTS)];
	private float 	currPolyY[] = 	new float 	[4*(DataSet.SLOTS)];
	private int 	numCurrPoly;
	private int 	numLastUpper;
	private int 	numCurrUpper;
	private int 	polyX[] = 		new int 	[4*(DataSet.SLOTS+1)];
	private int 	polyY[] = 		new int 	[4*(DataSet.SLOTS+1)];
	
	
	
	
	public StackPanel(GlobalConfig config) {
		super(config);
	}
	
	public void setData (PerfData perfData, DataSet[] dataSet, String names[]) {
		this.perfData=perfData;
		completeDataSet=dataSet;
		completeNames=names;
		required_top=dataSet.length;	// all data sets are required
		
		this.dataSet = new DataSet[required_top];
		overrideNames = new String[required_top];
		extractTopByAvg();
		computeMaxValue();
		createColors();
	}
	
	
	public void setMaxData(DataSet ds) {
		maxData = ds;
		computeMaxValue();
		//createColors();
	}
	
	public void setMaxData(float f) {
		if (f>0) {
			maxValue = f;
			fixedMax=true;
		}
		//createColors();
	}
		
	
	
	protected void computeMaxValue() {
		if (fixedMax)
			return;
		
		if (maxData!=null) {
			maxValue = maxData.getMax();
			return;
		}
		
		maxValue=0;
		
		int i,j;
		float m;
		int lslot, rslot;
		
		for (i=0; i<DataSet.SLOTS; i++) {
			m=0;
			for (j=0; j<dataSet.length; j++)  {
				if (dataSet[j].getValue(i)>0)
					m+=dataSet[j].getValue(i);
				else {			
					lslot = dataSet[j].getNearestValidSlotLeft(i);
					rslot = dataSet[j].getNearestValidSlotRight(i);
					
					if (lslot<0 || rslot<0 || lslot>=DataSet.SLOTS || rslot>=DataSet.SLOTS)
						continue;

					m+=linear(i,lslot,rslot,dataSet[j].getValue(lslot),dataSet[j].getValue(rslot));
				}
			}
			if (m>maxValue)
				maxValue=m;
		}
		
		/*
		for (i=0; i<dataSet.length; i++)
			if (dataSet[i]!=null)
				maxValue+=dataSet[i].getMax();
		*/
	}
	
	
	public void paint(java.awt.Graphics g) {
		// Start painting panel
		super.paintComponent(g);
		
		int ysize = getHeight() - TEXT_SPACE;		// vertical size		
		
		int i,j,k;

		
		// If maxData is known, gray out invalid data
		if (maxData != null) {
			//g.setColor(Color.gray);
			g.setColor(configuration.getGraphBackColor());
			g.fillRect(0,0,DataSet.SLOTS-1,ysize-1);
			
			numCurrPoly=0;
			polyX[numCurrPoly]=0;
			polyY[numCurrPoly]=ysize-1;
			numCurrPoly++;
			for (i=0; i<DataSet.SLOTS; i++) {
				if (maxData.getValue(i)<0)
					continue;
				polyX[numCurrPoly]=i;
				polyY[numCurrPoly]= ysize-1 - (int)(maxData.getValue(i)/maxValue * (ysize-1));
				numCurrPoly++;
			}
			polyX[numCurrPoly]=DataSet.SLOTS-1;
			polyY[numCurrPoly]=ysize-1;
			numCurrPoly++;
			g.setColor(Color.green);
			g.drawPolygon(polyX,polyY,numCurrPoly);
			g.fillPolygon(polyX,polyY,numCurrPoly);
		} else {
			g.setColor(Color.green);
			g.fillRect(0,0,DataSet.SLOTS-1,ysize-1);
		}
		
		
		// Detect selected series
		getPointedSet(pointedX,pointedY);
		
		
		// We start with a dummy Poly at zero value
		lastPolyX[0]=0;					lastPolyY[0]=ysize-1;
		lastPolyX[1]=DataSet.SLOTS-1;	lastPolyY[1]=ysize-1;
		numLastUpper=2;
		
		float v;
		int min,max;
		int rslot, lslot;
		
		int lastPolyUsed;	// index in lastPoly that was used 
		
		// Paint polygons
		for (i=0; i<dataSet.length; i++) {
						
			numCurrPoly=0;
			numCurrUpper=0;
			lastPolyUsed = 0;
			
			// Create missing data copying from last polygon
			min = dataSet[i].getNearestValidSlotRight(0);
			if (min<0 || min==DataSet.SLOTS)
				continue;		// No valid data for this data set
				
			if (min>0) {
				for (j=0; lastPolyX[j]<=min; j++) {
					currPolyX[numCurrPoly]=lastPolyX[j];
					currPolyY[numCurrPoly]=lastPolyY[j];
					numCurrPoly++;
					lastPolyUsed=j;
				}	
				if (lastPolyX[lastPolyUsed]!=min) {
					lslot = lastPolyX[lastPolyUsed];
					rslot = lastPolyX[lastPolyUsed+1];
					currPolyX[numCurrPoly]=min;
					currPolyY[numCurrPoly]=linear(min,lslot,rslot,lastPolyY[lastPolyUsed],lastPolyY[lastPolyUsed+1]);
					numCurrPoly++;
				}
			}
			
			// Create lines relates to real data
			max = dataSet[i].getNearestValidSlotLeft(DataSet.SLOTS-1);
			if (max<0 || max==DataSet.SLOTS)
				continue;		// No valid data for this data set
			
			
			
			for (j=min; j<=max; j++) {
				v = dataSet[i].getValue(j);
				if (v<0) {	
					k=lastPolyUsed;
					while (lastPolyX[k]<j)
						k++;
					if (lastPolyX[k]!=j)
						continue;
					lastPolyUsed=k;
					
					// Detect a linear interpolation of data
					lslot = dataSet[i].getNearestValidSlotLeft(j);
					rslot = dataSet[i].getNearestValidSlotRight(j);
					
					currPolyX[numCurrPoly]=j;
					currPolyY[numCurrPoly]=lastPolyY[k] - 
												1f * 
												linear(j,lslot,rslot,dataSet[i].getValue(lslot),dataSet[i].getValue(rslot)) /
												maxValue *
												(ysize-1);
					numCurrPoly++;					
				} else { 
					k=lastPolyUsed;
					while (k+1<numLastUpper && lastPolyX[k+1]<=j)
						k++;
					lastPolyUsed = k;
					
					currPolyX[numCurrPoly]=j;
					
					if (lastPolyX[k]==j)
						currPolyY[numCurrPoly]= lastPolyY[k] - 1f * v/maxValue * (ysize-1);
					else {
						lslot = lastPolyX[k];
						rslot = lastPolyX[k+1];
						currPolyY[numCurrPoly]= linear(j,lslot,rslot,lastPolyY[k],lastPolyY[k+1]) -
													1f * v/maxValue * (ysize-1);												
					}
					numCurrPoly++;
				}
				
			}
			
			// Create missing data
			if (max<DataSet.SLOTS-1) {
				if (lastPolyX[lastPolyUsed]==max) {
					currPolyX[numCurrPoly]=max;
					currPolyY[numCurrPoly]=lastPolyY[lastPolyUsed];
					numCurrPoly++;
				} else {
					currPolyX[numCurrPoly]=max;
					lslot = lastPolyX[lastPolyUsed];
					rslot = lastPolyX[lastPolyUsed+1];
					currPolyY[numCurrPoly]= linear(max,lslot,rslot,lastPolyY[lastPolyUsed],lastPolyY[lastPolyUsed+1]);
					numCurrPoly++;
				}
				
				for (j=lastPolyUsed+1; j<numLastUpper; j++) {
					currPolyX[numCurrPoly]=lastPolyX[j];
					currPolyY[numCurrPoly]=lastPolyY[j];
					numCurrPoly++;
				}
			}			
			
			numCurrUpper=numCurrPoly;
			
			// Create lower lines
			for (j=numLastUpper-1; j>=0; j--) {
				currPolyX[numCurrPoly]=lastPolyX[j];
				currPolyY[numCurrPoly]=lastPolyY[j];
				numCurrPoly++;				
			}
			
			// Setup final polygon
			for (j=0; j<numCurrPoly; j++) {
				polyX[j]=(int)currPolyX[j];
				polyY[j]=(int)currPolyY[j];
			}
			polyX[numCurrPoly]=0;
			polyY[numCurrPoly]=ysize-1;
			
			// Paint polygon
			if (i==selectedSet) 
				g.setColor(Color.red);
			else
				g.setColor(getColor(i));
			g.drawPolygon(polyX,polyY,numCurrPoly);
			g.fillPolygon(polyX,polyY,numCurrPoly);
			
			// Swap values
			int ia[] = currPolyX;
			currPolyX = lastPolyX;
			lastPolyX = ia;
			float fa[] = currPolyY;
			currPolyY = lastPolyY;
			lastPolyY = fa;
			numLastUpper=numCurrUpper;
		
		}
		
		// Draw zoom bars
		drawZoomBars(g);
		
		drawGrid(g);
		drawLimits(g);
		
	}
	

	
	
	
	
	
	/*
	 * Draw limits
	 */
	protected void drawLimits(java.awt.Graphics g) {
		g.setColor(java.awt.Color.lightGray);
		g.fillRect(DataSet.SLOTS,0,getWidth(),getHeight()-TEXT_SPACE);
		
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setFont(configuration.getPanelDataFont());
		int limitsFontSize = configuration.getPanelDataFont().getSize();
				
		g2.setColor(Color.BLACK);
		g2.drawString("From:",DataSet.SLOTS+3,limitsFontSize+5);
		g2.drawString(gcToString(perfData.getStart(),true),DataSet.SLOTS+50,limitsFontSize+5);
		g2.drawString("To:",DataSet.SLOTS+3,(limitsFontSize+5)*2);
		g2.drawString(gcToString(perfData.getEnd(),true),DataSet.SLOTS+50,(limitsFontSize+5)*2);	
		
		
		g2.setColor(Color.WHITE);
		g2.drawString("Limits: [ Min - Avg - Max ]",DataSet.SLOTS+3,(limitsFontSize+5)*3);	
		
		if (selectedSet>=0) {
			g2.setColor(Color.red);
			//g2.drawString("Selected: "+limitString(dataSet[selectedSet]),DataSet.SLOTS+3,60+20);
			g2.drawString("Selected: "+limitString(selectedSet),DataSet.SLOTS+3,(limitsFontSize+5)*4);
		}
		
		int lineY = (limitsFontSize+5)*5;
		int i;
		
		for (i=0; i<dataSet.length && lineY<getHeight()-TEXT_SPACE-(limitsFontSize+5); i++) {
			g2.setColor(getColor(i));
			//g2.setColor(new Color(colors[i][0],colors[i][1],colors[i][2]));
			//g2.drawString(limitString(dataSet[i]),DataSet.SLOTS+3,lineY);
			g2.drawString(limitString(i),DataSet.SLOTS+3,lineY);
			lineY+=(limitsFontSize+5);
		}
		
		if (lineY>=getHeight()-TEXT_SPACE-(limitsFontSize+5) && i<dataSet.length) {
			g2.setColor(Color.WHITE);
			g2.drawString("... others not shown ...",DataSet.SLOTS+3,lineY);
		}
		
		
	}
	
	
	private void getPointedSet(int x, int y) {
		// Sanity check: exit if mouse outside visible range
		if ( x<0 || x>=DataSet.SLOTS ||
				y<0 || y>getHeight() - TEXT_SPACE ) {
			selectedSet = -1;
			return;
		}
		
		// Adjust data cohordinates with graph cohordinates
		float value = maxValue-y*maxValue/(getHeight() - TEXT_SPACE);


		float f=0; 	// sum of last dataSets
		
		int j;
		int lslot, rslot;
			
		for (j=0; j<dataSet.length; j++)  {
			if (dataSet[j].getValue(x)>0)
				f+=dataSet[j].getValue(x);
			else {			
				lslot = dataSet[j].getNearestValidSlotLeft(x);
				rslot = dataSet[j].getNearestValidSlotRight(x);
				
				if (lslot<0 || rslot<0 || lslot>=DataSet.SLOTS || rslot>=DataSet.SLOTS)
					continue;

				f+=linear(x,lslot,rslot,dataSet[j].getValue(lslot),dataSet[j].getValue(rslot));
			}
			
			if (value<f) {
				selectedSet = j;
				return;
			}
		}
		
		// None found
		selectedSet = -1;
		
		

		
		/*
		
		
		int i;
		float f;
		

		
		//Just check
		if (last==null) {
			last = new float[dataSet.length];
			curr = new float[dataSet.length];
		}
		
		f=getValidData(dataSet[0],x);
		if (f>=0)
			curr[0]=f;
		else
			curr[0]=0;
		for (i=1; i<dataSet.length; i++) {
			f = getValidData(dataSet[i],x);
			if (f>=0)
				curr[i]=f+curr[i-1];
			else
				curr[i] = curr[i-1];
		}
		
		for (i=0; i<dataSet.length; i++)
			if (value<curr[i]) {
				selectedSet=i;
				return;
			}
		
		selectedSet=-1;
		return;
		
		*/
		
	}

	
	
	protected String getPointedData(int time) {
		if (time<0 || time>=DataSet.SLOTS)
			return " ";
		
		String s="["+gcToString(slotToGc(time),true)+"]";
		
		if (selectedSet>=0)
			//s += " "+dataSet[selectedSet].getName()+"="+dataSet[selectedSet].getValue(time);
			s += " "+overrideNames[selectedSet]+"="+dataSet[selectedSet].getValue(time);
		else
			s += " no selection";
			
		return s;
	}
	
	
	private Color getColor(int n) {
		//n = n % theColors.length;
		return theColors[n];
		//return theColors[(int)(1f*n/dataSet.length*theColors.length)];
	}
	
	private float linear(int x, int x1, int x2, float y1, float y2) {
		float f;
		
		f = 1f*(y2-y1)/(x2-x1)*x+(1f*x2*y1-1f*x1*y2)/(x2-x1);
		
		return f;
		
	}

	
	
	
	protected String limitString(int n) {
		String result;
		float v;
		
		result = overrideNames[n] + ": [ ";
		v = dataSet[n].getMin();
		if (v<0)
			result += "N/A";
		else
			result += Float.toString(v);
		result += " - ";
		v = dataSet[n].getAvg();
		if (v<0)
			result += "N/A";
		else
			result += Float.toString(v);
		result += " - ";
		v = dataSet[n].getMax();
		if (v<0)
			result += "N/A";
		else
			result += Float.toString(v);
		result += " ]";
		
		return result;
	}
	
	
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		
		// Detect the pointed area
		getPointedSet(pointedX,pointedY);
	
		firePropertyChange("focus", null, getPointedData(validPointedSlot));
	}
	
	
	private void createColors() {
		/*
		int i;
		
		if (dataSet.length > theColors.length)
			for (i=0; i<theColors.length; i++)
				theColors[i] = new Color(0,0,1f*(i+1)/theColors.length);
		else
			for (i=0; i<dataSet.length; i++)
				theColors[i] = new Color(0,0,1f*(i+1)/dataSet.length);
		*/
		
		int i;
		
		theColors = new Color[dataSet.length];
		for (i=0; i<dataSet.length; i++)
			theColors[i] = new Color(0,0,1f*(i+1)/dataSet.length);
		
	}
	
		
	

}
