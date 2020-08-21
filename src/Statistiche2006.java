/*
 * Author: 	Argentino Trombin
 * Date:	2011/2012?
 * Statistiche per creazioe e cattura
 * 
 * inpurt:	tr_tit_bib.sta.out.bytes.srt
 * output:	tr_tit_bib.sta.out.bytes.srt.sta
 * 
 */



import it.finsiel.misc.Misc;
import it.finsiel.misc.MiscString;
import it.finsiel.misc.MiscStringTokenizer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;





class Statistiche2006 {
	public static final int MAX_BYTES_PER_UTF8_CHARACTER = 4;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 

		char charSepArrayEquals[] = { '='};
		char charSepArraySpace[] = { ' '};

		if(args.length < 2)
	    {
	        System.out.println("Uso: Statistiche2006 trTitBibFilename trTitBibStatisticheFilename"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="Statistiche2006 tool - (C) Almaviva S.p.A 2011"+
		 "\n====================================="+
		 "\nTool di creazione statistiche dal 2006"
		 ;

	    System.out.println(start);
		
	    Statistiche2006 statistiche2006 = new Statistiche2006(args);
	    statistiche2006.run();
	    
	    System.exit(0);
	}// End main		
		

	public

	enum Fields {
	BID,
	CD_POLO,
	CD_BIBLIOTECA,
	TS_INS,
	FL_MUTILO,
	DS_CONSISTENZA,
	FL_POSSESSO,
	FL_GESTIONE,
};
	
	
	String trTitBibFilename;
	String trTitBibFilenameOut;
    
	char charSepArray[] = { 0x01 }; //'�' C0

	byte[] byteBuffer = new byte[12000];		
	int indexByteBuffer = 0;		
	byte[] fieldByteBuffer = new byte[4000];		
//	byte fieldSeparatorByte = (byte)0xC0; // carattere non usato in indice
	byte fieldSeparatorByte = (byte)0x01; // carattere non usato in indice
	
	
	String oldBid="";
	String oldPolo="";
	String ar[];

	
	
    BufferedWriter OutLog;
		
    
    
	Statistiche2006 (String[] args)
	{
	    trTitBibFilename = args[0];
	    trTitBibFilenameOut = args[1];
	}
	
	private byte[] myGetBytesUtf8(String s) {
		int len = s.length();
		int en = MAX_BYTES_PER_UTF8_CHARACTER * len;
		byte[] ba = new byte[en];
		if (len == 0)
			return ba;

		int ctr = 0;

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			
			if (c < 0x80) {
				ba[ctr++] = (byte) c;
			} else if (c < 0x800) {
				ba[ctr++] = (byte) (0xC0 | c >> 6);
				ba[ctr++] = (byte) (0x80 | c & 0x3F);
			} else if (c < 0x10000) {
				ba[ctr++] = (byte) (0xE0 | c >> 12);
				ba[ctr++] = (byte) (0x80 | c >> 6 & 0x3F);
				ba[ctr++] = (byte) (0x80 | c & 0x3F);
			} else if (c < 0x200000) {
				ba[ctr++] = (byte) (0xE0 | c >> 18);
				ba[ctr++] = (byte) (0x80 | c >> 12 & 0x3F);
				ba[ctr++] = (byte) (0x80 | c >> 6 & 0x3F);
				ba[ctr++] = (byte) (0x80 | c & 0x3F);
			} else if (c < 0x800) {

			}
		} // end for

		return trim(ba, ctr);
	} // End myGetBytesUtf8

