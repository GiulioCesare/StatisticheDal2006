import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import it.finsiel.misc.Misc;

public class Test {
	String filenameIn;
	String filenameOut;
	BufferedReader in = null;
    BufferedWriter out = null;

    Test (String[] args)
	{
	    filenameIn = args[0];
	    filenameOut = args[1];

	    System.out.println("filenameIn " + filenameIn);
	    System.out.println("filenameOut " + filenameOut);
	    
	}

	
	public static void main(String[] args) {
		
		if(args.length < 2)
	    {
	        System.out.println("Uso: Test filenameIn filenameOut"); 
	        System.exit(1);
	    }

		Test test = new Test(args);
	    test.run();
	    
	    System.exit(0);
	}// End main		

	void run()
	{
		String s="";
		//ConfigTable configTable=null;
		
		int state = 0;
		
		
		try {
			in = new BufferedReader(new FileReader(filenameIn));
//			in = new BufferedReader(new InputStreamReader(new FileInputStream(filenameIn), "UTF-8"));
			
			out = new BufferedWriter(new FileWriter(filenameOut));
//			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filenameOut), "UTF-8"));
			
			
			String keyLast = null, key;
			int ctr=0;
			while (true) {
				ctr++;
				if (ctr > 5)
					break;
				try {
					s = in.readLine();
					if (s == null)
						break;

					if ((	s.length() == 0) 
							||  (s.charAt(0) == '#') 
							|| (Misc.emptyString(s) == true))
						continue;
					
//						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						
					System.out.println(s);
					out.write(s+"\n");
					
				} catch (Exception e) { // IOException
					// 
					e.printStackTrace();
					
				}
				
				
				
				} // End while 

			
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
		} catch (FileNotFoundException e) {
			// 
			e.printStackTrace();
		} 
		catch (IOException e1) {
			// 
			e1.printStackTrace();
		}
		
	} // End run
	
	
}
