import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import it.finsiel.misc.Misc;
import it.finsiel.misc.MiscString;
import it.finsiel.misc.MiscStringTokenizer;


public class LegamiM09a {
	public static final int MAX_BYTES_PER_UTF8_CHARACTER = 4;

	String bidListFilename;
	String filenameOut;
    
	int legamiAutoreNonTrovatiCtr =0;
	int legamiTitoloNonTrovatiCtr =0;

    
	BufferedReader bidListIn = null;
	BufferedWriter fileOut = null;

	long fileOffetLength;

	String trTitAutRelFilename;
    String trTitAutRelOffsetFilename;
	RandomAccessFile  	trTitAutRelIn = null;
    MappedByteBuffer 	trTitAutRelOffsetIn = null;
	long bidsInTrTitAutRel;
	
	String trTitTitRelFilename;
    String trTitTitRelOffsetFilename;
	RandomAccessFile  	trTitTitRelIn = null;
    MappedByteBuffer 	trTitTitRelOffsetIn = null;
	long bidsInTrTitTitRel;
	
	
	
	
	//String  sepOutC0 = "�"; // C0 - <EF><BF><BD>
	//String  sepOutPipe = "|"; // C0 - <EF><BF><BD>
	String  sepOut01 = ""; // 0x01
	
	
	//char charSepArray[] = { '�'}; // C0
	char charSepArrayPipe[] = { '|'}; 
	char charSepArray2[] = { ''}; // 0x01 
	char charSepArrayComma[] = { ','}; 

	String ar[];
	String arLegami[];

