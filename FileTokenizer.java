package pGraph;

import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;

public class FileTokenizer {
	
	private String				name;				// name of file (for error output only)
	private BufferedReader		br = null;
	private int					lines_read;			// number of lines read so far
	private char				separator;
	private String				line;				// current line
	
	private int					start[] = null;		// first char of token
	private int					end[] = null;		// first char AFTER the token or -1 if token is on end of line
	private int					tokens = 0;
	private static byte			TOKENBLOCK = 10;	// allocation block for first and last
	
	
	public FileTokenizer(BufferedReader br, String name) {
		this.br = br;	
		this.name = name;
		lines_read = 0;
		separator = ' ';
		start = new int[TOKENBLOCK];
		end  = new int[TOKENBLOCK];
		resetTokens();
	}
	
	
	/*
	 * Reset internal data on tokens
	 */
	private void resetTokens() {
		int i;
		
		for (i=0; i<start.length; i++)
			start[i]=-1;
		for (i=0; i<end.length; i++)
			end[i]=-1;
		tokens=0;		
	}
	
	
	/*
	 * Read a new line from the reader and return the number of tokens.
	 * If end of stream, return -1
	 * Parse line to detect position of tokens 
	 */
	public int readLine() {
		boolean newToken=true;	// true if we are looking for a new token 
		
		try {
			line = br.readLine();
		} catch (IOException ioe) {
			System.out.println("Warning: IO Exception while reading from "+name+" after line number "+lines_read+". Closing stream and continuing.");
			line=null;
		}
		
		if (line==null) {
			tokens = 0;
			return -1;
		}
		
		lines_read++;
		
		if (line.length()==0)
			return 0;
		
		resetTokens();
		
		for (int i=0; i<line.length(); i++) {	
			if (newToken) {
				if (line.charAt(i) == separator)
					continue;
				
				if (tokens>=start.length)
					expandTokenArrays();
				start[tokens]=i;
				newToken=false;					
			} else {
				if (line.charAt(i) != separator)
					continue;
				end[tokens]=i;
				tokens++;
				newToken=true;
			}				
		}
		if (!newToken) {
			end[tokens]=-1;
			tokens++;
		}
		
		return tokens;
		
	}
	
	
	/*
	 * Expand token arrays with new elements
	 */
	private void expandTokenArrays() {
		int oldStart[] = start;
		int oldEnd[]   = end;
		int i;
		
		start = new int[oldStart.length + TOKENBLOCK];
		end   = new int[oldEnd.length + TOKENBLOCK];
		
		for (i=0; i<oldStart.length; i++) {
			start[i]=oldStart[i];
			end[i]=oldEnd[i];
		}
		for (i=oldStart.length; i<start.length; i++) {
			start[i]=-1;
			end[i]=-1;
		}
	}
	
	
	/*
	 * Get token as a String
	 */
	public String getStringToken(int n) {
		if (end[n]==-1)
			return line.substring(start[n]);
		return line.substring(start[n], end[n]);
	}
	
	/*
	 * Get token as a float
	 */
	public float getFloatToken(int n) {
		try {
			return Float.parseFloat(getStringToken(n));
		} catch (NumberFormatException nfe) {
			System.out.println("Warning: cannot parse value on line "+lines_read+". Continuing skipping value. The following line shows error.");
			String errLine="";
			if (n>0)
				errLine+=line.substring(0, start[n]);
			errLine+=">>";
			if (end[n]==-1) {
				errLine+=line.substring(start[n]);
				errLine+="<<";
			} else {
				errLine+=line.substring(start[n],end[n]);
				errLine+="<<";
				errLine+=line.substring(end[n]);
			}		
			System.out.println(errLine);
			return -1;
		}
	}
	
	
/*	
	public static void main(String[] args) {
		FileTokenizer ft;
		int n;
		int line=1;
		
		try {
			ft = new FileTokenizer(
					new BufferedReader(new FileReader("C:\\Documents and Settings\\Administrator\\Desktop\\XXX\\vmstat-th.out"),1024*1024),
					"vmstat-th.out");
		} catch (FileNotFoundException nfne) {
			return;
		}
		
		while ( (n=ft.readLine()) >= 0 ) {
			System.out.println("Line #"+line+" Tokens="+n);
			for (int i=0; i<n; i++)
				System.out.println("Token #"+i+"="+ft.getFloatToken(i));
			System.out.println("=============================================================");
			line++;
		}
		

	}
*/

	public char getSeparator() {
		return separator;
	}


	public void setSeparator(char separator) {
		this.separator = separator;
	}
	
	
	
	
	

}
