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
 * Created on Jan 3, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JPanel;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class GenericPanel extends JPanel implements MouseMotionListener {
	protected int 		minZoom, maxZoom; // Zoom bars position
	protected float		maxValue=0;		  // max value to be displayed
	protected float 	computedMaxValue = 0;	// previous max value when forced
	
	protected GlobalConfig	configuration;
	
	protected DataSet		dataSet[]=null;
	protected PerfData		perfData=null;
	//protected DataManager dm[]=null;
	//protected Color			color[]=null;
	
	protected  int		TEXT_SPACE = 12;
	private  int 		STEP = DataSet.SLOTS/6;
	
	
	protected int pointedX=-1;			// mouse x position
	protected int pointedY=-1;			// mouse y position
	
	protected boolean hideZoomBars = false;		// use it to hide bars during PNG creation
	
	// Data structures used to compute top average sets (dynamic set)
	protected int 			required_top		= 0;	// required number of top avg sets
	protected String		completeNames[]		= null;	// complete set of names
	protected DataSet		completeDataSet[] 	= null; // complete set of data from which top set is to be extracted
	protected String		overrideNames[]		= null;	// names that override dataSet names
	
	protected int validPointedSlot = -1;
	
	public GenericPanel(GlobalConfig config) {
		super();
		configuration = config;
		
		// Compute grid width
		FontMetrics metrics = getFontMetrics( configuration.getPanelGridFont() );
		int width = metrics.stringWidth( "XD00-00:00:00X" );
		STEP = width;
		//STEP=DataSet.SLOTS/6;
		TEXT_SPACE = configuration.getPanelGridFont().getSize() + 2;
		
		minZoom = -1 ;
		maxZoom = DataSet.SLOTS;		// do not paint zoom bars
		addMouseMotionListener(this);
	}
	

	
	public void setData (PerfData perfData, DataSet[] dataSet, String names[], int num_top) {
		this.perfData=perfData;
		completeDataSet=dataSet;
		completeNames=names;
		required_top=num_top;
		
		this.dataSet = new DataSet[required_top];
		overrideNames = new String[required_top];
		extractTopByAvg();
		computeMaxValue();
	}
	
	
	
	
	public void setData (PerfData perfData, DataSet[] dataSet) {
		this.perfData=perfData;
		this.dataSet=dataSet;
		computeMaxValue();
	}
	
	/*
	 * Recreate top data set from the complete list. dataSet&overrideNames must be already created!
	 */
	protected void extractTopByAvg() {
		if (completeDataSet==null)
			return;
		
		int i,j,k;
		
		for (i=0; i<dataSet.length; i++) {
			dataSet[i]=null;
			overrideNames[i]=null;
		}
		
		float avg;
		
		for (i=0; i<completeDataSet.length; i++) {
			if (completeDataSet[i]==null)
				continue;
			avg = completeDataSet[i].getAvg();
			j=0;
			while (j<dataSet.length && dataSet[j]!=null && avg<dataSet[j].getAvg())
				j++;
			if (j>=dataSet.length)
				continue;	// avg is greater that top sets
			for (k=dataSet.length-1; k>j; k--) {
				dataSet[k]=dataSet[k-1];
				overrideNames[k]=overrideNames[k-1];
			}
			dataSet[j]=completeDataSet[i];
			overrideNames[j]=completeNames[i];
		}
	}

	
	

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		int m 		= e.getModifiers();
		int x 		= e.getX();
		int xsize 	= getWidth();
	
		// Do not accept values outside panel
		if (x < 0 || x > xsize-1)
			return;
	
		if ((m & java.awt.event.MouseEvent.BUTTON1_MASK) != 0 &&
			x < maxZoom)
				firePropertyChange("minZoom", null, new Integer(nearestValidPoint(x)));

		if ((m & java.awt.event.MouseEvent.BUTTON3_MASK) != 0 &&
			x > minZoom)
				firePropertyChange("maxZoom", null, new Integer(nearestValidPoint(x)));

		mouseMoved(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		int x 		= e.getX();
		int xsize 	= getWidth();
		
		pointedX = e.getX();
		pointedY = e.getY();
		
		// Do not accept values outside panel
		if (x < 0 || x > xsize -1)
			return;
			
		// Ask for the nearest valid pointed data
		validPointedSlot = nearestValidPoint(x);
		
		firePropertyChange("focusBar", null, new Integer(validPointedSlot));
		
		firePropertyChange("focus", null, getPointedData(validPointedSlot));
	}
	
	
	private int nearestValidPoint(int p) {
		int i,v;
		int result=-1;
		
		for (i=0; i<dataSet.length; i++) {
			if (dataSet[i]==null)
				continue;
			v  = dataSet[i].getNearestValidSlot(p);
			if (v<0)
				continue;
			if (result<0) {
				result = v;
				continue;
			}
			if (Math.abs(p-v)<Math.abs(p-result))
				result = v;
		}	
		return result;
	}
	
	/*
	 * Set min zoom bar.
	 */
	public void setMinZoomBar(int min) {
		minZoom = min;
		repaint();
	}

	/*
	 * Set max zoom bar.
	 */
	public void setMaxZoomBar(int max) {
		maxZoom = max;		
		repaint();
	}

	public void setFocusBar(int v) {
		validPointedSlot = v;		
		repaint();
	}



	
	/*
	 * Perform a zoom
	 */
	public void zoom() {
		minZoom=-1;
		maxZoom=DataSet.SLOTS;
		validPointedSlot=-1;
		extractTopByAvg();		// just in case this is a dynamic set
		computeMaxValue();
		repaint();
	}
	
	
	/*
	 * Draw limits
	 */
	protected void drawLimits(java.awt.Graphics g) {
		//g.setColor(java.awt.Color.lightGray);
		//g.setColor(java.awt.Color.lightGray.darker());
		g.setColor(configuration.getDataBackColor());
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
		g2.drawString("Limits: [ Min - Avg - Max ] Std Deviation",DataSet.SLOTS+3,(limitsFontSize+5)*3);
		

		for (int i=0; i<dataSet.length; i++) {
			if (dataSet[i]==null)
				continue;
			
			if (this instanceof CPUPerfPanel) {
				switch (i) {
					case	0:	g2.setColor(configuration.getUsColor()); break;
					case	1:	g2.setColor(configuration.getSyColor()); break;
					case	2:	g2.setColor(configuration.getWaColor()); break;
					case	3:	g2.setColor(configuration.getIdColor()); break;
					default:	g2.setColor(Color.BLACK); break;	// must never happen!!!!!
				}
			} else {
				//g2.setColor(color[i]);
				g2.setColor(configuration.getLineColor(i));
			}
			//g2.drawString(limitString(i),DataSet.SLOTS+3,70+15*i);
			g2.drawString(limitString(i),DataSet.SLOTS+3,(limitsFontSize+5)*(4+i));
		}		
	}
	
	
	private String limitString(int index) {
		String result;
		float v;
		float min,avg,max;
		
		DataSet ds = dataSet[index];
		
		if (overrideNames!=null && overrideNames[index]!=null)
			result = overrideNames[index] + ": [ ";
		else
			result = ds.getName() + ": [ ";
		
		min = ds.getMin();
		avg = ds.getAvg();
		max = ds.getMax();
		
		if (min>max && avg==0 && max==0)
			return result+"N/A ]";
		
		v = ds.getMin();
		if (v<0)
			result += "N/A";
		else
			result += Float.toString(v);
		result += " - ";
		v = ds.getAvg();
		if (v<0)
			result += "N/A";
		else
			result += Float.toString(v);
		result += " - ";
		v = ds.getMax();
		if (v<0)
			result += "N/A";
		else
			result += Float.toString(v);
		result += " ]";
		
		result += " " + ds.getDevStd();
		
		return result;
	}
	
	
	
	/*
	 * Draw grid
	 */
	protected void drawGrid (java.awt.Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		float pattern[]=new float[2];
		pattern[0]=10f;
		pattern[1]=5f;
				
		// Show horizontal lines
		g2.setPaint(Color.BLACK); 
		g2.setFont(configuration.getPanelGridFont());
		
		g2.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,5.0f,pattern,0));
		
		int ysize = getHeight()-TEXT_SPACE;		// vertical size
		float f;
		
		g2.drawLine(0,ysize/4,DataSet.SLOTS-1,ysize/4);
		g2.drawLine(0,ysize/2,DataSet.SLOTS-1,ysize/2);
		g2.drawLine(0,3*ysize/4,DataSet.SLOTS-1,3*ysize/4);	
			
		f = (int)(3*maxValue/4*100)/100f;
		g2.drawString(Float.toString(f),1,ysize/4-2);
		f = (int)(maxValue/2*100)/100f;
		g2.drawString(Float.toString(f),1,ysize/2-2);
		f = (int)(maxValue/4*100)/100f;
		g2.drawString(Float.toString(f),1,3*ysize/4-2);
		
		// Show vertical lines
		String s;
		FontMetrics metrics = getFontMetrics( configuration.getPanelGridFont() );
		int size;
		
		for (int i=STEP; i<DataSet.SLOTS; i+=STEP) {
			g2.drawLine(i,0,i,ysize);
			s=gcToString(slotToGc(i),false);
			size = metrics.stringWidth(s);
			g2.drawString(s,i-size/2,ysize+configuration.getPanelGridFont().getSize());
		}
			
	}
	
	
	/*
	 * Draw zoom bars
	 */
	protected void drawZoomBars (java.awt.Graphics g) {
		
		if (hideZoomBars)
			return;	
		
		int ysize = getHeight()-TEXT_SPACE;		// vertical size
		
		g.setColor(java.awt.Color.WHITE);
		g.drawLine(validPointedSlot,0,validPointedSlot,ysize-1);		
		
		// Draw zoom bars
		g.setColor(java.awt.Color.black);
		if (minZoom >=0 )
			g.drawLine(minZoom,0,minZoom,ysize-1);
		if (maxZoom < DataSet.SLOTS )
			g.drawLine(maxZoom,0,maxZoom,ysize-1);			
	}
	
	/*
	 * For objects that will need it, compute max value;
	 */
	protected void computeMaxValue() {
		float v;
		
		maxValue=Float.MIN_VALUE;
		
		for (int i=0; i<dataSet.length; i++) {
			if (dataSet[i]==null)
				continue;
			if ( (v=dataSet[i].getMax()) > maxValue)
				maxValue=v;
		}
		
		computedMaxValue = maxValue;
	}
	
	
	/*
	 * Create string containing pointed values
	 */
	protected String getPointedData(int time) {
		if (time<0 || time>=DataSet.SLOTS)
			return " ";
		
		String s = "["+gcToString(slotToGc(time),true)+"]";
		float v,min,max;
		
		for (int i=0; i<dataSet.length; i++) {		
			if (dataSet[i]==null)
				continue;
			if (overrideNames!=null && overrideNames[i]!=null)
				s += " "+overrideNames[i]+"=";
			else
				s += " "+dataSet[i].getName()+"=";
			v = dataSet[i].getValue(time);
			min = dataSet[i].getAbsMin(time);
			max = dataSet[i].getAbsMax(time);
			if (v<0)
				s += "N/A";
			else {
				if (min<0 || max<0 || (min==v && v==max) )
					s += v;
				else 
					s += "["+min+" - "+v+" - "+max+"]";
			}
		}
			
		return s;
	}

	
	
	protected GregorianCalendar slotToGc(int time) {
		GregorianCalendar t= new GregorianCalendar();
		long delta = time * (perfData.getEnd().getTime().getTime() - perfData.getStart().getTime().getTime()) / (DataSet.SLOTS-1);
		t.setTime(new Date(perfData.getStart().getTime().getTime()+delta));
		return t;
	}
	
	
	protected String gcToString(GregorianCalendar gc, boolean year) {
		int h,m,s;
		int Y,M,D;
		
		String str;
			
		Y=gc.get(Calendar.YEAR);
		M=gc.get(Calendar.MONTH)+1;
		D=gc.get(Calendar.DAY_OF_MONTH); 			
			
		h=gc.get(Calendar.HOUR_OF_DAY);
		m=gc.get(Calendar.MINUTE);
		s=gc.get(Calendar.SECOND);
		
		str = "";
		
		if (year) {
			
			if (Y<1980) {
				// Date is just a counter from 1970-01-01
				str = "Day " + D + ": ";		// fix  for residency ONLY!!!!!!!
				
				
			} else {
				str = Y+"-";
				if (M<10)
					str+="0";
				str=str+M+"-";
				if (D<10)
					str+="0";
				str=str+D+" ";
			}
		} else if (D<10)
			str = "D0"+D+"-";
		else
			str = "D"+D+"-";
			
				
		if (h<10)
			str+="0";
		str=str+h+":";
		if (m<10)
			str+="0";
		str=str+m+":";
		if (s<10)
			str+="0";
		str=str+s;
		
		return str;
	}


	public void setHideZoomBars(boolean hideZoomBars) {
		this.hideZoomBars = hideZoomBars;
	}
	
	
	
	
	public String getCSVNames(String prefix) {
		String s="";
		
		for (int i=0; i<dataSet.length; i++) {		
			if (dataSet[i]==null)
				continue;
			if (overrideNames!=null && overrideNames[i]!=null)
				s += prefix+" "+overrideNames[i]+";";
			else
				s += prefix+" "+dataSet[i].getName()+";";
		}
		return s;
	}
	
	public String getCSVTime(int i) {
		if (i<0 || i>=DataSet.SLOTS)
			return null;
		
		// Dump time labels
		GregorianCalendar t= new GregorianCalendar();
		long start 	= perfData.getStart().getTime().getTime();
		long end	= perfData.getEnd().getTime().getTime();
		long delta	= i * (end - start) / (DataSet.SLOTS-1);
		t.setTime(new Date(start+delta));
		
		return(""+t.get(Calendar.YEAR)+"-"+(t.get(Calendar.MONTH)+1)+"-"+t.get(Calendar.DAY_OF_MONTH)+
					" "+
					t.get(Calendar.HOUR_OF_DAY)+":"+t.get(Calendar.MINUTE)+":"+t.get(Calendar.SECOND)+
					";");				
	}
	
	
	public String getCSVData(int slot) {
		float v;
		String s="";
		
		for (int i=0; i<dataSet.length; i++) {		
			if (dataSet[i]==null)
				continue;
			v = dataSet[i].getValue(slot);
			if (v<0)
				s += ";";
			else
				s += v+";";
		}
		return s;
	}
		

	public float getMaxValue() {
		return computedMaxValue;
	}
	
	public void setMaxValue(float f) {
		maxValue=f;
		repaint();
	}
	
	public void restoreMaxValue() {
		maxValue = computedMaxValue;
		repaint();
	}
	
	
}

