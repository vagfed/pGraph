package pGraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

//import javax.swing.JFrame;
import javax.swing.JPanel;
//import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;

public class TimePanel extends JPanel implements MouseInputListener, MouseMotionListener {

	private static final long serialVersionUID = 8544039225571683710L;
	
	private GregorianCalendar minTime, maxTime;
	private GregorianCalendar minActive, maxActive;
	private GregorianCalendar minZoom, maxZoom;
	
	
	private int activeFrom, activeTo;		// position of active time frame
	private int zoomFrom, zoomTo;			// position og knobs
	
	//private GregorianCalendar paintGC = new GregorianCalendar(); // global to save memory!

	
	private final static byte 		ACTIVE_HEIGHT = 20;
	private final static byte 		KNOB_HALF = 5;				// distance of knob's edge from knob's mid-pixel
	private final static Font 		font = new Font("SansSerif", Font.BOLD, 10);
	private float 					pattern[]=new float[2];
	
	private byte 					activeLimit = NONE;
	private boolean 				pressed = false;
	private int 					pressedPoint, pressedZoomFrom, pressedZoomTo;
	//private byte selectedLimit = NONE;
	private static final byte NONE  = 0;
	private static final byte START = 1;
	private static final byte END   = 2;
	private static final byte BOTH	= 3;
	
	
	public TimePanel () {
		super();
		addMouseListener(this);
		addMouseMotionListener(this);
		minTime=maxTime=null;
		
		minActive=maxActive=null;
		minZoom=maxZoom=null;

		pattern[0]=10f;
		pattern[1]=5f;
		
		setTime(new GregorianCalendar(1970,0,1), new GregorianCalendar());		// Just a start value
		//setTime(new GregorianCalendar(), new GregorianCalendar());		// Just a start value
		
	}
	
	
	public Dimension getPreferredSize() {
		Graphics g = getGraphics();
		if (g==null)
			return null;
		
		FontMetrics metrics = g.getFontMetrics(font);
		
		return new Dimension(3*metrics.stringWidth("2008-12-12 00:00:00"),ACTIVE_HEIGHT+4*metrics.getAscent());

	}

	
	public void setTime(GregorianCalendar minTime, GregorianCalendar maxTime) {
		this.minTime=minTime;
		this.maxTime=maxTime;
		
		minActive=(GregorianCalendar)minTime.clone();
		maxActive=(GregorianCalendar)maxTime.clone();
		minZoom=(GregorianCalendar)minTime.clone();
		maxZoom=(GregorianCalendar)maxTime.clone();
	}
	
