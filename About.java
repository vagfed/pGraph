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
 * Created on Oct 2, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&
 * gt;Java&gt;Code Generatiionon&gt;Code and Comments
 */
package pGraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;


/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class About extends JFrame {

	private static final long serialVersionUID = -8901028839453120585L;
	

	/**
	 * This is the default constructor
	 */
	public About() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		
		JPanel p;
		JLabel l;
		JTextArea jt;
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBackground(Color.white);
		
		// Title Panel
		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBackground(contentPanel.getBackground());
		
		l = new JLabel("pGraph");
		l.setFont(new Font("Serif", Font.BOLD, 22));
		l.setHorizontalTextPosition(SwingConstants.CENTER);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		l.setForeground(Color.black);
		p.add(l,BorderLayout.CENTER);
		
		l = new JLabel(Viewer.version);
		l.setFont(new Font("Serif", Font.BOLD, 16));
		l.setHorizontalTextPosition(SwingConstants.CENTER);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		l.setForeground(Color.black);
		p.add(l,BorderLayout.SOUTH);
		
		contentPanel.add(p,BorderLayout.NORTH);
		
		// Description Panel
		p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.setBackground(contentPanel.getBackground());
		
		p.add(Box.createVerticalGlue());
		
		jt = new JTextArea(	"\n" + 
								"  pGraph is capable of producing data graphs either interactively or in batch mode reading the\n" + 
								"  following input files:\n" +
								"   >  nmon\n" +
								"   >  vmstat -t\n" +
								"   >  topasout <xmwlm file>\n" +
								"   >  topasout <topascec file>\n" +
								"   >  iostat -alDT\n" +
								"   >  sar -A\n" +
								"   >  HMC's lslparutil\n");
		jt.setEditable(false);
		jt.setFont(new Font("Serif", Font.PLAIN, 14));
		jt.setMaximumSize(new Dimension(800,600));
		jt.setAlignmentX(0.0f);
		jt.setBackground(p.getBackground());
		p.add(jt);
		
		l = new JLabel(" ");
		l.setFont(new Font("Serif", Font.PLAIN, 14));
		p.add(l);
		
		jt = new JTextArea(	"  For additional information:\n" +
							"  web:       http://tinyurl.com/fed-pgraph\n" +
							"  email:     vagnini@it.ibm.com\n");
		jt.setEditable(false);
		jt.setFont(new Font("Serif", Font.PLAIN, 14));
		jt.setMaximumSize(new Dimension(800,70));
		jt.setAlignmentX(0.0f);
		jt.setBackground(p.getBackground());
		p.add(jt);
		
		p.add(Box.createVerticalGlue());
		
		contentPanel.add(p,BorderLayout.CENTER);
		
		// Close button
		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBackground(contentPanel.getBackground());

		
		JButton b = new JButton();
		b.setMaximumSize(new Dimension(70,60));
		b.setText("OK");
		b.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {    
				dispose();
			}
		});
		p.add(b,BorderLayout.CENTER);
		
		contentPanel.add(p,BorderLayout.SOUTH);
		
		setSize(800, 500);
		setContentPane(contentPanel);
		setTitle("About pGraph");
		
	}

}  
