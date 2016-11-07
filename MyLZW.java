/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/
import java.util.*;

public class MyLZW {
	private static final int MIN_WIDTH = 9;
	private static final int MAX_WIDTH = 16;
    private static final int R = 256;       		// number of input chars
	private static int MODE = 0;					// '0' Do Nothing, '1' Reset, '2' Monitor
    private static int L = 512;		 				// number of codewords = 2^W
    private static int W = MIN_WIDTH;         		// codeword width

    public static void compress() { 
		BinaryStdOut.write(MODE, 2);
        String input = BinaryStdIn.readString();
        TST<Integer> st = makeInitialCodebookTST();
        int code = R+1;  							// R is codeword for EOF
		//for Monitor mode
		int bIn = 0, bOut = 0;
		float prevRatio = 1, currRatio = 0;
		boolean monitor = false;
		//-----

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  	// Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);   	// Print s's encoding.
            int t = s.length();
            if (t < input.length()) {				// Add s to symbol table.
            	if (code >= L) {
            		boolean full = !adjWidth(W+1); 	// resize codeword width
					if (full) {
						boolean clear = false;
						switch (MODE) {
							case 1: clear = true;	//Reset mode
									break;
							case 2: bIn += t*8;		// Monitor mode
									bOut += W;
									currRatio = (float)bIn / bOut; 
			        				if (!monitor) {
			        					prevRatio = currRatio;
			        					monitor = true;
			        				} else if (prevRatio / currRatio > 1.1) {
			        					clear = true;
			        					monitor = false;
			        				}
									break;
						}
						if (clear) {
            				st = makeInitialCodebookTST();
            				code = R + 1;
            				adjWidth(MIN_WIDTH); 
						}
					}
            	}
				if (code < L) {
            		st.put(input.substring(0, t + 1), code++);
            	}
            }    				     
            input = input.substring(t);
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
		MODE = BinaryStdIn.readInt(2);
        ArrayList<String> st = makeInitialCodebookList();
        int i = R + 1;
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           	// expanded message is empty string
        String val = st.get(codeword);
		//for Monitor mode
		int bIn = 0, bOut = 0;
		float prevRatio = 1, currRatio = 0;
		boolean monitor = false;
		//-----

        while (codeword != R) {
			if (i >= L) {
				boolean full = !adjWidth(W+1); 	// check if we resize codeword width
				if (full) {
					boolean clear = false;
					switch (MODE) {
						case 1: clear = true;	//Reset mode
								break;
						case 2: bIn += val.length()*8; // Monitor mode
								bOut += W;
								currRatio = (float)bIn / bOut; 
		        				if (!monitor) {
		        					prevRatio = currRatio;
		        					monitor = true;
		        				} else if (prevRatio / currRatio > 1.1) {
		        					clear = true;
		        					monitor = false;
		        				}
								break;
					}
					if (clear) {
        				st = makeInitialCodebookList();
        				i = R + 1;
        				adjWidth(MIN_WIDTH); 
					}
				}
			}
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s;
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            else s = st.get(codeword);
            if (i < L) {
            	st.add(val + s.charAt(0));
            	i++;
            }
            val = s;
        }
        BinaryStdOut.close();
    }
	
    public static TST<Integer> makeInitialCodebookTST() {
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        return st;
    }
	
    public static ArrayList<String> makeInitialCodebookList() {
    	ArrayList<String> st = new ArrayList<String>();
        for (int i = 0; i < R; i++)
            st.add("" + (char)i);
        st.add("");					//lookahead for EOF
        return st;
    }
	
    public static boolean adjWidth(int newW) {
    	if (newW <= MAX_WIDTH){
    		W = newW;
    		L = (int)Math.pow(2, W);
    		return true;
    	} else {
    		return false;
    	}	
    }

    public static void main(String[] args) {
        if (args[0].equals("-")) {
			switch (args[1].toLowerCase()){
				case "n": MODE = 0; //Do Nothing
						  break;
				case "r": MODE = 1; //Reset
						  break;
				case "m": MODE = 2; //Monitor
						  break;
			}
			compress();
		}
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}