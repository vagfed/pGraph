/*
 * Created on Oct 24, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package pGraph;


/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CPUPerfPanel extends GenericPanel {


	public CPUPerfPanel(GlobalConfig config) {
		super(config);
	}

	private static final long serialVersionUID = -1735174618520322946L;

	// DataManagers MUST be in order: US-SY-WA
	public void paint(java.awt.Graphics g) {
		// Start painting panel
		super.paintComponent(g);
		
		int ysize = getHeight() - TEXT_SPACE;		// vertical size
		int us, sy, wa;					// line heights
		int x=-1;						// last valid sample
		int usx=0,syx=0,wax=0;			// last valid y-cohordinates
		int px[]=new int[4];
		int py[]=new int[4];
		
		float v[]=new float[3];
		int j;
		
		// Paint the background in solid color for idle time
		//g.setColor(Color.yellow);
		g.setColor(configuration.getIdColor());
		g.fillRect(0,0,DataSet.SLOTS,ysize);
		
		// Paint data
		for (int i=0; i<DataSet.SLOTS; i++) {
			
			for (j=0; j<3; j++)
				v[j]=dataSet[j].getValue(i);
			
				
			// If invalid, draw a backround color line
			if (v[0]<0 || v[1]<0 || v[2]<0) {
				
				//g.setColor(java.awt.Color.lightGray);
				g.setColor(configuration.getGraphBackColor());
				g.drawLine(i,0,i,ysize-1);
				
				continue;
			}
			
			
			us = (int)(1f * v[0] * (ysize-1) / 100);
			sy = (int)(1f * v[1] * (ysize-1) / 100);
			wa = (int)(1f * v[2] * (ysize-1) / 100);
			
			// Manage bad data between samples
			if (i>0 && x>=0 && x<i-1) {
				//g.setColor(java.awt.Color.red);
				g.setColor(configuration.getUsColor());
				px[0]=x+1; py[0]=ysize;
				px[1]=x+1; py[1]=usx;
				px[2]=i-1; py[2]=ysize-us;
				px[3]=i-1; py[3]=ysize;
				g.drawPolygon(px,py,4);
				g.fillPolygon(px,py,4);

				//g.setColor(java.awt.Color.green);
				g.setColor(configuration.getSyColor());
				px[0]=x+1; py[0]=usx;
				px[1]=x+1; py[1]=syx;
				px[2]=i-1; py[2]=ysize-us-sy;
				px[3]=i-1; py[3]=ysize-us-1;
				g.drawPolygon(px,py,4);
				g.fillPolygon(px,py,4);
				
				//g.setColor(java.awt.Color.blue);
				g.setColor(configuration.getWaColor());
				px[0]=x+1; py[0]=-1;
				px[1]=x+1; py[1]=wax-1;
				px[2]=i-1; py[2]=wa-1;
				px[3]=i-1; py[3]=-1;
				g.drawPolygon(px,py,4);
				g.fillPolygon(px,py,4);
			}
			
			// Draw User%
			g.setColor(configuration.getUsColor());
			if (us > 0)
				g.drawLine(i,ysize-1,i,ysize-us);
			
			// Draw System%
			g.setColor(configuration.getSyColor());
			if (sy > 0)
				g.drawLine(i,ysize-us-1,i,ysize-us-sy);
			
			// Draw Wait%
			g.setColor(configuration.getWaColor());
			if (wa > 0)
				g.drawLine(i,0,i,wa);
				
			x=i;
			usx=ysize-us;
			syx=ysize-us-sy;
			wax=wa;
		}
		
		// Draw zoom bars
		drawZoomBars(g);
		
		maxValue = 100;    // Always show a 100% scale!
		
		drawGrid(g);
		drawLimits(g);
	}
	
}
