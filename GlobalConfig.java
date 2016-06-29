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

package pGraph;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GlobalConfig {
	
	private String	configFileName = null;
	
	
	class ColorData {
		Color	color;
		
		public ColorData(Color c) { color = c; }
		public ColorData(int r, int g, int b) { color = new Color(r,g,b); }
		
		public Color getColor() { return color; }
		
		public void parseColor(String label) {
			if (label==null)
				return;
			
			String items[] = label.split(",");
			for (int i=0; i<items.length; i++)
				items[i]=items[i].trim().toLowerCase();
			
			if (items.length==1) {
				// Label must be provided
				if (items[0].equals("black")) 		color = Color.BLACK;
				if (items[0].equals("blue")) 		color = Color.BLUE;
				if (items[0].equals("cyan")) 		color = Color.CYAN;
				if (items[0].equals("darkgray")) 	color = Color.DARK_GRAY;
				if (items[0].equals("gray")) 		color = Color.GRAY;
				if (items[0].equals("green")) 		color = Color.GREEN;
				if (items[0].equals("lightgray")) 	color = Color.LIGHT_GRAY;
				if (items[0].equals("magenta")) 	color = Color.MAGENTA;
				if (items[0].equals("orange")) 		color = Color.ORANGE;
				if (items[0].equals("pink")) 		color = Color.PINK;
				if (items[0].equals("red")) 		color = Color.RED;
				if (items[0].equals("white")) 		color = Color.WHITE;

				return;			
			}
			
			if (items.length==3) {
				// RGB in 0-255 range is expected
				int r,g,b;
				try { 
					r = Integer.parseInt(items[0]); 
					g = Integer.parseInt(items[1]); 
					b = Integer.parseInt(items[2]); 
					color = new Color(r,g,b);
				} catch (NumberFormatException nfe) {};
			}			
		}
	}
	
	
	class FontData {
		String 	name;
		int		style;
		int		size;
		
		public FontData(String name, int style, int size) {
			this.name = name;
			this.style = style;
			this.size = size;
		}
		
		public Font getFont() 				{ return new Font(name,style,size);	}
		
		public void parseFont(String label) {
			if (label==null)
				return;
			
			String items[] = label.split(",");
			if (items.length!=3)
				return;
			for (int i=0; i<items.length; i++)
				items[i]=items[i].trim().toLowerCase();
			
			if (items[0].equals("dialog")) 		name = items[0];
			if (items[0].equals("dialoginput")) name = items[0];
			if (items[0].equals("monospaced")) 	name = items[0];
			if (items[0].equals("serif")) 		name = items[0];
			if (items[0].equals("sanserif")) 	name = items[0];
			
			if (items[1].equals("plain")) 		style = Font.PLAIN;
			if (items[1].equals("bold")) 		style = Font.BOLD;
			if (items[1].equals("italic")) 		style = Font.ITALIC;
			if (items[1].equals("bolditalic")) 	style = Font.BOLD|Font.ITALIC;
						
			try { size = Integer.parseInt(items[2]); } catch (NumberFormatException nfe) {};					
		}
	}
	
	
	
	// Fonts to be used
	private FontData 		buttonFont  	= new FontData("SansSerif", Font.BOLD, 12);
	private FontData		panelDataFont	= new FontData("SansSerif", Font.BOLD, 12);
	private FontData		panelGridFont	= new FontData("SansSerif", Font.PLAIN, 10);
	private FontData		panelTitleFont	= new FontData("SansSerif", Font.BOLD, 12);
	private FontData		pointedDataFont	= new FontData("SansSerif", Font.PLAIN, 12);
	
	// Sizes
	private int				panelHeight		= 200;
	private int				textareaWidth	= 250;
	
	// Colors
	private ColorData		usColor 		= new ColorData(Color.RED);
	private ColorData		syColor 		= new ColorData(Color.GREEN);
	private ColorData		waColor 		= new ColorData(Color.BLUE);
	private ColorData		idColor 		= new ColorData(Color.YELLOW);
	private ColorData		graphBackColor	= new ColorData(Color.LIGHT_GRAY);
	private ColorData		seriesColor[]	= {
												new ColorData(Color.RED),
												new ColorData(Color.GREEN),
												new ColorData(Color.BLUE),
												new ColorData(Color.YELLOW),
												new ColorData(Color.CYAN)
											  };
	private ColorData		dataBackColor	= new ColorData(170,170,170);
	
	// Directory
	private File			workingDirectory = null;
	
	private int				maxTopProcs		 = 6000;
	private int				maxDisks		 = 100;
	
	
	public int	getMaxTopProcs()		{ return maxTopProcs; }
	public int	getMaxDisks()			{ return maxDisks; }
	public Font getButtonFont() 		{ return buttonFont.getFont(); }
	public Font getPanelDataFont() 		{ return panelDataFont.getFont(); }
	public Font getPanelGridFont() 		{ return panelGridFont.getFont(); }
	public Font getPanelTitleFont() 	{ return panelTitleFont.getFont(); }
	public Font getPointedDataFont() 	{ return pointedDataFont.getFont(); } 
	public int	getPanelHeight()		{ return panelHeight; }
	public int	getTextareaWidth()		{ return textareaWidth; }
	
	public Color getUsColor()			{ return usColor.getColor(); };
	public Color getSyColor()			{ return syColor.getColor(); };
	public Color getWaColor()			{ return waColor.getColor(); };
	public Color getIdColor()			{ return idColor.getColor(); };
	
	public Color getGraphBackColor()	{ return graphBackColor.getColor(); };
	public Color getDataBackColor()		{ return dataBackColor.getColor(); };
	
	public Color getLineColor(int lineno) {
		if (lineno<seriesColor.length)
			return seriesColor[lineno].getColor();
		return null;
	}
	
	public File getWorkingDirectory()	{ return workingDirectory; }
	
	
	
	
	public GlobalConfig(String name) {
		configFileName = name;
		readProperties();
	}
	
	
	public void readProperties() {
		Properties 	configFile = new Properties();
		
		try {
			 FileInputStream in = new FileInputStream(configFileName);
			 configFile.load(in);
			 in.close();
			
		} catch (IOException ioe) {
			return;
		}
		
		buttonFont.parseFont(configFile.getProperty("ButtonFont"));
		panelDataFont.parseFont(configFile.getProperty("PanelDataFont"));
		panelGridFont.parseFont(configFile.getProperty("PanelGridFont"));
		panelTitleFont.parseFont(configFile.getProperty("PanelTitleFont"));
		pointedDataFont.parseFont(configFile.getProperty("PointedDataFont"));
		
		try { panelHeight = Integer.parseInt(configFile.getProperty("GraphHeight")); } catch (NumberFormatException nfe) {};
		try { 
			int panelWidth = Integer.parseInt(configFile.getProperty("GraphWidth"));
			if (panelWidth>0)
				DataSet.SLOTS=panelWidth;
		} catch (NumberFormatException nfe) {};
		try { 
			textareaWidth = Integer.parseInt(configFile.getProperty("TextAreaWidth"));
			if (textareaWidth<250)
				textareaWidth=250;
		} catch (NumberFormatException nfe) {};
		
		try { 
			maxTopProcs = Integer.parseInt(configFile.getProperty("MaxTopProcs")); 
		} catch (NumberFormatException nfe) {};
		
		try { 
			maxDisks = Integer.parseInt(configFile.getProperty("MaxDisks")); 
		} catch (NumberFormatException nfe) {};
		
		
		//System.out.println(panelHeight);
		
		usColor.parseColor(configFile.getProperty("UserColor")); 
		syColor.parseColor(configFile.getProperty("SystemColor"));
		waColor.parseColor(configFile.getProperty("WaitColor"));
		idColor.parseColor(configFile.getProperty("IdleColor"));
		
		graphBackColor.parseColor(configFile.getProperty("GraphBackColor"));
		dataBackColor.parseColor(configFile.getProperty("DataBackColor"));
		
		seriesColor[0].parseColor(configFile.getProperty("Series1Color"));
		seriesColor[1].parseColor(configFile.getProperty("Series2Color"));
		seriesColor[2].parseColor(configFile.getProperty("Series3Color"));
		seriesColor[3].parseColor(configFile.getProperty("Series4Color"));
		seriesColor[4].parseColor(configFile.getProperty("Series5Color"));
		
		String workDir = configFile.getProperty("WorkingDirectory");
		if (workDir!=null) {
			workingDirectory = new File(workDir);
			if (!workingDirectory.canRead() || !workingDirectory.isDirectory())
				workingDirectory=null;
		} else
			workingDirectory = null;
		
	}
	



}
