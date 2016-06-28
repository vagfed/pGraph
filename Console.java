package pGraph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console extends JFrame implements Runnable {

	private static final long serialVersionUID = -3902418647512280344L;
	
	private JTextArea			textArea = null;	
	private PipedInputStream 	pis_out=new PipedInputStream(); 
	private PipedInputStream 	pis_err=new PipedInputStream(); 
	private Thread				outThread;
	private Thread				errThread;
	
	private static final int	MAXBUFFER = 1024;
	private byte				out_buffer[] = new byte[MAXBUFFER];
	private byte				err_buffer[] = new byte[MAXBUFFER];
	
	public Console() {
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width,screenSize.height/5);
		setLocation(0, screenSize.height/5*4);
		
		//setSize(500, 300);
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
		setVisible(false);		
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);	
		setName("pGraph Console");
		setTitle("pGraph Console");
		
		try	{
			PipedOutputStream pos_out = new PipedOutputStream(pis_out);
			System.setOut(new PrintStream(pos_out,true)); 
		} catch (Exception e) {
			textArea.append("Couldn't redirect STDOUT to this console\n"+e.getMessage());
	    } 
		
		try	{
			PipedOutputStream pos_err=new PipedOutputStream(pis_err);
			System.setErr(new PrintStream(pos_err,true));
		} catch (Exception e) {
			textArea.append("Couldn't redirect STDERR to this console\n"+e.getMessage());
		}
		
		// Start two threads to read from the PipedInputStreams				
		outThread=new Thread(this);
		outThread.setDaemon(true);	
		outThread.start();	
		
		errThread=new Thread(this);	
		errThread.setDaemon(true);	
		errThread.start();
	}
	
	
	private synchronized void writeData(PipedInputStream pis) throws IOException {
		int num;
		byte buffer[];
		boolean out_of_memory = false;
			
		if (pis==pis_out) {
			buffer = out_buffer;
		} else {
			buffer = err_buffer;
		}
		
		num = pis.available();
		if (num != 0) {
			setVisible(true);
			//if (buffer == err_buffer);
		} else
			return;
		
		int read;
		String s;
		String hour;
		GregorianCalendar gc = new GregorianCalendar();
		
		hour = " ";
		read = gc.get(Calendar.HOUR_OF_DAY);
		if (read<10)
			hour = hour + "0" + read + ":";
		else 
			hour = hour + read + ":";
		read = gc.get(Calendar.MINUTE);
		if (read<10)
			hour = hour + "0" + read + ":";
		else 
			hour = hour + read + ":";
		read = gc.get(Calendar.SECOND);
		if (read<10)
			hour = hour + "0" + read;
		else 
			hour = hour + read;
		
		textArea.append("\n\n==========================================================================\n");
		textArea.append(hour);
		textArea.append("\n==========================================================================\n");
		
		while (num>0) {
			 read = pis.read(buffer, 0, MAXBUFFER);
			 s = new String(buffer, 0, read);
			 textArea.append(s);
			 num -= read;	
			 if (s.indexOf("OutOfMemoryError")>=0)
				 out_of_memory=true;
		}
		
		if (out_of_memory) {
			textArea.append("\n\nIncrease maximum Java memory using the -X switch.");
			textArea.append("\nExample (200MB): java -Xmx200m -cp pGraph.jar pGraph.Viewer");
		}
	}
	

	public synchronized void run()
	{
		try
		{			
			while (Thread.currentThread()==outThread)
			{
				try { 
					this.wait(100);
				} catch (InterruptedException ie) {}
				
				writeData(pis_out);
			}
		
			while (Thread.currentThread()==errThread)
			{
				try { 
					this.wait(100);
				} catch (InterruptedException ie) {}
				
				writeData(pis_err);
			}	
			
		} catch (Exception e) {
			textArea.append("\nConsole reports an Internal error.");
			textArea.append("The error is: "+e);			
		}

	}


}
