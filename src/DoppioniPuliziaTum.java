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
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;


class DoppioniPuliziaTum
{
//	boolean filtraEditore = true; 

	
	class GruppoDuplicati{
		ArrayList<String> editori = new ArrayList<String>(); 
		ArrayList<String> records = new ArrayList<String>(); 
	}
	
	ArrayList<GruppoDuplicati> gruppoDuplicatiAL = new ArrayList<GruppoDuplicati>();
	
	
	String ar[];
	String titoloAr[];
	BufferedReader in = null;
    BufferedWriter out = null;

//    ArrayList<String> doppioniAL = new ArrayList<String>(); // Elenco doppioni escluso test editore
    ArrayList<String[]> doppioniAL = new ArrayList<String []>(); // Elenco doppioni escluso test editore

    int	id_gruppo = 0;
    
    
	String sortedStopWords[] = {
			"BY",
			"CASA",
			"CO",
			"COMPANY",
			"COOP",
			"DISTRIBUTED",
			"DISTRIBUTORE",
			"ED",
			"EDITEUR",
			"EDITIONS",
			"EDITOR",
			"EDITORE",
			"EDITRICE",
			"EDIZIONI",
			"HERAUSGEBER",
			"IL",
			"PUBLISHED",
			"PUBLISHER",
			"PUBLISHING",
			"SOC",
			"SOCIETA",
			"TIP",
			"TIPOGRAFIA",
			"VERLAG",
			"VERLAG HAUS",
			"VERLEGER"			
			
	};
    
       
	RandomAccessFile  	trTitBibIn = null;	
    MappedByteBuffer 	trTitBibOffsetIn = null;	
	long bidsInTrTitBib=0;
    
//	RandomAccessFile  	tbTitoloIn = null;	
//    MappedByteBuffer 	tbTitoloOffsetIn = null;	
    
	long fileOffetLength;
	long bidsInTbTitolo=0;
	long bidNonTrovatiCtr=0;
    
    
    
    
//  String lastRecord = "                                                  �          �    �    �    "; // Non comprende l'editore
    String lastRecord = "                                                  |          |    |    |    "; // Non comprende l'editore
//	String lastEditore = ""; 
	

	int rowCtr = 0;
	int droppedCtr=0;
	int writtenCtr=0;
	int lastRecordCtr=1;
	
	
	