	public void setActive(GregorianCalendar minActive, GregorianCalendar maxActive) {
		this.minActive = minActive;
		this.maxActive = maxActive;
		minZoom=(GregorianCalendar)minActive.clone();
		maxZoom=(GregorianCalendar)maxActive.clone();
	}
	
	
	public void paint(java.awt.Graphics g) {
		String			from, to;
		int				fromSize, toSize;
		//int				labelSize;
		FontMetrics 	metrics;
		//int x,y;
	
		
		// Start painting panel
		super.paintComponent(g);
		

		
		// Get size
		int xsize = getWidth();		// horizontal size	
		int ysize = getHeight();	// vertical size
		
		if (xsize == 0)
			return;
		
		long min = minTime.getTimeInMillis();
		long max = maxTime.getTimeInMillis();
		
		// Keep KNOB_HALF space before and after sliding area
		
		activeFrom = KNOB_HALF + (int)( 1f * (xsize-KNOB_HALF*2) * (minActive.getTimeInMillis()-min) / (max-min)  );
		activeTo   = KNOB_HALF + (int)( 1f * (xsize-KNOB_HALF*2) * (maxActive.getTimeInMillis()-min) / (max-min)  );
		
		zoomFrom   = KNOB_HALF + (int)( 1f * (xsize-KNOB_HALF*2) * (minZoom.getTimeInMillis()-min)   / (max-min)  );
		zoomTo     = KNOB_HALF + (int)( 1f * (xsize-KNOB_HALF*2) * (maxZoom.getTimeInMillis()-min)   / (max-min)  );
	
		
		
		Graphics2D g2 = (Graphics2D)g;
		
		// Set font
		g2.setFont(font);
		
		
		// Draw background
		g.setColor(new Color(157, 156, 153));
		g.drawLine(KNOB_HALF, ysize/2-2, xsize-KNOB_HALF, ysize/2-2);
		g.setColor(new Color(242, 241, 233));
		g.drawLine(KNOB_HALF, ysize/2-1, xsize-KNOB_HALF, ysize/2-1);
		g.drawLine(KNOB_HALF, ysize/2, xsize-KNOB_HALF, ysize/2);
		g.setColor(new Color(240, 237, 224));
		g.drawLine(KNOB_HALF, ysize/2+1, xsize-KNOB_HALF, ysize/2+1);
		g.setColor(new Color(255, 255, 255));
		g.drawLine(KNOB_HALF, ysize/2+2, xsize-KNOB_HALF, ysize/2+2);
		
		
		// Draw active timeframe
		//g.setColor(new Color(0, 0, 0));
		g.setColor(Color.BLACK);
		g.drawLine(activeFrom, ysize/2-1, activeTo, ysize/2-1);
		g.drawLine(activeFrom, ysize/2, activeTo, ysize/2);
		g.drawLine(activeFrom, ysize/2+1, activeTo, ysize/2+1);
		g.drawLine(activeFrom, ysize/2-5, activeFrom, ysize/2+5);
		g.drawLine(activeFrom-1, ysize/2-5, activeFrom-1, ysize/2+5);
		g.drawLine(activeFrom+1, ysize/2-5, activeFrom+1, ysize/2+5);
		g.drawLine(activeTo, ysize/2-5, activeTo, ysize/2+5);
		g.drawLine(activeTo-1, ysize/2-5, activeTo-1, ysize/2+5);
		g.drawLine(activeTo+1, ysize/2-5, activeTo+1, ysize/2+5);
		
	
		g.setColor(new Color(0,0,0,30));
		g.fillRect(activeFrom, 0, activeTo-activeFrom+1, ysize);
		
		/*
	    
	    // get metrics from the graphics
    	FontMetrics metrics = graphics.getFontMetrics(font);
	    // get the height of a line of text in this font and render context
	    int hgt = metrics.getHeight();
	    // get the advance of my text in this font and render context
	    int adv = metrics.stringWidth(text);
	    // calculate the size of a box to hold the text with some padding.
	    Dimension size = new Dimension(adv+2, hgt+2);
		
		*/
		
		if (minTime==null)
			return;
		
		int asc;

		from = gcToString(minActive);
		to   = gcToString(maxActive);
		metrics = g2.getFontMetrics(font);
		fromSize = metrics.stringWidth(from);
		toSize = metrics.stringWidth(to);
		//labelSize = metrics.stringWidth(" active ");
		asc = metrics.getAscent()+2;
		
		g.setColor(Color.BLACK);
		if (fromSize > activeFrom) {
			g2.drawString(from, 0, ysize/2+ACTIVE_HEIGHT/2+asc);
			//g2.drawLine(fromSize, ysize/2+ACTIVE_HEIGHT/2, activeFrom, ysize/2);
		} else if (activeTo+toSize>xsize && activeFrom>xsize-toSize-20) {
			g2.drawString(from, xsize-toSize-20-fromSize, ysize/2+ACTIVE_HEIGHT/2+asc);
			g2.drawLine(xsize-toSize-20, ysize/2+ACTIVE_HEIGHT/2, activeFrom, ysize/2);
		} else
			g2.drawString(from, activeFrom-fromSize, ysize/2+ACTIVE_HEIGHT/2+asc);
		
		if (activeTo+toSize>xsize)
			g2.drawString(to, xsize-toSize, ysize/2+ACTIVE_HEIGHT/2+asc);
		else if (fromSize>activeFrom && fromSize+20>activeTo) {
			g2.drawString(to, fromSize+20, ysize/2+ACTIVE_HEIGHT/2+asc);
			g2.drawLine(fromSize+20, ysize/2+ACTIVE_HEIGHT/2, activeTo, ysize/2);
		} else
			g2.drawString(to, activeTo, ysize/2+ACTIVE_HEIGHT/2+asc);			
		
		
		// Show zoom time frame if defined
		if (zoomFrom>=0) {			
			g.setColor(Color.RED);
			g.drawLine(zoomFrom, ysize/2-1, zoomTo, ysize/2-1);
			g.drawLine(zoomFrom, ysize/2, zoomTo, ysize/2);
			g.drawLine(zoomFrom, ysize/2+1, zoomTo, ysize/2+1);
			
			from = gcToString(minZoom);
			to   = gcToString(maxZoom);
			metrics = g2.getFontMetrics(font);
			fromSize = metrics.stringWidth(from);
			toSize = metrics.stringWidth(to);
			//height = metrics.getHeight();
			
			if (fromSize > zoomFrom)
				g2.drawString(from, 0, ysize/2-ACTIVE_HEIGHT/2-2);
			else if (zoomTo+toSize>xsize && zoomFrom>xsize-toSize-20) {
				g2.drawString(from, xsize-toSize-20-fromSize, ysize/2-ACTIVE_HEIGHT/2-2);
				g2.drawLine(xsize-toSize-20, ysize/2-ACTIVE_HEIGHT/2-2, zoomFrom, ysize/2);
			} else
				g2.drawString(from, zoomFrom-fromSize, ysize/2-ACTIVE_HEIGHT/2-2);
			
			if (zoomTo+toSize>xsize)
				g2.drawString(to, xsize-toSize, ysize/2-ACTIVE_HEIGHT/2-2);
			else if (fromSize>zoomFrom && fromSize+20>zoomTo) {
				g2.drawString(to, fromSize+20, ysize/2-ACTIVE_HEIGHT/2-2);
				g2.drawLine(fromSize+20, ysize/2-ACTIVE_HEIGHT/2-2, zoomTo, ysize/2);
			} else
				g2.drawString(to, zoomTo, ysize/2-ACTIVE_HEIGHT/2-2);			
		}
		
		
		paintKnob(g,zoomFrom,ysize/2,activeLimit==START);
		paintKnob(g,zoomTo,ysize/2,activeLimit==END);
		
		
		
		
		
	}

	
/*	
	private String getTime(int t) {
		String str;
		int v;
		
		paintGC.setTimeInMillis( 
				minTime.getTimeInMillis() + 
				(long)(1f*t*(maxTime.getTimeInMillis()-minTime.getTimeInMillis())/getWidth()) );
		
			
		v=paintGC.get(Calendar.YEAR);
		str = v+"-";
		
		v=paintGC.get(Calendar.MONTH)+1;
		if (v<10)
			str+="0";
		str=str+v+"-";
			
		v=paintGC.get(Calendar.DAY_OF_MONTH); 
		if (v<10)
			str+="0";
		str=str+v+" ";
			
		v=paintGC.get(Calendar.HOUR_OF_DAY);
		if (v<10)
			str+="0";
		str=str+v+":";
		
		v=paintGC.get(Calendar.MINUTE);
		if (v<10)
			str+="0";
		str=str+v+":";
		
		v=paintGC.get(Calendar.SECOND);
		if (v<10)
			str+="0";
		str=str+v;
		
		return str;
	}
*/
	
	
	protected String gcToString(GregorianCalendar gc) {
		int h,m,s;
		int Y,M,D;
		
		String str;
			
		Y=gc.get(Calendar.YEAR);
		M=gc.get(Calendar.MONTH)+1;
		D=gc.get(Calendar.DAY_OF_MONTH); 			
			
		h=gc.get(Calendar.HOUR_OF_DAY);
		m=gc.get(Calendar.MINUTE);
		s=gc.get(Calendar.SECOND);
		

		str = Y+"-";
		if (M<10)
			str+="0";
		str=str+M+"-";
		if (D<10)
			str+="0";
		str=str+D+" ";
			
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

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		int x 		= e.getX();
		int xsize 	= getWidth();
		
		if (x<0 || x>xsize) 
			return;
		

		
		if (zoomFrom>=0 && !pressed) {
			if (zoomFrom-KNOB_HALF<=x && x<=zoomFrom+KNOB_HALF)
				activeLimit=START;
			else if (zoomTo-KNOB_HALF<=x && x<=zoomTo+KNOB_HALF)
				activeLimit=END;
			else if (zoomFrom<x && x<zoomTo)
				activeLimit=BOTH;
			else
				activeLimit=NONE;
		}
		
		if (pressed) {
			if (activeLimit == START) {
				if (x<zoomTo)
					zoomFrom = x;
				if (zoomFrom<KNOB_HALF)
					zoomFrom = KNOB_HALF;
			} else if (activeLimit == END) {
				if (x>zoomFrom)
					zoomTo = x;
				if (zoomTo>xsize-KNOB_HALF)
					zoomTo = xsize-KNOB_HALF;
			} else if (activeLimit == BOTH) {
				zoomFrom = pressedZoomFrom + (x - pressedPoint);
				zoomTo = pressedZoomTo + (x - pressedPoint);
				if (zoomFrom<KNOB_HALF)
					zoomFrom=KNOB_HALF;
				if (zoomTo>xsize-KNOB_HALF)
					zoomTo=xsize-KNOB_HALF;
			}
			
			long delta = maxTime.getTimeInMillis()-minTime.getTimeInMillis();
			minZoom.setTimeInMillis(minTime.getTimeInMillis()+(long)(1f*delta*(zoomFrom-KNOB_HALF)/(getWidth()-2*KNOB_HALF)) );
			maxZoom.setTimeInMillis(minTime.getTimeInMillis()+(long)(1f*delta*(zoomTo-KNOB_HALF)/(getWidth()-2*KNOB_HALF)) );
		}


		repaint();	
	}
	
