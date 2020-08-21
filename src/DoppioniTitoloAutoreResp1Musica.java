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

//import EstrazioneDatiPerDoppioniMusica.fieldTbComposizione;



public class DoppioniTitoloAutoreResp1Musica {
    String tbComposizioneDopFilename;
	String filenameOut;
    String trTitAutRelFilename;
    String trTitAutRelOffsetFilename;

    
	BufferedReader tbComposizioneDopIn = null;
	BufferedWriter fileOut = null;

	RandomAccessFile  	trTitAutRelIn = null;
    MappedByteBuffer 	trTitAutRelOffsetIn = null;
	long fileOffetLength;
	long bidsInTrTitAutRel;
	
	
	
	char charSepArray[] = { 0x01 }; //'�', C0
	char charSepArray2[] = { '|'};
//	char charSepArray2[] = { '�'};
	
//	String stringSepArray[] = { "&$%" };
//	String stringSepArray[] = { "�" };

	char charSepOut = 0x01; //'�'; // |
	
	String ar[];
	String arLegame[];

	enum Fields {
//		bid,
//		cd_forma_1,
//		cd_forma_2,
//		cd_forma_3,
//		numero_ordine,
//		numero_opera,
//		numero_cat_tem,
//		cd_tonalita,
//		datazione,
//		aa_comp_1,
//		aa_comp_2,
//		ds_sezioni,
//		ky_ord_ric,
//		ky_est_ric,
//		ky_app_ric,
//		ky_ord_clet,
//		ky_est_clet,
//		ky_app_clet,
//		ky_ord_pre,
//		ky_est_pre,
//		ky_app_pre,
//		ky_ord_den,
//		ky_est_den,
//		ky_app_den,
//		ky_ord_nor_pre,
//		ky_est_nor_pre,
//		ky_app_nor_pre,
//		ute_ins,
//		ts_ins,
//		ute_var,
//		ts_var,
//		fl_canc

