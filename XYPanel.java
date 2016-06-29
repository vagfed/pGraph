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
 * Created on Oct 24, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class XYPanel extends GenericPanel {
	
	protected static final byte		VALUE 	= 0;
	protected static final byte		MIN		= 1;
	protected static final byte		MAX		= 2;
	
	private boolean 	showErrorBars = true;			// show error bars, depending on button
	private boolean 	forbidErrorBars = false;		// static decision, regardless button
	
	

	public void setShowErrorbars(boolean showErrorBars) {
		this.showErrorBars = showErrorBars;
		repaint();
	}
	
	public void setForbidErrorbars(boolean v) {
		forbidErrorBars = v;
		repaint();
	}

	public XYPanel(GlobalConfig config) {
		super(config);
	}

	private static final long serialVersionUID = -1515932918244424846L;
	private static final byte MIN_PIXEL_DIST = 8;
	private static final float DOT_SIZE = 6f;
	
	private static final float dash[] = {MIN_PIXEL_DIST/4f,MIN_PIXEL_DIST/4f};
	
	
	private static BasicStroke lineStroke = new BasicStroke(2.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	private static BasicStroke minMaxStroke = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	private static BasicStroke dotStroke = new BasicStroke(DOT_SIZE,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	private static BasicStroke connStroke = new BasicStroke(1f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1f,dash,MIN_PIXEL_DIST/4f);
	
	public void paint(java.awt.Graphics g) {
		// Start painting panel
		super.paintComponent(g);
		
		int ysize = getHeight() - TEXT_SPACE;		// vertical size
		
		//g.setColor(java.awt.Color.lightGray);
		g.setColor(configuration.getGraphBackColor());
		g.fillRect(0,0,DataSet.SLOTS,ysize);
		
		if (dataSet == null)
			return;
		
		for (int i=0; i<dataSet.length; i++)
			if (dataSet[i]!=null)
				drawLine(i,g);
		
		// Draw zoom bars
		drawZoomBars(g);
		
		drawGrid(g);
		drawLimits(g);
	}
	
	private void drawLine(int n, Graphics g) {
		
		if (!forbidErrorBars && showErrorBars) {
			drawLine(n,g,MIN);
			drawLine(n,g,MAX);
		}
		drawLine(n,g,VALUE);
		
		/*
		float value;
		int ysize = getHeight() - TEXT_SPACE -1;	// vertical size
		int goodx=-1;				// good value xy 
		int goody=-1;				// good value xy 
		int y;
				
		Graphics2D g2 = (Graphics2D)g;
		
		// Paint data
		g2.setColor(configuration.getLineColor(n));
		g2.setStroke(lineStroke);
		
		// Data in the slot
		for (int i=0; i<DataSet.SLOTS; i++) {
			value = dataSet[n].getValue(i);
			// If invalid data, skip
			if (value<0) 
				continue;
			
			y = ysize-1 - (int)(1f * value * (ysize-1) / (maxValue)) +1;

			if (goodx>=0 && i-goodx<MIN_PIXEL_DIST)
				g2.drawLine(goodx,goody,i,y);
			else {			
				if (goodx>=0) {
					g2.setStroke(dotStroke);
					g2.drawLine(goodx,goody,goodx,goody);
					g2.setStroke(connStroke);
					g2.drawLine(goodx,goody,i,y);
				}
				g2.setStroke(dotStroke);
				g2.drawLine(i,y,i,y);
				g2.setStroke(lineStroke);
			}
			
			goodx=i;
			goody=y;
		}
		
		if (goodx>=0) {
			g2.setStroke(dotStroke);
			g2.drawLine(goodx,goody,goodx,goody);
			g2.setStroke(lineStroke);
		}
		
		
		// Paint absolute limits only if available
		if (!dataSet[n].isAbsLimitActive())
			return;
		
		
		// Show error bars
		g2.setColor(configuration.getLineColor(n).darker().darker());
		float 	errMin, errMax;
		int		minY, maxY;
		for (int i=0; i<DataSet.SLOTS; i++) {
			value = dataSet[n].getValue(i);
				
			// If invalid data, skip
			if (value<0) 
				continue;
			
			errMin = dataSet[n].getAbsMin(i);
			errMax = dataSet[n].getAbsMax(i);
			
			minY = ysize-1 - (int)(1f * errMin * (ysize-1) / (maxValue)) +1;
			maxY = ysize-1 - (int)(1f * errMax * (ysize-1) / (maxValue)) +1;
			y    = ysize-1 - (int)(1f * value  * (ysize-1) / (maxValue)) +1;
			
			if (errMin<value)
				//g2.drawLine(i, minY, i, y+1);
				g2.drawLine(i, minY, i, minY);
			if (errMax>value)
				//g2.drawLine(i, y-1, i, maxY);
				g2.drawLine(i, maxY, i, maxY);
		}	
		
		*/
	}
	
	private void drawLine(int n, Graphics g, byte type) {
		float value;
		int ysize = getHeight() - TEXT_SPACE -1;	// vertical size
		int goodx=-1;				// good value xy 
		int goody=-1;				// good value xy 
		int y;
				
		Graphics2D  g2 = (Graphics2D)g;
		Color		color = configuration.getLineColor(n);
		
		if (type==MIN || type==MAX)
			color = color.darker().darker();
		g2.setColor(color);
		
		if (type==MIN || type==MAX)
			g2.setStroke(minMaxStroke);
		else
			g2.setStroke(lineStroke);
		
		// Data in the slot
		for (int i=0; i<DataSet.SLOTS; i++) {
			switch (type) {
				case VALUE:		value = dataSet[n].getValue(i); break;
				case MIN:		value = dataSet[n].getAbsMin(i); break;
				case MAX:		value = dataSet[n].getAbsMax(i); break;
				default:		return;
			}
			
			// If invalid data, skip
			if (value<0) 
				continue;
			
			y = ysize-1 - (int)(1f * value * (ysize-1) / (maxValue)) +1;

			if (goodx>=0 && i-goodx<MIN_PIXEL_DIST)
				g2.drawLine(goodx,goody,i,y);
			else {			
				if (goodx>=0) {
					g2.setStroke(dotStroke);
					g2.drawLine(goodx,goody,goodx,goody);
					g2.setStroke(connStroke);
					g2.drawLine(goodx,goody,i,y);
				}
				g2.setStroke(dotStroke);
				g2.drawLine(i,y,i,y);
				if (type==MIN || type==MAX)
					g2.setStroke(minMaxStroke);
				else
					g2.setStroke(lineStroke);
			}
			
			goodx=i;
			goody=y;
		}
		
		if (goodx>=0) {
			g2.setStroke(dotStroke);
			g2.drawLine(goodx,goody,goodx,goody);
			if (type==MIN || type==MAX)
				g2.setStroke(minMaxStroke);
			else
				g2.setStroke(lineStroke);
		}
	}
	
	
	


}