	/*
	public static void main(String[] args) {
		try {
			// Set native look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Create the frame 
			JFrame viewer = new JFrame();
			//viewer.setSize(1000,150);
			//viewer.setPreferredSize(new Dimension(1000,150));
	
			// Calculate the screen size 
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
			// Center frame on the screen 
			Dimension frameSize = viewer.getSize();
			if (frameSize.height > screenSize.height)
					frameSize.height = screenSize.height;
			if (frameSize.width > screenSize.width)
					frameSize.width = screenSize.width;
			viewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
			
			
			TimePanel tp = new TimePanel();
			GregorianCalendar start = new GregorianCalendar(2008, 0, 1, 10, 0, 0);
			GregorianCalendar end   = new GregorianCalendar(2008, 0, 4, 1, 2, 3);
			tp.setTime(start, end);
			
			GregorianCalendar z1 = new GregorianCalendar(2008, 0, 2, 12, 30, 0);
			GregorianCalendar z2   = new GregorianCalendar(2008, 0, 3, 11, 20, 30);
			tp.setActive(z1,z2);
			
			
			
			JPanel viewerContent = new JPanel();
			viewerContent.setLayout(new java.awt.BorderLayout());
			viewerContent.add(tp,"Center");
			viewer.setContentPane(viewerContent);
					
			viewer.setVisible(true);
			
		} catch (Throwable exception) {
			System.err.println("Exception occurred in main() of TimePanel");
			exception.printStackTrace(System.out);
		}
	}
	*/

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
		int m 		= e.getModifiers();
		
