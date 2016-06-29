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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;


/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Viewer extends JFrame {
	
	private static final long serialVersionUID = -6031065953328192272L;
	
	private int PNGindex = 0;	// Index for PNG name generation
	
	public static String version = "Version 2.5.17";
	
	private GlobalConfig configuration = new GlobalConfig("pGraph.properties");
	
	
	public static final int VIEWER_HEIGHT = 400;
	public static final int VIEWER_WIDTH = 1007;
	
	private JMenuItem exitMenuItem 			= null;
	private JMenuItem openMMDirMenu 		= null;
	private JMenuItem openSMDirMenu 		= null;
	private JMenuItem openFileMenu 			= null;
	private JMenuItem aboutMenu	 			= null;
	private JMenuItem consoleMenu			= null;
	private JMenuItem openCfgMenu			= null;
	
	private About about = null;
	private Console console = null;
	
	private JPanel sliderPanel				= null;		// panel containing sliders
	private JPanel buttonPanel				= null;		// panel containing control buttons
	
	
	private int	FRAME_X;								// x size of data frame
	private int	FRAME_Y;								// y size of data frame

	
	private JMenu 			fileMenu 		= null;
	private JMenuBar 		menuBar 		= null;
	private JFileChooser 	openFileDialog 	= null;
	
	private JButton		zoomButton			= null;
	private TimePanel	timePanel			= null;
	
	/*
	private JButton 	plus[] 				= null;
	private JLabel 		time[] 				= null;
	private JLabel 		day[] 				= null;
	private JButton 	minus[] 			= null;
	private JSlider 	slider[] 			= null;
	*/
	
	private JLabel 		message 			= null;
	
	private JCheckBox	noDisks = null;
	
	private JToggleButton 	button[] = null;
	private final byte CPU 					= 0;
	private final byte RAM 					= 1;
	private final byte DISKBUSY				= 2;
	private final byte DISKRW				= 3;
	private final byte DISKXFER				= 4;
	private final byte DISKBLOCK			= 5;
	private final byte ESSRW				= 6;
	private final byte ESSXFER				= 7;
	private final byte ADAPTERRW			= 8;
	private final byte ADAPTERXFER			= 9;
	private final byte NETWORKRW			= 10;
	private final byte CHECKER				= 11;
	private final byte KERNEL				= 12;
	private final byte SYSCALL				= 13;
	private final byte RSERVICE				= 14;
	private final byte WSERVICE				= 15;
	private final byte RWTO					= 16;
	private final byte RWFAIL				= 17;
	private final byte WQTIME				= 18;
	private final byte QSIZE				= 19;
	private final byte SQFULL				= 20;
	private final byte ADRSERVICE			= 21;
	private final byte ADWSERVICE			= 22;
	private final byte ADWQTIME				= 23;
	private final byte ADQSIZE				= 24;
	private final byte ADSQFULL				= 25;
	private final byte WPAR_CPU				= 26;
	private final byte WPAR_MEM				= 27;
	private final byte WPAR_DISK			= 28;
	private final byte TOPCPU				= 29;
	private final byte TOPRAM				= 30;
	private final byte FS					= 31;
	private final byte AIO					= 32;
	private final byte FCRW					= 33;
	private final byte FCXFER				= 34;
	private final byte DACRW				= 35;
	private final byte DACXFER				= 36;
	private final byte DSKSERVICE			= 37;
	private final byte DSKWAIT				= 38;
	private final byte ESSSERVICE			= 39;
	private final byte ESSWAIT				= 40;
	private final byte ESSBUSY				= 41;
	private final byte PROCPOOL				= 42;
	private final byte RWSERVICE			= 43;
	private final byte SEA					= 44;
	private final byte PCPU					= 45;
	private final byte MEMDETAILS			= 46;
	private final byte BUTTONS				= 47;
	
	private GenericFrame perfFrame[] = new GenericFrame[BUTTONS];
	
	private JToggleButton 	textButton[] = null;
	private final byte	T_LSSRAD			= 0;
	private final byte	T_LPARSTATI			= 1;
	private final byte	T_VMSTATV			= 2;
	private final byte	T_BUTTONS			= 3;
	
	private TextFrame	textFrame[] = new TextFrame[T_BUTTONS];
	
	private Parser parser=null;
	
	private JProgressBar progressBar = new JProgressBar();
	private ParserManager parserManager = new ParserManager(this, configuration);
	
	//private GregorianCalendar begin=null;	// first second
	//private GregorianCalendar end=null;		// last second
	
	//private boolean settingUp = true; 		// avoid managing slider changes
	
	private boolean batchMode = false;		// true if Viewer in batch mode
	private String batchOutput = null;		// directory where to store batch output
	private GregorianCalendar batchBegin=null;	// date passed in cmd line for batch
	private GregorianCalendar batchEnd=null;	// date passed in cmd line for batch
	
	protected ViewerApplet applet = null;
	private byte	appletMode = NO_APPLET;
	private static final byte	NO_APPLET						= 0;
	private static final byte	SINGLE_FILE						= 1;
	private static final byte	MULTIPLE_FILES_SINGLE_HOST		= 2;
	private static final byte	MULTIPLE_FILES_MULTIPLE_HOSTS	= 3;
	private static final byte	LOCAL_FILESYSTEM				= 4;
	
	
	
	private MyEventHandler myEventHandler = null;

	
	
	class MyEventHandler implements ChangeListener, 
									ItemListener, 
									ActionListener, 
									ComponentListener, 
									WindowListener, 
									java.beans.PropertyChangeListener {
		@SuppressWarnings("unchecked")
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("zoomEvent")) {
				Vector v = (Vector)(evt.getNewValue());
				
				long delta = timePanel.getMaxActive().getTimeInMillis()-timePanel.getMinActive().getTimeInMillis();
				GregorianCalendar newBegin = new GregorianCalendar();
				GregorianCalendar newEnd = new GregorianCalendar();
				
				newBegin.setTimeInMillis(timePanel.getMinActive().getTimeInMillis() + 
						(long)(1f * ((Integer)v.elementAt(0)).intValue() * delta / (DataSet.SLOTS-1) ) );
				newEnd.setTimeInMillis(timePanel.getMinActive().getTimeInMillis() + 
						(long)(1f * ((Integer)v.elementAt(1)).intValue() * delta / (DataSet.SLOTS-1) ) );
				
				zoomEvent(newBegin, newEnd);
				
				/*
				int a = slider[0].getValue();
				int b = slider[1].getValue();
				
				// Avoid parsing before setting end zoom
				settingUp=true;
				slider[0].setValue( a + 
									(int)(1f * ((Integer)v.elementAt(0)).intValue() / (DataSet.SLOTS-1) * (b-a) )
								   );
				// Enable parsing 
				settingUp=false;
				slider[1].setValue( a + 
									(int)(1f * ((Integer)v.elementAt(1)).intValue() / (DataSet.SLOTS-1) * (b-a) )
								   );
				*/
				
			}
			
			if (evt.getPropertyName().equals("errorBar")) {
				Vector v = (Vector)(evt.getNewValue());
				Boolean b = (Boolean)(v.elementAt(0));
				
				for (int i=0; i<perfFrame.length; i++) {
					if (perfFrame[i]!=null)
						perfFrame[i].showErrorBars(b.booleanValue());
				}
			}

			
		};
		
		public void stateChanged(javax.swing.event.ChangeEvent e) {
			/*
			if (e.getSource() == slider[0])
				startSliderChanged(slider[0].getValue());
			else if (e.getSource() == slider[1]) 
				endSliderChanged(slider[1].getValue());	
			*/
		};
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == zoomButton) {
				zoomEvent(timePanel.getMinZoom(),timePanel.getMaxZoom());
			}
			
			if (e.getSource() == exitMenuItem) {
				if (applet==null)
					System.exit(0);
				else {
					closeAllFrames();
					parser=null;
					setVisible(false);	// Just hide Viewer.
				}
			}
			
			if (e.getSource() == openFileMenu) {
				openFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
				openFileDialog.setMultiSelectionEnabled(false);
				int rc=openFileDialog.showOpenDialog(openFileMenu);
				if (rc==JFileChooser.APPROVE_OPTION) {
					loadSingleFile(openFileDialog.getSelectedFile());
				}
			}
			
			if (e.getSource() == aboutMenu) {
				if (about==null)
					about = new About();
				about.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				about.setVisible(true);
			}
			
			if (e.getSource() == consoleMenu) {
				console.setVisible(true);
			}

			if (e.getSource() == openMMDirMenu) {
				openFileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				openFileDialog.setMultiSelectionEnabled(false);
				int rc=openFileDialog.showOpenDialog(openMMDirMenu);
				if (rc==JFileChooser.APPROVE_OPTION) {
					loadCECDirectory(openFileDialog.getSelectedFile());
				}
			}
			
			if (e.getSource() == openCfgMenu) {
				openFileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
				openFileDialog.setMultiSelectionEnabled(false);
				int rc=openFileDialog.showOpenDialog(openCfgMenu);
				if (rc==JFileChooser.APPROVE_OPTION) {
					loadConfigurationFile(openFileDialog.getSelectedFile());
				}
			}
			
			if (e.getSource() == openSMDirMenu) {
				openFileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				openFileDialog.setMultiSelectionEnabled(false);
				int rc=openFileDialog.showOpenDialog(openSMDirMenu);
				if (rc==JFileChooser.APPROVE_OPTION) {
					loadMultipleFilesSingleHost(openFileDialog.getSelectedFile());
				}
			}

			
			/*
			if (e.getSource() == minus[0])
				beginButtonChange(-1);
			if (e.getSource() == minus[1])
				endButtonChange(-1);
			if (e.getSource() == plus[0])
				beginButtonChange(+1);
			if (e.getSource() == plus[1])
				endButtonChange(+1);
			*/
		};
			
		public void componentHidden(ComponentEvent e) {};
		public void componentMoved(ComponentEvent e) {};
		public void componentResized(ComponentEvent e) {};
		public void componentShown(ComponentEvent e) {};
		
		public void windowActivated(WindowEvent e) {};
		public void windowClosed(WindowEvent e) {
			for (int i=0; i<BUTTONS; i++)
				if ( e.getSource() == perfFrame[i] ) {
					closeFrame(i);
					return;
				}	
			for (int i=0; i<T_BUTTONS; i++)
				if ( e.getSource() == textFrame[i] ) {
					closeTFrame(i);
					return;
				}	
		} ;
		
		public void windowClosing(WindowEvent e) {
			if (e.getSource() == Viewer.this) {
				if (applet==null)
					System.exit(0);
				else {
					closeAllFrames();
					parser=null;
					setVisible(false);
				}
			}
		};
		
		public void windowDeactivated(WindowEvent e) {};
		public void windowDeiconified(WindowEvent e) {};
		public void windowIconified(WindowEvent e) {};
		public void windowOpened(WindowEvent e) {};
		
		public void itemStateChanged(ItemEvent e) {
			
			if (e.getSource() == noDisks) {
				if (noDisks.isSelected()) {
					if (parser!=null)
						parser.setAvoidDisk(true);
				} else {
					if (parser!=null)
						parser.setAvoidDisk(false);				
				}
			}

			if (e.getSource() == button[CPU]) {
				if (button[CPU].isSelected()) {
					setupCPUGraphic(null);
				} else {
					// Remove frame
					closeFrame(CPU);
				}
			}
			
			if (e.getSource() == button[PCPU]) {
				if (button[PCPU].isSelected()) {
					setupPCPUGraphic(null);
				} else {
					// Remove frame
					closeFrame(PCPU);
				}
			}
			
			if (e.getSource() == button[MEMDETAILS]) {
				if (button[MEMDETAILS].isSelected()) {
					setupMemDetailsGraphic(null);
				} else {
					// Remove frame
					closeFrame(MEMDETAILS);
				}
			}
			
			if (e.getSource() == button[RAM]) {
				if (button[RAM].isSelected()) {
					setupMemoryGraphic(null);
				} else {
					// Remove frame
					closeFrame(RAM);
				}
			}
			
			if (e.getSource() == button[DISKBUSY]) {
				if (button[DISKBUSY].isSelected()) {
					setupDiskBusyGraphic(null);
				} else {
					// Remove frame
					closeFrame(DISKBUSY);
				}
			}
			
			if (e.getSource() == button[ESSBUSY]) {
				if (button[ESSBUSY].isSelected()) {
					setupESSBusyGraphic(null);
				} else {
					// Remove frame
					closeFrame(ESSBUSY);
				}
			}
			
			if (e.getSource() == button[PROCPOOL]) {
				if (button[PROCPOOL].isSelected()) {
					setupPoolGraphic(null);
				} else {
					// Remove frame
					closeFrame(PROCPOOL);
				}
			}
			
			if (e.getSource() == button[DISKRW]) {
				if (button[DISKRW].isSelected()) {
					setupDiskRWGraphic(null);
				} else {
					// Remove frame
					closeFrame(DISKRW);
				}
			}
			
			if (e.getSource() == button[DISKXFER]) {
				if (button[DISKXFER].isSelected()) {
					setupDiskXferGraphic(null);
				} else {
					// Remove frame
					closeFrame(DISKXFER);
				}
			}
			
			if (e.getSource() == button[ESSRW]) {
				if (button[ESSRW].isSelected()) {
					setupEssRWGraphic(null);
				} else {
					// Remove frame
					closeFrame(ESSRW);
				}
			}
			
			if (e.getSource() == button[ESSXFER]) {
				if (button[ESSXFER].isSelected()) {
					setupEssXferGraphic(null);
				} else {
					// Remove frame
					closeFrame(ESSXFER);
				}
			}				
		
			if (e.getSource() == button[DISKBLOCK]) {
				if (button[DISKBLOCK].isSelected()) {
					setupDiskBlockGraphic(null);
				} else {
					// Remove frame
					closeFrame(DISKBLOCK);
				}
			}
			
			if (e.getSource() == button[ADAPTERRW]) {
				if (button[ADAPTERRW].isSelected()) {
					setupAdapterRWGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADAPTERRW);
				}
			}
				
			if (e.getSource() == button[ADAPTERXFER]) {
				if (button[ADAPTERXFER].isSelected()) {
					setupAdapterXferGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADAPTERXFER);
				}				
			}
			
			if (e.getSource() == button[FCRW]) {
				if (button[FCRW].isSelected()) {
					setupFibreRWGraphic(null);
				} else {
					// Remove frame
					closeFrame(FCRW);
				}
			}
			
			if (e.getSource() == button[FCXFER]) {
				if (button[FCXFER].isSelected()) {
					setupFibreXferGraphic(null);
				} else {
					// Remove frame
					closeFrame(FCXFER);
				}				
			}
			
			if (e.getSource() == button[DACRW]) {
				if (button[DACRW].isSelected()) {
					setupDacRWGraphic(null);
				} else {
					// Remove frame
					closeFrame(DACRW);
				}				
			}
			
			if (e.getSource() == button[DACXFER]) {
				if (button[DACXFER].isSelected()) {
					setupDacXferGraphic(null);
				} else {
					// Remove frame
					closeFrame(DACXFER);
				}				
			}
			
			if (e.getSource() == button[NETWORKRW]) {
				if (button[NETWORKRW].isSelected()) {
					setupNetworkRWGraphic(null);
				} else {
					// Remove frame
					closeFrame(NETWORKRW);
				}
			}
			
			if (e.getSource() == button[SEA]) {
				if (button[SEA].isSelected()) {
					setupSEAGraphic(null);
				} else {
					// Remove frame
					closeFrame(SEA);
				}
			}
			
			if (e.getSource() == button[KERNEL]) {
				if (button[KERNEL].isSelected()) {
					setupKernelGraphic(null);
				} else {
					// Remove frame
					closeFrame(KERNEL);
				}
			}
			
			if (e.getSource() == button[SYSCALL]) {
				if (button[SYSCALL].isSelected()) {
					setupSyscallGraphic(null);
				} else {
					// Remove frame
					closeFrame(SYSCALL);
				}
			}
			
			if (e.getSource() == button[RSERVICE]) {
				if (button[RSERVICE].isSelected()) {
					setupDiskReadServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(RSERVICE);
				}
			}
			
			if (e.getSource() == button[WSERVICE]) {
				if (button[WSERVICE].isSelected()) {
					setupDiskWriteServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(WSERVICE);
				}
			}
			
			if (e.getSource() == button[RWSERVICE]) {
				if (button[RWSERVICE].isSelected()) {
					setupDiskReadWriteServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(RWSERVICE);
				}
			}
			
			if (e.getSource() == button[DSKSERVICE]) {
				if (button[DSKSERVICE].isSelected()) {
					setupDiskServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(DSKSERVICE);
				}
			}
			
			if (e.getSource() == button[ESSSERVICE]) {
				if (button[ESSSERVICE].isSelected()) {
					setupEssServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(ESSSERVICE);
				}
			}
			
			if (e.getSource() == button[DSKWAIT]) {
				if (button[DSKWAIT].isSelected()) {
					setupDiskWaitGraphic(null);
				} else {
					// Remove frame
					closeFrame(DSKWAIT);
				}
			}
			
			if (e.getSource() == button[ESSWAIT]) {
				if (button[ESSWAIT].isSelected()) {
					setupEssWaitGraphic(null);
				} else {
					// Remove frame
					closeFrame(ESSWAIT);
				}
			}
			
			if (e.getSource() == button[RWTO]) {
				if (button[RWTO].isSelected()) {
					setupDiskRWTimeoutGraphic(null);
				} else {
					// Remove frame
					closeFrame(RWTO);
				}
			}
			
			if (e.getSource() == button[RWFAIL]) {
				if (button[RWFAIL].isSelected()) {
					setupDiskRWFailedGraphic(null);
				} else {
					// Remove frame
					closeFrame(RWFAIL);
				}
			}
			
			if (e.getSource() == button[WQTIME]) {
				if (button[WQTIME].isSelected()) {
					setupDiskWaitQTimeGraphic(null);
				} else {
					// Remove frame
					closeFrame(WQTIME);
				}
			}
			
			if (e.getSource() == button[QSIZE]) {
				if (button[QSIZE].isSelected()) {
					setupDiskQueueSizeGraphic(null);
				} else {
					// Remove frame
					closeFrame(QSIZE);
				}
			}
			
			if (e.getSource() == button[SQFULL]) {
				if (button[SQFULL].isSelected()) {
					setupDiskServiceQFullGraphic(null);
				} else {
					// Remove frame
					closeFrame(SQFULL);
				}
			}
			
			if (e.getSource() == button[ADRSERVICE]) {
				if (button[ADRSERVICE].isSelected()) {
					setupAdapterReadServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADRSERVICE);
				}
			}
			
			if (e.getSource() == button[ADWSERVICE]) {
				if (button[ADWSERVICE].isSelected()) {
					setupAdapterWriteServiceGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADWSERVICE);
				}
			}
			
			if (e.getSource() == button[ADWQTIME]) {
				if (button[ADWQTIME].isSelected()) {
					setupAdapterWaitQTimeGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADWQTIME);
				}
			}
			
			if (e.getSource() == button[ADQSIZE]) {
				if (button[ADQSIZE].isSelected()) {
					setupAdapterQueueSizeGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADQSIZE);
				}
			}
			
			if (e.getSource() == button[ADSQFULL]) {
				if (button[ADSQFULL].isSelected()) {
					setupAdapterServiceQFullGraphic(null);
				} else {
					// Remove frame
					closeFrame(ADSQFULL);
				}
			}
			
			if (e.getSource() == button[WPAR_CPU]) {
				if (button[WPAR_CPU].isSelected()) {
					setupWPAR_CPU_Graphic(null);
				} else {
					// Remove frame
					closeFrame(WPAR_CPU);
				}
			}
			
			if (e.getSource() == button[WPAR_MEM]) {
				if (button[WPAR_MEM].isSelected()) {
					setupWPAR_Memory_Graphic(null);
				} else {
					// Remove frame
					closeFrame(WPAR_MEM);
				}
			}
			
			if (e.getSource() == button[WPAR_DISK]) {
				if (button[WPAR_DISK].isSelected()) {
					setupWPAR_Disk_Graphic(null);
				} else {
					// Remove frame
					closeFrame(WPAR_DISK);
				}
			}
			
			if (e.getSource() == button[TOPCPU]) {
				if (button[TOPCPU].isSelected()) {
					setupTopCPU_Graphic(null);
				} else {
					// Remove frame
					closeFrame(TOPCPU);
				}
			}
			
			if (e.getSource() == button[TOPRAM]) {
				if (button[TOPRAM].isSelected()) {
					setupTopRAM_Graphic(null);
				} else {
					// Remove frame
					closeFrame(TOPRAM);
				}
			}
			
			if (e.getSource() == button[FS]) {
				if (button[FS].isSelected()) {
					setupFS_Graphic(null);
				} else {
					// Remove frame
					closeFrame(FS);
				}
			}
			
			if (e.getSource() == button[AIO]) {
				if (button[AIO].isSelected()) {
					setupAIO_Graphic(null);
				} else {
					// Remove frame
					closeFrame(AIO);
				}
			}
			
			if (e.getSource() == textButton[T_LSSRAD]) {
				if (textButton[T_LSSRAD].isSelected()) {
					setupAffinityText(null);
				} else {
					// Remove frame
					closeTFrame(T_LSSRAD);
				}
			}
			
			if (e.getSource() == textButton[T_LPARSTATI]) {
				if (textButton[T_LPARSTATI].isSelected()) {
					setupLparstatiText(null);
				} else {
					// Remove frame
					closeTFrame(T_LPARSTATI);
				}
			}
			
			if (e.getSource() == textButton[T_VMSTATV]) {
				if (textButton[T_VMSTATV].isSelected()) {
					setupVmstatvText(null);
				} else {
					// Remove frame
					closeTFrame(T_VMSTATV);
				}
			}
		}
		
	}
	

		
	public Viewer() {
		setup();
	}
	
	public Viewer(ViewerApplet applet, boolean single_host) {
		this.applet = applet;
		
		String files[] = applet.getFiles();
		
		if (files==null)
			appletMode = LOCAL_FILESYSTEM;
		else if (files.length==1)
			appletMode = SINGLE_FILE;
		else if (single_host)
			appletMode = MULTIPLE_FILES_SINGLE_HOST;
		else
			appletMode = MULTIPLE_FILES_MULTIPLE_HOSTS;

		setup();	
	}
	
	public void activateFromApplet() {
		if (applet==null)
			return;
	
		if (appletMode==LOCAL_FILESYSTEM)
			return;
		
		/*
		 * Start reading the files from the web server
		 */
		
		// Reset all variable data
		closeAllFrames();
		parser = null;
			
		// Avoid change of sliders
		//settingUp = true;
		
		// Reset GUI
		activateButtons();
		
		// Start thread managing data
		parserManager.setSource(null);
		parserManager.setApplet(applet);
		if (appletMode == SINGLE_FILE || appletMode == MULTIPLE_FILES_SINGLE_HOST)
			parserManager.setSingleHost(true);
		else
			parserManager.setSingleHost(false);

		Thread th = new Thread(parserManager);
		th.start();		
	}

	/*
	// Single step change of "begin"
	private void beginButtonChange(int delta) {
		if (slider[0].getValue()+delta < slider[0].getMinimum() || 
			slider[0].getValue()+delta>=slider[1].getValue() ||
			settingUp)
			return;
		// Change slider value: it will update text
		slider[0].setValue(slider[0].getValue()+delta);

	}

	// Single step change of "end"
	private void endButtonChange(int delta) {
		if (slider[1].getValue()+delta > slider[1].getMaximum() ||
			slider[1].getValue()+delta<=slider[0].getValue() ||
			settingUp)
			return;
		// Change slider value: it will update text
		slider[1].setValue(slider[1].getValue()+delta);

	}
	
	// Start slider change
	private void startSliderChanged(int v) {
		if (settingUp)
			return;
		time[0].setText(timeString(v));
		day[0].setText(dateString(v));
		if (!slider[0].getValueIsAdjusting()) {
			// Knob is not moving
			if (v>=slider[1].getValue()) {
				time[0].setText(timeString(slider[1].getValue()));
				day[0].setText(dateString(slider[1].getValue()));
				slider[0].setValue(slider[1].getValue());
			}
			// Update data windows
			zoom(slider[0].getValue(),slider[1].getValue());
		}
	}
	
	//	End slider change
	 private void endSliderChanged(int v) {
	 	if (settingUp)
	 		return;
		time[1].setText(timeString(v));
		day[1].setText(dateString(v));
		if (!slider[1].getValueIsAdjusting()) {
			// Knob is not moving
			if (v<=slider[0].getValue()) {
				time[1].setText(timeString(slider[0].getValue()));
				day[1].setText(dateString(slider[0].getValue()));
				slider[1].setValue(slider[0].getValue());
			}
			// Update data windows
			zoom(slider[0].getValue(),slider[1].getValue());
		}
	 }
	 */
	 
	 public void createConsole() {
		 console = new Console();
		 console.setVisible(false);
	 }
	


	private void setup() {
		int i;
		TitledBorder tb;
		
	
		// Create event handler object
		myEventHandler = new MyEventHandler();
		
		// Setup File->Exit menu item
		exitMenuItem = new JMenuItem();
		exitMenuItem.setName("ExitMenuItem");
		exitMenuItem.setText("Exit");
		exitMenuItem.addActionListener(myEventHandler);

		// Setup File->Open SINGLE FILE menu item
		openFileMenu = new JMenuItem();
		openFileMenu.setName("OpenSingleFile");
		openFileMenu.setText("Single file");
		openFileMenu.addActionListener(myEventHandler);
		
		// Setup File->Open CEC menu item
		openMMDirMenu = new JMenuItem();
		openMMDirMenu.setName("OpenMMDirMenu");
		openMMDirMenu.setText("Directory: MULTIPLE hosts, multiple files");
		openMMDirMenu.addActionListener(myEventHandler);
		
		// Setup File->Open host dir menu item
		openSMDirMenu = new JMenuItem();
		openSMDirMenu.setName("OpenSMDirMenu");
		openSMDirMenu.setText("Directory: SINGLE host, multiple files");
		openSMDirMenu.addActionListener(myEventHandler);
		
		// Setup File->Open cfg menu item
		openCfgMenu = new JMenuItem();
		openCfgMenu.setName("openCfgMenu");
		openCfgMenu.setText("Configuration file");
		openCfgMenu.addActionListener(myEventHandler);
		
		// Setup File menu
		fileMenu = new JMenu();
		fileMenu.setName("FileMenu");
		fileMenu.setText("File");
		if (appletMode == NO_APPLET) {
			fileMenu.add(openFileMenu);
			fileMenu.add(openMMDirMenu);
			fileMenu.add(openSMDirMenu);
			fileMenu.add(openCfgMenu);
		}
		fileMenu.add(exitMenuItem);
		
		// Setup Help - About menu
		aboutMenu = new JMenuItem();
		aboutMenu.setName("AboutMenuItem");
		aboutMenu.setText("About");
		aboutMenu.addActionListener(myEventHandler);
		
		// Setup Console menu
		consoleMenu = new JMenuItem();
		consoleMenu.setName("ConsoleMenuItem");
		consoleMenu.setText("Console");
		consoleMenu.addActionListener(myEventHandler);
		
		// Setup Help menu
		JMenu help = new JMenu();
		help.setName("HelpMenu");
		help.setText("Help");
		help.add(consoleMenu);
		help.add(aboutMenu);
		

		// Setup (File, Help) menu bar
		menuBar = new JMenuBar();
		menuBar.setName("ControlCenterJMenuBar");
		menuBar.add(fileMenu);
		menuBar.add(help);
		
		
		// Setup openFileDialog object
		openFileDialog = new JFileChooser();
		openFileDialog.setName("OpenFile");
		openFileDialog.addComponentListener(myEventHandler);
		openFileDialog.setCurrentDirectory(configuration.getWorkingDirectory());
		
		
		// New slider panel
		sliderPanel = new JPanel();
		sliderPanel.setBorder(new javax.swing.border.EtchedBorder());
		sliderPanel.setLayout(new java.awt.BorderLayout());
		
		zoomButton = new JButton();
		zoomButton.setFocusPainted(false);
		zoomButton.setFont(configuration.getButtonFont());
		zoomButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
		zoomButton.setText("Zoom");
		zoomButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		zoomButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		zoomButton.addActionListener(myEventHandler);
		
		timePanel = new TimePanel();
		
		sliderPanel = new JPanel();
		sliderPanel.setBorder(new javax.swing.border.EtchedBorder());
		sliderPanel.setLayout(new java.awt.BorderLayout());
		sliderPanel.add(zoomButton,"West");
		sliderPanel.add(timePanel,"Center");
		tb = new TitledBorder("Time Selection");
		sliderPanel.setBorder(tb);
		
		
		
		
		// Create message label
		message = new JLabel();
		message.setText("No performance file selected.");
		message.setBorder(new javax.swing.border.EtchedBorder());
		
		// Create noDisks checkbox
		noDisks = new JCheckBox("No disk data");
		noDisks.setFocusPainted(false);
		noDisks.addItemListener(myEventHandler);
		noDisks.setFont(configuration.getButtonFont());
		
		// Setup viewer buttons
		button = new JToggleButton[BUTTONS];
		for (i=0; i<BUTTONS; i++) {
			button[i] = new JToggleButton();
			button[i].setFocusPainted(false);
			button[i].setFont(configuration.getButtonFont());
			button[i].setMargin(new java.awt.Insets(2, 10, 2, 10));
			button[i].addItemListener(myEventHandler);
		}
		
		button[CPU].setToolTipText("Show CPU usage");
		button[CPU].setText("CPU");
		
		button[PCPU].setToolTipText("Show per CPU physical usage");
		button[PCPU].setText("PCPU");
		
		button[MEMDETAILS].setToolTipText("Show data based on page size");
		button[MEMDETAILS].setText("Page size");
		
		button[RAM].setToolTipText("Show memory usage");
		button[RAM].setText("Memory");
		
		button[DISKBUSY].setToolTipText("Show disk busy statistics");
		button[DISKBUSY].setText("Disk Busy");
		
		button[ESSBUSY].setToolTipText("Show MPIO device busy statistics");
		button[ESSBUSY].setText("MPIO Busy");
		
		button[PROCPOOL].setToolTipText("Show Shared ProcPool statistics");
		button[PROCPOOL].setText("Proc Pool");
		
		button[DISKRW].setToolTipText("Show disk RW statistics");
		button[DISKRW].setText("Disk RW");
		
		button[DISKXFER].setToolTipText("Show disk Xfer statistics");
		button[DISKXFER].setText("Disk Xfer");
		
		button[DISKBLOCK].setToolTipText("Show disk block statistics");
		button[DISKBLOCK].setText("Disk Block");
		
		button[ESSRW].setToolTipText("Show MPIO RW statistics");
		button[ESSRW].setText("MPIO RW");
		
		button[ESSXFER].setToolTipText("Show MPIO Xfer statistics");
		button[ESSXFER].setText("MPIO Xfer");
		
		button[ADAPTERRW].setToolTipText("Show Disk Adapter RW statistics");
		button[ADAPTERRW].setText("Dsk I/O RW");
		
		button[ADAPTERXFER].setToolTipText("Show Disk Adapter Xfer statistics");
		button[ADAPTERXFER].setText("Dsk I/O Xfer");
		
		button[FCRW].setToolTipText("Show Fibre Channel RW statistics");
		button[FCRW].setText("FC RW");
		
		button[FCXFER].setToolTipText("Show Fibre Channel Xfer statistics");
		button[FCXFER].setText("FC I/O Xfer");
		
		button[DACRW].setToolTipText("Show dac RW statistics");
		button[DACRW].setText("Dac RW");
		
		button[DACXFER].setToolTipText("Show dac Xfer statistics");
		button[DACXFER].setText("Dac Xfer");
		
		button[NETWORKRW].setToolTipText("Show Network RW statistics");
		button[NETWORKRW].setText("Net RW");
		
		button[SEA].setToolTipText("Show SEA statistics");
		button[SEA].setText("SEA");
		
		button[KERNEL].setToolTipText("Show kernel data");
		button[KERNEL].setText("Kernel");
		
		button[SYSCALL].setToolTipText("Show system calls");
		button[SYSCALL].setText("SysCall");				
		
		button[CHECKER].setToolTipText("Search data patterns");
		button[CHECKER].setText("Checker");
		
		button[RSERVICE].setToolTipText("Show disk read service statistics");
		button[RSERVICE].setText("Dsk RServ");
		
		button[WSERVICE].setToolTipText("Show disk write service statistics");
		button[WSERVICE].setText("Dsk WServ");
		
		button[RWSERVICE].setToolTipText("Show disk read+write service statistics");
		button[RWSERVICE].setText("Dsk RWServ");
		
		button[DSKSERVICE].setToolTipText("Show disk service statistics");
		button[DSKSERVICE].setText("Dsk Serv");
		
		button[ESSSERVICE].setToolTipText("Show MPIO service statistics");
		button[ESSSERVICE].setText("MPIO Serv");
		
		button[DSKWAIT].setToolTipText("Show disk wait statistics");
		button[DSKWAIT].setText("Dsk Wait");
		
		button[ESSWAIT].setToolTipText("Show MPIO wait statistics");
		button[ESSWAIT].setText("MPIO Wait");
		
		button[RWTO].setToolTipText("Show disk RW timeout statistics");
		button[RWTO].setText("Dsk RW TO");
		
		button[RWFAIL].setToolTipText("Show disk RW failures statistics");
		button[RWFAIL].setText("Dsk RW Fails");
		
		button[WQTIME].setToolTipText("Show disk Wait Queue Time statistics");
		button[WQTIME].setText("Dsk WaitQ T");
		
		button[QSIZE].setToolTipText("Show disk Queue Size statistics");
		button[QSIZE].setText("Dsk Queues");
		
		button[SQFULL].setToolTipText("Show disk Full Service Queue statistics");
		button[SQFULL].setText("Dsk SQ Full");
		
		button[ADRSERVICE].setToolTipText("Show adapter read service statistics");
		button[ADRSERVICE].setText("I/O RServ");
		
		button[ADWSERVICE].setToolTipText("Show adapter write service statistics");
		button[ADWSERVICE].setText("I/O WServ");
		
		button[ADWQTIME].setToolTipText("Show adapter wait queue statistics");
		button[ADWQTIME].setText("I/O WaitQ");
		
		button[ADQSIZE].setToolTipText("Show adapter queue size statistics");
		button[ADQSIZE].setText("I/O Queues");
		
		button[ADSQFULL].setToolTipText("Show adapter service queue full statistics");
		button[ADSQFULL].setText("I/O SrvQ Full");
		
		button[WPAR_CPU].setToolTipText("Show WLM/WPAR CPU statistics");
		button[WPAR_CPU].setText("WLM CPU");
		
		button[WPAR_MEM].setToolTipText("Show WLM/WPAR Memory statistics");
		button[WPAR_MEM].setText("WLM RAM");
		
		button[WPAR_DISK].setToolTipText("Show WLM/WPAR Disk statistics");
		button[WPAR_DISK].setText("WLM Disk");
		
		button[TOPCPU].setToolTipText("Show top CPU processes");
		button[TOPCPU].setText("Top CPU");
		
		button[TOPRAM].setToolTipText("Show top Memory processes");
		button[TOPRAM].setText("Top Mem");
		
		button[FS].setToolTipText("Show file system statistics");
		button[FS].setText("FileSystem");
		
		button[AIO].setToolTipText("Show Asynchronous I/O statistics");
		button[AIO].setText("Async IO");
		
		// Setup text buttons
		textButton = new JToggleButton[T_BUTTONS];
		for (i=0; i<T_BUTTONS; i++) {
			textButton[i] = new JToggleButton();
			textButton[i].setFocusPainted(false);
			textButton[i].setFont(configuration.getButtonFont());
			textButton[i].setMargin(new java.awt.Insets(2, 10, 2, 10));
			textButton[i].addItemListener(myEventHandler);
		}
		
		textButton[T_LSSRAD].setToolTipText("Show cpu/memory affinity");
		textButton[T_LSSRAD].setText("Affinity");
		textButton[T_LPARSTATI].setToolTipText("Lpar configuration");
		textButton[T_LPARSTATI].setText("LPAR CFG");
		textButton[T_VMSTATV].setToolTipText("vmstat -v output");
		textButton[T_VMSTATV].setText("vmstat -v");
		
		buttonPanel = new JPanel();
		//buttonPanel.setPreferredSize(new java.awt.Dimension(200, 30));
		buttonPanel.setLayout(null);

		JPanel viewerContent = new JPanel();
		viewerContent.setLayout(new java.awt.BorderLayout());
		viewerContent.add(sliderPanel,"North");
		viewerContent.add(buttonPanel,"Center");
		
		
		JPanel jpa2 = new JPanel();
		jpa2.setLayout(new java.awt.BorderLayout());
		jpa2.add(message,"West");
		jpa2.add(noDisks,"East");
		
		JPanel jpa = new JPanel();
		jpa.setLayout(new java.awt.BorderLayout());
		//jpa.add(message,"North");	
		jpa.add(jpa2,"North");
		jpa.add(progressBar,"South");	
		
		//viewerContent.add(message,"South");	
		viewerContent.add(jpa,"South");	
		setContentPane(viewerContent);
					
		activateButtons();	
		
		setName("pGraph Viewer");

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(myEventHandler);
		setJMenuBar(menuBar);
		setResizable(true);
		// setSize(1100, 180); <<= set in main
		setTitle("pGraph Viewer");	
		
		progressBar.setValue(0);
	}
	
	
	private void activateButtons() {
		//int rows;
		
		buttonPanel.removeAll();
		
		switch (parserManager.getFiletype()) {
			case ParserManager.UNKNOWN:
							//rows=0;
							break;
			case ParserManager.SNMP:
							addButton(CPU,0,0);
							break;
			case ParserManager.INSIGHT:
							addButton(CPU,0,0);
							break;
			case ParserManager.COLLECTL:
							addButton(CPU,0,0);
							addButton(RAM,1,0);
							addButton(DISKRW,2,0);
							addButton(NETWORKRW,3,0);
							break;
			case ParserManager.SAR:
							addButton(CPU,0,0);
							addButton(DISKBUSY,1,0);
								//addButton(WQTIME,1,1);
								//addButton(QSIZE,1,2);
							addButton(KERNEL,2,0);
							addButton(SYSCALL,2,1);
							//rows=1;
							break;
			case ParserManager.NMON:
							addButton(CPU,0,0);
								addButton(PCPU,0,1);
								addTButton(T_LSSRAD,0,2);
								addTButton(T_LPARSTATI,0,3);
							addButton(RAM,1,0);
								addTButton(T_VMSTATV,1,1);
								addButton(MEMDETAILS,1,2);
							addButton(DISKBUSY,2,0);
								addButton(DISKRW,2,1);
								addButton(DISKXFER,2,2);
								addButton(DISKBLOCK,2,3);
								addButton(DSKSERVICE,2,4);
								addButton(DSKWAIT,2,5);
								addButton(RSERVICE,2,6);
								addButton(WSERVICE,2,7);
								addButton(RWSERVICE,2,8);
							addButton(ESSRW,3,0);
								addButton(ESSXFER,3,1);
								addButton(ESSSERVICE,3,2);
								addButton(ESSWAIT,3,3);
								addButton(DACRW,3,4);
								addButton(DACXFER,3,5);
							addButton(ADAPTERRW,4,0);
								addButton(ADAPTERXFER,4,1);
								addButton(FCRW,4,2);
								addButton(FCXFER,4,3);
							addButton(NETWORKRW,5,0);
								addButton(SEA,5,1);
							addButton(KERNEL,6,0);
								addButton(SYSCALL,6,1);
								addButton(AIO,6,2);
							addButton(TOPCPU,7,0);
								addButton(TOPRAM,7,1);
							addButton(WPAR_CPU,8,0);
								addButton(WPAR_MEM,8,1);
								addButton(WPAR_DISK,8,2);
							addButton(FS,9,0);
							//addButton(CHECKER,7,0);
							//rows=6;
							break;
			
			case ParserManager.VMSTAT:
							addButton(CPU,0,0);
							addButton(RAM,1,0);
							addButton(KERNEL,2,0);
								addButton(SYSCALL,2,1);
							//addButton(CHECKER,3,0);
							//rows=2;
							break;
							
			case ParserManager.CECDIR:	
							addButton(CPU,0,0);
							addButton(RAM,1,0);
							addButton(ADAPTERRW,2,0);
							addButton(NETWORKRW,3,0);
							//addButton(CHECKER,1,0);
							//rows=1;
							break;
							
			case ParserManager.CONFIGFILE:	
							addButton(CPU,0,0);
							addButton(RAM,1,0);
							addButton(ADAPTERRW,2,0);
							addButton(NETWORKRW,3,0);
							//addButton(CHECKER,1,0);
							//rows=1;
							break;
							
			case ParserManager.XMTREND:	
							addButton(CPU,0,0);
							addButton(RAM,1,0);
							addButton(DISKBUSY,2,0);
								addButton(DISKRW,2,1);
								addButton(DISKXFER,2,2);
								addButton(DSKSERVICE,2,3);
								addButton(DSKWAIT,2,4);
							addButton(ESSRW,3,0);
								addButton(ESSXFER,3,1);
								addButton(ESSSERVICE,3,2);
								addButton(ESSWAIT,3,3);
								addButton(DACRW,3,4);
								addButton(DACXFER,3,5);
							addButton(NETWORKRW,4,0);
							addButton(KERNEL,5,0);
								addButton(SYSCALL,5,1);
							addButton(WPAR_CPU,6,0);
								addButton(WPAR_MEM,6,1);
								addButton(WPAR_DISK,6,2);
							addButton(FS,7,0);
							//addButton(CHECKER,5,0);
							//rows=6;
							break;
							
			case ParserManager.TOPASCEC:	
							addButton(CPU,0,0);
							//addButton(CHECKER,1,0);
							//rows=1;
							break;
							
			case ParserManager.IOSTAT:	
							addButton(DISKBUSY,0,0);
								addButton(DISKRW,0,1);
								addButton(DISKXFER,0,2);
								addButton(ESSBUSY,0,3);
								addButton(ESSRW,0,4);
								addButton(ESSXFER,0,5);
							addButton(RSERVICE,1,0);
								addButton(WSERVICE,1,1);
								addButton(RWSERVICE,1,2);
							addButton(RWTO,2,0);
							addButton(RWFAIL,2,1);
							addButton(WQTIME,3,0);
							addButton(QSIZE,3,1);
							addButton(SQFULL,3,2);
							addButton(ADAPTERRW,4,0);
								addButton(ADAPTERXFER,4,1);
							addButton(ADRSERVICE,5,0);
								addButton(ADWSERVICE,5,1);
							addButton(ADWQTIME,6,0);
								addButton(ADQSIZE,6,1);
								addButton(ADSQFULL,6,2);
							//addButton(CHECKER,7,0);
							//rows=6;
							break;
							
			case ParserManager.LSLPARUTIL: 
							addButton(CPU,0,0);
							addButton(PROCPOOL,0,1);
							addButton(RAM,1,0);
							//rows=2;
							break;
			
			default:		//rows=0;
							break;
		
		}
		
		validate();
		repaint();
	}
	
	/*
	private void addButton(byte id, int x, int y) {
		button[id].setBounds(x*100, y*24, 98, 22);
		buttonPanel.add(button[id]);
	}
	*/
	
	private void addButton(byte id, int x, int y) {
		button[id].setBounds(x*100, y*24, 98, 22);
		buttonPanel.add(button[id]);
	}
	
	private void addTButton(byte id, int x, int y) {
		textButton[id].setBounds(x*100, y*24, 98, 22);
		buttonPanel.add(textButton[id]);
	}
	
	
	private static GregorianCalendar getGC(String string) {
		if (string.length()!="YYYYMMDDhhmmss".length())
			return null;
			
		int Y,M,D,h,m,s;
		
		try {
			Y = Integer.parseInt(string.substring(0,4),10);
			M = Integer.parseInt(string.substring(4,6),10);
			D = Integer.parseInt(string.substring(6,8),10);
			h = Integer.parseInt(string.substring(8,10),10);
			m = Integer.parseInt(string.substring(10,12),10);
			s = Integer.parseInt(string.substring(12),10);
		} catch (NumberFormatException nfe) {
			return null;
		}
		
		return new GregorianCalendar(Y,M-1,D,h,m,s);
	}
	
	
	private static void usage_exit() {
		System.out.println("");
		System.out.println("pGraph.Viewer - "+version);
		System.out.println("Interactive Usage: pGraph.Viewer [file]");
		System.out.println("Batch Usage:       pGraph.Viewer [ -l begin end ] [ -d | -D ] <source> <output dir>");
		System.out.println("\t-l limits time frame: begin and end in YYYYMMDDhhmmss format.");
		System.out.println("\t-d source=directory : multiple hosts in multiple source files.");
		System.out.println("\t-D source=directory : single host with multiple source files.");
		System.out.println("\t<source> is by default a file. Use -d or -D to select a directory.");
		System.out.println("\t\tValid input files are: \"nmon\", \"vmstat -t\",");
		System.out.println("\t\t\"topasout <xmtrend file>\", \"topasout <topas_cec>\",");
		System.out.println("\t\t\"iostat -alDT\", \"lslparutil\"");
		System.out.println("\t<output dir> is where system usage reports in PNG format are written.");
		System.out.println("");
		System.exit(2);		
	}
	
	
	/*
	 * Parse cmd line and provide all arguments in the following order:
	 * begin, end, type, source, dir. Missing args are NULL. 
	 * If invalid data, stop and show usage.
	 */
	private static String[] parse_args(String[] args) {
		if (args.length!=2 && args.length!=3 &&
			args.length!=5 && args.length!=6)
			usage_exit();
			
		String result[] = new String[5];
		for (int i=0; i<result.length; i++)
			result[i]=null;
			
		switch (args.length) {
			case 2:	// source dir
					result[2] = "FILE";
					result[3] = args[0];	
					result[4] = args[1];
					break;
					
			case 3: // [-d|-D] source dir
					if (args[0].equals("-d"))
						result[2] = "DIRMH";
					else if (args[0].equals("-D"))
						result[2] = "DIRSH";
					else
						usage_exit();

					result[3] = args[1];
					result[4] = args[2];
					break;
					
			case 5: // -l begin end source dir
					if (!args[0].equals("-l"))
						usage_exit();
					result[0] = args[1];
					result[1] = args[2];
					result[2] = "FILE";
					result[3] = args[3];
					result[4] = args[4];
					break;
					
			case 6: // -l begin end [-d|-D] source dir
					if (!args[0].equals("-l"))
						usage_exit();
					
					if (args[3].equals("-d"))
						result[2] = "DIRMH";
					else if (args[3].equals("-D"))
						result[2] = "DIRSH";
					else
						usage_exit();
					
					result[0] = args[1];
					result[1] = args[2];					
					result[3] = args[4];
					result[4] = args[5];

					break;
					
			default: usage_exit();
		}
		
		if (result[2]==null ||
			!(result[2].equals("FILE") || result[2].equals("DIRMH") || result[2].equals("DIRSH")) )
			usage_exit();
		
		return result;
	}
	
	
	
	private static void goInteractive (String fileName) {
		try {
			/* Set native look and feel */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			/* Create the frame */
			Viewer viewer = new Viewer();
			viewer.setSize(VIEWER_WIDTH,VIEWER_HEIGHT);
			viewer.setPreferredSize(new Dimension(VIEWER_WIDTH,VIEWER_HEIGHT));
			
			// Create Console for stdout and stderr
			viewer.createConsole();
	
			/* Calculate the screen size */
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
			/* Center frame on the screen */
			Dimension frameSize = viewer.getSize();
			if (frameSize.height > screenSize.height)
					frameSize.height = screenSize.height;
			if (frameSize.width > screenSize.width)
					frameSize.width = screenSize.width;
			viewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);	
			
			viewer.pack();
			viewer.setVisible(true);
			
			
			
			if (fileName!=null) {
				// Check if the file exists
				File file = new File(fileName);
				if (file.exists() && file.isFile() && file.canRead())
					viewer.loadSingleFile(file);	
				else
					System.out.println("Cannot load "+fileName);
			}
		} catch (Throwable exception) {
			System.err.println("Exception occurred in main() of Viewer");
			exception.printStackTrace(System.out);
		}
		
	}
	
	


	public static void main(String[] args) {		
		if (args.length==0)
			goInteractive(null);
		else if (args.length==1 && !args[0].equalsIgnoreCase("-h") && !args[0].equalsIgnoreCase("-?"))
			goInteractive(args[0]);
		else 
			goBatch(args);
	}
	
	
	private static void goBatch(String[] args) {

		String params[] = parse_args(args);
		File file		= new File(params[3]);
		File dir		= new File(params[4]);
		
		// Check that file is valid
		if (!file.exists()	|| 
			!file.canRead()	||
			( params[2].equals("FILE") && !file.isFile() ) ||
			( !params[2].equals("FILE") && !file.isDirectory() ) )  {
				
			System.out.println("ERROR: " + params[3] + " is an invalid file or directory");
			System.exit(2);
		}
			
		// Check that directory is valid
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				System.out.println("ERROR: " + params[4] + " exists and it is not a directory");
				System.exit(2);
			}
		} else if (!dir.mkdirs()) {
			System.out.println("ERROR: cannot create directory " + params[4]);
			System.exit(2);
		}	
		
		
		Viewer viewer = null;
		try {
			viewer = new Viewer();
		} catch (HeadlessException e) {	
			System.out.println("ERROR: A graphical environment is required.");
			System.exit(2);					
		}
		
		
		viewer.setBatchMode(dir.getPath());
		
		// Change limits if needed	
		if ( params[0]!=null && params[1]!=null ) {				
			GregorianCalendar begin = getGC(params[0]);
			GregorianCalendar end   = getGC(params[1]);
			
			if (begin==null || end==null) {
				System.out.println("ERROR: cannot parse limits.");
				System.exit(2);
			}
			
			if (end.before(begin)) {
				System.out.println("ERROR: end label can not be before begin label");
				System.exit(2);
			}
			
			viewer.setNewLimits(begin,end);
		}
		
		// Load file
		if (params[2].equals("FILE")) {
			viewer.loadSingleFile(file);
		} else if (params[2].equals("DIRMH")){
			viewer.loadCECDirectory(file);
		} else if (params[2].equals("DIRSH")) {
			viewer.loadMultipleFilesSingleHost(file);
		} else {
			System.out.println("ERROR: internal error");
			System.exit(2);
		}
	}
	

	
	/*
	 * Valid only for batch mode: set time limits
	 */
	private void setNewLimits(GregorianCalendar begin, GregorianCalendar end) {
		//timePanel.setActive(begin,end);
		batchBegin = begin;
		batchEnd = end;
		
		//this.begin = begin;
		//this.end   = end;
	}
	

	
	private void createPNG(GenericPanel p, String s) {
		if (p==null || s ==null)
			return;
		
		int height = configuration.getPanelHeight();
			
		p.setSize(new Dimension(DataSet.SLOTS, height));
		p.setOpaque(true);
		p.setDoubleBuffered(false);
		
		File file = new File(s);
			
		// Create PNG 
		BufferedImage bi = new BufferedImage(DataSet.SLOTS+configuration.getTextareaWidth(), configuration.getPanelHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		p.validate();
		p.printAll(g2d);
		
		try {
			// Save as PNG
			javax.imageio.ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			System.out.println("ERROR: could not create file "+file);
			e.printStackTrace();
			return;
		}		
		System.out.print(".");
	}
	
	
	
	private void publishPanel( GenericFrame gf, 
								PrintWriter html, 
								String dir,
								GenericPanel panel,
								String description) {
		
		if (gf!=null)
			gf.addGraph(panel,description);	
		
		if ( dir != null && !(panel instanceof SumXYPanel) ) {
			createPNG(panel,dir+File.separatorChar+PNGindex+".png");
			html.println("<H2>"+description+"</H2>\n");
			html.println("<IMG SRC="+PNGindex+".png><BR>\n");
			PNGindex++;
		}		
	}
	
	private void addSeparator( GenericFrame gf, PrintWriter html, String string ) {
		if (html==null) {
			// GUI
			gf.addSeparator(string);
		} else {
			html.println("<H2><CENTER>"+string+"</CENTER></H2>\n");
		}
	}
		
		
	/*
	 * Count non-null elements in array
	 */
	private int numElementsNotNull(DataSet[] ds) {
		if (ds==null)
			return 0;
		int i,num;
		for ( i=0, num=0; i<ds.length; i++)
			if (ds[i]!=null)
				num++;
		return num;
	}
	
	
	/*
	 * Create WPAR panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */
	private int setupWPAR_CPU_Graphic(String dir) {
		int i,j,k;
		int panels = 0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, WPAR_CPU);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		names = pd.getNames(PerfData.WPAR);
		if (names==null)
			return panels;
		
		// We need to get classes and subclasses names
		// NOTE: names are sorted with shorter names fist!!!!!
		final int MAX = 64;
		int wlmclass[] = new int[64];
		int wlmsubclass[][] = new int[MAX][MAX];
		for (i=0; i<MAX; i++) {
			wlmclass[i]=-1;
			for (j=0; j<MAX; j++)
				wlmsubclass[i][j]=-1;
		}
		
		String s[];
		for (i=0; i<names.length; i++) {
			s=names[i].split("\\.",2);
			if (s.length==1) {
				// WLM class
				j=0; while (wlmclass[j]>=0) j++;
				wlmclass[j]=i;				
			} else {
				// WLM subclass (class has already been read)
				j=0; while (s[0].compareTo(names[wlmclass[j]])!=0) j++;
				k=0; while (wlmsubclass[j][k]>=0) k++;
				wlmsubclass[j][k]=i;
			}
		}
		int numclasses=0;
		int numsubclasses=0;
		while (wlmclass[numclasses]>=0)
			numclasses++;
		String newNames[];
		
		
		// CPU% usage by WPAR
		addSeparator(perfFrame[WPAR_CPU], html, "Global CPU%" );
		ds=new DataSet[numclasses];
		for (i=0; i<numclasses; i++)
			ds[i]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_CPU);
		newNames=new String[numclasses];
		for (i=0; i<numclasses; i++)
			newNames[i]=names[wlmclass[i]];
		panel=new StackPanel(configuration);
		((StackPanel)panel).setData(pd, ds, newNames);
		((StackPanel)panel).setMaxData(100);
		publishPanel( perfFrame[WPAR_CPU], html, dir, panel,"Global CPU% Usage");
		panels++;
			
		
		// CPU% by WLM class
		for (i=0; i<numclasses; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_CPU);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[WPAR_CPU], html, dir, panel,newNames[i]+"-CPU%");
			panels++;
		}
		
		
		// CPU% by subclass (if any)
		for (i=0; i<numclasses; i++)  {
			if (wlmsubclass[i][0]==-1)
				continue;	// no subclass present
			numsubclasses=0;
			while (wlmsubclass[i][numsubclasses]>=0)
				numsubclasses++;
			
			addSeparator(perfFrame[WPAR_CPU], html, "Class "+names[wlmclass[i]]+" CPU%" );
			
			// CPU% usage: WPAR summary
			ds=new DataSet[numsubclasses];
			for (j=0; j<numsubclasses; j++)
				ds[j]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_CPU);
			newNames=new String[numsubclasses];
			for (j=0; j<numsubclasses; j++)
				newNames[j]=names[wlmsubclass[i][j]];
			panel=new StackPanel(configuration);
			((StackPanel)panel).setData(pd, ds, newNames);
			((StackPanel)panel).setMaxData(100);
			publishPanel( perfFrame[WPAR_CPU], html, dir, panel,"Class "+names[wlmclass[i]]+" CPU Usage");
			panels++;
			
			// CPU% by WLM subclass
			for (j=0; j<numsubclasses; j++) {		
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_CPU);
				panel = new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[WPAR_CPU], html, dir, panel,newNames[j]+"-CPU%");
				panels++;
			}
			
		}
		
	
		// If LPAR does not provide PC, no WPAR_PROC is available
		if (pd.getData(PerfData.WPAR, 0, PerfData.WPAR_PROC)!=null &&
				pd.getData(PerfData.SYSTEM, 0, PerfData.PC)!=null) {
			
			
			// CPU% usage by WPAR
			addSeparator(perfFrame[WPAR_CPU], html, "Global Proc" );
			ds=new DataSet[numclasses];
			for (i=0; i<numclasses; i++)
				ds[i]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_PROC);	
			newNames=new String[numclasses];
			for (i=0; i<numclasses; i++)
				newNames[i]=names[wlmclass[i]];
			panel=new StackPanel(configuration);
			((StackPanel)panel).setData(pd, ds, newNames);
			((StackPanel)panel).setMaxData(pd.getData(PerfData.SYSTEM, 0, PerfData.PC));
			publishPanel( perfFrame[WPAR_CPU], html, dir, panel,"Global Proc Consumed");
			panels++;
			
			// Proc consumed by WLM class
			for (i=0; i<numclasses; i++) {		
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_PROC);
				panel = new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[WPAR_CPU], html, dir, panel,newNames[i]+"-PC");
				panels++;
			}
			
			
			// proc consumed by subclass (if any)
			for (i=0; i<numclasses; i++)  {
				if (wlmsubclass[i][0]==-1)
					continue;	// no subclass present
				numsubclasses=0;
				while (wlmsubclass[i][numsubclasses]>=0)
					numsubclasses++;
				
				addSeparator(perfFrame[WPAR_CPU], html, "Class "+names[wlmclass[i]]+" Proc" );
				
				// Proc Consumed: WPAR summary
				ds=new DataSet[numsubclasses];
				for (j=0; j<numsubclasses; j++)
					ds[j]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_PROC);
				newNames=new String[numsubclasses];
				for (j=0; j<numsubclasses; j++)
					newNames[j]=names[wlmsubclass[i][j]];
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, newNames);
				((StackPanel)panel).setMaxData(pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_PROC));
				publishPanel( perfFrame[WPAR_CPU], html, dir, panel,"Class "+names[wlmclass[i]]+" Proc Consumed");
				panels++;
				
				// Proc Consumed by WLM subclass
				for (j=0; j<numsubclasses; j++) {		
					ds=new DataSet[1];
					ds[0]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_PROC);
					panel = new XYPanel(configuration);
					panel.setData(pd, ds);
					publishPanel( perfFrame[WPAR_CPU], html, dir, panel,newNames[j]+"-PC");
					panels++;
				}
				
			}			
	
		}
		
		finishFrame(dir, WPAR_CPU, html, panels);
		return panels;		
	}
	
	
	private int setupWPAR_Memory_Graphic(String dir) {
		int i,j,k;
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, WPAR_MEM);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		names = pd.getNames(PerfData.WPAR);
		if (names==null)
			return panels;
		
		// We need to get classes and subclasses names
		// NOTE: names are sorted with shorter names fist!!!!!
		final int MAX = 64;
		int wlmclass[] = new int[64];
		int wlmsubclass[][] = new int[MAX][MAX];
		for (i=0; i<MAX; i++) {
			wlmclass[i]=-1;
			for (j=0; j<MAX; j++)
				wlmsubclass[i][j]=-1;
		}
		
		String s[];
		for (i=0; i<names.length; i++) {
			s=names[i].split("\\.",2);
			if (s.length==1) {
				// WLM class
				j=0; while (wlmclass[j]>=0) j++;
				wlmclass[j]=i;				
			} else {
				// WLM subclass (class has already been read)
				j=0; while (s[0].compareTo(names[wlmclass[j]])!=0) j++;
				k=0; while (wlmsubclass[j][k]>=0) k++;
				wlmsubclass[j][k]=i;
			}
		}
		int numclasses=0;
		int numsubclasses=0;
		while (wlmclass[numclasses]>=0)
			numclasses++;
		String newNames[];
		
		
		// MEM% usage by WPAR
		addSeparator(perfFrame[WPAR_MEM], html, "Global Memory" );
		ds=new DataSet[numclasses];
		for (i=0; i<numclasses; i++)
			ds[i]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_MEM);
		newNames=new String[numclasses];
		for (i=0; i<numclasses; i++)
			newNames[i]=names[wlmclass[i]];
		panel=new StackPanel(configuration);
		((StackPanel)panel).setData(pd, ds, newNames);
		((StackPanel)panel).setMaxData(100);
		publishPanel( perfFrame[WPAR_MEM], html, dir, panel,"WLM Memory% Usage");
		panels++;
			
		
		// MEM% by WLM class
		for (i=0; i<numclasses; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_MEM);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[WPAR_MEM], html, dir, panel,newNames[i]);
			panels++;
		}
		
		
		// MEM% by subclass (if any)
		for (i=0; i<numclasses; i++)  {
			if (wlmsubclass[i][0]==-1)
				continue;	// no subclass present
			numsubclasses=0;
			while (wlmsubclass[i][numsubclasses]>=0)
				numsubclasses++;
			
			addSeparator(perfFrame[WPAR_MEM], html, "Class "+names[wlmclass[i]]+" MEM%" );
			
			// MEM% usage: WPAR summary
			ds=new DataSet[numsubclasses];
			for (j=0; j<numsubclasses; j++)
				ds[j]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_MEM);
			newNames=new String[numsubclasses];
			for (j=0; j<numsubclasses; j++)
				newNames[j]=names[wlmsubclass[i][j]];
			panel=new StackPanel(configuration);
			((StackPanel)panel).setData(pd, ds, newNames);
			((StackPanel)panel).setMaxData(100);
			publishPanel( perfFrame[WPAR_MEM], html, dir, panel,"Class "+names[wlmclass[i]]+" MEM Usage");
			panels++;
			
			// MEM% by WLM subclass
			for (j=0; j<numsubclasses; j++) {		
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_MEM);
				panel = new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[WPAR_MEM], html, dir, panel,newNames[j]);
				panels++;
			}
			
		}
		
		finishFrame(dir, WPAR_MEM, html, panels);
		return panels;		
	}	
	
	
	private int setupWPAR_Disk_Graphic(String dir) {
		int i,j,k;
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, WPAR_DISK);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		names = pd.getNames(PerfData.WPAR);
		if (names==null)
			return panels;
		
		
		// We need to get classes and subclasses names
		// NOTE: names are sorted with shorter names fist!!!!!
		final int MAX = 64;
		int wlmclass[] = new int[64];
		int wlmsubclass[][] = new int[MAX][MAX];
		for (i=0; i<MAX; i++) {
			wlmclass[i]=-1;
			for (j=0; j<MAX; j++)
				wlmsubclass[i][j]=-1;
		}
		
		String s[];
		for (i=0; i<names.length; i++) {
			s=names[i].split("\\.",2);
			if (s.length==1) {
				// WLM class
				j=0; while (wlmclass[j]>=0) j++;
				wlmclass[j]=i;				
			} else {
				// WLM subclass (class has already been read)
				j=0; while (s[0].compareTo(names[wlmclass[j]])!=0) j++;
				k=0; while (wlmsubclass[j][k]>=0) k++;
				wlmsubclass[j][k]=i;
			}
		}
		int numclasses=0;
		int numsubclasses=0;
		while (wlmclass[numclasses]>=0)
			numclasses++;
		String newNames[];
		
		
		// Disk usage by WPAR
		addSeparator(perfFrame[WPAR_DISK], html, "Global Disk I/O" );
		ds=new DataSet[numclasses];
		for (i=0; i<numclasses; i++)
			ds[i]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_DISK);
		newNames=new String[numclasses];
		for (i=0; i<numclasses; i++)
			newNames[i]=names[wlmclass[i]];
		panel=new StackPanel(configuration);
		((StackPanel)panel).setData(pd, ds, newNames);
		((StackPanel)panel).setMaxData(-1);
		publishPanel( perfFrame[WPAR_DISK], html, dir, panel,"Global Disk I/O");
		panels++;
			
		
		// Disk by WLM class
		for (i=0; i<numclasses; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_DISK);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[WPAR_DISK], html, dir, panel,newNames[i]);
			panels++;
		}
		
		// Disk by subclass (if any)
		for (i=0; i<numclasses; i++)  {
			if (wlmsubclass[i][0]==-1)
				continue;	// no subclass present
			numsubclasses=0;
			while (wlmsubclass[i][numsubclasses]>=0)
				numsubclasses++;
			
			addSeparator(perfFrame[WPAR_DISK], html, "Class "+names[wlmclass[i]] );
			
			// Disk usage: WPAR summary
			ds=new DataSet[numsubclasses];
			for (j=0; j<numsubclasses; j++)
				ds[j]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_DISK);	
			newNames=new String[numsubclasses];
			for (j=0; j<numsubclasses; j++)
				newNames[j]=names[wlmsubclass[i][j]];
			panel=new StackPanel(configuration);
			((StackPanel)panel).setData(pd, ds, newNames);
			((StackPanel)panel).setMaxData(pd.getData(PerfData.WPAR, wlmclass[i], PerfData.WPAR_DISK));
			publishPanel( perfFrame[WPAR_DISK], html, dir, panel,"Class "+names[wlmclass[i]]);
			panels++;
			
			// Disk by WLM subclass
			for (j=0; j<numsubclasses; j++) {		
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.WPAR, wlmsubclass[i][j], PerfData.WPAR_DISK);
				panel = new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[WPAR_DISK], html, dir, panel,newNames[j]);
				panels++;
			}
			
		}
		
		finishFrame(dir, WPAR_DISK, html, panels);
		return panels;		
	}	

	
	private int setupTopCPU_Graphic(String dir) {
		int i;
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, TOPCPU);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		names = pd.getNames(PerfData.TOPPROC_BY_NAME);
		
		if (names!=null) {
			ds = new DataSet[names.length];
			for (i=0; i<names.length; i++)
				ds[i] = pd.getData(PerfData.TOPPROC_BY_NAME, i, PerfData.TOP_CPU_BYNAME);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, names, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[TOPCPU], html, dir, panel, "Top Avg Processes");
			panels++;
		}
		
		
			
		// Top data
		
		for (i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.TOPPROC_BY_NAME, i, PerfData.TOP_CPU);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[TOPCPU], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, TOPCPU, html, panels);
		return panels;
	}	

	
	private int setupTopRAM_Graphic(String dir) {
		int i;
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, TOPRAM);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		names = pd.getNames(PerfData.TOPPROC_BY_NAME);
		
		if (names!=null) {
			ds = new DataSet[names.length];
			for (i=0; i<names.length; i++)
				ds[i] = pd.getData(PerfData.TOPPROC_BY_NAME, i, PerfData.TOP_RAM_BYNAME);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, names, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[TOPRAM], html, dir, panel, "Top Avg Processes");
			panels++;
		}
		
			
		// Top data
		
		for (i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.TOPPROC_BY_NAME, i, PerfData.TOP_RAM_BYNAME);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[TOPRAM], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, TOPRAM, html, panels);
		return panels;
	}	

	
	private int setupFS_Graphic(String dir) {
		int i;
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, FS);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		names = pd.getNames(PerfData.FS);	
			
		// Filesystem data
		
		for (i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.FS, i, PerfData.SPACEUSED);
			ds[1]=pd.getData(PerfData.FS, i, PerfData.INODEUSED);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[FS], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, FS, html, panels);
		return panels;
	}	

	
	private int setupPCPUGraphic(String dir) {
		int panels=0;
			
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, PCPU);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		byte smt_threads;
		DataSet ds[]=null;
		
		names = pd.getNames(PerfData.PCPU);
		smt_threads = pd.getSmt_threads();
		String labels[] = new String[4];
		labels[0]="Thread1";
		labels[1]="Thread2";
		labels[2]="Thread3";
		labels[3]="Thread4";
		
		if (smt_threads<=0 || names==null)
			return 0;
		
		// Average
		ds=new DataSet[1];
		ds[0]=pd.getData(PerfData.PCPU, 0, PerfData.P_TOT);
		panel = new XYPanel(configuration);
		panel.setData(pd, ds);
		publishPanel( perfFrame[PCPU], html, dir, panel,"Global");
		panels++;
		
		for (int i=1; i<names.length; i+=smt_threads) {	
			ds=new DataSet[smt_threads];
			ds[0]=pd.getData(PerfData.PCPU, i, PerfData.P_TOT);
			if (smt_threads>=2)
				ds[1]=pd.getData(PerfData.PCPU, i+1, PerfData.P_TOT);
			if (smt_threads==4) {
				ds[2]=pd.getData(PerfData.PCPU, i+2, PerfData.P_TOT);
				ds[3]=pd.getData(PerfData.PCPU, i+3, PerfData.P_TOT);
			}
			panel=new StackPanel(configuration);
			((StackPanel)panel).setData(pd, ds, labels);
			((StackPanel)panel).setMaxData(1);
			publishPanel( perfFrame[PCPU], html, dir, panel,"VP"+(i/smt_threads+1));
			panels++;
		}
		
		finishFrame(dir, PCPU, html, panels);
		return panels;
	
	}
	

	
	/*
	 * Create CPU panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */	
	private int setupCPUGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, CPU);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		String cecName = parser.getCecName();

		
		if (parser.getParserNames()!=null) {
			if (cecName!=null)
				addSeparator(perfFrame[CPU], html, "CEC "+cecName );
			else
				addSeparator(perfFrame[CPU], html, "LPARs / Systems" );
		}
		
		
		// Number of accounted LPARs
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.DED);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.SHARED);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.SHARED_DED);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel,"Number of LPARs");
				panels++;
			}
		}


		// CPU usage by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			String descr;
			
			if (cecName!=null)
				descr = "CEC CPU";
			else
				descr = "System/LPAR CPU";
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SYSTEM, 0, PerfData.PC)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.PC);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				DataSet max=pd.getData(PerfData.SYSTEM, 0, PerfData.TOT_CPU); 
				if (max == null) {
					max=pd.getData(PerfData.SYSTEM, 0, PerfData.POOL);
					descr+=" (uLPAR only)";
				} else
					descr+=" (DED included)";
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(max);
				publishPanel( perfFrame[CPU], html, dir, panel,descr);
				panels++;
			}
		}
		
		
		// Dedicated CPU usage by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			String descr;
			
			if (cecName!=null)
				descr = "Hint: CEC dedicated CPU usage";
			else
				descr = "Hint: System/LPAR dedicated CPU usage";
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SYSTEM, 0, PerfData.DED_PC)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.DED_PC);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[CPU], html, dir, panel,descr);
				panels++;
			}
		}
		
		
		// Stack PC and DED_PC if multiple LPARs and both statistics are present
		if (parser.getParserNames()!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.PC);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.DED_PC);
			names = new String[2];
			names[0]="uLPAR";
			names[1]="Hint on dedicated LPAR";
			if (numElementsNotNull(ds)==2) {
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[CPU], html, dir, panel,"uLPAR and hint on dedicated");
				panels++;
			}	
			
			names = pd.getNames(PerfData.SYSTEM);
			if (names!=null) {
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.GLOB_PC);
				if (numElementsNotNull(ds)>0) {
					panel=new XYPanel(configuration);
					panel.setData(pd, ds);
					publishPanel( perfFrame[CPU], html, dir, panel,"uLPAR + hint dedicated");	
					panels++;
				}
			}
		}

			 
		// CPU Performance
		names = pd.getNames(PerfData.CPU);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[4];
			ds[0]=pd.getData(PerfData.CPU, i, PerfData.US);
			ds[1]=pd.getData(PerfData.CPU, i, PerfData.SY);
			ds[2]=pd.getData(PerfData.CPU, i, PerfData.WA);
			ds[3]=pd.getData(PerfData.CPU, i, PerfData.ID);
			panel = new CPUPerfPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[CPU], html, dir, panel,names[i]);
			panels++;
		}
		
		
		// Processor usage
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			
			if (parser.getParserNames()!=null) {
				// Multiple LPARs: show POOL, not VPs
				ds=new DataSet[4];
				ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.POOL);
				ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.ENT);
				ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.PC);
				ds[3]=pd.getData(PerfData.SYSTEM, 0, PerfData.ENT_USED);
			} else {		
				ds=new DataSet[4];
				ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.VP);
				ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.ENT);
				ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.PC);
				ds[3]=pd.getData(PerfData.SYSTEM, 0, PerfData.PC_USED);
				//ds[4]=pd.getData(PerfData.SYSTEM, 0, PerfData.FOLDED);
			}
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel,"uLPARs: Processor Usage");
				panels++;
			}
		}
		
		
		// Folded CPUs
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			
			if (parser.getParserNames()!=null) {
				// Multiple LPARs: no folded info
				ds=null;
			} else {		
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FOLDED);
			}
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel,"uLPARs: Folded VPs");
				panels++;
			}
		}
		
		
		// Number of VPs configured
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null && parser.getParserNames()!=null) {
			// Show global VP counters
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.VP);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel,"Global VP");
				panels++;
			}
		}
		
		// Virtual Processor usage
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null && parser.getParserNames()==null) {		
			ds=new DataSet[4];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.VP_US);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.VP_SY);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.VP_WA);
			ds[3]=pd.getData(PerfData.SYSTEM, 0, PerfData.VP_ID);
			
			if (numElementsNotNull(ds)>0) {
				panel = new CPUPerfPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel, "uLPARs: VP Usage");
				panels++;
			}
		}
		
		
		// Processor usage FOR DEDICATED LPAR
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.DED_PC);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				String smt;
				switch (pd.getSmtStatus()) {
					case PerfData.SMT_PARSED:			smt = "SMT ok"; break;
					case PerfData.SMT_SUPPOSED_OFF:		smt = "SMT OFF?"; break;
					case PerfData.SMT_SUPPOSED_ON:		smt = "SMT ON?"; break;
					case PerfData.SMT_UNKNOWN:			smt = "SMT Unknown"; break;
					default:							smt = "SMT ??"; break;
				}
				publishPanel( perfFrame[CPU], html, dir, panel,"Hint: Processor Usage - "+smt);
				panels++;
			}
		}

		
		
		// Logical processors: only for single LPAR/System
		if (parser.getParserNames()==null) {
			names = pd.getNames(PerfData.SYSTEM);
			if (names!=null) {
				ds=new DataSet[1];
				ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.LP);
				if (numElementsNotNull(ds)>0) {
					panel=new XYPanel(configuration);
					panel.setData(pd, ds);
					publishPanel( perfFrame[CPU], html, dir, panel,"uLPARs: Logical CPU");	
					panels++;
				}
			}
		}
		
		
		// Entitlement usage
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.EC);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel,"Entitlement Usage");	
				panels++;
			}
		}
		
		
		// Pool usage
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.POOL);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.FREEPOOL);
			if (numElementsNotNull(ds)==2 && ds[0].getMax()>0 && ds[1].getMax()>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[CPU], html, dir, panel,"Pool Usage");	
				panels++;
			}
		}
		
		
		// Single CPU usage for multiple LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			
			boolean separatorAdded = false;
			
			SumXYPanel sumXYPanel = null;
			
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				ds=new DataSet[4];
				ds[0]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.VP);
				ds[1]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.ENT);
				ds[2]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.PC);
				ds[3]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.PC_USED);
				if (ds[2]==null)
					continue;	// skip if not uLPAR
				
				if (!separatorAdded) {
					if (p instanceof Parser_Lslparutil)
						addSeparator(perfFrame[CPU], html, "ALL LPARs: physical CPU" );
					else
						addSeparator(perfFrame[CPU], html, "uLPAR: physical CPU" );
					separatorAdded = true;
					
					// Add sum of CPU usage
					sumXYPanel = new SumXYPanel(pd, configuration);
					if (! (p instanceof Parser_Lslparutil) )
						sumXYPanel.setSmartComputing(true);		// data is not time aligned: interpolate
					publishPanel( perfFrame[CPU], html, dir, sumXYPanel, "Physical CPU of selected LPAR");
					panels++;
				}
				
				panel = new XYPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[CPU], html, dir, panel, "Phy-"+lparNames[i]);
				panels++;
				
				if (sumXYPanel != null) {
					ds = new DataSet[1];
					ds[0]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.PC);
					sumXYPanel.addPanel(ds, panel);
				}
			}
			
			if (sumXYPanel != null)
				sumXYPanel.computeData();
			
		}
		
		
		// Estimated CPU usage for multiple dedicated LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			String smt;
			
			boolean separatorAdded = false;
			
			SumXYPanel sumXYPanel = null;
			
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				ds=new DataSet[1];
				ds[0]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.DED_PC);
				if (ds[0]==null)
					continue;	// skip if not Dedicated
				
				if (!separatorAdded) {
					addSeparator(perfFrame[CPU], html, "Dedicated LPAR: estimated physical CPU (check SMT!)" );
					separatorAdded = true;
					
					// Add sum of CPU usage
					sumXYPanel = new SumXYPanel(pd, configuration);
					if (! (p instanceof Parser_Lslparutil) )
						sumXYPanel.setSmartComputing(true);		// data is not time aligned: interpolate
					publishPanel( perfFrame[CPU], html, dir, sumXYPanel, "Est. physical CPU of selected LPAR");
					panels++;
				}
							
				switch (pd_local.getSmtStatus()) {
					case PerfData.SMT_PARSED:			smt = "SMT ok"; break;
					case PerfData.SMT_SUPPOSED_OFF:		smt = "SMT OFF?"; break;
					case PerfData.SMT_SUPPOSED_ON:		smt = "SMT ON?"; break;
					case PerfData.SMT_UNKNOWN:			smt = "SMT OFF?"; break;
					default:							smt = "SMT ??"; break;
				}
				
				panel = new XYPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[CPU], html, dir, panel, "Phy-"+lparNames[i]+" - "+smt);
				panels++;
				
				if (sumXYPanel != null) {
					ds = new DataSet[1];
					ds[0]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.DED_PC);
					sumXYPanel.addPanel(ds, panel);
				}
			}
			
			if (sumXYPanel != null)
				sumXYPanel.computeData();
		}
			

		// Average CPU usage for multiple LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			
			boolean separatorAdded = false;
					
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				if (pd_local.getNames(PerfData.CPU) == null)
					continue;
				
				if (!separatorAdded) {
					addSeparator(perfFrame[CPU], html, "Avg CPU Usage" );
					separatorAdded = true;
				}
				
				ds=new DataSet[4];
				ds[0]=pd_local.getData(PerfData.CPU, 0, PerfData.US);
				ds[1]=pd_local.getData(PerfData.CPU, 0, PerfData.SY);
				ds[2]=pd_local.getData(PerfData.CPU, 0, PerfData.WA);
				ds[3]=pd_local.getData(PerfData.CPU, 0, PerfData.ID);
				panel = new CPUPerfPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[CPU], html, dir, panel, "Avg-"+lparNames[i]);
				panels++;
			}
		}
		
	
		// CPI for multiple LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			
			boolean separatorAdded = false;
					
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				ds=new DataSet[1];
				ds[0]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.CPI);
				if (ds[0]==null)
					continue;
				
				if (!separatorAdded) {
					addSeparator(perfFrame[CPU], html, "Cycles Per Instruction" );
					separatorAdded = true;
				}
				
				panel = new XYPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[CPU], html, dir, panel, "CPI-"+lparNames[i]);
				panels++;
			}
		}
		
		finishFrame(dir, CPU, html, panels);
		return panels;
		
		/*
		if (perfFrame[CPU]!=null)
			perfFrame[CPU].repaint();
		
		if (html!=null){
			html.println("</BODY>\n" +
							"</HTML>\n");
			html.close();			
		}
		*/
	
	}
			