	enum Fields {
		cles1_2, 
		vid, 
		bid, 
		cd_livello,
		isbd
//		cd_forma_1, 
//		numero_ordine,
//		numero_opera,
//		numero_cat_tem,
//		cd_tonalita,
//		// Da musica
//		organico_sintetico,
//		organico_analitico
		
	};	

//	enum tbTitoloFields {
//		bid,
//		isadn,
//		tp_materiale,
//		tp_record_uni,
//		cd_natura,
//		cd_paese,
//		cd_lingua_1,
//		cd_lingua_2,
//		cd_lingua_3,
//		aa_pubb_1,
//		aa_pubb_2,
//		tp_aa_pubb,
//		cd_genere_1,
//		cd_genere_2,
//		cd_genere_3,
//		cd_genere_4,
//		ky_cles1_t,
//		ky_cles2_t,
//		ky_clet1_t,
//		ky_clet2_t,
//		ky_cles1_ct,
//		ky_cles2_ct,
//		ky_clet1_ct,
//		ky_clet2_ct,
//		cd_livello,
//		fl_speciale,
//		isbd,
//		indice_isbd,
//		ky_editore,
//		cd_agenzia,
//		cd_norme_cat,
//		nota_inf_tit,
//		nota_cat_tit,
//		bid_link,
//		tp_link,
//		ute_ins,
//		ts_ins,
//		ute_var,
//		ts_var,
//		ute_forza_ins,
//		ute_forza_var,
//		fl_canc,
//};
	
	
	public static void main(String[] args) {
		// 

		char charSepArrayEquals[] = { '='};
		char charSepArraySpace[] = { ' '};

		if(args.length < 4)
	    {
	        System.out.println("Uso: DoppioniPuliziaMusica filenameIn filenameOut nonefileTrTitBib nomefileTrTitBibOffset"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="DoppioniPuliziaTum tool - (C) Almaviva S.p.A 2013"+
		 "\n=========================================================="+
		 "\nTool di pulizia per doppioni Tum"
		 ;

	    System.out.println(start);
		
	    DoppioniPuliziaTum doppioniPuliziaTum = new DoppioniPuliziaTum(args);
	    doppioniPuliziaTum.run();
	    
	    System.exit(0);
	}// End main		
		

	public


	String filenameIn;
	String filenameOut;

//	String tbTitoloFilename;
//	String tbTitoloOffsetFilename;
	
	String trTitBibFilename;
	String trTitBibOffsetFilename;
    
	  
	//char tbTitoloCharSepArray[] = { '�' }; // C0 '�' 
	//char tbTitoloCharSepArray[] = { 'À' }; // C0 '�'
	char tbTitoloCharSepArray[] = { 0xC0 }; 
	
	//char charSepArray[] = { '|' };   
	char charSepArray[] = { 0x01 }; // '�'   
	
//	String  sepOut = "�"; // "|"
	char  sepOut = 0x01; 
	
	String oldBid="";
	String oldPolo="";

	
	
    BufferedWriter OutLog;
		
    
    
    DoppioniPuliziaTum (String[] args)
	{
	    filenameIn = args[0];
	    filenameOut = args[1];

	    
//	    tbTitoloFilename = args[2];
//	    tbTitoloOffsetFilename = args[3];
	    
	    trTitBibFilename = args[2];
	    trTitBibOffsetFilename = args[3];

		fileOffetLength = new File(trTitBibOffsetFilename).length();
		bidsInTrTitBib = fileOffetLength/22; 
	    
	    System.out.println("filenameIn " + filenameIn);
	    System.out.println("filenameOut " + filenameOut);
	    
//	    System.out.println("trTitoloFilename " + tbTitoloFilename);
//	    System.out.println("trTitoloOffsetFilename " + tbTitoloOffsetFilename);

	    System.out.println("trTitBibFilename " + trTitBibFilename);
	    System.out.println("trTitBibOffsetFilename " + trTitBibOffsetFilename);
	    
	}
	
	void run()
	{
		String s="";
		//ConfigTable configTable=null;
		
		int state = 0;
		
		
		try {
			trTitBibIn = new RandomAccessFile(trTitBibFilename, "r");
			trTitBibOffsetIn = new FileInputStream(trTitBibOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			
			
			in = new BufferedReader(new FileReader(filenameIn));
			out = new BufferedWriter(new FileWriter(filenameOut));
			
			fileOffetLength = new File(trTitBibOffsetFilename).length();
			bidsInTbTitolo = fileOffetLength/22; 
			
//			tbTitoloIn = new RandomAccessFile(tbTitoloFilename, "r");
//			tbTitoloOffsetIn = new FileInputStream(tbTitoloOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			
			String lastRecordAr[] = {"", "", "", "", "", "", "", "", "", ""};
			// lastRecordAr = MiscString.estraiCampi("'�''�''�''�''�''�''�'", charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
			
			while (true) {
				try {
					s = in.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						rowCtr++;
//						if ((rowCtr & 0x1FFF) == 0)
						if ((rowCtr & 0xFFF) == 0)
//						if ((rowCtr & 0xFF) == 0)
//						if ((rowCtr & 0xF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Scartati  " + droppedCtr + " record");
							System.out.println("Scritti " + writtenCtr + " record");
						}
						
//if (rowCtr == 2000)
//	break;
//
//if (s.startsWith("1 ARTI"))
//		System.out.println("rowCtr=" + rowCtr);

						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;
					
//						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_FALSE); //  " "
						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
//for (int i=0; i< ar.length; i++)
//	System.out.println(i + " '" + ar[i] + "'");
						
						// Stessa chiave (cels+vid)
						// "1 2 CORINTHIANS                                   �BVEV054400�"
						
						
//						String key = ar[1]+ar[0]; //s.substring(0,62);
//						String keyLast = lastRecordAr[1] + lastRecordAr[0]; //lastRecord.substring(0,62);
//						if (keyLast.equals(key)

//						if (ar[Fields.cd_tonalita.ordinal()].length() > 2)
//						{
//							System.out.println(ar[Fields.bid.ordinal()] + " cd_tonalita' max 2 chars. Length="+ar[Fields.cd_tonalita.ordinal()].length());
//							System.out.println(s);
////							droppedCtr++; 
////							continue;
//						}
						
						
						if (
							   ar[Fields.cles1_2.ordinal()].equals(lastRecordAr[Fields.cles1_2.ordinal()]) 
							&& ar[Fields.vid.ordinal()].equals(lastRecordAr[Fields.vid.ordinal()]) 		
/*
							&& ar[Fields.cd_forma_1.ordinal()].equals(lastRecordAr[Fields.cd_forma_1.ordinal()]) 
							&& ar[Fields.numero_ordine.ordinal()].equals(lastRecordAr[Fields.numero_ordine.ordinal()]) 
							&& ar[Fields.numero_opera.ordinal()].equals(lastRecordAr[Fields.numero_opera.ordinal()]) 
							&& ar[Fields.numero_cat_tem.ordinal()].equals(lastRecordAr[Fields.numero_cat_tem.ordinal()]) 
							&& ar[Fields.cd_tonalita.ordinal()].equals(lastRecordAr[Fields.cd_tonalita.ordinal()]) 
							&& ar[Fields.organico_sintetico.ordinal()].equals(lastRecordAr[Fields.organico_sintetico.ordinal()]) 
							&& ar[Fields.organico_analitico.ordinal()].equals(lastRecordAr[Fields.organico_analitico.ordinal()])
*/
							) 
						{
							doppioniAL.add(lastRecordAr);
							//editoriAL.add(lastEditore);
							lastRecordCtr++;
						
						}
						else
						{	// Cambio chiave
							// Ultimo record era singolo o multiplo?
							if (lastRecordCtr == 1)
							{
								droppedCtr++; // Singolo, droppato
//System.out.println("Chiave univoca" + lastCles);
							}
							else
							{
								
//								out.write(lastRecord + "\n"); // Scriviamo record precedente in quanto multiplo
//								writtenCtr++;
								
//								writeRecord();

								doppioniAL.add(lastRecordAr);
								//editoriAL.add(lastEditore);
								
								writeDoppioni();
								
								
								doppioniAL.clear();
								//editoriAL.clear();
								
							}
							lastRecordCtr=1;
						}						
						//lastRecord = s;
						lastRecordAr=ar;
						
						
					}
				} catch (Exception e) { // IOException
					// 
					System.out.println ("Errore a record  " + rowCtr + ": "+ s);
					e.printStackTrace();
					
				}
				
				
//				if (rowCtr == 100)	// for debugging
//					break;
				
				
				} // End while 
			
			
				try {
					in.close();
					out.close();
//					tbTitoloIn.close();	
					trTitBibIn.close();
					
					
					
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				try {
					System.out.println("\n\nInput " + filenameIn);
					System.out.println("Output " + filenameOut);
					System.out.println("Letti " + rowCtr + " record");
					System.out.println("Scartati  " + droppedCtr + " record");
					System.out.println("Scritti " + writtenCtr + " record");
					System.out.println("Gruppi " + id_gruppo);
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
	


	

	protected boolean isEquals(String a, String b) {
		if (a == null && b == null)
			return true;
		else if (a == null || b == null) {
			return false;
		} else {
			return a.equals(b);
		}
	}
	protected HashSet stringTokenizer(String input, char separatore) {
		StringTokenizer st = new StringTokenizer(input, "" + separatore);
		HashSet v = new HashSet();
		while (st.hasMoreElements()) {
			v.add(st.nextToken());
		}
		return v;
	}

	protected Vector stringTokenizerVector(String input, char separatore) {
		StringTokenizer st = new StringTokenizer(input, "" + separatore);
		Vector v = new Vector();
		while (st.hasMoreElements()) {
			v.add(st.nextToken());
		}
		return v;
	}
	

//	long findOffset(String key)
//	{
//		byte[] keyOffsetTitolo = new byte[21];
//		
//	    long first = 0;
//	    long upto  = bidsInTbTitolo;
//	    long returnOffset = -1;
//	    String s, bid, offset;
//	    int positionTo;
//	    
//	    while (first < upto) {
//	        long mid = (first + upto) / 2;  // Compute mid point.
//
//	        positionTo = (int)mid*22;
//	    	tbTitoloOffsetIn.position(positionTo);
//	    	tbTitoloOffsetIn.get(keyOffsetTitolo, 0, 21);			
//	        
//	    	s = new String (keyOffsetTitolo); //.toString();
//
//	//System.out.println("Test " + s + ", positionTo=" + positionTo + ", mid=" + mid);    	
//	    	
//	    	bid = s.substring(0, 10);
//	        
//	        if (key.compareTo(bid) < 0) {
//	            upto = mid;       // repeat search in bottom half.
//	        } else if (key.compareTo(bid) > 0) {
//	            first = mid + 1;  // Repeat search in top half.
//	        } else {
//	        	// Convertiamo da stringa in numero
//	        	offset = s.substring(10);
//	        	returnOffset =  Long.parseLong(offset);
//	            return returnOffset; //mid;       // Found it. return position
//	        }
//	    }
//	    return returnOffset; //-(first + 1);      // Failed to find key
//	    
//	} // End findOffset
	
	
	






	/** Binary search of sorted array.  Negative value on search failure.
	 *  The upperbound index is not included in the search.
	 *  This is to be consistent with the way Java in general expresses ranges.
	 *  The performance is O(log N).
	 *  @param sorted Array of sorted values to be searched.
	 *  @param first Index of first element to serach, sorted[first].
	 *  @param upto  Index of last element to search, sorted[upto-1].
	 *  @param key   Value that is being looked for.
	 *  @return      Returns index of the first match, or or -insertion_position
	 *               -1 if key is not in the array. This value can easily be
	 *               transformed into the position to insert it.
	 */	
	public int findStopword(String key) {
	    int first = 0;
	    int upto  = sortedStopWords.length;
	    
	    while (first < upto) {
	        int mid = (first + upto) / 2;  // Compute mid point.
	        if (key.compareTo(sortedStopWords[mid]) < 0) {
	            upto = mid;       // repeat search in bottom half.
	        } else if (key.compareTo(sortedStopWords[mid]) > 0) {
	            first = mid + 1;  // Repeat search in top half.
	        } else {
	            return mid;       // Found it. return position
	        }
	    }
	    return -(first + 1);      // Failed to find key
	}
	
	
	
	
	
//int cercaInSottogruppi(String editore)
//{
//	for (int i=0; i < gruppoDuplicatiAL.size(); i++)
//	{
//
//		GruppoDuplicati gruppoDupliucati = (GruppoDuplicati)gruppoDuplicatiAL.get(i);
//		
//		for (int j=0; j < gruppoDupliucati.editori.size(); j++)
//		{
//			if (isEditoreUguale(gruppoDupliucati.editori.get(j), editore))
//			{
//				return i; // Ritorna il gruppo
//			}
//		}
//		
//	}
//
//	return -1; // Editore in nessun gruppo
//}
	

//void scriviGruppiDuplicati()
//{
//	for (int i=0; i < gruppoDuplicatiAL.size(); i++)
//	{
//		GruppoDuplicati gruppoDupliucati = (GruppoDuplicati)gruppoDuplicatiAL.get(i);
//		id_gruppo++;
//		for (int j=0; j < gruppoDupliucati.editori.size(); j++)
//		{
//			writeRecord(gruppoDupliucati.records.get(j));			
//		}
//		
//	}
//	
//	gruppoDuplicatiAL.clear();
//	
//}

//boolean isEditoreUguale(String prevEditore, String curEditore)
//{
//
//    // ARGE Normalizza lettere accentate 17/11/2011
//	String prevEditoreNoAccent =  removeAccents(prevEditore);
//	String curEditoreNoAccent =  removeAccents(curEditore);
//	// END ARGE remove accents
//	
//	// Tutto upper case per i test
//    String upperPrevEditore = prevEditoreNoAccent.toUpperCase().replace(',', ' ');
//    String upperCurEditore = curEditoreNoAccent.toUpperCase().replace(',', ' ');
//	
//	
//	
//    //Tolgo tutti i token di tipo x. (lettera punto spazio)
//    StringTokenizer st = new StringTokenizer(upperPrevEditore, " ");
//    String token;
//    int n;
//    while (st.hasMoreTokens()) {
//        token = st.nextToken();
//        if (token.length() == 2 && token.charAt(1) == '.') {
//            n = upperPrevEditore.indexOf(token);
//            upperPrevEditore = upperPrevEditore.substring(0, n) + upperPrevEditore.substring(n + 2);
//        }
//    }
//    st = new StringTokenizer(upperCurEditore, " ");
//    while (st.hasMoreTokens()) {
//        token = st.nextToken();
//        if (token.length() == 2 && token.charAt(1) == '.') {
//            n = upperCurEditore.indexOf(token);
//            upperCurEditore = upperCurEditore.substring(0, n) + upperCurEditore.substring(n + 2);
//        }
//    }
//
//    
//    
//	//MANTIS 2236: Il protocollo deve tener conto dei casi in cui il nome editore è seguito dal "."
//	// Es.: "Donzelli." deve essere considerato "Donzelli"
//    upperPrevEditore = upperPrevEditore.replace('.',' ');
//    upperCurEditore = upperCurEditore.replace('.',' ');
//
//    
//    // Si eliminano i token nelle stoplist
//    st = new StringTokenizer(upperPrevEditore);
////    upperPrevEditore = null;
//    upperPrevEditore = "";
//    n = 0;
//    while (st.hasMoreTokens() && n < 4) {
//        token = st.nextToken();
//        if ( findStopword(token) < 0) {
//            if (n == 0)
//            	upperPrevEditore = token;
//            else
//            	upperPrevEditore = upperPrevEditore + " " + token;
//            n++;
//        }
//    }
//    
//    st = new StringTokenizer(upperCurEditore);
////    upperCurEditore = null;
//    upperCurEditore = "";
//    n = 0;
//    while (st.hasMoreTokens() && n < 4) {
//        token = st.nextToken();
//        if (findStopword(token) < 0) {
//            if (n == 0)
//            	upperCurEditore = token;
//            else
//            	upperCurEditore = upperCurEditore + " " + token;
//            n++;
//        }
//    }
//    
//    
//    if (upperPrevEditore.equals("") && upperCurEditore.equals(""))
//        return true;
//    
//    if 	(
//    		( upperPrevEditore.contains("S.N") || upperPrevEditore.contains("S.T") || upperPrevEditore.contains("S.E") || upperPrevEditore.equals(""))
//            && 
//            (upperCurEditore.contains("S.N") || upperCurEditore.contains("S.T") || upperCurEditore.contains("S.E") || upperCurEditore.equals(""))
//       	) 
//    	return true;        
//    
//  
//    HashSet l_import = stringTokenizer(upperPrevEditore, ' ');
//
//    HashSet l_indice = stringTokenizer(upperCurEditore, ' ');
//
//    if (l_import.size() >= l_indice.size()) {
//
//      if (l_import.containsAll(l_indice)) 
//          return true;
//    } 
//    else {
//
//      if (l_indice.containsAll(l_import)) 
//          return true;
//	}
//	return false;
//} // End isEditoreUguale

public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
}


long findOffsetLocalizzazioni(String key)
{
	byte[] keyOffsetTitolo = new byte[21];
	
    long first = 0;
    long upto  = bidsInTrTitBib;
    long returnOffset = -1;
    String s, bid, offset;
    int positionTo;
    
    while (first < upto) {
        long mid = (first + upto) / 2;  // Compute mid point.

        positionTo = (int)mid*22;
    	trTitBibOffsetIn.position(positionTo);
    	trTitBibOffsetIn.get(keyOffsetTitolo, 0, 21);			
        
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
    
} // End findOffsetLocalizzazioni



	boolean writeRecord(String []ar) // record
	{
		
		//String ar[] = MiscString.estraiCampi(record, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "

		int localizzazioniCtr = 0;
		
		// Troviamo le localizzazioni 
		String bid = ar[Fields.bid.ordinal()];
		

		// Contiamo le localizzazioni
		long offset;
		try {
			String s;
			// Troviamo le localizzazioni
			offset = findOffsetLocalizzazioni(bid);
			if (offset != -1)
			{
				trTitBibIn.seek(offset);
				while (true)
				{
					s = trTitBibIn.readLine();
					
					if (s == null || !s.startsWith(bid))
						break;
					//System.out.println(t);
					localizzazioniCtr++;
				}
			}
				
				
			// Aggiungiamo i dati del titolo	
//			offset = findOffset(bid);
//			if (offset == -1)
//			{
//				//System.out.println("Bid non trovato: '" + ar[Fields.bid.ordinal()] + "'");
//				bidNonTrovatiCtr++;
//			//	continue;
//			}
//			else
//			{
//				tbTitoloIn.seek(offset);
//				s = tbTitoloIn.readLine();
//				titoloAr = MiscString.estraiCampi(s, tbTitoloCharSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "

				String oustString =	""+id_gruppo + sepOut 
						+ ar[Fields.vid.ordinal()] + sepOut
						+ ar[Fields.cles1_2.ordinal()] + sepOut
						+ ar[Fields.bid.ordinal()] + sepOut
						
//						+ titoloAr[tbTitoloFields.ky_cles1_t.ordinal()]  
//						+ titoloAr[tbTitoloFields.ky_cles2_t.ordinal()] + sepOut 
						
//						+ ar[Fields.cd_forma_1.ordinal()] + sepOut 
//						+ ar[Fields.numero_ordine.ordinal()] + sepOut 
//						+ ar[Fields.numero_opera.ordinal()] + sepOut 
//						+ ar[Fields.numero_cat_tem.ordinal()] + sepOut 
//						+ ar[Fields.cd_tonalita.ordinal()] + sepOut 
//						// Dati di musica
//						+ ar[Fields.organico_sintetico.ordinal()] + sepOut 
//						+ ar[Fields.organico_analitico.ordinal()] + sepOut 
						// Dati di titolo
//						+ titoloAr[tbTitoloFields.cd_livello.ordinal()]  + sepOut 

						+ ar[Fields.cd_livello.ordinal()] + sepOut
						+ localizzazioniCtr + sepOut
						+ ar[Fields.isbd.ordinal()] + sepOut
						
						
//						+ titoloAr[tbTitoloFields.isbd.ordinal()]  + sepOut
//						+  "CURRENT_TIMESTAMP" // TS_INS
						+ "\n";

		
				out.write(oustString);
//			}
			
		} catch (IOException e) {
			// 
			e.printStackTrace();
		} // Scriviamo record precedente`
		writtenCtr++;
		lastRecordCtr++;
		
		
		return true;
	} // End writeRecord

	boolean writeDoppioni()
	{
	    //ArrayList<String> doppioniEditoreAL = new ArrayList<String>(); // Elenco doppioni escluso test editore

			id_gruppo ++;
			for (int i=0; i < doppioniAL.size(); i++)
			{
				writeRecord(doppioniAL.get(i));
					
			}

		return false;
	} // End writeDoppioni	



};  // End Statistiche2006
	


