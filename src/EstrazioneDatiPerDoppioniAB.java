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


public class EstrazioneDatiPerDoppioniAB {

    String tbTitoloFilename;
	String filenameOut;

	BufferedReader tbTitoloIn = null;
	BufferedWriter fileOut = null;
	
	char charSepArray[] = { '�' }; // C0 '�' 
	char charSepArray2[] = {  }; // C0
//	char charSepOut = '�'; // '|'
	char charSepOut = 0x01; // '|'
	
	String ar[];
	
	enum Fields {
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
	        System.out.println("Uso: EstrazioneDatiPerDoppioniAB tbTitoloFilenameIN tbTitoloFilenameOut"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="EstrazioneDatiPerDoppioniAB tool - � Almaviva S.p.A 2013"+
		 "\n====================================="+
		 "\nTool di estrazione dati per la gestione dei doppioni (fusioni)";

	    System.out.println(start);

	    EstrazioneDatiPerDoppioniAB estrazioneDatiPerDoppioniAB = new EstrazioneDatiPerDoppioniAB(args);
	    estrazioneDatiPerDoppioniAB.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
		
	} // End main



	EstrazioneDatiPerDoppioniAB (String[] args)
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
		int recordMusicali=0;
		
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
//						if ((rowCtr & 0x1FFF) == 0)
						if ((rowCtr & 0x1FFFF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Scritti " + (rowCtr-droppedCtr-recordMusicali-deletedCtr) + " record");
							System.out.println("Scartati " + droppedCtr + " record");
							System.out.println("Musicali scartati " + recordMusicali + " record");
							System.out.println("Cancellati " + deletedCtr + " record");
							
							
						}
						
//						if (rowCtr > 1000)
//						{
//							break;
//						}
						
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
							System.out.println("Campi in tabella ("+ar.length+") != 42 per bid: " + ar[Fields.bid.ordinal()] + " Riga " + rowCtr);
							droppedCtr++;
							continue;
						}
						
//						// Se record non cancellato
//						if (ar[Fields.fl_canc.ordinal()].length() == 0)
//						{
//							System.out.println("fl_canc vuoto per bid: " + ar[Fields.bid.ordinal()] + " Riga " + rowCtr);
//							droppedCtr++;
//							continue;
//							
//						}
						
						if (ar[Fields.fl_canc.ordinal()].charAt(0) == 'S')
						{
							deletedCtr++;
							continue;
						}
						if (ar[Fields.tp_materiale.ordinal()].charAt(0) == 'U')
						{
							recordMusicali++;
							continue;
						}
						
						// E' una natura valida?
						if (ar[Fields.cd_natura.ordinal()].charAt(0) == 'A' || 
							ar[Fields.cd_natura.ordinal()].charAt(0) == 'B')
						{
//							// E' una data valida?
//							if (ar[Fields.aa_pubb_1.ordinal()].length() == 0 || ar[Fields.aa_pubb_1.ordinal()].charAt(0) == ' ')
//							{
//								droppedCtr++;
//								continue;
//							}
							
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
							
							
							// Scriviamo il record
							fileOut.write(
									ar[Fields.ky_cles1_t.ordinal()] + ar[Fields.ky_cles2_t.ordinal()] + charSepOut // "�" // Cles1 + cles2 
									+ ar[Fields.cd_natura.ordinal()] + charSepOut // Natura
									+ ar[Fields.cd_livello.ordinal()] + charSepOut // "�" // Codice livello di autorita'
									+ ar[Fields.bid.ordinal()] + charSepOut //"�" 	// Bid
									+ ar[Fields.isbd.ordinal()]
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
	System.out.println("Scritti " + (rowCtr-droppedCtr-recordMusicali-deletedCtr) + " record");
	System.out.println("Scartati " + droppedCtr + " record");
	System.out.println("Musicali scartati " + recordMusicali + " record");
	System.out.println("Cancellati " + deletedCtr + " record");
	System.out.println("Fine ");
	
	
	
	} // End run
		
} // End EstrazioneDatiPerDoppioniAB