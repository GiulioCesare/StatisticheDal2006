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


public class LegamiM09aTitoloLegato {
	public static final int MAX_BYTES_PER_UTF8_CHARACTER = 4;

	String filenameIn;
	String filenameOut;
    
	int legamiTitoloCollegatoNonTrovatiCtr =0;
	int legamiTitoloNonTrovatiCtr =0;

    
	BufferedReader fileIn = null;
	BufferedWriter fileOut = null;

	long fileOffetLength;

	String tbTitoloFilename;
    String tbTitoloOffsetFilename;
	RandomAccessFile  	tbTitoloIn = null;
    MappedByteBuffer 	tbTitoloOffsetIn = null;
	long bidsInTbTitolo;
	
//	String trTitTitRelFilename;
//    String trTitTitRelOffsetFilename;
//	RandomAccessFile  	trTitTitRelIn = null;
//    MappedByteBuffer 	trTitTitRelOffsetIn = null;
//	long bidsInTrTitTitRel;
	
	
	
	
//	String  sepOutC0 = "�"; // C0 - <EF><BF><BD>
	String  sepOut01 = ""; // 0x01
	
	
	
	char charSepArrayC0[] = { 'À'}; // 0xC0
	char charSepArray01[] = { ''}; // 0x01
	char charSepArrayPipe[] = { '|'}; 
	char charSepArrayComma[] = { ','}; 

	String ar[];
	String arTbTitolo[];

	enum FieldsTitolo {
		bidColl,
		vid,
		bid,
		cles1_2,
		titolo
	};	

	enum FieldsTbTitolo {
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
		// TODO Auto-generated method stub

		
		if(args.length < 4)
	    {
	        System.out.println("Uso: LegamiM09aTitoloLegato file_in file_out indice_tb_titolo record_tb_titolo"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="LegamiM09aTitoloLegato tool - � Almaviva S.p.A 20/07/2016"+
		 "\n============================================="+
		 "\nTool di preparazione file per legami M09 (aggiunta di cless 1 e 2 e titolo del titolo legato)";

	    System.out.println(start);

	    LegamiM09aTitoloLegato legamiM09aTitoloLegato = new LegamiM09aTitoloLegato(args);
	    legamiM09aTitoloLegato.run();
	    

//	    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
	    System.exit(0);
		
	}

LegamiM09aTitoloLegato (String[] args)
	{
	    filenameIn = args[0];
	    filenameOut = args[1];
	    tbTitoloOffsetFilename = args[2];
	    tbTitoloFilename = args[3];
	} // LegamiM09aTitoloLegato


	void run()
	{
	    
		String s, t;
		//ConfigTable configTable=null;
		int rowCtr = 0;
		int noLegameResp1_2Ctr = 0;
		int writtenCtr = 0;
		int titoliNonCollegatiCtr=0;
		int titoliCollegatiCtr=0;
		int clesUnivocaCtr = 0;
//		int deletedCtr=0;
		
		
		try {
			
//			bidListIn = new BufferedReader(new FileReader(bidListFilename));
			fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(filenameIn), "UTF8"));
			fileOut = new BufferedWriter(new FileWriter(filenameOut));
			 
			
			// Portiamo l'indice dei titoli in memoria
			fileOffetLength = new File(tbTitoloOffsetFilename).length();
			bidsInTbTitolo = fileOffetLength/22; 
			tbTitoloOffsetIn = new FileInputStream(tbTitoloOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			tbTitoloIn = new RandomAccessFile(tbTitoloFilename, "r");
			
			
			
			String lastRecord = "";

			int lastClesCtr=0;
			String lastCles = "";
			
			while (true) {
				try {
					s = fileIn.readLine();
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
						if ((rowCtr & 0xFF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Legami a titolo A non trovati: " + titoliNonCollegatiCtr);
							System.out.println("Legami a titolo A trovati: " + titoliCollegatiCtr);
							System.out.println("Titoli scritti " + writtenCtr + " record");
						}
						
						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;

						
						
						ar = MiscString.estraiCampi(s, charSepArray01, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						if (ar.length != 5)
						{
							System.out.println("Numero campi errato " + ar.length + " invece di 5. Riga " + rowCtr + " Record: " + s);
							continue;
						}
						
						String bidColl = ar[FieldsTitolo.bidColl.ordinal()]; 
//System.out.println("bidColl='"+bidColl+"'");
						if(bidColl.equals("          ")) // Abbiamo un valido bid collegato?
						{
							fileOut.write(s+"||\n");
							writtenCtr++;
							titoliNonCollegatiCtr++;
							continue; 
						}
						
						// Cerchiamo il titolo collegato 
						long offset = findOffsetTitolo(bidColl);
						
						if (offset == -1)
						{
							System.out.println("Titolo collegato non trovato per Bid: '" + bidColl + "'");
							legamiTitoloCollegatoNonTrovatiCtr++;
							continue;
						}
						
						tbTitoloIn.seek(offset);
						t = tbTitoloIn.readLine(); // Leggi il titolo
//System.out.println(t);

						// Scompattiamo i campi dei legami
						arTbTitolo = MiscString.estraiCampi(t, charSepArrayC0, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE);
						String cles1 = arTbTitolo[FieldsTbTitolo.ky_cles1_t.ordinal()];
						String cles2 = arTbTitolo[FieldsTbTitolo.ky_cles2_t.ordinal()];

						String isbd = arTbTitolo[FieldsTbTitolo.isbd.ordinal()];

						int idx = isbd.indexOf(". - ");	// separatore di area
						if (idx != -1)
							isbd = isbd.substring(0, idx);

						if (isbd.endsWith(". -"))	// separatore di area balordo
							isbd = isbd.substring(0, isbd.length()-3);

						
						idx = isbd.indexOf(" / "); // separatore di editore o autore
						if (idx != -1)
							isbd = isbd.substring(0, idx);
						
						idx = isbd.indexOf(" : "); // separatore del complemento del titolo
						if (idx != -1)
							isbd = isbd.substring(0, idx);
						
						fileOut.write(s+"|"+cles1+cles2+"|"+isbd+"\n");

						titoliCollegatiCtr++;
						writtenCtr++;
						}
					
						
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			} // End while
			
			fileIn.close();
			tbTitoloIn.close();
			
//			trTitTitRelIn.close();	
			fileOut.close();
		
		} catch (FileNotFoundException e) {
		e.printStackTrace();
	} 
	catch (IOException e1) {
		e1.printStackTrace();
	}
	
	System.out.println("\nLetti " + rowCtr + " record");
	System.out.println("Legami a titolo A non trovati: " + titoliNonCollegatiCtr);
	System.out.println("Legami a titolo A trovati: " + titoliCollegatiCtr);
	System.out.println("Titoli scritti " + writtenCtr + " record");
	
	System.out.println("Fine ");
	
	
	} // End run


	long findOffsetTitolo(String key)
	{
		
		return findOffset21(key, tbTitoloOffsetIn, bidsInTbTitolo);
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
	
	
	
	
} // End TitoloLegato
