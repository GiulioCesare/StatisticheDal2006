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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import java.text.Normalizer;
import java.text.Normalizer.Form;


class DoppioniPulizia {
	boolean filtraEditore = true; 

	
	class GruppoDuplicati{
		ArrayList<String> editori = new ArrayList<String>(); 
		ArrayList<String> records = new ArrayList<String>(); 
	}
	
	ArrayList<GruppoDuplicati> gruppoDuplicatiAL = new ArrayList<GruppoDuplicati>();
	
	
	String ar[];
	BufferedReader in = null;
    BufferedWriter out = null;

    ArrayList<String> doppioniAL = new ArrayList<String>(); // Elenco doppioni escluso test editore
    ArrayList<String> editoriAL = new ArrayList<String>(); // Elenco doppioni escluso test editore

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
    
       
    
	RandomAccessFile  	trTitBibIn = null;	// tr_tit_bib.out.bytes.srt
    MappedByteBuffer 	trTitBibOffsetIn = null;	// tr_tit_bib.out.bytes.srt.off
	long fileOffetLength;
	long bidsInTrTitBib=0;
	long bidNonTrovatiCtr=0;
    
    
    
    
//  String lastRecord = "                                                  �          �    �    �    "; // Non comprende l'editore
    String lastRecord = "                                                  |          |    |    |    "; // Non comprende l'editore
	String lastEditore = ""; 
	

	int rowCtr = 0;
	int droppedCtr=0;
	int writtenCtr=0;
	int lastRecordCtr=1;
	
	
	
