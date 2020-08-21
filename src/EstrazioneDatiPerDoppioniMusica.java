/*
 * Step non necessario in quanto dati prelevato da db gia' filtrati per fl_can != 'S'
 */

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
import java.nio.channels.FileChannel;



public class EstrazioneDatiPerDoppioniMusica {

    String tbComposizioneFilename;
	String filenameOut;

	BufferedReader tbComposizioneIn = null;
	BufferedWriter fileOut = null;
	
//	char charSepArray[] = { '�' }; // C0 '�' 
//	char charSepArray2[] = {  }; // C0

	char charSepOut = 0x01; //'�';
	
	String sepArray[] = { "&$%" };  
	
	
	String ar[];
	
	
	enum fieldTbComposizione {
		bid,
		cd_forma_1,
		cd_forma_2,
		cd_forma_3,
		numero_ordine,
		numero_opera,
		numero_cat_tem,
		cd_tonalita,
		datazione,
		aa_comp_1,
		aa_comp_2,
		ds_sezioni,
		ky_ord_ric,
		ky_est_ric,
		ky_app_ric,
		ky_ord_clet,
		ky_est_clet,
		ky_app_clet,
		ky_ord_pre,
		ky_est_pre,
		ky_app_pre,
		ky_ord_den,
		ky_est_den,
		ky_app_den,
		ky_ord_nor_pre,
		ky_est_nor_pre,
		ky_app_nor_pre,
		ute_ins,
		ts_ins,
		ute_var,
		ts_var,
		fl_canc
		};
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 

		if(args.length < 2)
	    {
	        System.out.println("Uso: EstrazioneDatiPerDoppioniMusica tbComposizioneFilenameIN tbComposizioneFilenameOut"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="EstrazioneDatiPerDoppioniMusica tool - � Almaviva S.p.A 2013"+
		 "\n======================================================================"+
		 "\nTool di estrazione dati per la gestione dei doppioni (fusioni)";

	    System.out.println(start);

	    EstrazioneDatiPerDoppioniMusica estrazioneDatiPerDoppioniMusica = new EstrazioneDatiPerDoppioniMusica(args);
	    estrazioneDatiPerDoppioniMusica.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
		
	} // End main



	EstrazioneDatiPerDoppioniMusica (String[] args)
	{
	    tbComposizioneFilename = args[0];
	    filenameOut = args[1];
	} // End EstrazioneDatiPerDoppioni

	void run()
	{
		
	    
		String s, t;
		//ConfigTable configTable=null;
		int rowCtr = 0;
		int droppedCtr = 0;
		int deletedCtr=0;
		
		try {
			tbComposizioneIn = new BufferedReader(new FileReader(tbComposizioneFilename));
			fileOut = new BufferedWriter(new FileWriter(filenameOut));
			
			while (true) {

			
				try {
					s = tbComposizioneIn.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						
//						if ((rowCtr & 0x3ff) == 0x3ff)
//							System.out.println("rowCtr " + rowCtr);
						
						rowCtr++;
						if ((rowCtr & 0x1FFF) == 0)
//						if ((rowCtr & 0xFF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Scritti " + (rowCtr-droppedCtr) + " record");
//							System.out.println("Scartati " + droppedCtr + " record");
//							System.out.println("Cancellati " + deletedCtr + " record");
							
							
						}
						
//						if (rowCtr > 1000)
//						{
//							break;
//						}
						
						ar = MiscString.estraiCampi(s, sepArray, MiscStringTokenizer.RETURN_DELIMITERS_AS_TOKEN_FALSE); //  " "
						//System.out.println(ar[Fields.bid.ordinal()]);
						   
						
						
//if (rowCtr == 20000)	// for debugging
//	break;
						
						
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;
						
						if (ar.length != 32)
						{
							System.out.println("Campi in tabella ("+ar.length+") != 32 per bid: " + ar[fieldTbComposizione.bid.ordinal()] + " Riga " + rowCtr);
							droppedCtr++;
							continue;
						}
						
						// Se record non cancellato
						if (ar[fieldTbComposizione.fl_canc.ordinal()].length() == 0)
						{
							System.out.println("fl_canc vuoto per bid: " + ar[fieldTbComposizione.bid.ordinal()] + " Riga " + rowCtr);
							droppedCtr++;
							continue;
							
						}
						
						if (ar[fieldTbComposizione.fl_canc.ordinal()].charAt(0) == 'S')
						{
							deletedCtr++;
							continue;
						}
						
						// E' una natura valida?
//						if (ar[fieldTbComposizione.cd_natura.ordinal()].charAt(0) == 'M')
//						{
//							// E' una data valida?
//							if (ar[Fields.aa_pubb_1.ordinal()].length() == 0 || ar[Fields.aa_pubb_1.ordinal()].charAt(0) == ' ')
//							{
//								droppedCtr++;
//								continue;
//							}
//							
//							// Troviamo il numero di pagine
//							int indexPagine =  ar[Fields.isbd.ordinal()].indexOf(" p. ;"); //  ;
//							if (indexPagine == -1)
//							{
//								droppedCtr++;
//								continue;
//							}
//							String pagine = ar[Fields.isbd.ordinal()].substring(indexPagine-4, indexPagine);
//							// Troviamo l'edizione
//							String edizione="    ";
//							int indexEdizione =  ar[Fields.indice_isbd.ordinal()].indexOf("205-");
//							if (indexEdizione != -1)
//							{
////								System.out.println(ar[Fields.isbd.ordinal()]);
//								String offset = ar[Fields.indice_isbd.ordinal()].substring(indexEdizione+4, indexEdizione+4+4);
//								indexEdizione = Integer.parseInt(offset);
//								edizione = ar[Fields.isbd.ordinal()].substring(indexEdizione-1, indexEdizione+3);
//							}
//							
							
							// Scriviamo il record
							fileOut.write(
									ar[fieldTbComposizione.ky_ord_nor_pre.ordinal()] + ar[fieldTbComposizione.ky_est_nor_pre.ordinal()] + charSepOut  
									+ar[fieldTbComposizione.bid.ordinal()] + charSepOut //"�" 	// Bid
									+ar[fieldTbComposizione.cd_forma_1.ordinal()] + charSepOut
									+ar[fieldTbComposizione.numero_ordine.ordinal()] + charSepOut
									+ar[fieldTbComposizione.numero_opera.ordinal()] + charSepOut
									+ar[fieldTbComposizione.numero_cat_tem.ordinal()] + charSepOut
									+ar[fieldTbComposizione.cd_tonalita.ordinal()] //  + charSepOut
									
									+"\n"
									);// );
							
//						}
//						else
//						{
//							droppedCtr++;
//						}
						
//						fileOut.write(s);
//						fileOut.write("\n");
						
					}
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}
				
				
			} // End while
	} catch (FileNotFoundException e) {
		// 
		e.printStackTrace();
	} 
	catch (IOException e1) {
		// 
		e1.printStackTrace();
	}
	
	try {
		fileOut.close();
	} catch (IOException e) {
		// 
		e.printStackTrace();
	}
	
	System.out.println("\nLetti " + rowCtr + " record");
	System.out.println("Scritti " + (rowCtr-droppedCtr) + " record");
//	System.out.println("Scartati " + droppedCtr + " record");
//	System.out.println("Cancellati " + deletedCtr + " record");
	System.out.println("Fine ");
	
	
	
	} // End run
		
} // End EstrazioneDatiPerDoppioni
