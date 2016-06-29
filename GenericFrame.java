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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GenericFrame extends JFrame {

	private static final long serialVersionUID = -115448456728273692L;
	
	// Configuration
	private GlobalConfig configuration;
	

	
	// Graphic containers
	protected JPanel		graphicContainer;	// container of all data panels
	protected JPanel		boxPanel;			// container of all checkboxes
	protected JButton		allButton;			// check all CPUs
	protected JButton		noneButton;			// uncheck all CPUs
	protected JButton		saveButton;			// save picture
	protected JButton		zoomButton;			// make a zoom	
	protected JToggleButton	absMinMaxTButton;	// Absolute min&max enable/disable
	protected JCheckBox		sameMaxBox;			// Force same max value 
	protected boolean		forceSameMax=false;
	
	// Elements that change
	protected GenericPanel	dataPanel[]=null;	// objects with graphs
	protected String		dataPanelName[]=null;	// name of object
	protected JPanel 		graphPanel[]=null;	// panel containing cpu data
	protected JLabel		dataLimits[][]=null;// min-avg-max labels
	protected JLabel		pointedValues;		// Values pointed by mouse
	protected JCheckBox		checkBox[]=null;	// Data check boxes
	protected int			graphPanelIndex=0;	// graphPanel insertion point
	
	// Status before last reset: used to recreate checkbox&panels in the same state
	protected String		oldCheckBoxName[]=null;
	protected boolean		oldCheckBoxSelected[]=null;

	protected MyEventHandler myEventHandler = new MyEventHandler();
	
	protected int minZoom=-1;
	protected int maxZoom=DataSet.SLOTS;
	
	private int	XSIZE = DataSet.SLOTS+250;
	private int	YSIZE = 200;
	
	
	
	
	@SuppressWarnings("unchecked")
	protected Vector		separators = new Vector();			// contains JLabel in graph area
	@SuppressWarnings("unchecked")
	protected Vector		separatorIndex = new Vector();		// contains Integer with first graph index
	@SuppressWarnings("unchecked")
	protected Vector		separatorButton = new Vector();		// contains JToggleButton
	
	protected boolean		separatorPressed = false;			// used to avoid multiple changes
	
	private SumXYPanel[]	sumPanelList = null;				// list of panels that require feedback
	private AvgSumXYPanel[]	avgSumPanelList = null;				// list of panels that require feedback
	
	
	/*
	 * File Chooser
	 */
	class MyFileFilter extends FileFilter {
		private String[] extensions;
		private String description;
		
		public MyFileFilter (String ext) {
			this (new String[] {ext}, null);
		}
		
		public MyFileFilter (String[] exts, String descr ) {
			// Clone and lower case the extensions
			extensions = new String[exts.length];
			for (int i=exts.length-1; i>=0; i--) {
				extensions[i] = exts[i].toLowerCase();
			}
			// Make sure we have a valid description
			description = ( descr == null ? exts[0] + " files" : descr );
		}
		
		public boolean accept (File f) {
			// Always accept directories
			if (f.isDirectory())
				return true;
			
			// Check extensions
			String name = f.getName().toLowerCase();
			for (int i = extensions.length -1 ; i>=0; i--) {
				if (name.endsWith(extensions[i]))
					return true;
			}
			return false;
		}
		
		public String getDescription() {
			return description;
		}
	}
	
	
	/*
	 * Event handler
	 */
	class MyEventHandler implements java.beans.PropertyChangeListener,
									javax.swing.event.ChangeListener,
									java.awt.event.ActionListener,
									java.awt.event.ItemListener { 
		
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			
			if (evt.getPropertyName().equals("focus")) {
				pointedValues.setText((String)evt.getNewValue());
			}
			
			if (evt.getPropertyName().equals("minZoom")) {
				minZoom = ((Integer)(evt.getNewValue())).intValue();
				for (int i=0; i<dataPanel.length; i++)
					if (dataPanel[i] != null)
						dataPanel[i].setMinZoomBar(minZoom);
			}
			
			if (evt.getPropertyName().equals("maxZoom")) {
				maxZoom = ((Integer)(evt.getNewValue())).intValue();
				for (int i=0; i<dataPanel.length; i++)
					if (dataPanel[i] != null)
						dataPanel[i].setMaxZoomBar(maxZoom);
			}
			
			if (evt.getPropertyName().equals("focusBar")) {
				int fb = ((Integer)(evt.getNewValue())).intValue();
				for (int i=0; i<dataPanel.length; i++)
					if (dataPanel[i] != null)
						dataPanel[i].setFocusBar(fb);
			}			
		}
		
		public void stateChanged(javax.swing.event.ChangeEvent e) {			
			// If a checkbox state has changed, update the corresponding dataPanel
			for (int i=0; i<checkBox.length; i++)
				if (e.getSource() == checkBox[i]) {
					graphPanel[i].setVisible(checkBox[i].isSelected());
					if (!separatorPressed)
						hideShowSeparators(i);
					
					// Notify sumPanelList
					if (sumPanelList!=null) {
						for (int j=0; j<sumPanelList.length; j++)
							sumPanelList[j].panelIsSelected(dataPanel[i], checkBox[i].isSelected());					
					}
					
					// Notify avgSumPanelList
					if (avgSumPanelList!=null) {
						for (int j=0; j<avgSumPanelList.length; j++)
							avgSumPanelList[j].panelIsSelected(dataPanel[i], checkBox[i].isSelected());					
					}
					
					// If same max value is selected, recompute max
					if (forceSameMax) 
						alignMaxValue();
					
					return;
				}
			
			if (e.getSource() == sameMaxBox) {
				if (sameMaxBox.isSelected()) {
					if (!forceSameMax) { 
						alignMaxValue();
						forceSameMax = true;
					}
				} else {
					if (forceSameMax) {
						restoreMaxValue();
						forceSameMax = false;
					}
				}
			}
		}
		
		@SuppressWarnings({ "unchecked", "unchecked" })
		public void actionPerformed(java.awt.event.ActionEvent e) {
			// If "ALL" button has been pressed, select all check boxes
			if (e.getSource() == allButton) {
				for (int i=0; i<checkBox.length; i++)
					if (checkBox[i] != null)
						checkBox[i].setSelected(true);
				
				// Notify sumPanelList
				if (sumPanelList!=null) {
					for (int i=0; i<sumPanelList.length; i++)
						sumPanelList[i].selectAll();					
				}
				
				// Notify avgSumPanelList
				if (avgSumPanelList!=null) {
					for (int i=0; i<avgSumPanelList.length; i++)
						avgSumPanelList[i].selectAll();					
				}
				
				return;
			}

			// If "None" button has been pressed, deselect all check boxes
			if (e.getSource() == noneButton) {
				for (int i=0; i<checkBox.length; i++)
					if (checkBox[i] != null)
						checkBox[i].setSelected(false);
				
				// Notify sumPanelList
				if (sumPanelList!=null) {
					for (int i=0; i<sumPanelList.length; i++)
						sumPanelList[i].deselectAll();					
				}
				
				// Notify avgSumPanelList
				if (avgSumPanelList!=null) {
					for (int i=0; i<avgSumPanelList.length; i++)
						avgSumPanelList[i].deselectAll();					
				}
				
				return;
			}

			// If "Save" button has been pressed, print current data
			if (e.getSource() == saveButton) {
				//DataPrinter dp = new DataPrinter();
				//dp.saveData(allGraphsPanel);
				printFrame();
				return;
			}
				
			// If "Zoom" button has been pressed, make zoom
			if (e.getSource() == zoomButton) {
				// send zoom event. 
				// It will be managed and propagated by Viewer.
				
				// Skip if zoom is out of range
				if (minZoom>=0 && maxZoom<DataSet.SLOTS) {
					Vector v = new Vector();
					v.add(new Integer(minZoom));
					v.add(new Integer(maxZoom+1));
					firePropertyChange("zoomEvent", null, v);
				}	
				return;				
			}
			

			// Check if separator button
			for (int i=0; i<separatorButton.size(); i++) {
				if (e.getSource() == (JToggleButton)separatorButton.elementAt(i)) {
					separatorPressed(i);
				}
			}
			
		}

		@SuppressWarnings("unchecked")
		public void itemStateChanged(ItemEvent e) {
			
			// If absMinMaxTButton has been pressed, make propagate event
			if (e.getSource() == absMinMaxTButton) {
				int state = e.getStateChange();
				Vector v = new Vector();
				if (state == ItemEvent.SELECTED) {
					absMinMaxTButton.setText("Absolute Min&Max: ON");
					v.add(new Boolean(true));
				} else {
					absMinMaxTButton.setText("Absolute Min&Max: OFF");
					v.add(new Boolean(false));
				}
				firePropertyChange("errorBar", null, v);
			}
		};
	}


	public GenericFrame(String title, GlobalConfig config) {
		super(title);
		configuration=config;
		setup();
	}
	
	
	
	public void reset() {
		oldCheckBoxName = new String[checkBox.length];
		oldCheckBoxSelected = new boolean[checkBox.length];
		for (int i=0; i<checkBox.length; i++) {
			oldCheckBoxName[i] = checkBox[i].getText();
			oldCheckBoxSelected[i] = checkBox[i].isSelected();
		}
		
		dataPanel		=	null;	// objects with graphs
		dataPanelName	= 	null;
		graphPanel		=	null;	// panel containing cpu data
		dataLimits		=	null;// min-avg-max labels
		checkBox		=	null;	// Data check boxes
		graphPanelIndex	=	0;	// graphPanel insertion point
		minZoom			=	-1;
		maxZoom			=	DataSet.SLOTS;
		setup();
	}
	
	
	private int oldCheckBoxIndex(String s) {
		if (oldCheckBoxName == null)
			return -1;
		
		int i=0;
		
		while (i<oldCheckBoxName.length) {
			if (s.equals(oldCheckBoxName[i]))
				return i;
			i++;
		}
		
		return -1;
	}
	
	

	/*
	 * Create JFrame structure with buttons, panels and so on...
	 */
	protected void setup() {
		// Load YSIZE from config file
		YSIZE = configuration.getPanelHeight();
		
		// Load XSIZE from config file
		XSIZE = DataSet.SLOTS+configuration.getTextareaWidth();
		
		// Panel containing all graph Panels
		graphicContainer = new JPanel();
		graphicContainer.setLayout(new GridBagLayout());
		graphicContainer.setAlignmentY(0.5F);
			
		// Scroll Panel containing all graphicContainer
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportView(graphicContainer);
			
		// Create pointed values
		pointedValues = new JLabel();
		pointedValues.setFont(configuration.getPointedDataFont());
		pointedValues.setText("Current: ");
			
		// Create panel containing pointed values + scrollPane
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new java.awt.BorderLayout());
		dataPanel.add(pointedValues,"North");
		dataPanel.add(scrollPane,"Center");
			
		// Create panel containing all check boxes
		boxPanel = new javax.swing.JPanel();
		BoxLayout boxLayout = new javax.swing.BoxLayout(boxPanel, BoxLayout.Y_AXIS);
		boxPanel.setLayout(boxLayout);
			
		// Create scrollpanel containing check boxes
		scrollPane = new javax.swing.JScrollPane();
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportView(boxPanel);
		scrollPane.setPreferredSize(new java.awt.Dimension(220, 1));
			
		// Create panel containing buttons
		JPanel buttonPanel = new javax.swing.JPanel();
		//BoxLayout buttonLayout = new javax.swing.BoxLayout(buttonPanel, BoxLayout.Y_AXIS);
		//buttonPanel.setLayout(buttonLayout);
		buttonPanel.setLayout(null);
		buttonPanel.setPreferredSize(new java.awt.Dimension(220, 107)); //83
			
		// Create buttons and insert them in panel
		allButton = new JButton();		
		allButton.setName("Check All");
		allButton.setText("Check All");
		//allButton.setMaximumSize(new java.awt.Dimension(100, 25));
		//allButton.setPreferredSize(new java.awt.Dimension(100, 25));
		//allButton.setMinimumSize(new java.awt.Dimension(100, 25));
		allButton.setFont(configuration.getButtonFont());
		allButton.addActionListener(myEventHandler);
		allButton.setBounds(2, 2, 107, 25);
		buttonPanel.add(allButton, "All");
			
		noneButton = new JButton();
		noneButton.setName("Uncheck All");
		noneButton.setText("Uncheck All");
		//noneButton.setMaximumSize(new java.awt.Dimension(100, 25));
		//noneButton.setPreferredSize(new java.awt.Dimension(100, 25));
		//noneButton.setMinimumSize(new java.awt.Dimension(100, 25));
		noneButton.setFont(configuration.getButtonFont());
		noneButton.addActionListener(myEventHandler);
		noneButton.setBounds(111, 2, 107, 25);
		buttonPanel.add(noneButton, "None");
			
		saveButton = new JButton();
		saveButton.setName("Export");
		saveButton.setText("Export");
		//saveButton.setMaximumSize(new java.awt.Dimension(100, 25));
		//saveButton.setPreferredSize(new java.awt.Dimension(100, 25));
		//saveButton.setMinimumSize(new java.awt.Dimension(100, 25));
		saveButton.setFont(configuration.getButtonFont());
		saveButton.addActionListener(myEventHandler);
		saveButton.setBounds(2, 29, 107, 25);
		buttonPanel.add(saveButton, "Save");
			
		zoomButton = new JButton();
		zoomButton.setName("Zoom");
		zoomButton.setText("Zoom");
		//zoomButton.setMaximumSize(new java.awt.Dimension(100, 25));
		//zoomButton.setPreferredSize(new java.awt.Dimension(100, 25));
		//zoomButton.setMinimumSize(new java.awt.Dimension(100, 25));
		zoomButton.setFont(configuration.getButtonFont());
		zoomButton.addActionListener(myEventHandler);
		zoomButton.setBounds(111, 29, 107, 25);
		buttonPanel.add(zoomButton, "Zoom");
		
		absMinMaxTButton = new JToggleButton();
		absMinMaxTButton.setName("Absolute Min&Max");
		absMinMaxTButton.setText("Absolute Min&Max: ON");
		absMinMaxTButton.setSelected(true);
		absMinMaxTButton.setFont(configuration.getButtonFont());
		absMinMaxTButton.addItemListener(myEventHandler);
		absMinMaxTButton.setBounds(12, 56, 196, 25);
		buttonPanel.add(absMinMaxTButton, "Absolute Min&Max");
		
		sameMaxBox = new JCheckBox(); 
		sameMaxBox.setSelected(false);
		sameMaxBox.addChangeListener(myEventHandler);
		sameMaxBox.setText("Same max value");
		sameMaxBox.setFont(configuration.getButtonFont());
		sameMaxBox.setBounds(12, 85, 196, 14);
		buttonPanel.add(sameMaxBox, "sameMaxBox");
	
		// Create panel containing buttons and checkboxes
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new java.awt.BorderLayout());
		controlPanel.add(buttonPanel,"North");
		controlPanel.add(scrollPane,"Center");
			
		// Create panel containing all stuff
		JPanel framePanel = new JPanel();
		framePanel.setLayout(new java.awt.BorderLayout());
		framePanel.add(controlPanel,"West");
		framePanel.add(dataPanel,"Center");
			
		setContentPane(framePanel);
		
		validate();
							
	}
	
	/*
	 * Create a graphics file with frame's content
	 */
	protected void printFrame() {
		File file;
		int i;
		
		if (graphicContainer==null)
			return;
			
		JFileChooser openFileDialog = new JFileChooser();
		openFileDialog.setName("SaveFile");
		String cvs[] = new String[] {"csv"};
		String png[] = new String[] {"png"};	
		MyFileFilter cvsFilter = new MyFileFilter(cvs, "Semicolumn separated files (*.csv)");
		MyFileFilter pngFilter = new MyFileFilter(png, "PNG pictures (*.png)");
		openFileDialog.addChoosableFileFilter(cvsFilter);
		openFileDialog.addChoosableFileFilter(pngFilter);		
		
		//openFileDialog.setFileFilter(new MyFileFilter(png, "PNG pictures (*.png)"));

		int rc=openFileDialog.showSaveDialog(this);
		FileFilter chosenFilter = openFileDialog.getFileFilter();
		
		if (rc==JFileChooser.APPROVE_OPTION)
			file = openFileDialog.getSelectedFile();
		else
			return;
			
		String name = file.getName();
		
		
		// Hide all zoom bars
		for (i=0; i<dataPanel.length; i++)
			dataPanel[i].setHideZoomBars(true);
		
		
		if (chosenFilter==pngFilter) {
			if ( !name.toLowerCase().endsWith(".png") )
				file=new File(file.getPath()+".png");
			createPNG(file);
		} else if (chosenFilter==cvsFilter) {
			if ( !name.toLowerCase().endsWith(".csv") )
				file=new File(file.getPath()+".csv");
			createCVS(file);
		} else {
			// Default to PNG
			if ( !name.toLowerCase().endsWith(".png") )
				file=new File(file.getPath()+".png");
			createPNG(file);
		}
		
			
		// Create PNG 
		//createPNG(file);
		
		// Show all zoom bars
		for (i=0; i<dataPanel.length; i++)
			dataPanel[i].setHideZoomBars(false);		
	}
	
	
	private void createCVS(File file){
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(outStream);
			
			int i,j;
			
			// Create headers
			pw.print("Date;");
			for (i=0; i<graphPanel.length; i++) {
				if (graphPanel[i].isVisible()) {
					pw.print(dataPanel[i].getCSVNames(dataPanelName[i]));
				}
			}
			pw.println();
			
			// Dump data
			for (i=0; i<DataSet.SLOTS; i++) {
				// Date & Time
				pw.print(dataPanel[0].getCSVTime(i));
				
				// Data
				for (j=0; j<graphPanel.length; j++) {
					if (graphPanel[j].isVisible()) {
						pw.print(dataPanel[j].getCSVData(i));
					}	
				}
				pw.println();
			}
			
			pw.flush();
			pw.close();
			outStream.close();
			
		} catch (IOException ioe) {
			return;			
		}
		return;
	}
	
	
	private void createPNG(File file) {
		try {
			BufferedImage bi = new BufferedImage(graphicContainer.getWidth(), graphicContainer.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			graphicContainer.paint(g2d);
				
			try {
				// Save as PNG
				javax.imageio.ImageIO.write(bi, "png", file);
			} catch (IOException e) {
			}
			
		} catch (OutOfMemoryError oome) {
			System.out.println(" Insufficient memory to create PNG output!");
			System.out.println(" Provide more memory to Java using the -Xmx flag.");
			System.out.println(" Example with 500 MB: java -Xmx500m -cp pGraph.jar pGraph.Viewer ");
		}	
	}


	/*
	public void printFrame(String fileName) {
		File file = new File(fileName);
		int  i;
		
		if (graphicContainer==null)
			return;
		
		// Hide all zoom bars
		for (i=0; i<dataPanel.length; i++)
			dataPanel[i].setHideZoomBars(true);
			
		// Create PNG 
		BufferedImage bi = new BufferedImage(graphicContainer.getWidth(), graphicContainer.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		graphicContainer.paint(g2d);
		
		try {
			// Save as PNG
			javax.imageio.ImageIO.write(bi, "png", file);
		} catch (IOException e) {
		}
		
		// Show all zoom bars
		for (i=0; i<dataPanel.length; i++)
			dataPanel[i].setHideZoomBars(false);
	}
	*/
	
	
	/*
	 * Make a zoom of all panels
	 */
	public void zoom() {
		int i;
		for (i=0; i<dataPanel.length; i++) 
			if (dataPanel[i] != null) {
				dataPanel[i].zoom();					
			}
		forceSameMax = false;
		sameMaxBox.setSelected(false);
	}
	
	
	/*
	 * TO BE DELETED SOON
	 */
	public void addGraph (GenericPanel p, String name, Color color[]) {
		addGraph(p,name);
	}
	
	
	public void addGraph (GenericPanel p, String name) {
		int i;
		
		// Add a new item to structures
		if (dataPanel==null) {
			dataPanel=new GenericPanel[1];
			dataPanel[0]=p;
			dataPanelName=new String[1];
			dataPanelName[0]=name;
		} else {
			GenericPanel newArray[] = new GenericPanel[dataPanel.length+1];
			String newArrayName[] = new String[dataPanel.length+1];
			for (i=0; i<dataPanel.length; i++) {
				newArray[i]=dataPanel[i];
				newArrayName[i]=dataPanelName[i];
			}
			newArray[newArray.length-1]=p;
			newArrayName[newArray.length-1]=name;
			dataPanel=newArray;
			dataPanelName=newArrayName;
		}		
		
		if (graphPanel==null) {
			graphPanel=new JPanel[1];
			graphPanel[0]=new JPanel();
		} else {
			JPanel newJPanel[] = new JPanel[graphPanel.length+1];
			for (i=0; i<graphPanel.length; i++)
				newJPanel[i]=graphPanel[i];
			newJPanel[newJPanel.length-1]=new JPanel();
			graphPanel=newJPanel;
		}
		
		if (checkBox==null) {
			checkBox=new JCheckBox[1];
			checkBox[0]=new JCheckBox();
		} else {
			JCheckBox newBox[] = new JCheckBox[checkBox.length+1];
			for (i=0; i<checkBox.length; i++)
				newBox[i]=checkBox[i];
			newBox[newBox.length-1]=new JCheckBox();
			checkBox=newBox;
		}	
		
		// Update sumPanelList if needed
		if (p instanceof SumXYPanel) {
			if (sumPanelList == null) {
				sumPanelList = new SumXYPanel[1];
				sumPanelList[0] = (SumXYPanel)p;
			} else {
				SumXYPanel newSumPanelList[] = new SumXYPanel[sumPanelList.length+1];			
				for (i=0; i<sumPanelList.length; i++)
					newSumPanelList[i] = sumPanelList[i];					
				newSumPanelList[newSumPanelList.length-1] = (SumXYPanel)p;
				sumPanelList = newSumPanelList;
			}
		}
		
		// Update avgSumPanelList if needed
		if (p instanceof AvgSumXYPanel) {
			if (avgSumPanelList == null) {
				avgSumPanelList = new AvgSumXYPanel[1];
				avgSumPanelList[0] = (AvgSumXYPanel)p;
			} else {
				AvgSumXYPanel newAvgSumPanelList[] = new AvgSumXYPanel[avgSumPanelList.length+1];			
				for (i=0; i<avgSumPanelList.length; i++)
					newAvgSumPanelList[i] = avgSumPanelList[i];					
				newAvgSumPanelList[newAvgSumPanelList.length-1] = (AvgSumXYPanel)p;
				avgSumPanelList = newAvgSumPanelList;
			}
		}
		
		/*
		 * Setup graphics
		 */
		
		// Setup containing panel
		graphPanel[graphPanel.length-1].setPreferredSize(new java.awt.Dimension(XSIZE, YSIZE));
		graphPanel[graphPanel.length-1].setMaximumSize(new java.awt.Dimension(XSIZE, YSIZE));
		graphPanel[graphPanel.length-1].setLayout(new java.awt.BorderLayout());
		if ( (i=oldCheckBoxIndex(name)) >=0)
			graphPanel[graphPanel.length-1].setVisible(oldCheckBoxSelected[i]);	// was present before reset!
		
		// Title of panel
		JLabel label = new JLabel();
		label.setName(name);
		label.setText(name);
		label.setFont(configuration.getPanelTitleFont());
		label.setBorder(new javax.swing.border.EtchedBorder());
		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		label.setOpaque(true);
		graphPanel[graphPanel.length-1].add(label,"North");
		
		// Setup graphics
		p.setOpaque(true);
		p.addPropertyChangeListener(myEventHandler);
		graphPanel[graphPanel.length-1].add(p,"Center");
	
		GridBagConstraints constraints;		
		
		// Add graphics to graphicsContainer
		constraints = new java.awt.GridBagConstraints();
		constraints.gridx = 0;
		//constraints.gridy = graphPanel.length-1;
		constraints.gridy = graphPanelIndex++;
		constraints.fill = java.awt.GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;		
		graphicContainer.add(graphPanel[graphPanel.length-1],constraints);
		
		
		/*
		 * Setup checkbox
		 */
		 
		checkBox[checkBox.length-1].setSelected(true);
		if ( (i=oldCheckBoxIndex(name)) >=0)
			checkBox[checkBox.length-1].setSelected(oldCheckBoxSelected[i]);	// was present before reset!
		checkBox[checkBox.length-1].addChangeListener(myEventHandler);
		checkBox[checkBox.length-1].setText(name);
		boxPanel.add(checkBox[checkBox.length-1]);
	}
	
	@SuppressWarnings("unchecked")
	public void addSeparator(String name) {
		GridBagConstraints	constraints;	
		JLabel				label;
		
		
		// Add some space if not first separator
		if (separators.size()!=0) {
			label = new JLabel();
			label.setFont(new Font("",Font.BOLD,12));
			label.setPreferredSize(new java.awt.Dimension(200, 12));
			label.setMaximumSize(new java.awt.Dimension(200, 12));
			label.setText("");		
			label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			label.setOpaque(true);
			boxPanel.add(label);
		}
				
		label = new JLabel();
		label.setFont(new Font("",Font.BOLD,15));
		label.setName(name);
		label.setText(name);		
		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		label.setOpaque(true);
		label.setPreferredSize(new java.awt.Dimension(XSIZE, 30));
		label.setMaximumSize(new java.awt.Dimension(XSIZE, 30));
		label.setMinimumSize(new java.awt.Dimension(XSIZE, 30));
		label.setSize(new java.awt.Dimension(XSIZE, 30));
	
		constraints = new java.awt.GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = graphPanelIndex++;
		constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;		
		graphicContainer.add(label,constraints);
		separators.add(label);
		if (checkBox==null)
			separatorIndex.add(new Integer(0));
		else
			separatorIndex.add(new Integer(checkBox.length));
		
		/*
		label = new JLabel();
		label.setFont(new Font("",Font.BOLD,12));
		label.setPreferredSize(new java.awt.Dimension(200, 25));
		label.setMaximumSize(new java.awt.Dimension(200, 25));
		label.setName(name);
		label.setText("       " + name);		
		label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		label.setOpaque(true);
		boxPanel.add(label);
		*/
		
		JToggleButton jtb = new JToggleButton();
		jtb.setFont(configuration.getButtonFont());
		jtb.setPreferredSize(new java.awt.Dimension(200, 22));
		jtb.setMaximumSize(new java.awt.Dimension(200, 22));
		jtb.setName(name);
		jtb.setText(name);		
		jtb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jtb.setOpaque(true);
		jtb.setSelected(true);
		jtb.addActionListener(myEventHandler);
		separatorButton.add(jtb);
		boxPanel.add(jtb);
			
	}
	
	
	/*
	 * Upon a change in checkBox cb, check if related separator must be shown or not
	 */
	private void hideShowSeparators(int cb) {
		int from, to;
		int n;
		int i;
		int numSep;
		
		numSep = -1;	
		for (i=0; i<separatorIndex.size(); i++) {
			n = ((Integer)separatorIndex.elementAt(i)).intValue();
			if (n<=cb)
				numSep=i;
			else
				break;
		}
		if (numSep==-1)
			return;		// there are no separators!
		
		from = ((Integer)separatorIndex.elementAt(numSep)).intValue();
		if (numSep==separatorIndex.size()-1)
			to = checkBox.length-1;
		else
			to = ((Integer)separatorIndex.elementAt(numSep+1)).intValue()-1;
		
		boolean visible = false;
		
		for (i=from; i<=to && !visible; i++)
			if (checkBox[i].isSelected())
				visible = true;
		
		((JLabel)separators.elementAt(numSep)).setVisible(visible);		
		((JToggleButton)separatorButton.elementAt(numSep)).setSelected(visible);
	}
	
	
	/*
	 * A Separator has been pressed: setup checkboxes and update graphics
	 */
	private void separatorPressed(int n) {
		int from, to;
		boolean selected;
		
		from = ((Integer)separatorIndex.elementAt(n)).intValue();
		if (n==separatorIndex.size()-1)
			to = checkBox.length-1;
		else
			to = ((Integer)separatorIndex.elementAt(n+1)).intValue()-1;
		
		selected = ((JToggleButton)separatorButton.elementAt(n)).isSelected();
		
		separatorPressed = true;
		for (int i=from; i<=to; i++)
			checkBox[i].setSelected(selected);
		separatorPressed = false;
		
		hideShowSeparators(from);		
	}
	
	
	/*
	 * Activate/deactivate error bars (only for XYPanel!)
	 */
	public void showErrorBars(boolean show) {
		for (int i=0; i<dataPanel.length; i++)
			if (dataPanel[i]!=null && dataPanel[i] instanceof XYPanel)
				((XYPanel)dataPanel[i]).setShowErrorbars(show);
		absMinMaxTButton.setSelected(show);
	}
	
	
	/*
	 * Align all XYPanels (not SumXYPanels) to the same max value
	 */
	private void alignMaxValue() {
		float newMax = 0;
		float f;
		int i;
		
		for (i=0; i<dataPanel.length; i++) {
			if (dataPanel[i] instanceof XYPanel && 
					!(dataPanel[i] instanceof SumXYPanel) && 
					checkBox[i].isSelected() ) {
				f = ((XYPanel)dataPanel[i]).getMaxValue();
				if (f>newMax)
					newMax=f;
			}
		}
		
		for (i=0; i<dataPanel.length; i++) {
			if (dataPanel[i] instanceof XYPanel && 
					!(dataPanel[i] instanceof SumXYPanel) ) {
				((XYPanel)dataPanel[i]).setMaxValue(newMax);
			}
		}
	}
	
	/*
	 * restore all XYPanels (not SumXYPanels) maxValue
	 */
	private void restoreMaxValue() {
		int i;
		
		for (i=0; i<dataPanel.length; i++) {
			if (dataPanel[i] instanceof XYPanel && 
					!(dataPanel[i] instanceof SumXYPanel) ) {
				((XYPanel)dataPanel[i]).restoreMaxValue();
			}
		}
	}
	


}
