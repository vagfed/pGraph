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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.UIManager;


public class ViewerApplet extends JApplet {

	private static final long serialVersionUID = 2546257572763997509L;
	
	private Viewer 		viewer = null;
	private boolean		single_host = false;
	private String 		files[] = null;
	private String 		base = null;
	
	private JButton jb = null;
	private ViewerApplet va = this;
	
	private MyEventHandler myEventHandler = null;

	
	
	class MyEventHandler implements ActionListener {

		
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == jb) {
				
				//if (viewer!=null)
				//	return;		
				
				if (viewer.isVisible())
					return;
				
				viewer.setVisible(true);
				viewer.activateFromApplet();
				
				//startViewer();
				
				/*
				try {
			        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			            public void run() {
			                startViewer();
			            }
			        });
			    } catch (Exception e) {
			        System.err.println("startViewer didn't successfully complete");
			    }
			    */
		
				
			}
		}
		
	}
	
	
	
	private void startViewer() {
		viewer = new Viewer(va,single_host);
		viewer.createConsole();
		viewer.setSize(Viewer.VIEWER_WIDTH,Viewer.VIEWER_HEIGHT);
				
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension frameSize = viewer.getSize();
		if (frameSize.height > screenSize.height)
				frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
				frameSize.width = screenSize.width;
		viewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		
		//viewer.setVisible(true);
		
		//viewer.activateFromApplet();
	}
		
	
	
	
	
	public String[] getFiles() {
		return files;
	}
	
	public InputStream getInputStream(String file) {
		URL source;
		
    	try {   
    		//base="file:/C:\\Documents and Settings\\Administrator\\Desktop\\testenv\\";
    		if (file.contains("://"))
    			source = new URL(file);
    		else
    			source = new URL(base+file);
    		URLConnection uc = source.openConnection();
    		InputStream is = uc.getInputStream();
    		
    		return is;    		
    	} catch (Exception e) {
    		System.out.println("Error opening connection: "+base+file);
    		return null;
    	}
	}
	
	
	public void init() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e);
			return;
		}
		
		base = getCodeBase().toString();
    	String source = getParameter("FILES");
    	String mode = getParameter("SINGLE_HOST");
    	String label = getParameter("LABEL");
    	
    	
    	if (source!=null) {
    		files = source.split(",");
	    	if (files.length==1 || (mode!=null && mode.compareToIgnoreCase("true")==0) )
	    			single_host = true;	
    	}		
		
    	try {
	        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
	            public void run() {
	                startViewer();
	            }
	        });
	    } catch (Exception e) {
	        System.err.println("startViewer didn't successfully complete");
	    }
		
		myEventHandler = new MyEventHandler();
		jb = new JButton();
		if (label==null)
			jb.setText("Start");
		else
			jb.setText(label);
		jb.setFocusPainted(false);
		jb.setFont(new java.awt.Font("sansserif", 1, 12));
		//jb.setBounds(5,5,width-10,height-10);
		jb.addActionListener(myEventHandler);
		add(jb);
	}
	
	
	public void start() {	
		
		/*
		 
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e);
			return;
		}
		
		base = getCodeBase().toString();
    	String source = getParameter("FILES");
    	String mode = getParameter("SINGLE_HOST");
    	
    	
    	if (source!=null) {
    		files = source.split(",");
	    	if (files.length==1 || (mode!=null && mode.compareToIgnoreCase("true")==0) )
	    			single_host = true;	
    	}
		
		viewer = new Viewer(this,single_host);
		viewer.setSize(1007,250);
		
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension frameSize = viewer.getSize();
		if (frameSize.height > screenSize.height)
				frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
				frameSize.width = screenSize.width;
		viewer.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		
		viewer.setVisible(true);	
		
		*/
	}
	
	public void stop() {
		if (viewer!=null) {
			viewer.dispose();
			viewer = null;
		}
	}
	
	public String[][] getParameterInfo() {
	    String[][] info = {
	      // Parameter Name     Kind of Value   Description
	        {"FILES",     		"URLs",         "comma separated list of files"},
	        {"SINGLE_HOST",     "boolean",      "true when files belong to same host"},
	        {"LABEL",     		"String",       "label inside applet's button"}
	    };
	    return info;
	}

}