	enum FieldsTitolo {
		bid,
		cles1_2,
		titolo
	};	

	
	
//	private byte[] myGetBytesUtf8(String s) {
//		int len = s.length();
//		int en = MAX_BYTES_PER_UTF8_CHARACTER * len;
//		byte[] ba = new byte[en];
//		if (len == 0)
//			return ba;
//
//		int ctr = 0;
//
//		for (int i = 0; i < len; i++) {
//			char c = s.charAt(i);
//			if (c < 0x80) {
//				ba[ctr++] = (byte) c;
//			} else if (c < 0x800) {
//				ba[ctr++] = (byte) (0xC0 | c >> 6);
//				ba[ctr++] = (byte) (0x80 | c & 0x3F);
//			} else if (c < 0x10000) {
//				ba[ctr++] = (byte) (0xE0 | c >> 12);
//				ba[ctr++] = (byte) (0x80 | c >> 6 & 0x3F);
//				ba[ctr++] = (byte) (0x80 | c & 0x3F);
//			} else if (c < 0x200000) {
//				ba[ctr++] = (byte) (0xE0 | c >> 18);
//				ba[ctr++] = (byte) (0x80 | c >> 12 & 0x3F);
//				ba[ctr++] = (byte) (0x80 | c >> 6 & 0x3F);
//				ba[ctr++] = (byte) (0x80 | c & 0x3F);
//			} else if (c < 0x800) {
//
//			}
//		} // end for
//		return trim(ba, ctr);
//	} // End myGetBytesUtf8
//	private static byte[] trim(byte[] ba, int len) {
//		if (len == ba.length)
//			return ba;
//		byte[] tba = new byte[len];
//		System.arraycopy(ba, 0, tba, 0, len);
//		return tba;
//	}	
	

//	enum FieldsLegame {
//		targetVid,
//		tpResponsabilita,
//		cdRelazione,
//		flIncerto,
//		flSuperfluo
//	};	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		if(args.length < 6)
	    {
	        System.out.println("Uso: LegamiM09a file_titoli outfilename tr_tit_aut.out.srt.rel tr_tit_aut.out.srt.rel.off tr_tit_tit.out.srt.rel tr_tit_tit.out.srt.rel.off"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="LegamiM09a tool - � Almaviva S.p.A 15/07/2016"+
		 "\n============================================="+
		 "\nTool di preparazione file per legami M09 (monografie non musicali a titoli uniformi)";

	    System.out.println(start);

	    LegamiM09a LegamiM09a = new LegamiM09a(args);
	    LegamiM09a.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
	}
	LegamiM09a (String[] args)
	{
	    bidListFilename = args[0];
	    filenameOut = args[1];
	    trTitAutRelFilename = args[2];
	    trTitAutRelOffsetFilename = args[3];
	    
	    trTitTitRelFilename = args[4];
	    trTitTitRelOffsetFilename = args[5];
	    
	} // End EstrazioneDatiPerDoppioni


	

	void run()
	{
	    
		String s, t;
		//ConfigTable configTable=null;
		int rowCtr = 0;
		int noLegameResp1_2Ctr = 0;
		int writtenCtr = 0;
		int clesUnivocaCtr = 0;
//		int deletedCtr=0;
		
		
		try {
			
//			bidListIn = new BufferedReader(new FileReader(bidListFilename));
			bidListIn = new BufferedReader(new InputStreamReader(new FileInputStream(bidListFilename), "UTF8"));
			
			
			fileOut = new BufferedWriter(new FileWriter(filenameOut));
//			fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filenameOut), "UTF8"));
//			BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(filenameOut, false)); // dont append
			
//			fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("some_output.utf8"), Charset.forName("UTF-8").newEncoder()));
			
//			Writer fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("filenameOut"), "UTF-8"));
			 
			
			// Portiamo gli indici in memoria
			// Titolo-Autore
			fileOffetLength = new File(trTitAutRelOffsetFilename).length();
			bidsInTrTitAutRel = fileOffetLength/22; 
			trTitAutRelOffsetIn = new FileInputStream(trTitAutRelOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			trTitAutRelIn = new RandomAccessFile(trTitAutRelFilename, "r");
			
			// Titolo-Titolo
			fileOffetLength = new File(trTitTitRelOffsetFilename).length();
			bidsInTrTitTitRel = fileOffetLength/22; 
			trTitTitRelOffsetIn = new FileInputStream(trTitTitRelOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			trTitTitRelIn = new RandomAccessFile(trTitTitRelFilename, "r");
			
			
			
			
			String lastRecord = "";

			int lastClesCtr=0;
			String lastCles = "";
			
			while (true) {
				try {
					s = bidListIn.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						rowCtr++;
//if (true)
//{
//	fileOut.write(s+"\n");
//	continue;
//}

						
//if (rowCtr >= 5615)
//	System.out.println(s);
						
//						if ((rowCtr & 0x1FFF) == 0)
						if ((rowCtr & 0x7FF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Titoli con autore senza responsabilita 1 o 2 = " + noLegameResp1_2Ctr + " record (scartato/i)");
							System.out.println("Titoli senza legami ad autore: " + legamiAutoreNonTrovatiCtr + " (scartato/i)");
							System.out.println("Monografie non legate a titoli uniformi: " + legamiTitoloNonTrovatiCtr);
							System.out.println("Titoli con legami ad autore scritti " + writtenCtr + " record");
						}
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;

						
						
						ar = MiscString.estraiCampi(s, charSepArray2, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						if (ar.length != 3)
						{
							System.out.println("Numero campi errato " + ar.length + " invece di 7. Riga " + rowCtr + " Record: " + s);
							continue;
						}
						
						
						// Cerchiamo i legami all'autore 
						long offset = findOffsetTitoloAutore(ar[FieldsTitolo.bid.ordinal()]);
						if (offset == -1)
						{
//							System.out.println("Autore non trovato per Bid: '" + ar[FieldsTitolo.bid.ordinal()] + "'");
							legamiAutoreNonTrovatiCtr++;
							continue;
						}
						
						trTitAutRelIn.seek(offset);
						t = trTitAutRelIn.readLine(); // Leggi legami titolo autore
//System.out.println(t);

						// Scompattiamo i campi dei legami
						arLegami = MiscString.estraiCampi(t, charSepArrayPipe, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						int i=1;
						boolean legameTrovato=false;
						for (; i < arLegami.length; i++)
						{
							char chr = arLegami[i].charAt(11); 
							if ( chr == '1' || chr == '2') // Responsabilita' 1 e 2
							{
								legameTrovato=true;
								
								// Troviamo il legame al titolo uniforme se esiste
								String legametitoloUniforme = getLegameTitoloUniforme(ar[FieldsTitolo.bid.ordinal()]);
								
								fileOut.write(legametitoloUniforme+sepOut01+arLegami[i].substring(0,10)+sepOut01+s+"\n");
								
							}
						} // End for

						if (!legameTrovato)
						{
//							System.out.println("Autore con responsabilita 1 o 2 non trovato per Bid: '" + ar[FieldsTitolo.bid.ordinal()] + "'");
							noLegameResp1_2Ctr++;
							continue;
						}
//						fileOut.write(myGetBytesUtf8(sepOut+s+"\n"));
						writtenCtr++;
						
						}
					
						
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			} // End while
			
			bidListIn.close();
			trTitAutRelIn.close();
			
			trTitTitRelIn.close();	
			fileOut.close();
		
		} catch (FileNotFoundException e) {
		e.printStackTrace();
	} 
	catch (IOException e1) {
		e1.printStackTrace();
	}
	
	System.out.println("\nLetti " + rowCtr + " record");
	System.out.println("Titoli con autore senza responsabilita 1 o 2 = " + noLegameResp1_2Ctr + " record (scartato/i)");
	System.out.println("Titoli senza legami ad autore: " + legamiAutoreNonTrovatiCtr + " (scartato/i)");
	System.out.println("Monografie non legate a titoli uniformi: " + legamiTitoloNonTrovatiCtr);
	System.out.println("Titoli con legami ad autore scritti " + writtenCtr + " record");
	
	
	System.out.println("Fine ");
	
	
	} // End run


	long findOffsetTitoloAutore(String key)
	{
		
		return findOffset21(key, trTitAutRelOffsetIn, bidsInTrTitAutRel);
	}
	long findOffsetTitoloTitolo(String key)
	{
		
		return findOffset21(key, trTitTitRelOffsetIn, bidsInTrTitTitRel);
	}
	
	long findOffset21(String key, MappedByteBuffer mappedByteBuffer, long elements)
	{
		byte[] keyOffsetTitolo = new byte[21];
		
	    long first = 0;
	    long upto  = elements;
	    long returnOffset = -1;
	    String s, bid, offset;
	    int positionTo;
	    
	    while (first < upto) {
	        long mid = (first + upto) / 2;  // Compute mid point.

	        positionTo = (int)mid*22;
	        mappedByteBuffer.position(positionTo);
	        mappedByteBuffer.get(keyOffsetTitolo, 0, 21);			
	        
	    	s = new String (keyOffsetTitolo); //.toString();

	//System.out.println("Test " + s + ", positionTo=" + positionTo + ", mid=" + mid);    	
	    	
	    	bid = s.substring(0, 10);
	        
	        if (key.compareTo(bid) < 0) {
	            upto = mid;       // repeat search in bottom half.
	        } else if (key.compareTo(bid) > 0) {
	            first = mid + 1;  // Repeat search in top half.
	        } else {
	        	// Convertiamo da stringa in numero
	        	offset = s.substring(10);
	        	returnOffset =  Long.parseLong(offset);
	            return returnOffset; //mid;       // Found it. return position
	        }
	    }
	    return returnOffset; //-(first + 1);      // Failed to find key
	    
	} // End findOffset
	
	
	
	String getLegameTitoloUniforme(String bid)
	{
		String t="";
		String arLegame[];
		String arLegamiTitolo[];
		// Cerchiamo i legami all'autore 
		long offset = findOffsetTitoloTitolo(bid);
		if (offset == -1)
		{
//			System.out.println("Titolo uniforme non trovato per Bid: '" + bid + "'");
			legamiTitoloNonTrovatiCtr++;
			return "          ";
		}
		
		try {
			trTitTitRelIn.seek(offset);
			t = trTitTitRelIn.readLine(); // Leggi legami titolo titolo
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//System.out.println(t);

		// Scompattiamo i campi dei legami
		arLegamiTitolo = MiscString.estraiCampi(t, charSepArrayPipe, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
		int i=1;
		// Troviamo il legame al titolo uniforme se esiste
		for (; i < arLegamiTitolo.length; i++)
		{
			arLegame = MiscString.estraiCampi(arLegamiTitolo[i], charSepArrayComma, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "

			
			char natura = arLegame[4].charAt(0);	// Prendiamo la natura del titolo 
			if ( natura == 'A') // Gestiamo solo un titolo uniforme (natura A per monografia)
				return arLegame[0];
		} // End for
		
		return "          ";
	}
	
}
