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



public class EstrazioneDatiPerDoppioniTum {

    String tbTitoloFilename;
	String filenameOut;

	BufferedReader tbTitoloIn = null;
	BufferedWriter fileOut = null;
	
//	char charSepArray[] = { '�' }; // C0 '�' 
//	char charSepArray2[] = {  }; // C0

	char charSepOut = 0x01; //'�';
	char charSepArray[] = { '�' }; //  C0 '�' 

//	String sepArray[] = { "&$%" };  

	
	String ar[];
	
	
	enum fieldsTbTitolo {
		bid,
		isadn,
		tp_materiale,
		tp_record_uni,
		cd_natura,
		cd_paese,
		cd_lingua_1,
		cd_lingua_2,
		cd_lingua_3,
		aa_pubb_1,
		aa_pubb_2,
		tp_aa_pubb,
		cd_genere_1,
		cd_genere_2,
		cd_genere_3,
		cd_genere_4,
		ky_cles1_t,
		ky_cles2_t,
		ky_clet1_t,
		ky_clet2_t,
		ky_cles1_ct,
		ky_cles2_ct,
		ky_clet1_ct,
		ky_clet2_ct,
		cd_livello,
		fl_speciale,
		isbd,
		indice_isbd,
		ky_editore,
		cd_agenzia,
		cd_norme_cat,
		nota_inf_tit,
		nota_cat_tit,
		bid_link,
		tp_link,
		ute_ins,
		ts_ins,
		ute_var,
		ts_var,
		ute_forza_ins,
		ute_forza_var,
		fl_canc,
		};
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 

		if(args.length < 2)
	    {
	        System.out.println("Uso: EstrazioneDatiPerDoppioniTum tbTitoloIN tbTumOut"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="EstrazioneDatiPerDoppioniMusica tool - � Almaviva S.p.A 2013"+
		 "\n======================================================================"+
		 "\nTool di estrazione dati per la gestione dei doppioni (fusioni)";

	    System.out.println(start);

	    EstrazioneDatiPerDoppioniTum estrazioneDatiPerDoppioniTum = new EstrazioneDatiPerDoppioniTum(args);
	    estrazioneDatiPerDoppioniTum.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
		
	} // End main



	EstrazioneDatiPerDoppioniTum (String[] args)
	{
	    tbTitoloFilename = args[0];
	    filenameOut = args[1];
	} // End EstrazioneDatiPerDoppioni

	void run()
	{
		
	    
		String s, t;
		//ConfigTable configTable=null;
		int rowCtr = 0;
		int droppedCtr = 0;
		int deletedCtr=0;
		
//		
		System.out.println("charSepArray='"+charSepArray[0]+"'");
		
		try {
			tbTitoloIn = new BufferedReader(new FileReader(tbTitoloFilename));
			fileOut = new BufferedWriter(new FileWriter(filenameOut));
			
			while (true) {

			
				try {
					s = tbTitoloIn.readLine();
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
						
//						ar = MiscString.estraiCampi(s, sepArray, MiscStringTokenizer.RETURN_DELIMITERS_AS_TOKEN_FALSE); //  " "
						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						//System.out.println(ar[Fields.bid.ordinal()]);
						   
						
						
//if (rowCtr == 20000)	// for debugging
//	break;
						
						
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;
						
						if (ar.length != 42)
						{
							System.out.println("Campi estratti ("+ar.length+") != 42 per bid: " + ar[fieldsTbTitolo.bid.ordinal()] + " Riga " + rowCtr);
							droppedCtr++;
							continue;
						}
						
//						// Se record non cancellato
//						if (ar[fieldsTbTitolo.fl_canc.ordinal()].length() == 0)
//						{
//							System.out.println("fl_canc vuoto per bid: " + ar[fieldsTbTitolo.bid.ordinal()] + " Riga " + rowCtr);
//							droppedCtr++;
//							continue;
//							
//						}
						
						if (ar[fieldsTbTitolo.fl_canc.ordinal()].charAt(0) == 'S')
						{
							deletedCtr++;
							continue;
						}
						
						// E' una natura valida?
						if (ar[fieldsTbTitolo.cd_natura.ordinal()].charAt(0) == 'A'	// Titolo uniforme
							&& ar[fieldsTbTitolo.tp_materiale.ordinal()].charAt(0) == 'U') // Musica
						{
							
							// Scriviamo il record
							fileOut.write(
									ar[fieldsTbTitolo.ky_cles1_t.ordinal()] + ar[fieldsTbTitolo.ky_cles2_t.ordinal()] + charSepOut
									+ ar[fieldsTbTitolo.bid.ordinal()] + charSepOut
									+ ar[fieldsTbTitolo.cd_livello.ordinal()]  + charSepOut 
									+ ar[fieldsTbTitolo.isbd.ordinal()]  + charSepOut
									+ "\n");// );
						}
						else
						{
							droppedCtr++;
						}
						
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
