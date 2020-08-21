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
import java.util.Enumeration;
import java.util.Hashtable;



/*
 * Elabopriamo la statistica con i dati forniti dalla fase 1
*/
class Statistiche2006Fase2 {

	final char NAT_MONOGRAFIA = 'M';    
	final char NAT_TIT_NON_SIGNIFICATIVO = 'W';
	final char NAT_PERIODICO = 'S';
	
	final char MAT_ANTICO = 'E';    
	final char MAT_MODERNO = 'M';
	final char MAT_CARTOGRAFIA = 'C';
	final char MAT_GRAFICA = 'G';
	final char MAT_MUSICA = 'U';

	
	final int CTR_MAT_ANTICO = 0;
	final int CTR_MAT_MODERNO = 1;
	final int CTR_MAT_CARTOGRAFIA = 2;
	final int CTR_MAT_GRAFICA = 3;
	final int CTR_MAT_MUSICA = 4;
				
	final char OP_CREA = 'C';
	final char OP_LOCALIZZA = 'L';

	final int CTR_CREA = 0;
	final int CTR_CATTURA = 1;
	
	final int TIPI_MATERIALI = 5;
	
	
	public static void main(String[] args) {
		// 

//		char charSepArrayEquals[] = { '='};
//		char charSepArraySpace[] = { ' '};

		if(args.length < 4)
	    {
	        System.out.println("Uso: Statistiche2006Fase2 trTitBibStaFase2_Filename tbTitoloFilename tbTitoloOffsetFilename trTitBibStatFase2_indexCheck filenameOut"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="Statistiche2006Fase2 tool - (c) Almaviva S.p.A 2011-2020"+
		 "\n====================================="+
		 "\nTool di creazione statistiche dal 2006 per la fase2";

	    System.out.println(start);

	    Statistiche2006Fase2 statistiche2006Fase2 = new Statistiche2006Fase2(args);
	    statistiche2006Fase2.run();
	    //statistiche2006Fase2.runNew();
	    
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
	}// End main		
		

	public

	enum Fields {
		TS_INS,
		BID,
		CD_POLO,
		CD_BIBLIOTECA,
		FL_CREA_LOCALIZZA
};
	
	
	String trTitBibFilename;
    String tbTitoloFilename;
    String tbTitoloOffsetFilename;
	String trTitBibFilenameCheck;
	String trTitBibFilenameOut;
    
//	char charSepArrayEquals[] = { '='};
//	char charSepArraySpace[] = { ' '};
//	char charSepArray[] = { '�'}; // | pipe?

//	char charSepArray[] = { '�'}; // C0
	char charSepArray[] = { 0x01};

	char charSepArray2[] = { 0xC0}; // C0
//	char charSepArray2[] = { 0x01};
	
	String oldTsIns="";
	String oldBid="";
	String oldPolo="";
	String oldBiblioteca="";
	String ar[];
	String arTit[];
	
	byte[] bidBytes = new byte[22];
	long fileOffetLength;
	long bidsInTbTitolo;
	long bidsInTrTitBibCheck;
	
    BufferedWriter OutLog;
	BufferedReader trTitBinIn = null;
	RandomAccessFile  tbTitoloIn = null;

    //MappedByteBuffer trTitBibCheckIn = null;
	RandomAccessFile  trTitBibCheckRandomIn = null;
	
	BufferedWriter trTitBibOut = null;
    MappedByteBuffer trTitoloOffsetIn = null;
    
	
    long trTitBibFilenameCheckLength;
		
    
    
	Statistiche2006Fase2 (String[] args)
	{
	    trTitBibFilename = args[0];
	    tbTitoloFilename = args[1];
	    tbTitoloOffsetFilename = args[2];
	    trTitBibFilenameCheck = args[3];
	    trTitBibFilenameOut = args[4];
	    
	    System.out.println("trTitBibFilename="+trTitBibFilename);
	    System.out.println("tbTitoloFilename="+tbTitoloFilename);
	    System.out.println("tbTitoloOffsetFilename="+tbTitoloOffsetFilename);
	    System.out.println("trTitBibFilenameCheck="+trTitBibFilenameCheck);
	    System.out.println("trTitBibFilenameOut="+trTitBibFilenameOut);
	    
	    
	}
	
	void run()
	{
	    
		String s, t;
		//ConfigTable configTable=null;
		
		
		try {
			trTitBinIn = new BufferedReader(new FileReader(trTitBibFilename));
			tbTitoloIn = new RandomAccessFile(tbTitoloFilename, "r");

			// Portiamo tutto il file degli offset in memoria
			fileOffetLength = new File(tbTitoloOffsetFilename).length();
			bidsInTbTitolo = fileOffetLength/22; 
		    trTitoloOffsetIn = new FileInputStream(tbTitoloOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);

			trTitBibFilenameCheckLength = new File(trTitBibFilenameCheck).length();
			bidsInTrTitBibCheck = trTitBibFilenameCheckLength/(10+1+3+1+8+1); 
			trTitBibCheckRandomIn = new RandomAccessFile(trTitBibFilenameCheck, "r");


//String found = findTrTitBibCheckDate("PUV1010675 PUV");

			trTitBibOut = new BufferedWriter(new FileWriter(trTitBibFilenameOut));

//			if (!trTitoloOffsetIn.isLoaded())
//			{
//				System.out.println("Offset file not loaded");
////				trTitoloOffsetIn.load(); 
//			}	
			

				
			int tipiMaterialeCtr[][] = new int[5][2]; // 5 tipi materiale e due operazioni CREA/LOCALIZZA
			char[] tipiMaterialeChar = {
					MAT_ANTICO,
					MAT_MODERNO,
					MAT_CARTOGRAFIA,
					MAT_GRAFICA,
					MAT_MUSICA
					};
			
			int rowCtr = 0;
			int writtenCtr=0;
			int skippedCtr=0;
			int creaCtr = 0;
			int creaCtrInc = 0;
			int crea2LocCtr=0;
			int crea2LocCtrChecked=0;
			int keyNotFoundCtr=0;
			int bidNonTrovatoInTbTitoloCtr = 0;
			
			int tpMaterialeCtrEntry = 0;
			int ctrNatureInvalide = 0;
			while (true) {

//				if (rowCtr == 500)	// for debugging
//					break;
				
				try {
					s = trTitBinIn.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						
						if ((rowCtr & 0x3ff) == 0x3ff)
							System.out.println("rowCtr " + rowCtr);
						
						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						
						rowCtr++;						
//if (rowCtr == 10)
//	break;
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;
					
//						if (ar.length != 13)
//						{
//							System.out.println("Riga " + rowCtr + " non contiene 13 campi ma " + ar.length + ": '" + s + "', SCARTATA");
//							skippedCtr++;
//							continue;
//						}
	
						
						// E' un bid valido?
						long offset = findTitleOffset(ar[1]);
						if (offset == -1)
						{
							System.out.println("Bid non trovato: '" + ar[1] + "'");
							bidNonTrovatoInTbTitoloCtr++;
							continue;
						}
						
						// E' una natura valida?
						// Posizioniamoci sulla tb_titolo 
						tbTitoloIn.seek(offset);

						// Read line
						t = tbTitoloIn.readLine();

//System.out.println("Offset="+offset+", titolo='" + t + "'");
						
						// Scompattiamo i campi
						arTit = MiscString.estraiCampi(t, charSepArray2, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
	
//for (int i=0; i< arTit.length; i++)
//	System.out.println("arTit " + i + " '" + arTit[i] + "'");
	
						// Controlliamo di avere una natura valida
						if (arTit[4].charAt(0) != NAT_MONOGRAFIA 
							&& arTit[4].charAt(0) != NAT_TIT_NON_SIGNIFICATIVO
							&& arTit[4].charAt(0) != NAT_PERIODICO)
						{
//System.out.println("Riga " + rowCtr + ": " + arTit[0] + ", Natura=" + arTit[4]);
							ctrNatureInvalide++;
							continue;
						}
						// Troviamo la posizione del contatore del tipo materiale
						switch (arTit[2].charAt(0))
							{
							case MAT_ANTICO:
								tpMaterialeCtrEntry = CTR_MAT_ANTICO; 
								break;
							case MAT_CARTOGRAFIA:	
								tpMaterialeCtrEntry = CTR_MAT_CARTOGRAFIA; 
								break;
							case MAT_GRAFICA:	
								tpMaterialeCtrEntry = CTR_MAT_GRAFICA; 
								break;
							case MAT_MODERNO:	
								tpMaterialeCtrEntry = CTR_MAT_MODERNO; 
								break;
							case MAT_MUSICA:	
								tpMaterialeCtrEntry = CTR_MAT_MUSICA; 
								break;
							} // End switch
						
//if (ar[Fields.FL_CREA_LOCALIZZA.ordinal()].charAt(0) == 'C')
////if (ar[Fields.BID.ordinal()].equals("PBE0000867"))
//{
//	System.out.println("Record con flag 'C'rea in ingresso " + rowCtr + ": " + ar[Fields.BID.ordinal()]);
//	creaCtr++;
//}				
						
// Stiamo cambiando MESE?
if (	!ar[Fields.TS_INS.ordinal()].startsWith(oldTsIns)) 
		System.out.println("Cambio mese: " + ar[Fields.TS_INS.ordinal()]);						
						
						// Stiamo cambiando MESE, POLO o BIBLIOTECA
						if (	!ar[Fields.TS_INS.ordinal()].startsWith(oldTsIns) // solo mese
								|| !oldPolo.startsWith(ar[Fields.CD_POLO.ordinal()])
								|| !oldBiblioteca.startsWith(ar[Fields.CD_BIBLIOTECA.ordinal()])
								)
						{ // CAMBIO di mese, bid polo o biblioteca 
							// Scriviamo un record per ogni tipo materiale
							for (int i=0; i < TIPI_MATERIALI; i++)
							{
								if (tipiMaterialeCtr[i][CTR_CREA] != 0 || tipiMaterialeCtr[i][CTR_CATTURA] != 0) // scarichiamo solo per contatori valorizzati 
								{
									trTitBibOut.write(oldTsIns 
//+ "|" + oldBid // SOLO per TEST 
									                     + "|" + oldPolo 
									                     + "|" + oldBiblioteca 
									                     + "|" + tipiMaterialeChar[i] 
									                     + "|" + tipiMaterialeCtr[i][CTR_CREA] 
									                     + "|" + tipiMaterialeCtr[i][CTR_CATTURA] 
									                     + "|CURRENT_TIMESTAMP\n");	
									writtenCtr++;				
								
								}
							} // End for tipiMateriali

							// Reinizializzziamo variabili
							oldTsIns = ar[Fields.TS_INS.ordinal()].substring(0,6);
							oldBid = ar[Fields.BID.ordinal()];
							oldPolo = ar[Fields.CD_POLO.ordinal()];
							oldBiblioteca = ar[Fields.CD_BIBLIOTECA.ordinal()];
							
							// Puliamo i contatori
							for (int i=0; i < 5; i++)
								{
								tipiMaterialeCtr[i][0]=0;
								tipiMaterialeCtr[i][1]=0;
								}
							
						}
//						else { // NESSUN CAMBIO incrementiamo contatori
						if (ar[Fields.FL_CREA_LOCALIZZA.ordinal()].charAt(0) == 'C')
							{
								// Controlla se effettivamente e' una creazione controllando la data del titolo
								String dataTitolo = arTit[36];
								// Formattiamo la data yyyymmdd
								// 2005-5-19.16.43. 13. 500000000
								int monthStart, monthEnd, dayEnd;
								monthStart = dataTitolo.indexOf('-');
								monthEnd = dataTitolo.indexOf('-', monthStart+1);
//								dayEnd = dataTitolo.indexOf('.', monthEnd+1);
								dayEnd = dataTitolo.indexOf(' ', monthEnd+1); // 12/11/2012
								
								String dataTitoloNormalizzata = dataTitolo.substring(0,monthStart);
								String anno = dataTitoloNormalizzata;
								String month = dataTitolo.substring(monthStart+1, monthEnd);
								String day = dataTitolo.substring(monthEnd+1, dayEnd);
								
								if (month.length() > 1)
									dataTitoloNormalizzata += month;
								else 
									dataTitoloNormalizzata += "0" + month;

								if (day.length() > 1)
									dataTitoloNormalizzata += day;
								else 
									dataTitoloNormalizzata += "0" + day;

//if (ar[Fields.BID.ordinal()].equals("PBE0012567"))
//		{
//	System.out.println("Data in ingresso =" + ar[Fields.TS_INS.ordinal()] + " dataTitoloNormalizzata=" + dataTitoloNormalizzata); 
//		}
								
								if (ar[Fields.TS_INS.ordinal()].compareTo(dataTitoloNormalizzata) > 0)
								{ // Trttasi di localizzazione?
									
									//System.out.println("CREAZIONE che DIVENTA CATTURA per bid " + ar[Fields.BID.ordinal()] + ", data in TrTitBib=" + ar[Fields.TS_INS.ordinal()] + ", data in tbTitolo=" + dataTitoloNormalizzata);


									
									// Controlliamo se veramente creazione  tramite tr_tit_bib
									String oldestData = findTrTitBibCheckDate(ar[Fields.BID.ordinal()] + " " + ar[Fields.CD_POLO.ordinal()]);
									String srcData = ar[Fields.TS_INS.ordinal()];

//if (ar[Fields.BID.ordinal()].equals("PBE0012567"))
//	{
//	System.out.println("Data + vecchia in tr_tit_bib =" + oldestData); 
//	}
									
									if (oldestData != null && (srcData.compareTo(oldestData) > 0))
//									if (oldestData != null && (srcData.compareTo(oldestData) >= 0))
																				
									{
//if (ar[Fields.BID.ordinal()].equals("PBE0000867"))
//	System.out.println("CREAZIONE che DIVENTA CATTURA per bid con controllo tr_tit_bib" + ar[Fields.BID.ordinal()] + " dataTitoloNormalizzata=" + dataTitoloNormalizzata + ", data in TrTitBib=" + ar[Fields.TS_INS.ordinal()] + ", oldestDate=" + oldestData);
										tipiMaterialeCtr[tpMaterialeCtrEntry][CTR_CATTURA]++;
										crea2LocCtr++;
										crea2LocCtrChecked++;
										
									}
									else
									{
//if (ar[Fields.BID.ordinal()].equals("PBE0000867"))
//	System.out.println("CREAZIONE che RIMANE CREAZIONE dataTitoloElaborato" +   ar[Fields.TS_INS.ordinal()] + " " + ar[Fields.BID.ordinal()]  + " dataTitoloNormalizzata=" + dataTitoloNormalizzata + " oldestDate=" + oldestData); 

										if (oldestData == null)
											keyNotFoundCtr++;
										
										tipiMaterialeCtr[tpMaterialeCtrEntry][CTR_CREA]++;
										creaCtrInc++;
									}
								}
								else
								{
//if (ar[Fields.BID.ordinal()].equals("PBE0000867"))
//	System.out.println("CREAZIONE che RIMANE CREAZIONE con data in ingresso > di data titolo " + ar[Fields.BID.ordinal()] + " " + ar[Fields.TS_INS.ordinal()] + " dataTitoloNormalizzata=" + dataTitoloNormalizzata); 

									tipiMaterialeCtr[tpMaterialeCtrEntry][CTR_CREA]++;
									creaCtrInc++;
								
								}
							}
							else if (ar[Fields.FL_CREA_LOCALIZZA.ordinal()].charAt(0) == 'L')
							{
								tipiMaterialeCtr[tpMaterialeCtrEntry][CTR_CATTURA]++;
							}
							else
								System.out.println("Non capisco CREA/LOCALIZZA per " );
//						}
						
						
					}
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				} // End while 

			
			// Scriviamo l'ultimo record
			for (int i=0; i < TIPI_MATERIALI; i++)
			{
				if (tipiMaterialeCtr[i][CTR_CREA] != 0 || tipiMaterialeCtr[i][CTR_CATTURA] != 0) // scarichiamo solo per contatori valorizzati 
				{
					trTitBibOut.write(oldTsIns 
//							+ "|" + oldBid // SOLO per TEST 
					                     + "|" + oldPolo 
					                     + "|" + oldBiblioteca 
					                     + "|" + tipiMaterialeChar[i] 
					                     + "|" + tipiMaterialeCtr[i][CTR_CREA] 
					                     + "|" + tipiMaterialeCtr[i][CTR_CATTURA] 
					                     + "|CURRENT_TIMESTAMP\n");	
					writtenCtr++;				
				}
			} // End for tipiMateriali
			
			
			System.out.println("Input " + trTitBibFilename);
			System.out.println("Input " + tbTitoloFilename);
			System.out.println("Input " + tbTitoloOffsetFilename);
			System.out.println("Output " + trTitBibFilenameOut);
			System.out.println("Righe lette " + rowCtr);
			System.out.println("Righe scritte " + writtenCtr);
			System.out.println("Nature non valide " + ctrNatureInvalide);
			System.out.println("Ignorati " + skippedCtr);

			System.out.println("Crea trovati " + creaCtr);
			System.out.println("Crea incrementati " + creaCtrInc);

						
			System.out.println("Creazioni che sono diventate localizzazioni " + (crea2LocCtr - crea2LocCtrChecked));
			System.out.println("Creazioni che sono diventate localizzazioni dopo controllo " + crea2LocCtrChecked);
			System.out.println("Creazioni che sono diventate localizzazioni totali " + crea2LocCtr);
			
			System.out.println("" + keyNotFoundCtr + " chiavi non trovate in " + trTitBibFilenameCheck);
			System.out.println("" + bidNonTrovatoInTbTitoloCtr + " bid non trovati in " + tbTitoloOffsetFilename);
			
			
				try {
					trTitBinIn.close();
					tbTitoloIn.close();
				    //trTitoloOffsetIn
					//trTitBibCheckIn
					trTitBibOut.close();
					trTitBibCheckRandomIn.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				try {
					System.out.println("\n\nFine !");
					trTitBinIn.close();
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
	





String findTrTitBibCheckDate(String key)
{
	int keyValueSize = 10+1+3+1+8;  
	
	
	int keyValueLfSize = keyValueSize+1;
	byte[] keyValue = new byte[keyValueSize];
	
	
	
    long first = 0;
    long upto  = bidsInTrTitBibCheck;
    long returnOffset = -1;
    String s, bidPolo, offset;
    int positionTo;
    
    while (first < upto) {
        long mid = (first + upto) / 2;  // Compute mid point.

        positionTo = (int)mid * keyValueLfSize;
        
//        trTitBibCheckIn.position(positionTo);
//        trTitBibCheckIn.get(keyValue, 0, keyValueSize);
        
        
        try {
			trTitBibCheckRandomIn.seek(positionTo);
			trTitBibCheckRandomIn.read(keyValue, 0, keyValueSize);
			
			
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
        
    	s = new String (keyValue); //.toString();
    	bidPolo = s.substring(0, 14);

//System.out.println("Test '" + s + "', positionTo=" + positionTo + ", mid=" + mid);    	
        
        if (key.compareTo(bidPolo) < 0) {
            upto = mid;       // repeat search in bottom half.
        } else if (key.compareTo(bidPolo) > 0) {
            first = mid + 1;  // Repeat search in top half.
        } else {
        	// prendiamo la data
            return s.substring(14+1); // La data minima di bid polo 
        }
    }

    System.out.println("Key '" +  key + "' non trovata in " + trTitBibFilenameCheck);
    return null; 
} // End findTrTitBibCheckDate



long findTitleOffset(String key)
{
	byte[] keyOffsetTitolo = new byte[21];
	
    long first = 0;
    long upto  = bidsInTbTitolo;
    long returnOffset = -1;
    String s, bid, offset;
    int positionTo;
    
    while (first < upto) {
        long mid = (first + upto) / 2;  // Compute mid point.

        positionTo = (int)mid*22;
    	trTitoloOffsetIn.position(positionTo);
    	trTitoloOffsetIn.get(keyOffsetTitolo, 0, 21);			
        
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
    
} // End findTitleOffset




};  // End Statistiche2006
	



