package pGraph;

public class NmonTokenizer {
	
	private String	input = null;			// string to be parsed
	private String	tokens[] = null;		// input string split in tokens
	private String	keys[][] = null;		// array of labels where first is primary key
	private int		line_num = 0;			// line number 
	
	private static boolean DEBUG = false;
	
	
	/*
	 * Retrieve string and split it into tokens that are kept into object.
	 * This call removed previous input string.
	 */
	public void parseString(String s, int num) {
		this.input = s;
		this.line_num = num;
		tokens = input.split(",+");
	}
	
	/*
	 * Treat current string as labels to be used as keys. Add it to the end of keys.
	 * Primary key duplication is not managed: always add to the end of keys.
	 */
	public void updateKeys() {
		String	newKeys[][];
		int 	i;
		
		if (keys==null) {
			newKeys = new String[1][];
			newKeys[0] = new String[tokens.length];
		} else {
			newKeys = new String[keys.length+1][];
		
			for (i=0; i<keys.length; i++)
				newKeys[i] = keys[i];
			
			newKeys[keys.length] = new String[tokens.length];
		}
		for (i=0; i<tokens.length; i++)
			newKeys[newKeys.length-1][i] = tokens[i].trim();   // skip white spaces if needed
		
		keys = newKeys;
	}
	
	
	public float getValueFromKey(String label) {
		
		// Sanity check
		if (tokens==null || keys == null)
			return -1;
		
		
		int i,j;
		float result;
		
		// Primary key is tokens[0]
		for (i=0; i<keys.length; i++)
			if (tokens[0].equals(keys[i][0]))
				break;
		if (i==keys.length) {
			System.out.println("Error line " + line_num + ": unknown key " + tokens[0]);
			return -1;
		}
		
		// If number of expected items has changed, do not provide any value and log error
		if (tokens.length != keys[i].length) {
			System.out.println("Error line " + line_num + ": unexpected change of number of tokens: ");
			return -1;
		}
		
		for (j=2; j<keys[i].length; j++)
			if (keys[i][j].equals(label))
				break;
		if (j==keys[i].length) 
			return -1;
		
		
		// Parse float if possible
		try {
			result = Float.parseFloat(tokens[j]);
		} catch (NumberFormatException e) {
			if (DEBUG)
				System.out.println("Error line " + line_num + ": error parsing label " + label);
			return -1;
		}	
		
		// Sanity check
//		if (result>1e16) 
//			result = -1;
		
		return result;
	}
	
	public float getValue(int n) {
		// Sanity check
		if (tokens==null)
			return -1;		
		
		if (n>=tokens.length)
			return -1;
		
		float result;
		
		// Parse float if possible
		try {
			result = Float.parseFloat(tokens[n]);
		} catch (NumberFormatException e) {
			if (DEBUG)
				System.out.println("Error line " + line_num + ": error parsing token " + tokens[n]);
			return -1;
		}	
		
		// Sanity check
		if (result>1e8) 
			result = -1;
		
		return result;		
	}
	
	public String getString(int n) {
		// Sanity check
		if (tokens==null)
			return null;		
		
		if (n>=tokens.length)
			return null;
		
		return tokens[n];
	}
	
	public String getStringFromKey(String label) {
		
		// Sanity check
		if (tokens==null || keys == null)
			return null;
		
		
		int i,j;
		
		// Primary key is tokens[0]
		for (i=0; i<keys.length; i++)
			if (tokens[0].equals(keys[i][0]))
				break;
		if (i==keys.length) {
			System.out.println("Error line " + line_num + ": unknown key " + tokens[0]);
			return null;
		}
		
		// If number of expected items has changed, do not provide any value and log error
		if (tokens.length != keys[i].length) {
			System.out.println("Error line " + line_num + ": unexpected change of number of tokens: ");
			return null;
		}
		
		for (j=2; j<keys[i].length; j++)
			if (keys[i][j].equals(label))
				break;
		if (j==keys[i].length) 
			return null;
		
		return tokens[j];
	}
	
	
	/*
	 * Get keys related to current string
	 */
	public String[] getKeys() {
		// Sanity check
		if (tokens==null || keys == null)
			return null;
		
		int i;
		
		// Primary key is tokens[0]
		for (i=0; i<keys.length; i++)
			if (tokens[0].equals(keys[i][0]))
				break;
		if (i==keys.length) {
			System.out.println("Error line " + line_num + ": unknown key " + tokens[0]);
			return null;
		}
		
		return keys[i];		
	}
	
	public int getNumTokens() {
		if (tokens==null)
			return -1;
		return tokens.length;
	}

}