	private static byte[] trim(byte[] ba, int len) {
		if (len == ba.length)
			return ba;
		byte[] tba = new byte[len];
		System.arraycopy(ba, 0, tba, 0, len);
		return tba;
	}
	
	
	void run()
	{
		BufferedReader in = null;
	    //BufferedWriter out = null;
	    BufferedOutputStream out = null;
	    
		String s;
		//ConfigTable configTable=null;
		
		int state = 0;
		
		try {
			in = new BufferedReader(new FileReader(trTitBibFilename));
			//out = new BufferedWriter(new FileWriter(trTitBibFilenameOut));
			out = new BufferedOutputStream(new FileOutputStream(trTitBibFilenameOut, false)); // don't append


			int rowCtr = 0;
			int skippedCtr=0;
			int writtenCtr=0;
			while (true) {
				try {
					indexByteBuffer = 0;
					s = in.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
					rowCtr++;
					if ((rowCtr & 0x1FFF) == 0)						
						System.out.println("Fatti " + rowCtr + " record");
						
//if (rowCtr == 50)
//	break;
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;
					
//						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_FALSE); //  " "
						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); // 19/08/2020
					
						

//for (int i=0; i< ar.length; i++)
//	System.out.println(i + " '" + ar[i] + "'");
						if (ar.length != 8)
						{
							System.out.println("Riga " + rowCtr + " non contiene 8 campi ma " + ar.length + ": '" + s + "', SCARTATA");
							skippedCtr++;
							continue;
						}
						
						
						// Stiamo cambiando BID?
						if (oldBid.startsWith(ar[Fields.BID.ordinal()]))
						{ // STESSO BID
						}
						else
						{ // CAMBIO BID
							oldBid = ar[Fields.BID.ordinal()];
							oldPolo = ""; // ar[Fields.CD_POLO.ordinal()];
						}
						char operazione = trovaTipoOperazione();
						if (operazione != '?')
						{
							writtenCtr++;
							
//							out.write(ar[Fields.TS_INS.ordinal()] + "�" + ar[Fields.BID.ordinal()] + "�" + ar[Fields.CD_POLO.ordinal()] + "�" + ar[Fields.CD_BIBLIOTECA.ordinal()] + "�" + operazione + "\n");// );

//							String outString = ar[Fields.TS_INS.ordinal()] + "�" + ar[Fields.BID.ordinal()] + "�" + ar[Fields.CD_POLO.ordinal()] + "�" + ar[Fields.CD_BIBLIOTECA.ordinal()] + "�" + operazione + "\n"; 
//							out.write(myGetBytesUtf8(outString)); // utf8 translation
							
							 
							fieldByteBuffer = myGetBytesUtf8(ar[Fields.TS_INS.ordinal()]); 
							System.arraycopy(fieldByteBuffer, 0, byteBuffer, indexByteBuffer, fieldByteBuffer.length);
							indexByteBuffer += fieldByteBuffer.length;				

							byteBuffer[indexByteBuffer++] = fieldSeparatorByte;
							fieldByteBuffer = myGetBytesUtf8(ar[Fields.BID.ordinal()]); 
							System.arraycopy(fieldByteBuffer, 0, byteBuffer, indexByteBuffer, fieldByteBuffer.length);
							indexByteBuffer += fieldByteBuffer.length;				

							byteBuffer[indexByteBuffer++] = fieldSeparatorByte; 
							fieldByteBuffer = myGetBytesUtf8(ar[Fields.CD_POLO.ordinal()]); 
							System.arraycopy(fieldByteBuffer, 0, byteBuffer, indexByteBuffer, fieldByteBuffer.length);
							indexByteBuffer += fieldByteBuffer.length;				
							
							byteBuffer[indexByteBuffer++] = fieldSeparatorByte; 
							fieldByteBuffer = myGetBytesUtf8(ar[Fields.CD_BIBLIOTECA.ordinal()]); 
							System.arraycopy(fieldByteBuffer, 0, byteBuffer, indexByteBuffer, fieldByteBuffer.length);
							indexByteBuffer += fieldByteBuffer.length;				

							byteBuffer[indexByteBuffer++] = fieldSeparatorByte; 
							byteBuffer[indexByteBuffer++] = (byte)operazione; // \n

							
							byteBuffer[indexByteBuffer++] = (byte)0x0a; // \n
							// Scrittura BYTES
							byte [] outByteBuffer = new byte[indexByteBuffer];
							System.arraycopy(byteBuffer, 0, outByteBuffer, 0, indexByteBuffer);
							out.write(outByteBuffer);
							
							
						}
						else
						{
//							System.out.println("Skipped bid " + ar[Fields.BID.ordinal()] + ", FL_POSSESSO=" + ar[Fields.FL_POSSESSO.ordinal()]);
							skippedCtr++;
						}
						
					}
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				
//				if (rowCtr == 100)	// for debugging
//					break;
				
				
				} // End while 
			
			
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				try {
					System.out.println("Input " + trTitBibFilename);
					System.out.println("Output " + trTitBibFilenameOut);
					System.out.println("Letti " + rowCtr + " record");
					System.out.println("Scartati  " + skippedCtr + " record");
					System.out.println("Scritti " + writtenCtr + " record");
					System.out.println("\n\nFine !");
					in.close();
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
	

	public char trovaTipoOperazione()
	{
		char operazione = '?';
		
		if (ar[Fields.FL_POSSESSO.ordinal()].charAt(0) == 'N') // 26/04/2011
			return operazione;
		
		
		if (!ar[Fields.BID.ordinal()].startsWith(ar[Fields.CD_POLO.ordinal()])) 
		{ // codice polo diverso da polo in BID
				operazione =  'L';	
		}
		else
		{// codice polo uguale a polo in BID
			// Cambio POLO?
			if (oldPolo.startsWith(ar[Fields.CD_POLO.ordinal()]))
			{ // STESSO POLO
					operazione =  'L';	
			}
			else
			{ // CAMBIO POLO
				oldPolo = ar[Fields.CD_POLO.ordinal()];
				if (!ar[Fields.BID.ordinal()].startsWith(ar[Fields.CD_POLO.ordinal()])) 
				{ // codice polo diverso a polo in BID
						operazione =  'L';	
				}
				else
				{ // CREAZIONE
					operazione =  'C';	
				}
			}
		}
		return operazione;
	}

};  // End Statistiche2006
	