/*	
	private void createCheckerFrame() {
		if (parser == null)
			return;
			
		checkerFrame = new CheckerFrame(parser);
		checkerFrame.setSize(FRAME_X, FRAME_Y);
		checkerFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		checkerFrame.addWindowListener(myEventHandler);
		checkerFrame.addPropertyChangeListener(myEventHandler);	
		checkerFrame.setVisible(true);		
		
	}
*/

			
	/*
	 * Create Disk busy panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */					
	private int setupDiskBusyGraphic(String dir) {
		int i;
		int panels=0;
	
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DISKBUSY);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		// Get disk names
		names = pd.getNames(PerfData.DISK);
		if (names==null)
			return panels;		

		// Prepare top avg diskbusy avoiding _Global, if and only if 2+ disks
		// In case of VIOS the virtual disks do not have disk busy data!
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_BUSY);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[DISKBUSY], html, dir, panel, "Top Avg Disk Busy");
			panels++;
		}
		
			
		// Disk busy		
		for (i=0; names!=null && i<names.length; i++) {	
			if (names[i].startsWith("_Global"))
				continue;		// Skip global disk busy: it's empty!
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_BUSY);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DISKBUSY], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, DISKBUSY, html, panels);
		return panels;
	}

	
	private int setupESSBusyGraphic(String dir) {
		int i;
		int panels=0;
	
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ESSBUSY);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		// Get disk names
		names = pd.getNames(PerfData.ESS);
		if (names==null)
			return panels;		

		// Prepare top avg diskbusy avoiding _Global, if and only if 2+ disks
		// In case of VIOS the virtual disks do not have disk busy data!
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.ESS, i, PerfData.ESS_BUSY);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[ESSBUSY], html, dir, panel, "Top Avg MPIO Busy");
			panels++;
		}
		
			
		// Disk busy		
		for (i=0; names!=null && i<names.length; i++) {	
			if (names[i].startsWith("_Global"))
				continue;		// Skip global disk busy: it's empty!
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.ESS, i, PerfData.ESS_BUSY);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ESSBUSY], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, ESSBUSY, html, panels);
		return panels;
	}


					
	
	private int setupDiskRWGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DISKRW);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
		
		names = pd.getNames(PerfData.DISK);
		
		// Prepare top avg R & W panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_READKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[DISKRW], html, dir, panel, "Top Avg Read");
			panels++;
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_WRITEKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[DISKRW], html, dir, panel, "Top Avg Write");
			panels++;
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[DISKRW], html, dir, sumXYPanel, "Selected disks");
			panels++;
		}
				
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_READKB);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_WRITEKB);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DISKRW], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, DISKRW, html, panels);
		return panels;
	}

	
	private int setupDacRWGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DACRW);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		names = pd.getNames(PerfData.DAC);
		
		// Prepare top avg R & W panels, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].endsWith("-utm"))  {
					ds[counter] = pd.getData(PerfData.DAC, i, PerfData.DSK_READKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[DACRW], html, dir, panel, "Top Avg Read");
			panels++;
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].endsWith("-utm"))  {
					ds[counter] = pd.getData(PerfData.DAC, i, PerfData.DSK_WRITEKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[DACRW], html, dir, panel, "Top Avg Write");
			panels++;
		}
				
		for (int i=0; names!=null && i<names.length; i++) {	
			if (names[i].endsWith("-utm"))
				continue;
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.DAC, i, PerfData.DSK_READKB);
			ds[1]=pd.getData(PerfData.DAC, i, PerfData.DSK_WRITEKB);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DACRW], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, DACRW, html, panels);
		return panels;
	}
				
	private int setupDiskXferGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DISKXFER);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		SumXYPanel sumXYPanel = null;
		
		// Read/s, Write/s, Xfer/s
		names = pd.getNames(PerfData.DISK);
		
		// Prepare top avg panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_RPS);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[DISKXFER], html, dir, panel, "Top Avg Read/sec");
				panels++;
			}
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_WPS);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[DISKXFER], html, dir, panel, "Top Avg Write/sec");
				panels++;
			}
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_XFER);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[DISKXFER], html, dir, panel, "Top Avg Transfers/sec");
				panels++;
			}
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[DISKXFER], html, dir, sumXYPanel, "Selected disks");
			panels++;
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_RPS);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_WPS);
			ds[2]=pd.getData(PerfData.DISK, i, PerfData.DSK_XFER);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DISKXFER], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, DISKXFER, html, panels);
		return panels;
	}		

	
	private int setupDacXferGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DACXFER);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		// Read/s, Write/s, Xfer/s
		names = pd.getNames(PerfData.DAC);
		
		// Prepare top avg panels, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].endsWith("-utm"))  {
					ds[counter] = pd.getData(PerfData.DAC, i, PerfData.DSK_RPS);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[DACXFER], html, dir, panel, "Top Avg Read/sec");
				panels++;
			}
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].endsWith("-utm"))  {
					ds[counter] = pd.getData(PerfData.DAC, i, PerfData.DSK_WPS);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[DACXFER], html, dir, panel, "Top Avg Write/sec");
				panels++;
			}
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].endsWith("-utm"))  {
					ds[counter] = pd.getData(PerfData.DAC, i, PerfData.DSK_XFER);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[DACXFER], html, dir, panel, "Top Avg Transfers/sec");
				panels++;
			}
		}
		
		for (int i=0; names!=null && i<names.length; i++) {	
			if (names[i].endsWith("-utm"))
				continue;
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DAC, i, PerfData.DSK_RPS);
			ds[1]=pd.getData(PerfData.DAC, i, PerfData.DSK_WPS);
			ds[2]=pd.getData(PerfData.DAC, i, PerfData.DSK_XFER);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DACXFER], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, DACXFER, html, panels);
		return panels;
	}
	
				
	private int setupDiskBlockGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DISKBLOCK);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		// Only disk block!!
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {	
			if (names[i].startsWith("_Global"))
				continue;
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_BSIZE);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DISKBLOCK], html, dir, panel,names[i]);
			panels++;
		}
		
		finishFrame(dir, DISKBLOCK, html, panels);
		return panels;
	}


					
						
	private int setupEssRWGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ESSRW);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;		
		
		SumXYPanel sumXYPanel = null;
			
		// ESS read/write
		names = pd.getNames(PerfData.ESS);
		
		// Prepare top avg R & W panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.ESS, i, PerfData.ESS_READKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[ESSRW], html, dir, panel, "Top Avg Read");
			panels++;
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.ESS, i, PerfData.ESS_WRITEKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[ESSRW], html, dir, panel, "Top Avg Write");
			panels++;
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[ESSRW], html, dir, sumXYPanel, "Selected disks");
			panels++;
		}
		
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.ESS, i, PerfData.ESS_READKB);
			ds[1]=pd.getData(PerfData.ESS, i, PerfData.ESS_WRITEKB);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ESSRW], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, ESSRW, html, panels);
		return panels;
	}

	

	
	private int setupAdapterRWGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADAPTERRW);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
			
		names = pd.getNames(PerfData.SCSI);
		
		// Prepare top avg R & W panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_READKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADAPTERRW], html, dir, panel, "Top Avg Dsk Read");
				panels++;
			}
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_WRITEKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADAPTERRW], html, dir, panel, "Top Avg Dsk Write");
				panels++;
			}
			
			if (counter>2) {
				sumXYPanel = new SumXYPanel(pd, configuration);
				publishPanel( perfFrame[ADAPTERRW], html, dir, sumXYPanel, "Selected adapters");
				panels++;
			}
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			if (names.length==2 && names[i].startsWith("_Global"))
				continue;	// skip _global if only 1 adapter
			
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_READKB);
			ds[1]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_WRITEKB);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADAPTERRW], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
		
		
		
		
		// DISK READ by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_READKB)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_READKB);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[ADAPTERRW], html, dir, panel,"Global disk read");
				panels++;
			}
		}
		
		// DISK WRITE by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_WRITEKB)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_WRITEKB);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[ADAPTERRW], html, dir, panel,"Global disk write");
				panels++;
			}
		}
		
		
		// Disk RW for multiple LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				ds=new DataSet[2];
				ds[0]=pd_local.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_READKB);
				ds[1]=pd_local.getData(PerfData.SCSI, "_Global_Disk_Adapter", PerfData.SCSI_WRITEKB);
				if (ds[0]==null) {
					// There is only one adapter!
					ds[0]=pd_local.getData(PerfData.SCSI, 0, PerfData.SCSI_READKB);
					ds[1]=pd_local.getData(PerfData.SCSI, 0, PerfData.SCSI_WRITEKB);
				}
				
				panel = new XYPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[ADAPTERRW], html, dir, panel, "Disk-"+lparNames[i]);
				panels++;
			}
		}
		
		
		
			
		finishFrame(dir, ADAPTERRW, html, panels);
		return panels;
	}

		
	private int setupAdapterXferGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADAPTERXFER);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
			
		// Disk busy
		names = pd.getNames(PerfData.SCSI);
		
		// Prepare top avg panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_RPS);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADAPTERXFER], html, dir, panel, "Top Avg Read/sec");
				panels++;
			}

			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_WPS);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADAPTERXFER], html, dir, panel, "Top Avg Write/sec");
				panels++;
			}
		
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_XFER);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADAPTERXFER], html, dir, panel, "Top Avg Transfers/sec");
				panels++;
			}
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[ADAPTERXFER], html, dir, sumXYPanel, "Selected adapters");
			panels++;
			
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_RPS);
			ds[1]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_WPS);
			ds[2]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_XFER);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADAPTERXFER], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, ADAPTERXFER, html, panels);
		return panels;
	}	

		
	
	private int setupFibreRWGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, FCRW);
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
			
		names = pd.getNames(PerfData.FCSTAT);
		
		// Prepare top avg R & W panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.FCSTAT, i, PerfData.FCREAD);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[FCRW], html, dir, panel, "Top Avg FC Read");
			panels++;
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.FCSTAT, i, PerfData.FCWRITE);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[FCRW], html, dir, panel, "Top Avg FC Write");
			panels++;
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[FCRW], html, dir, sumXYPanel, "Selected adapters");
			panels++;
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.FCSTAT, i, PerfData.FCREAD);
			ds[1]=pd.getData(PerfData.FCSTAT, i, PerfData.FCWRITE);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[FCRW], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, FCRW, html, panels);
		return panels;
	}

	
	
	private int setupFibreXferGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, FCXFER);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
			
		// Disk busy
		names = pd.getNames(PerfData.FCSTAT);
		
		// Prepare top avg panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.FCSTAT, i, PerfData.FCXFERIN);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[FCXFER], html, dir, panel, "Top Avg FC TransfersIN/sec");
			panels++;
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.FCSTAT, i, PerfData.FCXFEROUT);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[FCXFER], html, dir, panel, "Top Avg FC TransfersOUT/sec");
			panels++;
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[FCXFER], html, dir, sumXYPanel, "Selected adapters");
			panels++;
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.FCSTAT, i, PerfData.FCXFERIN);
			ds[1]=pd.getData(PerfData.FCSTAT, i, PerfData.FCXFEROUT);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[FCXFER], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, FCXFER, html, panels);
		return panels;
	}	




				
   private int setupNetworkRWGraphic(String dir) {
	   int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, NETWORKRW);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
			

		names = pd.getNames(PerfData.NETWORK);
		
		// Prepare top avg R & W panels avoiding _Global & loopback, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global") && !names[i].startsWith("lo"))  {
					ds[counter] = pd.getData(PerfData.NETWORK, i, PerfData.NET_READKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[NETWORKRW], html, dir, panel, "Top Avg Read");
			panels++;
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global") && !names[i].equals("lo0"))  {
					ds[counter] = pd.getData(PerfData.NETWORK, i, PerfData.NET_WRITEKB);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			panel = new XYPanel(configuration);
			panel.setData(pd, ds, namesLocal, 5);
			((XYPanel)panel).setForbidErrorbars(true);
			publishPanel( perfFrame[NETWORKRW], html, dir, panel, "Top Avg Write");
			panels++;
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[NETWORKRW], html, dir, sumXYPanel, "Selected adapters");
			panels++;
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.NETWORK, i, PerfData.NET_READKB);
			ds[1]=pd.getData(PerfData.NETWORK, i, PerfData.NET_WRITEKB);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[NETWORKRW], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global") && !names[i].equals("lo0"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
		
		
		// NET READ by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_READKB)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_READKB);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[NETWORKRW], html, dir, panel,"Global network read");
				panels++;
			}
		}
		
		// NETWORK WRITE by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_WRITEKB)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_WRITEKB);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[NETWORKRW], html, dir, panel,"Global network write");
				panels++;
			}
		}
		
		
		// NET RW for multiple LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				ds=new DataSet[2];
				ds[0]=pd_local.getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_READKB);
				ds[1]=pd_local.getData(PerfData.NETWORK, "_Global_Network", PerfData.NET_WRITEKB);
				
				// if only one network + lo0, get the right adapter
				if (ds[0]==null && ds[1]==null) {
					String s[] = pd_local.getNames(PerfData.NETWORK);
					if (s!=null && s.length==2) {
						if (s[0].startsWith("lo")) {
							ds[0]=pd_local.getData(PerfData.NETWORK, 1, PerfData.NET_READKB);
							ds[1]=pd_local.getData(PerfData.NETWORK, 1, PerfData.NET_WRITEKB);
						} else {
							ds[0]=pd_local.getData(PerfData.NETWORK, 0, PerfData.NET_READKB);
							ds[1]=pd_local.getData(PerfData.NETWORK, 0, PerfData.NET_WRITEKB);
						}
					}
				}
				
				panel = new XYPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[NETWORKRW], html, dir, panel, "Net-"+lparNames[i]);
				panels++;
			}
		}
		
		
			
		finishFrame(dir, NETWORKRW, html, panels);
		return panels;
   }	

   
   
   private int setupSEAGraphic(String dir) {
	   int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, SEA);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;			

		names = pd.getNames(PerfData.SEA);
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SEA, i, PerfData.NET_READKB);
			ds[1]=pd.getData(PerfData.SEA, i, PerfData.NET_WRITEKB);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[SEA], html, dir, panel,names[i]);
			panels++;
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SEA, i, PerfData.NET_READS);
			ds[1]=pd.getData(PerfData.SEA, i, PerfData.NET_WRITES);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[SEA], html, dir, panel,names[i]);
			panels++;
		}
		
			
		finishFrame(dir, SEA, html, panels);
		return panels;
   }


		   	
	private int setupEssXferGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ESSXFER);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
		
		SumXYPanel sumXYPanel = null;
			
		// ESS XFER
		names = pd.getNames(PerfData.ESS);
		
		// Prepare top avg panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length-1];
			ds = new DataSet[names.length-1];
			
			counter=0;
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.ESS, i, PerfData.ESS_XFER);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			if (counter>2) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ESSXFER], html, dir, panel, "Top Avg Transfers/sec");
				panels++;
			}
			
			sumXYPanel = new SumXYPanel(pd, configuration);
			publishPanel( perfFrame[ESSXFER], html, dir, sumXYPanel, "Selected disks");
			panels++;
		}
		
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.ESS, i, PerfData.ESS_XFER);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ESSXFER], html, dir, panel,names[i]);
			panels++;
			
			if (sumXYPanel != null && !names[i].startsWith("_Global"))
				sumXYPanel.addPanel(ds, panel);
		}
		
		if (sumXYPanel != null)
			sumXYPanel.computeData();
			
		finishFrame(dir, ESSXFER, html, panels);
		return panels;
	}	
	
	
	
	/*
	 * Create Memory panels bye page size and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */	
	private int setupMemDetailsGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, MEMDETAILS);	
				
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	

		// 4K Pages
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMFRAMES4K);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMFRB4K);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMVPAGES4K);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[MEMDETAILS], html, dir, panel,"4KB Memory");
				panels++;
			}
		}
		
		// 4K Pages paging
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.PI4K);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.PO4K);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[MEMDETAILS], html, dir, panel,"4KB Memory Paging");
				panels++;
			}
		}
		
		// 64K Pages
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMFRAMES64K);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMFRB64K);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMVPAGES64K);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[MEMDETAILS], html, dir, panel,"64KB Memory");
				panels++;
			}
		}
		
		// 64K Pages paging
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.PI64K);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.PO64K);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[MEMDETAILS], html, dir, panel,"64KB Memory Paging");
				panels++;
			}
		}
		
		// Page faults
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FAULTS4K);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.FAULTS64K);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[MEMDETAILS], html, dir, panel,"Page Faults by size");
				panels++;
			}
		}
		
		finishFrame(dir, MEMDETAILS, html, panels);
		return panels;
	}
	

	/*
	 * Create Memory panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */	
	private int setupMemoryGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, RAM);	
				
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	

		// Average Virtual Memory
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.AVM);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Avg Virt Mem");
				panels++;
			}
		}
		
		
		// Physical memory
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FRE);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.RAM);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Physical Memory");
				panels++;
			}
		}

		
		// Paging activity
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.PI);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.PO);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Paging Space");
				panels++;
			}
		}
		
		
		// File paging activity
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FI);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.FO);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"File paging activity");
				panels++;
			}
		}
		
		
		// VMM activity
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FR);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.SR);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.CY);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"VMM Activity");
				panels++;
			}
		}
		
		
		// Page faults
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FAULTS);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Page faults");
				panels++;
			}
		}
		
		
		// Page scan/free
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.SRFR_RATIO);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Page scan-free ratio");
				panels++;
			}
		}


		// Permanent Pages
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMPERM);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.MINPERM);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.MAXPERM);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Permanent Pages");
				panels++;
			}
		}
		
		
		// Permanent Pages
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMCLIENT);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.MAXCLIENT);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Client Pages");
				panels++;
			}
		}
		

		// Free pages
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUMFREE);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.MINFREE);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.MAXFREE);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Free Pages");
				panels++;
			}
		}
		
		
		// Large pages
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.USED_LP);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.FREE_LP);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Large Pages");
				panels++;
			}
		}
		
		
		// Logical Memory
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.PHYS_MEM);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.LOGICAL_MEM);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.LOAN_MEM);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Active Shared Memory");
				panels++;
			}
		}
		
		
		// Compressed Memory (AME)
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[4];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.EXP_MEM);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.TRUE_MEM);
			ds[2]=pd.getData(PerfData.SYSTEM, 0, PerfData.UNC_POOL);
			ds[3]=pd.getData(PerfData.SYSTEM, 0, PerfData.COMP_POOL);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Active Memory Expansion");
				panels++;
			}
		}
		
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.CP_PI);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.CP_PO);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Comp Memory Pool Paging");
				panels++;
			}
		}
		
		
		
		// Logical Memory usage by LPAR (physical part only)
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SYSTEM, 0, PerfData.PHYS_MEM)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.PHYS_MEM);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[RAM], html, dir, panel,"Shared Memory Pool: physical memory usage");
				panels++;
				
				for (int i=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					ds=new DataSet[3];
					ds[0]=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.PHYS_MEM);
					ds[1]=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.LOGICAL_MEM);
					ds[2]=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.LOAN_MEM);
					if (numElementsNotNull(ds)>0) {
						panel=new XYPanel(configuration);
						panel.setData(pd, ds);
						publishPanel( perfFrame[RAM], html, dir, panel,lparNames[i]+": Active Shared Memory");
						panels++;
					}
					
				}
			}
		}
		
		
		// Hypervisor activity
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.HYPPAG_IN);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Hypervisor Page In");
				panels++;
			}
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.HYPPAG_TIME);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[RAM], html, dir, panel,"Hypervisor Page In Time");
				panels++;
			}
			
		}
		
		
		// Hypervisor activity by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SYSTEM, 0, PerfData.HYPPAG_IN)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.HYPPAG_IN);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[RAM], html, dir, panel,"Hypervisor Page In by LPAR");
				panels++;
			}
		}
		
		
		
		
		// RAM usage by LPAR
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;
			
			// Count valid parsers
			int num=0; 
			for (int i=0; i<lparNames.length; i++)
				if (parser.getParser(i)!=null &&
						parser.getParser(i).getPerfData().getData(PerfData.SYSTEM, 0, PerfData.USEDMEM)!=null)
					num++;
			if (num>0) {
				ds=new DataSet[num];
				names=new String[num];
				DataSet dsp;
				
				for (int i=0, j=0; i<lparNames.length; i++) {
					p=parser.getParser(i);
					if (p==null)
						continue;
					dsp=p.getPerfData().getData(PerfData.SYSTEM, 0, PerfData.USEDMEM);
					if (dsp==null)
						continue;
					ds[j]=dsp;
					names[j]=lparNames[i];
					j++;
				}
				
				panel=new StackPanel(configuration);
				((StackPanel)panel).setData(pd, ds, names);
				((StackPanel)panel).setMaxData(null);
				publishPanel( perfFrame[RAM], html, dir, panel,"Global RAM Usage");
				panels++;
			}
		}
		
		
		// Free RAM for multiple LPARs
		if (parser.getParserNames()!=null) {
			String lparNames[] = parser.getParserNames();
			Parser p=null;			
			PerfData pd_local;
			
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.USEDMEM);
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[RAM], html, dir, panel, "Globally Used MB");
			panels++;
			
			for (int i=0; i<lparNames.length; i++) {
				p=parser.getParser(i);
				if (p==null)
					continue;
				pd_local = p.getPerfData();
				ds=new DataSet[1];
				ds[0]=pd_local.getData(PerfData.SYSTEM, 0, PerfData.USEDMEM);
				panel = new XYPanel(configuration);
				panel.setData(pd_local, ds);
				publishPanel( perfFrame[RAM], html, dir, panel, "Used MB-"+lparNames[i]);
				panels++;
			}
		}
		
		finishFrame(dir, RAM, html, panels);
		return panels;
	}
	

	
	
	/*
	 * Create Kernel panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */	
	private int setupKernelGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, KERNEL);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;			
					
		// Run queue & swap queue
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.RUNQ);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.SWQ);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[KERNEL], html, dir, panel,"KernelQ");
				panels++;
			}
		}
		
		// Threads waiting for I/O
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.WQPS);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[KERNEL], html, dir, panel,"Threads waiting for I/O");
				panels++;
			}
		}
		
				
		// Process Switches
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.PSW);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[KERNEL], html, dir, panel,"Process Switch");
				panels++;
			}
		}
		
		finishFrame(dir, KERNEL, html, panels);
		return panels;
	}	
	
	
	private int setupAIO_Graphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, AIO);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
					
		// Number of AIO procs (total & active)
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.NUM_AIO);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.ACTIVE_AIO);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[AIO], html, dir, panel,"Async IO Processes");
				panels++;
			}
		}
		
				
		// AIO CPU usage
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.CPU_AIO);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[AIO], html, dir, panel,"Async IO CPU Usage");
				panels++;
			}
		}
		
		finishFrame(dir, AIO, html, panels);
		return panels;
	}	
	
	 
	

	/* 
	 * Create System call panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */
	private int setupSyscallGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, SYSCALL);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
			
					
		// Total
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.SYSC);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[SYSCALL], html, dir, panel,"All calls");
				panels++;
			}
		}
		
		
		// Fork - Exec
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.FORK);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.EXEC);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[SYSCALL], html, dir, panel,"Fork&Exec");
				panels++;
			}
		}
		
		
		// Read & Write
		names = pd.getNames(PerfData.SYSTEM);
		if (names!=null) {
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.READ);
			ds[1]=pd.getData(PerfData.SYSTEM, 0, PerfData.WRITE);
			if (numElementsNotNull(ds)>0) {
				panel=new XYPanel(configuration);
				panel.setData(pd, ds);
				publishPanel( perfFrame[SYSCALL], html, dir, panel,"Read&Write");
				panels++;
			}
		}
					
		finishFrame(dir, SYSCALL, html, panels);
		return panels;
	}
	
	
	
	
	
	private int setupDiskReadServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, RSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		AvgSumXYPanel avgSumXYPanel = null;
		
		names = pd.getNames(PerfData.DISK);
		
		// Prepare top avg R panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_R);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>1) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[RSERVICE], html, dir, panel, "Top Avg Read Service");
				panels++;
				
				avgSumXYPanel = new AvgSumXYPanel(pd, configuration);
				publishPanel( perfFrame[RSERVICE], html, dir, avgSumXYPanel, "Selected disks, weighted avg");
				panels++;
			}
		}
		
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_MIN_R);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_MAX_R);
			ds[2]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_R);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[RSERVICE], html, dir, panel,names[i]);
			panels++;
			
			DataSet wds = pd.getData(PerfData.DISK, i, PerfData.DSK_RPS);
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_R);
			if (avgSumXYPanel != null && !names[i].startsWith("_Global") && wds!=null)
				avgSumXYPanel.addPanel(ds, wds, panel);
		}
		
		if (avgSumXYPanel != null)
			avgSumXYPanel.computeData();
			
		finishFrame(dir, RSERVICE, html, panels);
		return panels;
	}	
	
	
	
	
	private int setupDiskReadWriteServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, RWSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		AvgSumXYPanel avgSumXYPanel = null;
		
		names = pd.getNames(PerfData.DISK);
		
		// Prepare top avg RW panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_RW);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>1) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[RWSERVICE], html, dir, panel, "Top Avg Read+Write Service");
				panels++;
				
				avgSumXYPanel = new AvgSumXYPanel(pd, configuration);
				publishPanel( perfFrame[RWSERVICE], html, dir, avgSumXYPanel, "Selected disks, weighted avg");
				panels++;
			}
		}
		
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_RW);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[RWSERVICE], html, dir, panel,names[i]);
			panels++;
			
			DataSet wds = pd.getData(PerfData.DISK, i, PerfData.DSK_RWPS);
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_RW);
			if (avgSumXYPanel != null && !names[i].startsWith("_Global") && wds!=null)
				avgSumXYPanel.addPanel(ds, wds, panel);
		}
		
		if (avgSumXYPanel != null)
			avgSumXYPanel.computeData();
			
		finishFrame(dir, RWSERVICE, html, panels);
		return panels;
	}	
	
	
	
	private int setupAdapterReadServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADRSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		AvgSumXYPanel avgSumXYPanel = null;
		
		names = pd.getNames(PerfData.SCSI);
		
		// Prepare top avg R panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_R);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>1) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADRSERVICE], html, dir, panel, "Top Avg Read Service");
				panels++;
				
				avgSumXYPanel = new AvgSumXYPanel(pd, configuration);
				publishPanel( perfFrame[ADRSERVICE], html, dir, avgSumXYPanel, "Selected adapter, weighted avg");
				panels++;
			}
		}
			
		// Disk busy
		names = pd.getNames(PerfData.SCSI);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_MIN_R);
			ds[1]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_MAX_R);
			ds[2]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_R);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADRSERVICE], html, dir, panel,names[i]);
			panels++;
			
			DataSet wds = pd.getData(PerfData.SCSI, i, PerfData.SCSI_RPS);
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_R);
			if (avgSumXYPanel != null && !names[i].startsWith("_Global") && wds!=null)
				avgSumXYPanel.addPanel(ds, wds, panel);
		}
		
		if (avgSumXYPanel != null)
			avgSumXYPanel.computeData();
			
		finishFrame(dir, ADRSERVICE, html, panels);
		return panels;
	}	
	
	private int setupAdapterWriteServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADWSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		AvgSumXYPanel avgSumXYPanel = null;
		
		names = pd.getNames(PerfData.SCSI);
		
		// Prepare top avg R panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_W);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>1) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[ADWSERVICE], html, dir, panel, "Top Avg Write Service");
				panels++;
				
				avgSumXYPanel = new AvgSumXYPanel(pd, configuration);
				publishPanel( perfFrame[ADWSERVICE], html, dir, avgSumXYPanel, "Selected adapter, weighted avg");
				panels++;
			}
		}
			
		// Disk busy
		names = pd.getNames(PerfData.SCSI);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_MIN_W);
			ds[1]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_MAX_W);
			ds[2]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_W);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADWSERVICE], html, dir, panel,names[i]);
			panels++;

			DataSet wds = pd.getData(PerfData.SCSI, i, PerfData.SCSI_WPS);
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_W);
			if (avgSumXYPanel != null && !names[i].startsWith("_Global") && wds!=null)
				avgSumXYPanel.addPanel(ds, wds, panel);
		}
		
		if (avgSumXYPanel != null)
			avgSumXYPanel.computeData();
			
		finishFrame(dir, ADWSERVICE, html, panels);
		return panels;
	}	
	
	
	private int setupDiskWriteServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, WSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;
		
		AvgSumXYPanel avgSumXYPanel = null;
		
		names = pd.getNames(PerfData.DISK);
		
		// Prepare top avg R panels avoiding _Global, if and only if 2+ disks
		if (names!=null && names.length>1) {
			int counter=0;
			String namesLocal[] = new String[names.length];
			ds = new DataSet[names.length];
			for (int i=0; i<names.length; i++)
				if (!names[i].startsWith("_Global"))  {
					ds[counter] = pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_W);
					if (ds[counter]==null)
						continue;
					namesLocal[counter] = names[i];
					counter++;
				}
			
			if (counter>1) {
				panel = new XYPanel(configuration);
				panel.setData(pd, ds, namesLocal, 5);
				((XYPanel)panel).setForbidErrorbars(true);
				publishPanel( perfFrame[WSERVICE], html, dir, panel, "Top Avg Write Service");
				panels++;
				
				avgSumXYPanel = new AvgSumXYPanel(pd, configuration);
				publishPanel( perfFrame[WSERVICE], html, dir, avgSumXYPanel, "Selected disks, weighted avg");
				panels++;
			}
		}
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_MIN_W);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_MAX_W);
			ds[2]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_W);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[WSERVICE], html, dir, panel,names[i]);
			panels++;
			
			DataSet wds = pd.getData(PerfData.DISK, i, PerfData.DSK_WPS);
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_W);
			if (avgSumXYPanel != null && !names[i].startsWith("_Global") && wds!=null)
				avgSumXYPanel.addPanel(ds, wds, panel);
		}
		
		if (avgSumXYPanel != null)
			avgSumXYPanel.computeData();
			
		finishFrame(dir, WSERVICE, html, panels);
		return panels;
	}	
	
	
	
	private int setupDiskServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DSKSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;

		
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVGSERV);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DSKSERVICE], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, DSKSERVICE, html, panels);
		return panels;
	}	
	
	private int setupEssServiceGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ESSSERVICE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.ESS);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.ESS, i, PerfData.ESS_AVGSERV);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ESSSERVICE], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, ESSSERVICE, html, panels);
		return panels;
	}	
	
	private int setupDiskWaitGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, DSKWAIT);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVGWAIT);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[DSKWAIT], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, DSKWAIT, html, panels);
		return panels;
	}	
	
	private int setupEssWaitGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ESSWAIT);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;		
			
		// Disk busy
		names = pd.getNames(PerfData.ESS);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.ESS, i, PerfData.ESS_AVGWAIT);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ESSWAIT], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, ESSWAIT, html, panels);
		return panels;
	}	
	
	
	private int setupDiskRWTimeoutGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, RWTO);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_TO_R);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_TO_W);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[RWTO], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, RWTO, html, panels);
		return panels;
	}	
	
	private int setupDiskRWFailedGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, RWFAIL);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_FAIL_R);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_FAIL_W);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[RWFAIL], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, RWFAIL, html, panels);
		return panels;
	}	
	
	private int setupDiskWaitQTimeGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, WQTIME);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_MIN_T);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_MAX_T);
			ds[2]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_T);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[WQTIME], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, WQTIME, html, panels);
		return panels;
	}	
	
	
	private int setupAdapterWaitQTimeGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADWQTIME);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.SCSI);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[3];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_MIN_T);
			ds[1]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_MAX_T);
			ds[2]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_T);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADWQTIME], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, ADWQTIME, html, panels);
		return panels;
	}	
	
	
	
	private int setupDiskQueueSizeGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, QSIZE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;		
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_WQ);
			ds[1]=pd.getData(PerfData.DISK, i, PerfData.DSK_AVG_SQ);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[QSIZE], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, QSIZE, html, panels);
		return panels;
	}	
	
	
	private int setupAdapterQueueSizeGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADQSIZE);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;		
			
		// Adapter Q size
		names = pd.getNames(PerfData.SCSI);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[2];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_WQ);
			ds[1]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_AVG_SQ);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADQSIZE], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, ADQSIZE, html, panels);
		return panels;
	}	
	
	
	
	private int setupDiskServiceQFullGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, SQFULL);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;	
			
		// Disk busy
		names = pd.getNames(PerfData.DISK);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.DISK, i, PerfData.DSK_FULLQ);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[SQFULL], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, SQFULL, html, panels);
		return panels;
	}	
	
	
	private int setupAdapterServiceQFullGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, ADSQFULL);	
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];
		DataSet ds[]=null;		
			
		// Disk busy
		names = pd.getNames(PerfData.SCSI);
		for (int i=0; names!=null && i<names.length; i++) {		
			ds=new DataSet[1];
			ds[0]=pd.getData(PerfData.SCSI, i, PerfData.SCSI_FULLQ);
			if (numElementsNotNull(ds)==0)
				continue;
			panel = new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[ADSQFULL], html, dir, panel,names[i]);
			panels++;
		}
			
		finishFrame(dir, ADSQFULL, html, panels);
		return panels;
	}
	
	
	
	/*
	 * Create POOL panels and publish them on screen or in HTML.
	 * Input file(s) must be already parsed.
	 * If dir!=null only HTML, otherwise only screen
	 * Only available data is shown
	 */	
	private int setupPoolGraphic(String dir) {
		int panels=0;
		
		// If no parser is available return
		if (parser == null)
			return panels;
		
		// Setup HTML if required
		PrintWriter html = setupFrame(dir, PROCPOOL);
		
		
		// Support variables and values
		GenericPanel panel=null;
		PerfData pd = parser.getPerfData();
		String names[];							// Processor pool names
		DataSet ds[]=null;						
		
		
		int num, i, j, k, w;

		
		names = pd.getNames(PerfData.PROCPOOL);
		if (names!=null) {
			addSeparator(perfFrame[PROCPOOL], html, "Pool Summary" );
		} else
			return panels; // There must be multiple LPAR to show Proc Pool data!
		
		// Stack procpool data
		ds=new DataSet[names.length];
		for (i=0; i<names.length; i++)
			ds[i] = pd.getData(PerfData.PROCPOOL, i, PerfData.POOLUSED);
		panel=new StackPanel(configuration);
		((StackPanel)panel).setData(pd, ds, names);
		((StackPanel)panel).setMaxData(pd.getData(PerfData.SYSTEM, 0, PerfData.POOL));
		publishPanel( perfFrame[PROCPOOL], html, dir, panel,"Pool Summary");
		panels++;
		
		// Show single pool data
		for (i=0; i<names.length; i++) {
			ds=new DataSet[2];
			
			// For unknown reasons POWER7 provides wrong data on DefaultPool size
			if (names[i].equals("DefaultPool"))
				ds[0]=pd.getData(PerfData.SYSTEM, 0, PerfData.POOL);
			else
				ds[0]=pd.getData(PerfData.PROCPOOL, i, PerfData.POOLSIZE);
			
			ds[1]=pd.getData(PerfData.PROCPOOL, i, PerfData.POOLUSED);
			panel=new XYPanel(configuration);
			panel.setData(pd, ds);
			publishPanel( perfFrame[PROCPOOL], html, dir, panel,names[i]);
			panels++;
		}
		
		String lparNames[] = parser.getParserNames();
		String selectedLpars[];			// LPARs within the same proc pool
		String lparPools[];				// all pools the LPAR has belonged
		Parser p=null;
		
		// Show details of each proc pool
		for (i=0; i<names.length; i++) {
			addSeparator(perfFrame[PROCPOOL], html, names[i] );
			
			// Stack LPAR data belonging to proc pool
			if (parser.getParserNames()!=null) {
							
				// Count valid parsers
				num=0; 
				for (j=0; j<lparNames.length; j++) {
					if (parser.getParser(j)==null)
						continue;
					lparPools = parser.getParser(j).getPerfData().getNames(PerfData.PROCPOOL);
					if (lparPools==null)
						continue;
					for (k=0; k<lparPools.length; k++)
						if (lparPools[k].compareTo(names[i])==0)
							num++;
				}
				if (num>0) {
					ds=new DataSet[num];
					selectedLpars=new String[num];
					DataSet dsp;
					
					for (j=0, k=0; j<lparNames.length; j++) {
						p=parser.getParser(j);
						if (p==null)
							continue;
						lparPools = parser.getParser(j).getPerfData().getNames(PerfData.PROCPOOL);
						if (lparPools==null)
							continue;
						for (w=0; w<lparPools.length; w++)
							if (lparPools[w].compareTo(names[i])==0) {
								dsp=p.getPerfData().getData(PerfData.PROCPOOL, w, PerfData.POOLUSED);
								if (dsp==null)
									continue;
								ds[k]=dsp;
								selectedLpars[k]=lparNames[j];
								k++;
								break;
							}
					}
					
					// For unknown reasons POWER7 provides wrong data on DefaultPool size
					DataSet max;
					if (names[i].equals("DefaultPool"))
						max=pd.getData(PerfData.SYSTEM, 0, PerfData.POOL);
					else
						max=pd.getData(PerfData.PROCPOOL, i, PerfData.POOLSIZE);

					panel=new StackPanel(configuration);
					((StackPanel)panel).setData(pd, ds, selectedLpars);
					((StackPanel)panel).setMaxData(max);
					publishPanel( perfFrame[PROCPOOL], html, dir, panel,names[i]+" Summary");
					panels++;
					
					for (j=0; j<num; j++) {
						panel=new XYPanel(configuration);
						DataSet ds1[] = new DataSet[1];
						ds1[0] = ds[j];
						panel.setData(pd, ds1);
						publishPanel( perfFrame[PROCPOOL], html, dir, panel,selectedLpars[j]);
						panels++;
					}
				}
			}
		}
	
		
		finishFrame(dir, PROCPOOL, html, panels);
		return panels;	
	}
			

	
	
	
	private void loadCECDirectory(File fileObj) {
		
		if (parserManager.running()) {
			System.out.println("Wait for parser to finish.");
			return;
		}
		
		// Reset all variable data
		closeAllFrames();
		parser = null;
		
		// Avoid change of sliders
		//settingUp = true;
		
		// Reset GUI
		activateButtons();
		
		// Start thread managing data
		parserManager.setSource(fileObj);
		parserManager.setSingleHost(false);
		if (batchMode)
			//parserManager.setLimits(begin, end);
			//parserManager.setLimits(timePanel.getMinZoom(), timePanel.getMaxZoom());
			parserManager.setLimits(batchBegin, batchEnd);
		Thread th = new Thread(parserManager);
		th.start();

	}
	
	
	private void loadConfigurationFile(File fileObj) {
		
		if (parserManager.running()) {
			System.out.println("Wait for parser to finish.");
			return;
		}
		
		// Reset all variable data
		closeAllFrames();
		parser = null;
		
		// Avoid change of sliders
		//settingUp = true;
		
		// Reset GUI
		activateButtons();
		
		// Start thread managing data
		parserManager.setSingleHost(false);
		parserManager.setConfigurationFile(fileObj.getAbsolutePath());
		if (batchMode)
			//parserManager.setLimits(begin, end);
			//parserManager.setLimits(timePanel.getMinZoom(), timePanel.getMaxZoom());
			parserManager.setLimits(batchBegin, batchEnd);
		Thread th = new Thread(parserManager);
		th.start();

	}
	
	
	private void loadSingleFile(File fileObj) {
		
		if (parserManager.running()) {
			System.out.println("Wait for parser to finish.");
			return;
		}
		
		// Reset all variable data
		closeAllFrames();
		parser = null;
			
		// Avoid change of sliders
		//settingUp = true;
		
		// Reset GUI
		activateButtons();
		
		// Start thread managing data
		parserManager.setSource(fileObj);
		if (batchMode && batchBegin!=null && batchEnd!=null )
			//parserManager.setLimits(begin, end);
			//parserManager.setLimits(timePanel.getMinZoom(), timePanel.getMaxZoom());
			parserManager.setLimits(batchBegin, batchEnd);
		Thread th = new Thread(parserManager);
		th.start();

	}	
	
	public void parsingComplete(String name, Parser p) {
						
		if (parser==null && p!=null) {
			parser = p;
			//p.setMaxTopProcs(configuration.getMaxTopProcs());
			//p.setMaxDisks(configuration.getMaxDisks());
			
			timePanel.setTime(parser.getStart(), parser.getEnd());
			
			/*
			begin=parser.getStart();
			end=parser.getEnd();
			
			// Setup slider length
			slider[0].setMinimum(0);
			slider[0].setMaximum(DataSet.SLOTS-1);
			slider[0].setValue(0);
			slider[1].setMinimum(0);
			slider[1].setMaximum(DataSet.SLOTS-1);
			slider[1].setValue(DataSet.SLOTS-1);
			
			time[0].setText(timeString(0));
			time[1].setText(timeString(DataSet.SLOTS-1));
			day[0].setText(dateString(0));
			day[1].setText(dateString(DataSet.SLOTS-1));	
			*/
			
			
			// Setup buttons
			activateButtons();
		} else {
			timePanel.setActive(parser.getStart(), parser.getEnd());
		}
		timePanel.repaint();
		
		for (int i=0; i< perfFrame.length; i++)
			if (perfFrame[i]!=null)
				perfFrame[i].zoom();
		
		//settingUp = false;		
		
		String fType=null;
		switch (parserManager.getFiletype()) {
			case ParserManager.NMON:		fType = "[nmon] "; 					break;
			case ParserManager.VMSTAT:		fType = "[vmstat -t] ";				break;
			case ParserManager.XMTREND:		fType = "[xmtrend] "; 				break;
			case ParserManager.TOPASCEC:	fType = "[topas_cec] "; 			break;
			case ParserManager.IOSTAT:		fType = "[iostat -alDT] "; 			break;
			case ParserManager.LSLPARUTIL:	fType = "[lslparutil] "; 			break;
			case ParserManager.CECDIR:		fType = "[Directory] ";				break;
			case ParserManager.CONFIGFILE:	fType = "[Configuration file] ";	break;
			case ParserManager.SAR:			fType = "[sar] "; 					break;
			case ParserManager.INSIGHT:		fType = "[Insight] "; 				break;
			case ParserManager.SNMP:		fType = "[Snmp] "; 					break;
			case ParserManager.COLLECTL:	fType = "[Collectl] "; 				break;
			default:						fType = "ERROR! "; 					break;
			 
		}
		message.setText(fType + name);
		
		noDisks.setSelected(parser.isAvoidDisk());
		
		if (batchMode) {
			System.out.println("\nCreating PNG");
			createAppropriateHTML();
			System.out.println("DONE!!");
		}
		
		
	}
	
	
	
	
	private void loadMultipleFilesSingleHost(File dir) {
		
		if (parserManager.running()) {
			System.out.println("Wait for parser to finish.");
			return;
		}
		
		// Reset all variable data
		closeAllFrames();
		parser = null;
		
		// Avoid change of sliders
		//settingUp = true;
		
		// Reset GUI
		activateButtons();
		
		// Start thread managing data
		parserManager.setSource(dir);
		parserManager.setSingleHost(true);		// Multiple files, single host
		if (batchMode)
			//parserManager.setLimits(begin, end);
			//parserManager.setLimits(timePanel.getMinZoom(), timePanel.getMaxZoom());
			parserManager.setLimits(batchBegin, batchEnd);
		Thread th = new Thread(parserManager);
		th.start();

	}	
		

	/*
	private String timeString(int t) {		
		GregorianCalendar gc= new GregorianCalendar();
		long delta = t * (end.getTime().getTime() - begin.getTime().getTime()) / (DataSet.SLOTS-1);
		gc.setTime(new Date(begin.getTime().getTime()+delta));

		int h,m,s;
		String str;
			
		h=gc.get(Calendar.HOUR_OF_DAY);
		m=gc.get(Calendar.MINUTE);
		s=gc.get(Calendar.SECOND); 
		
		str = "[";
		if (h<10)
			str+="0";
		str=str+h+":";
		if (m<10)
			str+="0";
		str=str+m+":";
		if (s<10)
			str+="0";
		str=str+s+"] ";

		return(str);
	}
	
	private String dateString(int t) {		
		GregorianCalendar gc= new GregorianCalendar();
		long delta = t * (end.getTime().getTime() - begin.getTime().getTime()) / (DataSet.SLOTS-1);
		gc.setTime(new Date(begin.getTime().getTime()+delta));

		int y,m,d;
		String str;
			
		y=gc.get(Calendar.YEAR);
		m=gc.get(Calendar.MONTH)+1;
		d=gc.get(Calendar.DAY_OF_MONTH); 
		
		str = "["+y+"-";
		if (m<10)
			str+="0";
		str=str+m+"-";
		if (d<10)
			str+="0";
		str=str+d+"] ";

		return(str);
	}
	*/
	
	
	/*
	 * Start a zoom caused by a slider value change
	 */
	/*
	private void zoom(int from, int to) {
		
		if (parserManager.running()) {
			System.out.println("Wait for parser to finish.");
			return;
		}
		
		GregorianCalendar newbegin= new GregorianCalendar();
		GregorianCalendar newend= new GregorianCalendar();
		long delta;
		
		if (begin==null || end==null)
			return;
		
		delta = from * (end.getTime().getTime() - begin.getTime().getTime()) / (DataSet.SLOTS-1);
		newbegin.setTime(new Date(begin.getTime().getTime()+delta));
		delta = to * (end.getTime().getTime() - begin.getTime().getTime()) / (DataSet.SLOTS-1);
		newend.setTime(new Date(begin.getTime().getTime()+delta));
		
		parserManager.setLimits(newbegin, newend);
		Thread th = new Thread(parserManager);
		th.start();

	}
	*/
	
	
	/*
	 * Start a zoom caused by the zoomButton or by a zoom on a JFrame
	 */
	private void zoomEvent(GregorianCalendar newBegin, GregorianCalendar newEnd) {
		
		if (parserManager.running()) {
			System.out.println("Wait for parser to finish.");
			return;
		}
		
		/*
		if (begin==null || end==null)
			return;
		*/
		if (parser==null)
			return;
		
		parserManager.setLimits(newBegin, newEnd);
		Thread th = new Thread(parserManager);
		th.start();

	}
	
	
	/*
	 * Reset a single window
	 */
	private void closeFrame(int id) {
		button[id].setSelected(false);
		if (perfFrame[id]!=null)
			perfFrame[id].dispose();
		perfFrame[id]=null;
	}
	
	
	private void closeTFrame(int id) {
		textButton[id].setSelected(false);
		if (textFrame[id]!=null)
			textFrame[id].dispose();
		textFrame[id]=null;
	}
	
	
	/*
	 * Reset all windows
	 */
	private void closeAllFrames() {
		for (int i=0; i<BUTTONS; i++)
			closeFrame(i);	
		for (int i=0; i<T_BUTTONS; i++)
			closeTFrame(i);
	}
	
	

	
	private void createAppropriateHTML() {
		// Create index of all reports
		PrintWriter index;
		try {
			index = new PrintWriter(
						new FileOutputStream(
							new File(batchOutput + File.separatorChar + "index.html")));
		}
		catch (IOException e) { return; }
		
		byte filetype = parserManager.getFiletype();
	
		// Sanity check
		if (parserManager.getFiletype()==ParserManager.UNKNOWN) {
			System.out.println("ERROR: unknown input data.");
			return;
		}
		
		// Create framed index.html
		if (filetype!=ParserManager.CECDIR && filetype!=ParserManager.TOPASCEC)
			index.println("<HTML><HEAD><TITLE>pGraph.Viewer graphs: " + parser.getFileName() + "</TITLE></HEAD>");
		else if (parser.getCecName()!=null)
			index.println("<HTML><HEAD><TITLE>pGraph.Viewer graphs: " + parser.getCecName() + "</TITLE></HEAD>");
		else
			index.println("<HTML><HEAD><TITLE>pGraph.Viewer graphs: multiple systems</TITLE></HEAD>");
		
		index.println("<frameset framespacing=\"0\" border=\"0\" rows=\"105,*\" frameborder=\"1\">");
		index.println("  <frame name=\"topframe\" scrolling=\"no\" noresize src=\"top.html\" marginwidth=\"0\" marginheight=\"0\">");
		index.println("  <frameset cols=\"215,*\">");
		index.println("    <frame name=\"menuframe\" target=\"graphframe\" src=\"menu.html\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"auto\">");
		index.println("    <frame name=\"graphframe\" src=\"welcome.html\" marginwidth=\"12\" marginheight=\"16\" scrolling=\"auto\">");
		index.println("  </frameset>");
		index.println("</frameset>");
		index.println("</HTML>");
		index.close();		
		
		// Create welcome page
		try {
			index = new PrintWriter(
						new FileOutputStream(
							new File(batchOutput + File.separatorChar + "welcome.html")));
		}
		catch (IOException e) { return; }
		
		index.println("<HTML><HEAD><TITLE>Menu</TITLE></HEAD>");
		index.println("<BODY bgcolor=#cccccc>");
		index.println("<H1>Welcome to pGraph generated reports.</H1>");
		index.println("Please select on the left frame the statistics you want to see.");
		index.println("</BODY></HTML>");
		index.close();	
		
		
	
		// Create top
		try {
			index = new PrintWriter(
						new FileOutputStream(
							new File(batchOutput + File.separatorChar + "top.html")));
		}
		catch (IOException e) { return; }
		GregorianCalendar gc;
		String from, to;
		int y,M,d,h,m,s;
				
		gc = parser.getStart();
		y=gc.get(Calendar.YEAR);
		M=gc.get(Calendar.MONTH)+1;
		d=gc.get(Calendar.DAY_OF_MONTH);
		h=gc.get(Calendar.HOUR_OF_DAY);
		m=gc.get(Calendar.MINUTE);
		s=gc.get(Calendar.SECOND);		
		
		from = "[" + 
				y 						+ "-" +
				(M<10? "0": "") + M 	+ "-" + 
				(d<10? "0": "") + d 	+
				' ' +
				(h<10? "0": "") + h 	+ ":" +
				(m<10? "0": "") + m 	+ ":" +
				(s<10? "0": "") + s		+
				"]";
			
		gc = parser.getEnd();
		y=gc.get(Calendar.YEAR);
		M=gc.get(Calendar.MONTH)+1;
		d=gc.get(Calendar.DAY_OF_MONTH);
		h=gc.get(Calendar.HOUR_OF_DAY);
		m=gc.get(Calendar.MINUTE);
		s=gc.get(Calendar.SECOND);		
		
		to   = "[" + 
				y 						+ "-" +
				(M<10? "0": "") + M 	+ "-" + 
				(d<10? "0": "") + d 	+
				' ' +
				(h<10? "0": "") + h 	+ ":" +
				(m<10? "0": "") + m 	+ ":" +
				(s<10? "0": "") + s		+
				"]";
	
		index.println("<HTML>" +
						"<HEAD>" +
							"<TITLE>Top</TITLE>" +
						"</HEAD>");
		index.println("<BODY bgcolor=#cccccc>" +
						"<CENTER>" +
							"<H1>");
		if (filetype!=ParserManager.CECDIR && filetype!=ParserManager.TOPASCEC && filetype!=ParserManager.LSLPARUTIL)
			index.println("System statistics ");
		else if (parser.getCecName()!=null)
			index.println("CEC " + parser.getCecName() + " statistics ");
		else
			index.println("Multiple systems statistics ");							

		index.println("\n</H1></CENTER>\n");
		index.println("<P><CENTER>from " + from + " to " + to +"</CENTER>\n");
		index.println("</HTML>");
		index.close();	
		
		
		try {
			index = new PrintWriter(
						new FileOutputStream(
							new File(batchOutput + File.separatorChar + "menu.html")));
		}
		catch (IOException e) { return; }

		
		// Create menu
		index.println("<HTML><HEAD><TITLE>Menu</TITLE></HEAD>");
		index.println("<BODY bgcolor=#cccccc>");
		index.println("<BR><UL>");
		
		if (setupCPUGraphic(batchOutput)>0)					addMenu(index,CPU);	
		if (setupPoolGraphic(batchOutput)>0)				addMenu(index,PROCPOOL);
		if (setupMemoryGraphic(batchOutput)>0)				addMenu(index,RAM);
		if (setupKernelGraphic(batchOutput)>0)				addMenu(index,KERNEL);
		if (setupSyscallGraphic(batchOutput)>0)				addMenu(index,SYSCALL);
		if (setupDiskBusyGraphic(batchOutput)>0)			addMenu(index,DISKBUSY);
		if (setupESSBusyGraphic(batchOutput)>0)				addMenu(index,ESSBUSY);
		if (setupDiskRWGraphic(batchOutput)>0)				addMenu(index,DISKRW);
		if (setupDiskXferGraphic(batchOutput)>0)			addMenu(index,DISKXFER);
		if (setupDiskBlockGraphic(batchOutput)>0)			addMenu(index,DISKBLOCK);
		if (setupDiskReadServiceGraphic(batchOutput)>0)		addMenu(index,RSERVICE);
		if (setupDiskWriteServiceGraphic(batchOutput)>0)	addMenu(index,WSERVICE);
		if (setupDiskReadWriteServiceGraphic(batchOutput)>0)	addMenu(index,RWSERVICE);
		if (setupDiskRWTimeoutGraphic(batchOutput)>0)		addMenu(index,RWTO);
		if (setupDiskWaitQTimeGraphic(batchOutput)>0)		addMenu(index,WQTIME);
		if (setupDiskQueueSizeGraphic(batchOutput)>0)		addMenu(index,QSIZE);
		if (setupDiskServiceQFullGraphic(batchOutput)>0)	addMenu(index,SQFULL);
		if (setupEssRWGraphic(batchOutput)>0)				addMenu(index,ESSRW);
		if (setupEssXferGraphic(batchOutput)>0)				addMenu(index,ESSXFER);
		if (setupAdapterRWGraphic(batchOutput)>0)			addMenu(index,ADAPTERRW);
		if (setupFibreRWGraphic(batchOutput)>0)				addMenu(index,FCRW);
		if (setupAdapterXferGraphic(batchOutput)>0)			addMenu(index,ADAPTERXFER);
		if (setupFibreXferGraphic(batchOutput)>0)			addMenu(index,FCRW);
		if (setupAdapterReadServiceGraphic(batchOutput)>0)	addMenu(index,ADRSERVICE);
		if (setupAdapterWriteServiceGraphic(batchOutput)>0)	addMenu(index,ADWSERVICE);
		if (setupAdapterWaitQTimeGraphic(batchOutput)>0)	addMenu(index,ADWQTIME);
		if (setupAdapterQueueSizeGraphic(batchOutput)>0)	addMenu(index,ADQSIZE);
		if (setupAdapterServiceQFullGraphic(batchOutput)>0)	addMenu(index,ADSQFULL);
		if (setupNetworkRWGraphic(batchOutput)>0)			addMenu(index,NETWORKRW);
		if (setupSEAGraphic(batchOutput)>0)					addMenu(index,SEA);
		if (setupWPAR_CPU_Graphic(batchOutput)>0)			addMenu(index,WPAR_CPU);
		if (setupWPAR_Memory_Graphic(batchOutput)>0)		addMenu(index,WPAR_MEM);
		if (setupWPAR_Disk_Graphic(batchOutput)>0)			addMenu(index,WPAR_DISK);
		if (setupTopCPU_Graphic(batchOutput)>0)				addMenu(index,TOPCPU);
		if (setupTopRAM_Graphic(batchOutput)>0)				addMenu(index,TOPRAM);
		if (setupFS_Graphic(batchOutput)>0)					addMenu(index,FS);
		if (setupAIO_Graphic(batchOutput)>0)				addMenu(index,AIO);
		if (setupDiskServiceGraphic(batchOutput)>0)			addMenu(index,DSKSERVICE);
		if (setupEssServiceGraphic(batchOutput)>0)			addMenu(index,ESSSERVICE);
		if (setupDiskWaitGraphic(batchOutput)>0)			addMenu(index,DSKWAIT);
		if (setupEssWaitGraphic(batchOutput)>0)				addMenu(index,ESSWAIT);

		index.println("</BODY></HTML>");
		index.close();			
	}
	
	
	private void addMenu(PrintWriter html, int frameId) {
		html.println(
				"<LI>" + 
				"<A HREF=\""+frameHtmlFile(frameId)+"\" target=\"graphframe\">"+
					frameDescription(frameId)+
				"</A>");
		System.out.print(" "+frameDescription(frameId)+": done!\n");
	}
	
	
	private PrintWriter setupFrame(String dir, int frameId) {
		
		boolean		newFrame = false;		// true if this procedure creates a new frame
		
		// If no parser is available return
		if (parser == null)
			return null;
		
		String frameName=frameDescription(frameId);
		String htmlName=frameHtmlFile(frameId);		
		
		
		// Setup HTML if required
		PrintWriter html=null;
			
		if (dir!=null) {
			if (dir.charAt(dir.length()-1)!=File.separatorChar)
				dir = dir + File.separatorChar;
			
			try {
				html = new PrintWriter(
						new FileOutputStream(
						new File(dir + htmlName)));
			}
			catch (IOException e) { return null; }
			html.println("<HTML>\n" +
							"<HEAD><TITLE>"+frameName+"</TITLE></HEAD>\n" +
							"<BODY bgcolor=#cccccc>\n" +
							"<H1>"+frameName+"</H1>\n");		
		}
		
		
		// If screen output reset existing frame or create a new one
		if (dir == null) {
			if (perfFrame[frameId] == null) {
				perfFrame[frameId] = new GenericFrame(frameName, configuration);
				newFrame = true;
			} else {
				perfFrame[frameId].reset();
				newFrame = false;
			}
		}


			
		// End setup
		if (newFrame) {
			FRAME_X		= 480 + DataSet.SLOTS;	// x size of data frame
			FRAME_Y		= 450;	// y size of data frame
			
			perfFrame[frameId].setSize(FRAME_X, FRAME_Y);
			perfFrame[frameId].setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			perfFrame[frameId].addWindowListener(myEventHandler);
			perfFrame[frameId].addPropertyChangeListener(myEventHandler);	
			perfFrame[frameId].setVisible(true);
		} else if (perfFrame[frameId]!=null)
			perfFrame[frameId].repaint();	
		
		return html;
	}	


	private void finishFrame(String dir, int frameId, PrintWriter html, int numPanels) {	
		
		// Force repaint of frame in GUI
		if (perfFrame[frameId]!=null)
			perfFrame[frameId].repaint();	
		
		if (html!=null){
			// complete HTML file
			html.println("</BODY></HTML>\n");
			html.close();	
			
			// if nothing written on HTML file, delete it
			if (numPanels==0 && dir!=null) {
				String htmlName=frameHtmlFile(frameId);	

				if (dir.charAt(dir.length()-1)!=File.separatorChar)
					dir = dir + File.separatorChar;
				File file = new File(dir + htmlName);
				file.delete();
			}
		}
	}
	
	
	private String frameHtmlFile(int id) {
		switch (id) {
			case CPU:			return "cpu.html";
			case PROCPOOL:		return "procpool.html";
			case RAM:			return "memory.html"; 
			case DISKBUSY:		return "diskbusy.html"; 
			case ESSBUSY:		return "mpiobusy.html"; 
			case DISKRW:		return "diskrw.html"; 
			case DISKXFER:		return "diskxfers.html"; 
			case DISKBLOCK:		return "disksize.html"; 
			case ESSRW:			return "vpathrw.html"; 
			case ESSXFER:		return "vpathxfers.html"; 
			case ADAPTERRW:		return "adapterRW.html"; 
			case ADAPTERXFER:	return "adapterXfer.html"; 
			case FCRW:			return "fibreRW.html";
			case FCXFER:		return "fibreXfer.html"; 
			case DACRW:			return "dacRW.html";
			case DACXFER:		return "dacXfer.html";
			case NETWORKRW:		return "network.html"; 
			case CHECKER:		return null;
			case KERNEL:		return "kernel.html"; 
			case SYSCALL:		return "syscall.html"; 
			case RSERVICE:		return "diskrservice.html"; 
			case WSERVICE:		return "diskwservice.html";
			case RWSERVICE:		return "diskrwservice.html";
			case DSKSERVICE:	return "diskservice.html";
			case ESSSERVICE:	return "essservice.html";
			case DSKWAIT:		return "diskwait.html";
			case ESSWAIT:		return "esswait.html";
			case RWTO:			return "diskrwtimeout.html"; 
			case RWFAIL:		return "diskrwfail.html"; 
			case WQTIME:		return "diskwqt.html"; 
			case QSIZE:			return "diskqs.html"; 
			case SQFULL:		return "diskqf.html"; 
			case ADRSERVICE:	return "adapterRservice.html"; 
			case ADWSERVICE:	return "adapterWservice.html"; 
			case ADWQTIME:		return "adapterwqt.html"; 
			case ADQSIZE:		return "adapterqs.html"; 
			case ADSQFULL:		return "adapterqf.html"; 
			case WPAR_CPU:		return "wpar_cpu.html"; 
			case WPAR_MEM:		return "wpar_ram.html"; 
			case WPAR_DISK:		return "wpar_disk.html";
			case TOPCPU:		return "top_procs_cpu.html";
			case TOPRAM:		return "top_procs_mem.html";
			case FS:			return "file_system.html";
			case AIO:			return "asyncio.html";
			case SEA:			return "SEA.html";
			case PCPU:			return "pcpu.html";
			case MEMDETAILS:	return "memdetail.html";
			
			default:	System.out.println("frameHtmlFile(): unknown frameId"); return null;
		}
	}


	private String frameDescription(int id) {
		switch (id) {
			case CPU:			return "CPU Usage";
			case PROCPOOL:		return "Shared ProcPools";
			case RAM:			return "Memory Usage";
			case DISKBUSY:		return "Disk Busy";
			case ESSBUSY:		return "MPIO Device Busy";
			case DISKRW:		return "Disk RW Rates";
			case DISKXFER:		return "Disk Transfers";
			case DISKBLOCK:		return "Disk I/O Size"; 
			case ESSRW:			return "MPIO RW Rates";
			case ESSXFER:		return "MPIO Transfers";
			case ADAPTERRW:		return "Disk Adapter RW Rates";
			case ADAPTERXFER:	return "Disk Adapter Transfers";
			case FCRW:			return "Fibre Channel RW Rates";
			case FCXFER:		return "Fibre Channel Transfers";
			case DACRW:			return "Dac RW rates";
			case DACXFER:		return "Dac transfers";
			case NETWORKRW:		return "Network RW Rates"; 
			case CHECKER:		return null;
			case KERNEL:		return "Kernel"; 			
			case SYSCALL:		return "System Calls"; 		
			case RSERVICE:		return "Disk Read Service"; 	
			case WSERVICE:		return "Disk Write Service"; 
			case RWSERVICE:		return "Disk Read+Write Service"; 
			case DSKSERVICE:	return "Disk Service";
			case ESSSERVICE:	return "MPIO Service";
			case DSKWAIT:		return "Disk Wait Time";
			case ESSWAIT:		return "MPIO Wait Time";
			case RWTO:			return "Disk Read/Write Timeouts"; 
			case RWFAIL:		return "Disk Read/Write Failures"; 
			case WQTIME:		return "Disk Wait Queue Time Spent";
			case QSIZE:			return "Disk Wait&Service Queue Size";
			case SQFULL:		return "Disk service queue full";
			case ADRSERVICE:	return "Adapter Read Service"; 	
			case ADWSERVICE:	return "Adapter Write Service"; 	
			case ADWQTIME:		return "Adapter Wait Queue Time Spent"; 
			case ADQSIZE:		return "Adapter Wait&Service Queue Size";
			case ADSQFULL:		return "Adapter service queue full"; 
			case WPAR_CPU:		return "WLM CPU Usage";
			case WPAR_MEM:		return "WLM MEM% Usage";
			case WPAR_DISK:		return "WLM Disk Usage";
			case TOPCPU:		return "Top processes CPU usage";
			case TOPRAM:		return "Top processes memory usage";
			case FS:			return "File System usage";
			case AIO:			return "Asynchronous IO usage";
			case SEA:			return "SEA Statistics";
			case PCPU:			return "Per CPU Physical Usage";
			case MEMDETAILS:	return "Memory details by page size";
			
			default:	System.out.println("frameDescription(): unknown frameId"); return null;
		}
	}
	
	public void showReadProgress (int p) {
		firePropertyChange("read_progress", null, new Integer(p));
	}
	
	public void setProgressValue(int v) {
		progressBar.setValue(v);
		if (batchMode) {
			if (v==0)
				System.out.println();
			System.out.print("#");
		}
	}
	
	public void setProgressIndeterminate(boolean v) {
		progressBar.setIndeterminate(v);
	}
	
	public void setProgressMessage(String s) {
		message.setText(s);
		if (batchMode)
			System.out.println(s);
	}


	public void setBatchMode(String batchOutput) {
		batchMode = true;
		this.batchOutput = batchOutput;
	}

	

	private void setupAffinityText(String dir) {
		
		// If no parser is available return
		if (parser == null)
			return;
		
		PerfData pd = parser.getPerfData();
		int num = pd.getNumLssrad();
		
		
		// Create a new Frame
		textFrame[T_LSSRAD] = new TextFrame();
		textFrame[T_LSSRAD].addWindowListener(myEventHandler);
		textFrame[T_LSSRAD].addPropertyChangeListener(myEventHandler);
		textFrame[T_LSSRAD].setVisible(true);
	

		textFrame[T_LSSRAD].setTitle("Affinity");
		
		String ss[];
		/*
		textFrame[T_LSSRAD].addRow("REF1\tSRAD\tMEM\t\tCPU");
		
		String s;
		
		
		for (int i=0; i<num; i++) {
			s = pd.getLssradRef1(i);
			s += "\t";
			s += pd.getLssradSrad(i);
			s += "\t";
			s += pd.getLssradMem(i);
			s += "\t";
			s += pd.getLssradCpu(i);
			
			textFrame[T_LSSRAD].addRow(s);
		}
		*/
		
		textFrame[T_LSSRAD].setHeader(new String[] {"REF1","SRAD","MEM","CPU"});
		for (int i=0; i<num; i++) {
			ss = new String[4];
			ss[0] = pd.getLssradRef1(i);
			ss[1] = pd.getLssradSrad(i);
			ss[2] = pd.getLssradMem(i);
			ss[3] = pd.getLssradCpu(i);
			
			textFrame[T_LSSRAD].addTableRow(ss);
		}
		
		textFrame[T_LSSRAD].reset();
	}	

	
	private void setupLparstatiText(String dir) {
		
		// If no parser is available return
		if (parser == null)
			return;
		
		PerfData pd = parser.getPerfData();		
		
		// Create a new Frame
		textFrame[T_LPARSTATI] = new TextFrame();
		
		textFrame[T_LPARSTATI].setTitle("LPAR Configuration");
		
		textFrame[T_LPARSTATI].setHeader(new String[] {"Variable","Value"});
		
		int i=0;
		String r[];
		while ( (r=pd.getTextlabelNum(PerfData.LPARSTATI, i++)) != null)
			textFrame[T_LPARSTATI].addTableRow(r);
	
		textFrame[T_LPARSTATI].addWindowListener(myEventHandler);
		textFrame[T_LPARSTATI].addPropertyChangeListener(myEventHandler);
		textFrame[T_LPARSTATI].setVisible(true);
		textFrame[T_LPARSTATI].reset();
	}	
	
	
	private void setupVmstatvText(String dir) {
		
		// If no parser is available return
		if (parser == null)
			return;
		
		PerfData pd = parser.getPerfData();		
		
		// Create a new Frame
		textFrame[T_VMSTATV] = new TextFrame();
		
		textFrame[T_VMSTATV].setTitle("vmstat -v");
		
		textFrame[T_VMSTATV].setHeader(new String[] {"Variable","Start value","End value"});
		
		int i=0;
		String before[],end[];
		String result[] = null;
		boolean stop=false;
		
		
		while ( !stop ) {
			before	= pd.getTextlabelNum(PerfData.VMSTATV, i);
			end 	= pd.getTextlabelNum(PerfData.VMSTATVEND, i);
			
			if (before==null)
				stop=true;
			else {
				result = new String[3];
				result[0] = before[0];
				result[1] = before[1];
				result[2] = end[1];
				textFrame[T_VMSTATV].addTableRow(result);
				i++;
			}
		}
	
		textFrame[T_VMSTATV].addWindowListener(myEventHandler);
		textFrame[T_VMSTATV].addPropertyChangeListener(myEventHandler);
		textFrame[T_VMSTATV].setVisible(true);
		textFrame[T_VMSTATV].reset();
	}	
}
