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



public class DoppioniTitoloAutoreResp1Cles {
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
	
	
	
	char charSepArray[] = { 0x01}; //'�' C0
	char charSepArray2[] = { '|'}; 
	char charSepOut = 0x01;
	
	String ar[];
	String arLegame[];

	enum Fields {
		cles1_2,
		cd_livello,
		aa_pubb_1,
		edizione,
		pagine,
		bid,
		tp_materiale,
		tp_record_uni,
		indice_isbd,
		isbd
	};	

	

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

		
		if(args.length < 4)
	    {
	        System.out.println("Uso: DoppioniTitoloAutoreResp1Cles ...dop.srt outfilename tr_tit_aut.out.srt.rel tr_tit_aut.out.srt.rel.off"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start=
	       "DoppioniTitoloAutoreResp1Cles tool - � Almaviva S.p.A 2011"+
		 "\n=========================================================="+
		 "\nTool pulizia dati per la gestione dei dooppioni (fusioni)";

	    System.out.println(start);

	    DoppioniTitoloAutoreResp1Cles doppioniTitoloAutoreResp1Cles = new DoppioniTitoloAutoreResp1Cles(args);
	    doppioniTitoloAutoreResp1Cles.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
	}
	DoppioniTitoloAutoreResp1Cles (String[] args)
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

			int lastClesCtr=0;
			String lastCles = "";
			
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
						if ((rowCtr & 0x7FF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Legami senza responsabilita 1 = " + noLegameResp1Ctr + " record");
							System.out.println("Legami non trovati: " + legamiNonTrovatiCtr);
							System.out.println("Cless univoche scartate " + clesUnivocaCtr);
							System.out.println("Scritti " + writtenCtr + " record");
							
						}
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;

						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
//						ar = MiscString.estraiCampi(s, charSepArray2, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						if (ar.length != 10)
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
								if (lastCles.equals(ar[Fields.cles1_2.ordinal()]))
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
								lastCles = ar[Fields.cles1_2.ordinal()];
//								lastRecord = arLegame[i].substring(0,10) + '�' + s + "\n"; 
								

								String editore = "";
								// Prendiamo l'area di pubblicazione
								String indiceAree = ar[Fields.indice_isbd.ordinal()];
								int areaPub = indiceAree.indexOf("210-");
								if (areaPub != -1)
								{
									int pos = Integer.parseInt(indiceAree.substring(areaPub+4, areaPub+4+4));
									
									int posNextArea;
									
									// Abbiamo un-altra area dopo la 210
									if (indiceAree.length() > areaPub + 9)
										posNextArea = Integer.parseInt(indiceAree.substring(areaPub+4+9, areaPub+4+9+4));
									else
										posNextArea = ar[Fields.isbd.ordinal()].length();
									
									if (posNextArea > ar[Fields.isbd.ordinal()].length() 
										|| posNextArea < 0 // 15/07/13 
										)
									{
										String errore = "Errore in indice aree per editore del bid " + ar[Fields.bid.ordinal()];  
										//editore = errore;
										System.out.println(errore);
										
									}
									else
									{
										if (pos -1 < 0 || posNextArea  < 0)
										{
											String errore = "Errore in indice aree per editore del bid " + ar[Fields.bid.ordinal()];  
											//editore = errore;
											System.out.println(errore);
										}
										else
										{
											if (posNextArea <=  pos-1)
											{
												String errore = "Errore in posNextArea: " + posNextArea + " pos-1=" +(pos-1);   
												//editore = errore;
												System.out.println(errore);
											}
											else
											{
												String areaPubblicazione = ar[Fields.isbd.ordinal()].substring(pos-1, posNextArea);
												// Troviamo ora l'editore
												int areaEditore = areaPubblicazione.indexOf(":");
												editore = areaPubblicazione.substring(areaEditore+1);
												// Ripuliamo dati dopo editore

												if (editore.indexOf(",") != -1) // data pubblicazione
													editore = editore.substring(0, editore.indexOf(","));
												else 
												if (editore.indexOf("[") != -1) // funzione distributore
													editore = editore.substring(0, editore.indexOf("["));
												else 
												if (editore.indexOf("(") != -1) // luogo di stampa
													editore = editore.substring(0, editore.indexOf("("));
												
											}
										}
										
									}
									
								}
								
								
								lastRecord =
								ar[Fields.cles1_2.ordinal()] + charSepOut // "�" // Cles1 + cles2
								+ arLegame[i].substring(0,10) + charSepOut // '�' // vid
								+ ar[Fields.aa_pubb_1.ordinal()] + charSepOut // "�"	// Anno di pubblicazione
								+ ar[Fields.pagine.ordinal()] + charSepOut // "�" // Numero di pagine
								+ ar[Fields.edizione.ordinal()] + charSepOut // "�" // Edizione
								+ ar[Fields.cd_livello.ordinal()] + charSepOut // "�" // Codice livello di autorita'
								+ ar[Fields.bid.ordinal()] + charSepOut // "�" 	// Bid
								+ ar[Fields.tp_materiale.ordinal()] + charSepOut // "�" 	
								+ ar[Fields.tp_record_uni.ordinal()] + charSepOut // "�"
								+ editore + charSepOut // "�"// Editore
								+ ar[Fields.isbd.ordinal()]  // Isbd
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
			

			tbTitoloDopIn.close();
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