		if ((m & java.awt.event.MouseEvent.BUTTON1_MASK) == 0) 
			return;
		
		pressed = true;
		pressedPoint = e.getX();
		pressedZoomFrom = zoomFrom;
		pressedZoomTo = zoomTo;
	
		
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		pressed = false;		
	}
	
	private void paintKnob(Graphics g, int x, int y, boolean selected) {		
		g.setColor(new Color(181,196,205));
		g.drawLine(x-5, y-ACTIVE_HEIGHT/2+2, x-5, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(212,211,225));
		g.drawLine(x-4, y-ACTIVE_HEIGHT/2+2, x-4, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(255,255,255));
		g.drawLine(x-3, y-ACTIVE_HEIGHT/2+2, x-3, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(241,241,241));
		g.drawLine(x-2, y-ACTIVE_HEIGHT/2+2, x-2, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(225,225,235));
		g.drawLine(x-1, y-ACTIVE_HEIGHT/2+2, x-1, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(212,211,225));
		g.drawLine(x, y-ACTIVE_HEIGHT/2+2, x, y+ACTIVE_HEIGHT/2-2);
		g.drawLine(x+1, y-ACTIVE_HEIGHT/2+2, x+1, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(198,198,210));
		g.drawLine(x+2, y-ACTIVE_HEIGHT/2+2, x+2, y+ACTIVE_HEIGHT/2-2);
		g.drawLine(x+3, y-ACTIVE_HEIGHT/2+2, x+3, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(183,182,196));
		g.drawLine(x+4, y-ACTIVE_HEIGHT/2+2, x+4, y+ACTIVE_HEIGHT/2-2);
		g.setColor(new Color(119,136,146));
		g.drawLine(x+5, y-ACTIVE_HEIGHT/2+2, x+5, y+ACTIVE_HEIGHT/2-2);

		
		if (selected)
			g.setColor(Color.GREEN);
		else
			g.setColor(Color.ORANGE);
		g.drawLine(x-5, y-ACTIVE_HEIGHT/2+1, x+5, y-ACTIVE_HEIGHT/2+1);
		g.drawLine(x-4, y-ACTIVE_HEIGHT/2, x+4, y-ACTIVE_HEIGHT/2);
		g.drawLine(x-5, y+ACTIVE_HEIGHT/2-1, x+5, y+ACTIVE_HEIGHT/2-1);
		g.drawLine(x-4, y+ACTIVE_HEIGHT/2, x+4, y+ACTIVE_HEIGHT/2);
		
		
	}


	public GregorianCalendar getMinZoom() {
		return minZoom;
	}


	public GregorianCalendar getMaxZoom() {
		return maxZoom;
	}


	public GregorianCalendar getMinActive() {
		return minActive;
	}


	public GregorianCalendar getMaxActive() {
		return maxActive;
	}

}