	enum Fields {
		cles1_2,
		vid,
		aa_pubb_1,
		pagine,
		edizione,
		cd_livello,
		bid,
		tp_materiale, // nuovo
		tp_record_uni, // nuovo
		editore, // nuovo
		isbd
	};	
	
	
	public static void main(String[] args) {
		// 

		char charSepArrayEquals[] = { '='};
		char charSepArraySpace[] = { ' '};

		if(args.length < 4)
	    {
	        System.out.println("Uso: DoppioniPulizia filenameIn filenameOut nonefileTrTitBib nomefileTrTitBibOffset"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="DoppioniPulizia tool - (C) Almaviva S.p.A 2011"+
		 "\n====================================="+
		 "\nTool di pulizia per doppioni"
		 ;

	    System.out.println(start);
		
	    DoppioniPulizia doppioniPulizia = new DoppioniPulizia(args);
	    doppioniPulizia.run();
	    
	    System.exit(0);
	}// End main		
		

	public


	String filenameIn;
	String filenameOut;
	String trTitBibFilename;
	String trTitBibOffsetFilename;
    
	char charSepArray[] = { '�' }; // C0 ''
	
	//String  sepOut = "|";
	String  sepOut = "�";
	
	String oldBid="";
	String oldPolo="";

	
	
    BufferedWriter OutLog;
		
    
    
    DoppioniPulizia (String[] args)
	{
	    filenameIn = args[0];
	    filenameOut = args[1];
	    trTitBibFilename = args[2];
	    trTitBibOffsetFilename = args[3];
	    
	    System.out.println("filenameIn " + filenameIn);
	    System.out.println("filenameOut " + filenameOut);
	    System.out.println("trTitBibFilename " + trTitBibFilename);
	    System.out.println("trTitBibOffsetFilename " + trTitBibOffsetFilename);
	    
	}
	
	void run()
	{
		String s="";
		//ConfigTable configTable=null;
		
		int state = 0;
		
		
		try {
			in = new BufferedReader(new FileReader(filenameIn));
			out = new BufferedWriter(new FileWriter(filenameOut));
			
			fileOffetLength = new File(trTitBibOffsetFilename).length();
			bidsInTrTitBib = fileOffetLength/22; 
			
			trTitBibIn = new RandomAccessFile(trTitBibFilename, "r");
			trTitBibOffsetIn = new FileInputStream(trTitBibOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);

			
			
			// String lastRecordAr[];
			// lastRecordAr = MiscString.estraiCampi("'�''�''�''�''�''�''�'", charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
			
			while (true) {
				try {
					s = in.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						rowCtr++;
						if ((rowCtr & 0x1FFF) == 0)
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
						
						// Stessa chiave (cels+vid+anno+pagine+edizione)
						String keyLast = lastRecord.substring(0,76);
						String key = s.substring(0,76);
						if (keyLast.equals(key))
						{
							doppioniAL.add(lastRecord);
							editoriAL.add(lastEditore);
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

								doppioniAL.add(lastRecord);
								editoriAL.add(lastEditore);
								
								writeDoppioni();
								
								
								doppioniAL.clear();
								editoriAL.clear();
								
							}
							lastRecordCtr=1;
						}						
						lastRecord = s;
						lastEditore = ar[Fields.editore.ordinal()];
						
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
	

	long findOffset(String key)
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
	    
	} // End findOffset
	
	
	






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
	
	
	boolean writeRecord(String record)
	{
		
		String ar[] = MiscString.estraiCampi(record, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "

		int localizzzazioniCtr = 0;
		
		// Troviamo le localizzazioni 
		String bid = ar[Fields.bid.ordinal()];
		

		// Contiamo le localizzazioni
		long offset = findOffset(bid);
		try {

			if (offset == -1)
			{
				//System.out.println("Bid non trovato: '" + ar[Fields.bid.ordinal()] + "'");
				bidNonTrovatiCtr++;
			//	continue;
			}
			else
			{
					trTitBibIn.seek(offset);
				String s;
				while (true)
				{
					s = trTitBibIn.readLine();
					
					if (s == null || !s.startsWith(bid))
						break;
					//System.out.println(t);
					localizzzazioniCtr++;
				}
			}
			
//			out.write(lastRecord + "\n");
			String oustString =	id_gruppo + sepOut +//"�"
							 
							ar[Fields.cles1_2.ordinal()].substring(0,6) + sepOut //"�" // Cles1
							+ ar[Fields.cles1_2.ordinal()].substring(6) + sepOut //"�" // cles2
							+ ar[Fields.vid.ordinal()] + sepOut //'�' // vid
							+ ar[Fields.aa_pubb_1.ordinal()] + sepOut //"�"	// Anno di pubblicazione
							+ ar[Fields.pagine.ordinal()] + sepOut //"�" // Numero di pagine
							+ ar[Fields.edizione.ordinal()] + sepOut //"�" // Edizione
							+ ar[Fields.cd_livello.ordinal()] + sepOut //"�" // Codice livello di autorita'
							+ ar[Fields.bid.ordinal()] + sepOut //"�" 	// Bid
							+ ar[Fields.tp_materiale.ordinal()] + sepOut //"�" 	
							+ ar[Fields.tp_record_uni.ordinal()] + sepOut //"�"
							+ ar[Fields.editore.ordinal()] + sepOut //"�" 	// Editore 
							+ localizzzazioniCtr + sepOut //"�"				// Localizzazioni
							+ ar[Fields.isbd.ordinal()]  + sepOut //"�" // Isbd
							+  "CURRENT_TIMESTAMP" // TS_INS
							+ "\n";
			
			out.write(oustString);
			
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

	    //	    boolean check=false;
//	    boolean idGruppoIncrementato = false;
//	    boolean almenoUnDoppione = false; 
	    
		
	    
	    
		if (filtraEditore)
		{
//			System.out.println(editoriAL.get(0));
//			if (editoriAL.get(0).contains("Rizzi"))
//			{
//				System.out.println("Controlla");
//				check = true;
//			}
			
			ArrayList<Integer> doppioniRaggruppatiAL = new ArrayList<Integer>(); // Elenco doppioni escluso test editore
			
			while (doppioniAL.size() > 1)
			{	// Creiamo un elkenco di doppioni dove viene considerato nel test anche l'editore
//				almenoUnDoppione = false;
				GruppoDuplicati gruppoDuplicati = new GruppoDuplicati();
				
				
				for (int i=1; i < doppioniAL.size(); i++)
				{
					if (isEditoreUguale(editoriAL.get(0), editoriAL.get(i)))
						{
						if (gruppoDuplicati.editori.isEmpty())
						{
							gruppoDuplicati.editori.add(editoriAL.get(0));
							gruppoDuplicati.records.add(doppioniAL.get(0));
						}
						
						gruppoDuplicati.editori.add(editoriAL.get(i));
						gruppoDuplicati.records.add(doppioniAL.get(i));
						
						// doppioniAL.remove(i);
						doppioniRaggruppatiAL.add(i);
						}
				}

				// Rimuoviamo i doppioni finiti in gruppo
				int shifted=0;
				for (int i = 0; i < doppioniRaggruppatiAL.size(); i++)
				{
//System.out.println("Rimuovi " + doppioniRaggruppatiAL.get(i));
					int entry = doppioniRaggruppatiAL.get(i) - shifted;
					doppioniAL.remove(entry);
					editoriAL.remove(entry);
					shifted++;
				}
				doppioniRaggruppatiAL.clear();	
				
				
				// Abbiamo qualcosa da scrivere
				if (gruppoDuplicati.editori.isEmpty())
				{	// Nessun editore uguale trovato nel gruppo proincipale
					// Cerchiamo il primo elemento nei gruppi precedentemente creati.
					int sottogruppo = cercaInSottogruppi(editoriAL.get(0));
					if (sottogruppo != -1)
					{
						// editore trovato in un sotrtogruppo
						gruppoDuplicatiAL.get(sottogruppo).editori.add(editoriAL.get(0));
						gruppoDuplicatiAL.get(sottogruppo).records.add(doppioniAL.get(0));
					}
					else
					{ // Editore non trovato in gruppo principale o sottogruppi.
						droppedCtr++;
					}	
				}
				else
				{ // Salviamo sottogruppo
					gruppoDuplicatiAL.add(gruppoDuplicati);
				}
				doppioniAL.remove(0); // Rimuoviamo il primo elemento usato per il compare che e' andato a finire in un nuovo gruppo, sottogruppo o perso
				editoriAL.remove(0);

			
			} // End while doppioni
			
			// Ultimo elemento rimasto
			if (doppioniAL.size() == 1)
			{
				// Nessun editore uguale trovato nel gruppo proincipale
				// Cerchiamo il primo elemento nei gruppi precedentemente creati.
				int sottogruppo = cercaInSottogruppi(editoriAL.get(0));
				if (sottogruppo != -1)
				{
					// editore trovato in un sotrtogruppo
					gruppoDuplicatiAL.get(sottogruppo).editori.add(editoriAL.get(0));
					gruppoDuplicatiAL.get(sottogruppo).records.add(doppioniAL.get(0));
				}
				else
				{ // Editore non trovato in gruppo principale o sottogruppi.
					droppedCtr++;
				}	
				
				doppioniAL.remove(0); // Rimuoviamo il primo elemento usato per il compare che e' andato a finire in un nuovo gruppo
			}
			// Scriviamo i gruppi
			scriviGruppiDuplicati();
			
		}
		else
		{ // Non si filtra per editore
			id_gruppo ++;
			for (int i=0; i < doppioniAL.size(); i++)
			{
				writeRecord(doppioniAL.get(i));
					
			}
		}
		return false;
	} // End writeDoppioni	
	
	
	
int cercaInSottogruppi(String editore)
{
	for (int i=0; i < gruppoDuplicatiAL.size(); i++)
	{

		GruppoDuplicati gruppoDupliucati = (GruppoDuplicati)gruppoDuplicatiAL.get(i);
		
		for (int j=0; j < gruppoDupliucati.editori.size(); j++)
		{
			if (isEditoreUguale(gruppoDupliucati.editori.get(j), editore))
			{
				return i; // Ritorna il gruppo
			}
		}
		
	}

	return -1; // Editore in nessun gruppo
}
	

void scriviGruppiDuplicati()
{
	for (int i=0; i < gruppoDuplicatiAL.size(); i++)
	{
		GruppoDuplicati gruppoDupliucati = (GruppoDuplicati)gruppoDuplicatiAL.get(i);
		id_gruppo++;
		for (int j=0; j < gruppoDupliucati.editori.size(); j++)
		{
			writeRecord(gruppoDupliucati.records.get(j));			
		}
		
	}
	
	gruppoDuplicatiAL.clear();
	
}

boolean isEditoreUguale(String prevEditore, String curEditore)
{

    // ARGE Normalizza lettere accentate 17/11/2011
	String prevEditoreNoAccent =  removeAccents(prevEditore);
	String curEditoreNoAccent =  removeAccents(curEditore);
	// END ARGE remove accents
	
	// Tutto upper case per i test
    String upperPrevEditore = prevEditoreNoAccent.toUpperCase().replace(',', ' ');
    String upperCurEditore = curEditoreNoAccent.toUpperCase().replace(',', ' ');
	
	
	
    //Tolgo tutti i token di tipo x. (lettera punto spazio)
    StringTokenizer st = new StringTokenizer(upperPrevEditore, " ");
    String token;
    int n;
    while (st.hasMoreTokens()) {
        token = st.nextToken();
        if (token.length() == 2 && token.charAt(1) == '.') {
            n = upperPrevEditore.indexOf(token);
            upperPrevEditore = upperPrevEditore.substring(0, n) + upperPrevEditore.substring(n + 2);
        }
    }
    st = new StringTokenizer(upperCurEditore, " ");
    while (st.hasMoreTokens()) {
        token = st.nextToken();
        if (token.length() == 2 && token.charAt(1) == '.') {
            n = upperCurEditore.indexOf(token);
            upperCurEditore = upperCurEditore.substring(0, n) + upperCurEditore.substring(n + 2);
        }
    }

    
    
	//MANTIS 2236: Il protocollo deve tener conto dei casi in cui il nome editore è seguito dal "."
	// Es.: "Donzelli." deve essere considerato "Donzelli"
    upperPrevEditore = upperPrevEditore.replace('.',' ');
    upperCurEditore = upperCurEditore.replace('.',' ');

    
    // Si eliminano i token nelle stoplist
    st = new StringTokenizer(upperPrevEditore);
//    upperPrevEditore = null;
    upperPrevEditore = "";
    n = 0;
    while (st.hasMoreTokens() && n < 4) {
        token = st.nextToken();
        if ( findStopword(token) < 0) {
            if (n == 0)
            	upperPrevEditore = token;
            else
            	upperPrevEditore = upperPrevEditore + " " + token;
            n++;
        }
    }
    
    st = new StringTokenizer(upperCurEditore);
//    upperCurEditore = null;
    upperCurEditore = "";
    n = 0;
    while (st.hasMoreTokens() && n < 4) {
        token = st.nextToken();
        if (findStopword(token) < 0) {
            if (n == 0)
            	upperCurEditore = token;
            else
            	upperCurEditore = upperCurEditore + " " + token;
            n++;
        }
    }
    
    
    if (upperPrevEditore.equals("") && upperCurEditore.equals(""))
        return true;
    
    if 	(
    		( upperPrevEditore.contains("S.N") || upperPrevEditore.contains("S.T") || upperPrevEditore.contains("S.E") || upperPrevEditore.equals(""))
            && 
            (upperCurEditore.contains("S.N") || upperCurEditore.contains("S.T") || upperCurEditore.contains("S.E") || upperCurEditore.equals(""))
       	) 
    	return true;        
    
  
    HashSet l_import = stringTokenizer(upperPrevEditore, ' ');

    HashSet l_indice = stringTokenizer(upperCurEditore, ' ');

    if (l_import.size() >= l_indice.size()) {

      if (l_import.containsAll(l_indice)) 
          return true;
    } 
    else {

      if (l_indice.containsAll(l_import)) 
          return true;
	}
	return false;
} // End isEditoreUguale

public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
}


};  // End Statistiche2006
	