		cles,  
		bid,
		cd_forma_1,
		numero_ordine,
		numero_opera,
		numero_cat_tem,
		cd_tonalita
		
	};	

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		if(args.length < 4)
	    {
	        System.out.println("Uso: DoppioniTitoloAutoreResp1Musica ...dopMusica.srt outfilename tr_tit_aut.out.srt.rel tr_tit_aut.out.srt.rel.off"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start=
	       "DoppioniTitoloAutoreResp1Musica tool - (c) Almaviva S.p.A 2013"+
		 "\n=============================================================="+
		 "\nTool pulizia dati per la gestione dei dooppioni di musica (fusioni)";

	    System.out.println(start);

	    DoppioniTitoloAutoreResp1Musica doppioniTitoloAutoreResp1Musica = new DoppioniTitoloAutoreResp1Musica(args);
	    doppioniTitoloAutoreResp1Musica.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
	}
	DoppioniTitoloAutoreResp1Musica (String[] args)
	{
	    tbComposizioneDopFilename = args[0];
	    filenameOut = args[1];
	    trTitAutRelFilename = args[2];
	    trTitAutRelOffsetFilename = args[3];
	    
	    for (int i=0; i < args.length; i++)
	    	System.out.println("arg[" + i+"] = " + args[i]);
	    
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
			
			tbComposizioneDopIn = new BufferedReader(new FileReader(tbComposizioneDopFilename));
			fileOut = new BufferedWriter(new FileWriter(filenameOut));
			

			// Portiamo tutto il file degli offset in memoria
			fileOffetLength = new File(trTitAutRelOffsetFilename).length();
			bidsInTrTitAutRel = fileOffetLength/22; 
			trTitAutRelOffsetIn = new FileInputStream(trTitAutRelOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			trTitAutRelIn = new RandomAccessFile(trTitAutRelFilename, "r");
			
			
			
			String lastRecord = "";

			int lastClesCtr=0;
			String lastCles = "";
			
			while (true) {
				try {
					s = tbComposizioneDopIn.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						rowCtr++;

//if (rowCtr >= 5615)
//	System.out.println(s);
						
						if ((rowCtr & 0x1FFF) == 0)
//						if ((rowCtr & 0x7FF) == 0)
//						if ((rowCtr & 0xFF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Legami senza responsabilita 1 = " + noLegameResp1Ctr + " record");
							System.out.println("Legami non trovati: " + legamiNonTrovatiCtr);
							System.out.println("ky_ord_nor_pre + ky_est_nor+pre (Cles) univoche scartate " + clesUnivocaCtr);
							System.out.println("Scritti " + writtenCtr + " record");
							
						}
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;

						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						if (ar.length != 7)
						{
							System.out.println("Numero campi errato " + ar.length + " invece di 7. Riga " + rowCtr + " Record: " + s);
							continue;
						}
						
						
						// Cerchiamo i legami all'autore
						long offset = findOffset(ar[Fields.bid.ordinal()]);
						if (offset == -1)
						{
							//System.out.println("Bid non trovato: '" + ar[Fields.bid.ordinal()] + "'");
							legamiNonTrovatiCtr++;
							continue;
						}
						trTitAutRelIn.seek(offset);
						t = trTitAutRelIn.readLine(); // Leggi legami titolo autore
//System.out.println(t);

						// Scompattiamo i campi dei legami
						arLegame = MiscString.estraiCampi(t, charSepArray2, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						
						int i=1;
						for (; i < arLegame.length; i++)
						{
							char chr = arLegame[i].charAt(11); 
							if ( chr == '1') // Top responsabilita' 1?
							{
								// Scriviamo il record
								
								// Stessa Cles?
								if (lastCles.equals(ar[Fields.cles.ordinal()]))
								{
									fileOut.write(lastRecord); // Scriviamo record precedente
									writtenCtr++;
									lastClesCtr++;
								}
								else
								{	// Cambio cles
									// Ultimo record era singolo o multiplo?
									if (lastClesCtr == 1)
									{
										clesUnivocaCtr++; // Singolo, droppato
//System.out.println("Chiave univoca" + lastCles);
										
										noLegameResp1Ctr++;
									}
									else
									{
										fileOut.write(lastRecord); // Scriviamo record precedente in quanto multiplo
										writtenCtr++;
									}
									lastClesCtr=1;
								}
								lastCles = ar[Fields.cles.ordinal()];
//								lastRecord = arLegame[i].substring(0,10) + '�' + s + "\n"; 
								

//								String editore = "";
								
								
								lastRecord =
								ar[Fields.cles.ordinal()]+charSepOut // Cles1 + cles2
								+ ar[Fields.bid.ordinal()] + charSepOut 	// Bid
								+ arLegame[i].substring(0,10) + charSepOut // vid
								+ ar[Fields.cd_forma_1.ordinal()] + charSepOut // Codice forma
								+ ar[Fields.numero_ordine.ordinal()] + charSepOut
								+ ar[Fields.numero_opera.ordinal()] + charSepOut
								+ ar[Fields.numero_cat_tem.ordinal()] + charSepOut
								+ ar[Fields.cd_tonalita.ordinal()] + charSepOut
								+ "\n";
								break;
							}
						} // End for
						
						if (i == arLegame.length)
						{
							//System.out.println("Lemane resp1 mancante:" + t);
							noLegameResp1Ctr++;	
						}
						
						}
					
						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			} // End while
			
			// Ultimo record era singolo o multiplo?
			if (lastClesCtr == 1)
			{
				clesUnivocaCtr++; // Singolo, droppato
				noLegameResp1Ctr++;
			}
			else
			{
				fileOut.write(lastRecord); // Scriviamo record precedente in quanto multiplo
				writtenCtr++;
			}
			

			tbComposizioneDopIn.close();
			trTitAutRelIn.close();	
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
	System.out.println("ky_ord_nor_pre + ky_est_nor+pre (Cles) univoche scartate " + clesUnivocaCtr);
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
	
} // End DoppioniAutoriResp1Musica
