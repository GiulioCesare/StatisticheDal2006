import it.finsiel.misc.Misc;
import it.finsiel.misc.MiscString;
import it.finsiel.misc.MiscStringTokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;



public class DoppioniTitoloAutoreResp1AB {
    String tbTitoloDopFilename;
	String filenameOut;
    String trTitAutRelFilename;
    String trTitAutRelOffsetFilename;

    
	BufferedReader tbTitoloDopIn = null;
	BufferedWriter fileOut = null;

	RandomAccessFile  	trTitAutRelIn = null;
    MappedByteBuffer 	trTitAutRelOffsetIn = null;
	long fileOffetLength;
	long bidsInTrTitAutRel;
	
	
	
//	char charSepArray[] = { '�'}; // C0
	char charSepArray[] = { ''}; // 0x01 
	char charSepArray2[] = { '|'}; 

//	String  sepOut = "�"; // "|"
	String  sepOut = ""; // 0x01

	String ar[];
	String arLegame[];

	enum Fields {
//		cles1_2,
		k_titolo,	// 25/09/2017
		k_complemento_titolo,
		cd_natura,
		cd_livello,
		bid,
		isbd
	};	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		if(args.length < 4)
	    {
	        System.out.println("Uso: doppioniTitoloAutoreResp1AB ...dopAB.srt outfilename tr_tit_aut.out.srt.rel tr_tit_aut.out.srt.rel.off"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="doppioniTitoloAutoreResp1AB tool - (c) Almaviva S.p.A 2013-2017"+
	    		   "\n==============================================================="+
		 "\nTool pulizia dati per la gestione dei dooppioni A e B (fusioni)";

	    System.out.println(start);

	    DoppioniTitoloAutoreResp1AB doppioniTitoloAutoreResp1AB = new DoppioniTitoloAutoreResp1AB(args);
	    doppioniTitoloAutoreResp1AB.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
	}
	DoppioniTitoloAutoreResp1AB (String[] args)
	{
	    tbTitoloDopFilename = args[0];
	    filenameOut = args[1];
	    trTitAutRelFilename = args[2];
	    trTitAutRelOffsetFilename = args[3];
	} // End EstrazioneDatiPerDoppioni

	void run()
	{
	    
		String s, t;
		//ConfigTable configTable=null;
		int rowCtr = 0;
		int noLegameResp1Ctr = 0;
		int writtenCtr = 0;
		int clesUnivocaCtr = 0;
//		int deletedCtr=0;
		
		int legamiNonTrovatiCtr =0;
		
		try {
			
			tbTitoloDopIn = new BufferedReader(new FileReader(tbTitoloDopFilename));
			fileOut = new BufferedWriter(new FileWriter(filenameOut));
			

			// Portiamo tutto il file degli offset in memoria
			fileOffetLength = new File(trTitAutRelOffsetFilename).length();
			bidsInTrTitAutRel = fileOffetLength/22; 
			trTitAutRelOffsetIn = new FileInputStream(trTitAutRelOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			trTitAutRelIn = new RandomAccessFile(trTitAutRelFilename, "r");
			
			
			
			String lastRecord = "";

			int lastKTitoloCtr=0;
//			String lastCles = "";
			String lastKTitolo = "";
			
			while (true) {
				try {
					s = tbTitoloDopIn.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						rowCtr++;

//if (rowCtr >= 5615)
//	System.out.println(s);
						
//						if ((rowCtr & 0x1FFF) == 0)
//						if ((rowCtr & 0x7FF) == 0)
						if ((rowCtr & 0xFF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
//							System.out.println("Legami senza responsabilita 1 = " + noLegameResp1Ctr + " record");
//							System.out.println("Legami non trovati: " + legamiNonTrovatiCtr);
							System.out.println("Cless univoche scartate " + clesUnivocaCtr);
							System.out.println("Scritti " + writtenCtr + " record");
							
						}
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;

						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						if (ar.length != 6)
						{
							System.out.println("Numero campi errato " + ar.length + " invece di 6. Riga " + rowCtr + " Record: " + s);
							continue;
						}
						
						
						// Cerchiamo i legami all'autore
						long offset = findOffset(ar[Fields.bid.ordinal()]);
						if (offset != -1)
						{
							//System.out.println("Bid non trovato: '" + ar[Fields.bid.ordinal()] + "'");
							trTitAutRelIn.seek(offset);
							t = trTitAutRelIn.readLine(); // Leggi legami titolo autore
							arLegame = MiscString.estraiCampi(t, charSepArray2, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						}
						else 
							arLegame = null;


//System.out.println("Last cles='"+lastCles+"', current cles='"+ar[Fields.cles1_2.ordinal()]+"'");
						
//System.out.println("Legami ad autore: "+arLegame.length);

//						int i=1;
								
								// Stessa Cles?
								if (lastKTitolo.equals(ar[Fields.k_titolo.ordinal()]))
								{
									fileOut.write(lastRecord); // Scriviamo record precedente
									writtenCtr++;
									lastKTitoloCtr++;
								}
								else
								{	// Cambio cles
									
									// Ultimo record era singolo o multiplo?
									if (lastKTitoloCtr == 1)
									{
										clesUnivocaCtr++; // Singolo, droppato
//System.out.println("Chiave univoca" + lastCles);
										
//										noLegameResp1Ctr++;
									}
									else
									{
										fileOut.write(lastRecord); // Scriviamo record precedente in quanto multiplo
										writtenCtr++;
									}
									lastKTitoloCtr=1;
								}
								lastKTitolo = ar[Fields.k_titolo.ordinal()];
//								lastRecord = arLegame[i].substring(0,10) + '�' + s + "\n"; 
								

//								String editore = "";
								if (arLegame == null)
								{
									lastRecord =
											ar[Fields.k_titolo.ordinal()] + sepOut
											+ ar[Fields.k_complemento_titolo.ordinal()] + sepOut
											+ "" + sepOut // Senza autore
											+ ar[Fields.bid.ordinal()] + sepOut
											+ ar[Fields.cd_livello.ordinal()] + sepOut 
											+ ar[Fields.cd_natura.ordinal()] + sepOut 
											+ ar[Fields.isbd.ordinal()]
											+ "\n";
								}
								else
								{
									int i=1;
									for (; i < arLegame.length; i++)
									{
										char chr = arLegame[i].charAt(11); 
			//System.out.println("Legame autore resp: "+chr);
										if ( chr == '1') // Top responsabilita' 1?
										{
										lastRecord =
										ar[Fields.k_titolo.ordinal()] + sepOut
										+ ar[Fields.k_complemento_titolo.ordinal()] + sepOut
										+ arLegame[i].substring(0,10) + sepOut	// Con autore di resp 1
										+ ar[Fields.bid.ordinal()] + sepOut
										+ ar[Fields.cd_livello.ordinal()] + sepOut 
										+ ar[Fields.cd_natura.ordinal()] + sepOut 
										+ ar[Fields.isbd.ordinal()]
										+ "\n";
										break;
										}
									} // End for
									if (i == arLegame.length)
									{ // Nessuna responsabilita 1
										lastRecord =
												ar[Fields.k_titolo.ordinal()] + sepOut
												+ ar[Fields.k_complemento_titolo.ordinal()] + sepOut
												+ "" + sepOut // Senza autore
												+ ar[Fields.bid.ordinal()] + sepOut
												+ ar[Fields.cd_livello.ordinal()] + sepOut 
												+ ar[Fields.cd_natura.ordinal()] + sepOut 
												+ ar[Fields.isbd.ordinal()]
												+ "\n";
									}
								}
						}
						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			} // End while
			
			// Ultimo record era singolo o multiplo?
			if (lastKTitoloCtr == 1)
			{
				clesUnivocaCtr++; // Singolo, droppato
				noLegameResp1Ctr++;
			}
			else
			{
				fileOut.write(lastRecord); // Scriviamo record precedente in quanto multiplo
				writtenCtr++;
			}
			

			tbTitoloDopIn.close();
//			trTitAutRelIn.close();	
			fileOut.close();
		
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	System.out.println("\nLetti " + rowCtr + " record");
	System.out.println("Legami senza responsabilita 1 = " + noLegameResp1Ctr + " record");
	System.out.println("Legami non trovati: " + legamiNonTrovatiCtr);
	System.out.println("Cless univoche scartate " + clesUnivocaCtr);
	System.out.println("Scritti " + writtenCtr + " record");
	
		
	
	System.out.println("Fine ");
	
	
	} // End run
	

	
	long findOffset(String key)
	{
		byte[] keyOffsetTitolo = new byte[21];
		
	    long first = 0;
	    long upto  = bidsInTrTitAutRel;
	    long returnOffset = -1;
	    String s, bid, offset;
	    int positionTo;
	    
	    while (first < upto) {
	        long mid = (first + upto) / 2;  // Compute mid point.

	        positionTo = (int)mid*22;
	    	trTitAutRelOffsetIn.position(positionTo);
	    	trTitAutRelOffsetIn.get(keyOffsetTitolo, 0, 21);			
	        
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
	
	
}