package pGraph;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class TextFrame extends JFrame { 
	
	
	
	public class MyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		Object data[][] = null;
		String header[] = null;
		
		public void setData(Object o[][]) {
			data=o;
		}
		
		public void setHeader(String s[]) {
			header = s;
		}
		
		public int getRowCount() { return data.length; }
		public int getColumnCount() { return header.length; }
		public String getColumnName(int c) { return header[c]; }
		//public Class getColumnClass(int c) { return String.class; }
		public Object getValueAt(int r, int c) { return data[r][c]; }
		
		public boolean isCellEditable(int r, int c) { return false; }
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4386665744362492685L;
	private String mytitle = null;
	//private Vector<String> rows =  null;
	private static int		WIDTH = 800;
	private static int		HEIGHT = 300;
	
	private String 			header[] = null;
	private Vector<String[]> 	table = null;
	
	
	public TextFrame() {
		super();
		//initialize();
	}
	
	
	public void setTitle(String title) {
		this.mytitle = title;
	}
	
	
	public void setHeader(String s[]) {
		header = s;
	}
	
	public void addTableRow(String s[]) {
		if (table==null)
			table = new Vector<String[]>();
		table.add(s);
	}
	
	/*
	public void addRow(String line) {
		if (rows==null)
			rows = new Vector<String>();
		
		rows.add(line);
		
		String s[] = new String[1];
		s[0]=line;
		addTableRow(s);
	}
	*/
	
	
	private void initialize() {
		int i;	
		JTable jt;
		MyTableModel mtm = new MyTableModel();
				

		Object[][] content = new Object[table.size()][];
		for (i=0; i<table.size(); i++) {
			content[i] = table.elementAt(i);
		}
		mtm.setData(content); 
		
		if (header==null)
			mtm.setHeader((String[])content[0]);
		else
			mtm.setHeader(header);
		
		jt = new JTable(mtm);
		
		/*
		if (header==null)
			jt = new JTable(content,content[0]);
		else
			jt = new JTable(content, header);
		*/
		
		jt.setBackground(Color.white);
		jt.setRowSelectionAllowed(false);
		
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportView(jt);
		
		setSize(WIDTH,HEIGHT);
		setContentPane(scrollPane);
		
		super.setTitle(mytitle);	
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		
		validate();
	}
	
	/*
	private void initialize_old() {
		JPanel p;
		JTextArea jt;
		int i;		
	
		
		p = new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
		p.setBackground(Color.white);
		
		p.add(Box.createVerticalGlue());
		for (i=0; rows!=null && i<rows.size(); i++) {
			jt = new JTextArea(rows.elementAt(i));
			jt.setEditable(false);
			jt.setFont(new Font("Serif", Font.PLAIN, 14));
			jt.setMaximumSize(new Dimension(WIDTH,HEIGHT));
			jt.setAlignmentX(0.0f);
			jt.setBackground(p.getBackground());
			p.add(jt);
		}
		p.add(Box.createVerticalGlue());
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportView(p);
		
		setSize(WIDTH,HEIGHT);
		setContentPane(scrollPane);
		
		super.setTitle(mytitle);	
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		
		validate();
	}
	*/
	
	
	public void reset() {
		initialize();
	}
	

}
