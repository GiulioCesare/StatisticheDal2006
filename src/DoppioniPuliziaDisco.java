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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//import DoppioniAutoriResp1Disco.Fields;


class DoppioniPuliziaDisco {
//	boolean filtraEditore = true; 
	ArrayList<Pattern> patternCdList = new ArrayList<Pattern>(); 
	ArrayList<Pattern> patternDvdList = new ArrayList<Pattern>(); 
	ArrayList<Pattern> patternVhsList = new ArrayList<Pattern>(); 
	ArrayList<Pattern> patternBlueRayList = new ArrayList<Pattern>(); 

	int gruppiCd=0, gruppiDvd=0, gruppiVhs=0, gruppiBlueRay=0, gruppiDefault=0;
	
	class GruppoDuplicati{
		ArrayList<String> editori = new ArrayList<String>(); 
		ArrayList<String> records = new ArrayList<String>(); 
	}
	
	ArrayList<GruppoDuplicati> gruppoDuplicatiAL = new ArrayList<GruppoDuplicati>();
	
	
	String ar[];
	BufferedReader in = null;
    BufferedWriter out = null;

    ArrayList<String> doppioniAL = new ArrayList<String>(); // Elenco doppioni escluso test editore
//    ArrayList<String> editoriAL = new ArrayList<String>(); // Elenco doppioni escluso test editore

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
    
    
    
    
//String lastEditore = ""; 
	

	int rowCtr = 0;
	int droppedCtr=0;
	int writtenCtr=0;
	int lastRecordCtr=1;
	int filtrati = 0;
	int ripuliti = 0;
	
	
	enum Fields {
		cles1_2,
		cd_livello,
		aa_pubb_1,
		bid,
		cd_natura,
		volume,
		editore,
		isbd
	};	
	
	
	public static void main(String[] args) {
		// 

		char charSepArrayEquals[] = { '='};
		char charSepArraySpace[] = { ' '};

		if(args.length < 4)
	    {
	        System.out.println("Uso: DoppioniPuliziaDisco filenameIn filenameOut nonefileTrTitBib nomefileTrTitBibOffset"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="DoppioniPuliziaDisco tool - (C) Almaviva S.p.A 2015"+
		 "\n====================================="+
		 "\nTool di pulizia per doppioni"
		 ;

	    System.out.println(start);
		
	    DoppioniPuliziaDisco doppioniPuliziaDisco = new DoppioniPuliziaDisco(args);
	    doppioniPuliziaDisco.run();
	    
	    System.exit(0);
	}// End main		
		

	public


	String filenameIn;
	String filenameOut;
	String trTitBibFilename;
	String trTitBibOffsetFilename;
    
	char charSepArray[] = { 0x01 }; // '�' C0 '|'
	
//	String  sepOut = "�"; // |
	char  sepIn = 0x01;
	char  sepOut = 0x01;
	
	String oldBid="";
	String oldPolo="";

//	String lastRecord = " � � � � � � � � ";
	String lastRecord = " "+sepIn+" "+sepIn+" "+sepIn+" "+sepIn+" "+sepIn+" "+sepIn+" "+sepIn+" "+sepIn+" ";
	
	
    BufferedWriter OutLog;
		
    
    
    DoppioniPuliziaDisco (String[] args)
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
		compilePatterns();
		
		try {
			in = new BufferedReader(new FileReader(filenameIn));
			out = new BufferedWriter(new FileWriter(filenameOut));
			
			fileOffetLength = new File(trTitBibOffsetFilename).length();
			bidsInTrTitBib = fileOffetLength/22; 
			
			trTitBibIn = new RandomAccessFile(trTitBibFilename, "r");
			trTitBibOffsetIn = new FileInputStream(trTitBibOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);

			
			
			 String lastRecAr[];
			
			while (true) { //  rowCtr < 100000 true
				
				try {
					s = in.readLine();
					if (s == null)
						break;
					else {
//System.out.println(s);
						rowCtr++;
//if (gruppiDefault == 100)
//	break;
						if ((rowCtr & 0xFFFF) == 0)
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Scritti " + writtenCtr + " record");
							System.out.println("Scartati  " + (rowCtr - writtenCtr));
							System.out.println("Gruppi " + id_gruppo);

//							System.out.println("gruppiDefault "+gruppiDefault);
//							System.out.println("gruppiCd "+gruppiCd);
//							System.out.println("gruppiDvd "+gruppiDvd);
//							System.out.println("gruppiVhs "+gruppiVhs);
//							System.out.println("gruppiBlueRay "+gruppiBlueRay);
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
						String key = ar[0]+ar[1]+ar[2]+ar[4]+ar[6];

						lastRecAr = MiscString.estraiCampi(lastRecord, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE);
						String keyLast = lastRecAr[0]+lastRecAr[1]+lastRecAr[2]+lastRecAr[4]+lastRecAr[6]; 
						
						

						
						
						
						if (keyLast.equals(key))
						{
							doppioniAL.add(lastRecord);
//							editoriAL.add(lastEditore);
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
//								editoriAL.add(lastEditore);
								
								writeDoppioni();
								
//break;								
								doppioniAL.clear();
								
							}
							lastRecordCtr=1;
						}						
						lastRecord = s;
//						lastEditore = ar[Fields.editore.ordinal()];
						
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
					if (doppioniAL.size() > 1)
						writeDoppioni();
					else if (doppioniAL.size() == 1)
						droppedCtr++;
						
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
					System.out.println("Totale letti " + rowCtr + " record");
					System.out.println("Totale scritti " + writtenCtr + " record");
					System.out.println("Totale scartati  " + (rowCtr - writtenCtr));
					System.out.println("Totale gruppi " + id_gruppo);
					
					
					System.out.println("\nFinito !");
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

		int localizzazioniCtr = 0;
		
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
					localizzazioniCtr++;
				}
			}
			
			
		
			
			
//			out.write(lastRecord + "\n");
			String oustString =	
							""+id_gruppo + sepOut
							+ ar[Fields.cles1_2.ordinal()] + sepOut 
							+ ar[Fields.aa_pubb_1.ordinal()] + sepOut
							+ ar[Fields.cd_livello.ordinal()] + sepOut
							+ ar[Fields.bid.ordinal()] + sepOut
							+ ar[Fields.cd_natura.ordinal()] + sepOut
							+ ar[Fields.editore.ordinal()] + sepOut
							+ localizzazioniCtr + sepOut 
							+ ar[Fields.isbd.ordinal()]  + sepOut
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







void compilePatterns()
{

	patternCdList.add(Pattern.compile("Compact disc", Pattern.CASE_INSENSITIVE));
	patternCdList.add(Pattern.compile("Cd[ \\.,]", Pattern.CASE_INSENSITIVE));
	
	patternDvdList.add(Pattern.compile("Dvd[ \\.,]", Pattern.CASE_INSENSITIVE));
	
	patternVhsList.add(Pattern.compile("Vhs[ \\.,]", Pattern.CASE_INSENSITIVE));
	patternVhsList.add(Pattern.compile("videocassetta", Pattern.CASE_INSENSITIVE));

	patternBlueRayList.add(Pattern.compile("Blue* Ray", Pattern.CASE_INSENSITIVE));

}

ArrayList<String> getGruppoRipulito(ArrayList<String> duplicatiList)
{
	
	//Eliminiamo titoli diversi tra loro
	// per "radice : " "radice /" 
	
	
	
//*6 lezioni su : *pianeti, stelle, universo : le nuove frontiere della cosmologia / Margherita Hack. - Bologna : ASIA edizioni, [2011?]. - 3 DVD video (12 ore compl.) ; in contenitore, 19 c. ((Tit. del contenitore.�CURRENT_TIMESTAMP
//*6 lezioni su : la *matematica nell'arte : quando musica, pittura e letteratura incrociano la scienza esatta / Piergiorgio Odifreddi. - Bologna : ASIA edizioni, [2011?!. - 3 DVD video (12 ore compl.) ; in contenitore, 19 c. ((Tit. del contenitore.�CURRENT_TIMESTAMP
//*6 lezioni su : la *laicità della scienza / Giulio Giorello. - Bologna : ASIA edizioni, [2011?!. - 3 DVD video (12 ore compl.) ; in contenitore, 19 cm. ((Tit. del contenitore.�CURRENT_TIMESTAMP
//*6 lezioni su : il *mondo della vita e il mondo della tecnica / Umberto Galimberti. - Bologna : ASIA edizioni, [2011?!. - DVD video ; in contenitore, 19 cm. ((Tit. del contenitore.�CURRENT_TIMESTAMP
//*6 lezioni su : *mente e coscienza / Edoardo Boncinelli. - Bologna : ASIA edizioni, [2011?!. - 3 DVD video (12 ore compl.) ; in contenitore, 19 c. ((Tit del contenitore.�CURRENT_TIMESTAMP
	

		ArrayList<String> duplicatiFiltrati = new ArrayList<String>(); 
		
		String titleTop, titleBottom;
		String isbdTop = "", isbdBottom = ""; 
		boolean dupplicato = false;
		
		boolean topDuePunti=false;
		boolean bottomDuePunti=false;
		
		for (int i=1; i < duplicatiList.size(); i++)
		{
			dupplicato = false;
			topDuePunti=false;
			bottomDuePunti=false;

			ar = MiscString.estraiCampi(duplicatiList.get(i-1), charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
			isbdTop = ar[7];
			
			ar = MiscString.estraiCampi(duplicatiList.get(i), charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
			isbdBottom = ar[7];

			
			
//System.out.println("Top="+doppioniAL.get(i-1));
//System.out.println("Bot="+doppioniAL.get(i));

			// Se titolo diverso scartiamolo
			// Cerchiamo il fine area

			int idxArea = isbdTop.indexOf(". - ");
			if (idxArea > 0)
				titleTop = isbdTop.substring(0, idxArea);
			else
				titleTop = isbdTop;
			// Abbiamo primo separatore?
			int idxTopSep1 = titleTop.indexOf(" : ");
			int idxTopSep2 = -1;
			if (idxTopSep1 > 0)
			{
				topDuePunti = true;
				// Abbiamo secondo separatore
				idxTopSep2 = titleTop.indexOf(" : ", idxTopSep1+3);
				if (idxTopSep2 != -1)
					titleTop = titleTop.substring(0, idxTopSep2);
			}
			// Abbiamo commenti?
			int idxCom = titleTop.indexOf("((");
			if (idxCom > -1 )
				titleTop = titleTop.substring(0, idxCom);
				
			idxArea = isbdBottom.indexOf(". - ");
			if (idxArea > 0)
				titleBottom = isbdBottom.substring(0, idxArea);
			else
				titleBottom = isbdBottom;
			// Abbiamo primo separatore?
			int idxBotSep1 = titleBottom.indexOf(" : ");
			int idxBotSep2 = -1;
			if (idxBotSep1 > 0)
			{
				bottomDuePunti = true;
				// Abbiamo secondo separatore
				idxBotSep2 = titleBottom.indexOf(" : ", idxBotSep1+1);
				if (idxBotSep2 != -1)
					titleBottom = titleBottom.substring(0, idxBotSep2);
			}
			// Abbiamo commenti?
			idxCom = titleBottom.indexOf("((");
			if (idxCom > -1 )
				titleBottom = titleBottom.substring(0, idxCom);

			
			
			
			if (topDuePunti && !bottomDuePunti)
			{ // Tronchiamo top title
				titleTop = titleTop.substring(0, idxTopSep1);	
			}
			else if (!topDuePunti && bottomDuePunti)
			{ // Tronchiamo bottom title
				titleBottom = titleBottom.substring(0, idxBotSep1);	
			}
			else
			{
				// entrambi con " : " 
			}
			
/*
La è causa rimozione
	*1. Trofeo internazionale "Città di Mantova" : *triangolare a squadre di karatè maschile e femminile "
	*1. Trofeo internazionale "Città di Mantova" : *triangolare a squadre di karate maschile e femminile "

Normalizziamo le lettere accentate

	*12 monkeys : music from the motion picture /
	*12 Monkeys : [music from the motion picture] /

Rimuoviamo le []

CI PERDIAMO
	*23 pezzi facili per pianoforte : edizione senza note in calce /
	*23 pezzi facili per pianoforte / Bach ; Mugellini ; Massimiliano Damerini, pianista
 */
			String tt = titleTop.replace('à', 'a');
			tt = tt.replace('è', 'e');
			tt = tt.replace("[", "");
			tt = tt.replace("]", "");
			tt = tt.replace("*", "");

			int idx=tt.indexOf('/');
			if (idx > 0)
				tt = tt.substring(0, idx);
			tt=tt.trim();
			
			String tb = titleBottom.replace('à', 'a');
			tb = tb.replace('è', 'e');
			tb = tb.replace("[", "");
			tb = tb.replace("]", "");
			tb = tb.replace("*", "");

			idx=tb.indexOf('/');
			if (idx > 0)
				tb = tb.substring(0, idx);
			tb=tb.trim();
			
			if (!tt.equalsIgnoreCase(tb))
			{
//System.out.println("'"+tt+"' <<<>>> '"+tb+"'");
				continue;
			}	
			// Titolo simile prendiamolo
			duplicatiFiltrati.add(duplicatiList.get(i-1));
			dupplicato = true;
		}
		
	// Ultimo titolo duplicato?	
	if (dupplicato)	
	{
//		duplicatiFiltrati.add(isbdRight);
		duplicatiFiltrati.add(duplicatiList.get(duplicatiList.size()-1));
	}
		
/*		
	if (duplicatiFiltrati.size() != doppioniAL.size())
	{
		// Abbiamo dei doppioni filtrati
		droppedCtr += doppioniAL.size()-duplicatiFiltrati.size();
		filtrati += doppioniAL.size()-duplicatiFiltrati.size();		
		
		
		System.out.println("=====");
		// Vediamo cosa abbiamo rimosso
		for (int i=0; i < duplicatiFiltrati.size(); i++)
			System.out.println(duplicatiFiltrati.get(i));
		
		System.out.println("----");

		for (int i=0; i < doppioniAL.size(); i++)
			System.out.println(doppioniAL.get(i));
		
		System.out.println("=====");
	}
*/
	
	
	
	return duplicatiFiltrati;
} // End writeGruppo	

boolean writeDoppioni()
{
// Eliminiamo titoli con parole chiave diverse tra loro 
	int written=0;
	ArrayList<String> gruppoCd = new ArrayList<String>(); 
	ArrayList<String> gruppoDvd = new ArrayList<String>(); 
	ArrayList<String> gruppoVhs = new ArrayList<String>(); 
	ArrayList<String> gruppoBlueRay = new ArrayList<String>(); 
	ArrayList<String> gruppoDefault = new ArrayList<String>(); 
	
	String titleTop, titleBottom;
	String isbd, tp_record_uni; 
	boolean found = false;
	
	for (int i=0; i < doppioniAL.size(); i++)
	{
		
		ar = MiscString.estraiCampi(doppioniAL.get(i), charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
		isbd = ar[7];

//		tp_record_uni = isbd = ar[8];
		
		
//System.out.println("isbd "+isbd);		

		found = false;
		for (Pattern pattern : patternCdList) {
	      	 Matcher matcher = pattern.matcher(isbd);
	         if(matcher.find())
	         {
	     		gruppoCd.add(doppioniAL.get(i));
	     		found = true;
	     		break;
	         }
	        }
		if (found)
			continue;

		for (Pattern pattern : patternDvdList) {
	      	 Matcher matcher = pattern.matcher(isbd);
	         if(matcher.find())
	         {
	     		gruppoDvd.add(doppioniAL.get(i));
	     		found = true;
	     		break;
	         }
	        }
		if (found)
			continue;

		for (Pattern pattern : patternVhsList) {
	      	 Matcher matcher = pattern.matcher(isbd);
	         if(matcher.find())
	         {
	     		gruppoVhs.add(doppioniAL.get(i));
	     		found = true;
	     		break;
	         }
	        }
		if (found)
			continue;
		
		for (Pattern pattern : patternBlueRayList) {
	      	 Matcher matcher = pattern.matcher(isbd);
	         if(matcher.find())
	         {
	     		gruppoBlueRay.add(doppioniAL.get(i));
	     		found = true;
	     		break;
	         }
	        }
		if (found)
			continue;
		
		// Default group
		gruppoDefault.add(doppioniAL.get(i));
	} // End for

	if (gruppoDefault.size()>1)
	{
		ArrayList<String> duplicatiRipuliti = getGruppoRipulito(gruppoDefault);
		if (gruppoDefault.size() > duplicatiRipuliti.size())
		{
			droppedCtr += gruppoDefault.size()-duplicatiRipuliti.size();
			ripuliti += gruppoDefault.size()-duplicatiRipuliti.size();		
		}
		if (duplicatiRipuliti.size() > 1)
		{ // almeno due per essere considerati duplicati
//			gruppiDefault++;
//			id_gruppo ++;
//			for (int i=0; i < duplicatiRipuliti.size(); i++)
//				writeRecord(duplicatiRipuliti.get(i));
//			written+=duplicatiRipuliti.size();

			Hashtable gtp =	getGruppiPerTipoRecord(duplicatiRipuliti);
			scriviGruppo(gtp);
			
		}
		
	}
	
	if (gruppoCd.size()>1)
	{
		ArrayList<String> duplicatiRipuliti = getGruppoRipulito(gruppoCd);
		
		if (gruppoCd.size() > duplicatiRipuliti.size())
		{
			droppedCtr += gruppoCd.size()-duplicatiRipuliti.size();
			ripuliti += gruppoCd.size()-duplicatiRipuliti.size();		
		}
		if (duplicatiRipuliti.size() > 1)
		{ // almeno due per essere considerati duplicati
			
//			gruppiCd++;
//			id_gruppo ++;
//			for (int i=0; i < gruppoCd.size(); i++)
//				writeRecord(gruppoCd.get(i));
//			written+=gruppoCd.size();
			
			
			Hashtable gtp =	getGruppiPerTipoRecord(duplicatiRipuliti);
			scriviGruppo(gtp);

			

	}
}
	if (gruppoDvd.size()>1)
	{
		ArrayList<String> duplicatiRipuliti = getGruppoRipulito(gruppoDvd);
		if (gruppoDvd.size() > duplicatiRipuliti.size())
		{
			droppedCtr += gruppoDvd.size()-duplicatiRipuliti.size();
			ripuliti += gruppoDvd.size()-duplicatiRipuliti.size();		
		}
		if (duplicatiRipuliti.size() > 1)
		{ // almeno due per essere considerati duplicati
//			gruppiDvd++;
//			id_gruppo ++;
//			for (int i=0; i < duplicatiRipuliti.size(); i++)
//				writeRecord(duplicatiRipuliti.get(i));
//			written+=duplicatiRipuliti.size();
			
			Hashtable gtp =	getGruppiPerTipoRecord(duplicatiRipuliti);
			scriviGruppo(gtp);
		}
	}

  	if (gruppoVhs.size()>1)
	{
  		
		ArrayList<String> duplicatiRipuliti = getGruppoRipulito(gruppoVhs);
		if (gruppoVhs.size() > duplicatiRipuliti.size())
		{
			droppedCtr += gruppoVhs.size()-duplicatiRipuliti.size();
			ripuliti += gruppoVhs.size()-duplicatiRipuliti.size();		
		}
		if (duplicatiRipuliti.size() > 1)
		{ // almeno due per essere considerati duplicati
//			gruppiVhs++;
//			id_gruppo ++;
//			for (int i=0; i < duplicatiRipuliti.size(); i++)
//				writeRecord(duplicatiRipuliti.get(i));
//			written+=duplicatiRipuliti.size();
			
			Hashtable gtp =	getGruppiPerTipoRecord(duplicatiRipuliti);
			scriviGruppo(gtp);
		}
  		
	}

  	if (gruppoBlueRay.size()>1)
	{
		ArrayList<String> duplicatiRipuliti = getGruppoRipulito(gruppoBlueRay);
		if (gruppoBlueRay.size() > duplicatiRipuliti.size())
		{
			droppedCtr += gruppoBlueRay.size()-duplicatiRipuliti.size();
			ripuliti += gruppoBlueRay.size()-duplicatiRipuliti.size();		
		}
		if (duplicatiRipuliti.size() > 1)
		{ // almeno due per essere considerati duplicati
//			gruppiBlueRay++;
//			id_gruppo ++;
//			for (int i=0; i < duplicatiRipuliti.size(); i++)
//				writeRecord(duplicatiRipuliti.get(i));
//			written+=duplicatiRipuliti.size();
			
			Hashtable gtp =	getGruppiPerTipoRecord(duplicatiRipuliti);
			scriviGruppo(gtp);
		}
	}
	

	return true;
} // End writeDoppioni	



void scriviGruppo(Hashtable gtp)
{
	Enumeration e = gtp.keys();
	while (e.hasMoreElements()) {
	  Character key = (Character) e.nextElement();
	  ArrayList<String> dup = (ArrayList<String>)gtp.get(key);
	  
		if (dup.size() > 1)
		{
			id_gruppo ++;
			for (int i=0; i < dup.size(); i++)
				writeRecord(dup.get(i));
		}
	}
} // End scriviGruppo



Hashtable getGruppiPerTipoRecord(ArrayList<String> duplicatiList)
{
	boolean isDiscoSonoro = false;
	
	Hashtable table = new Hashtable();
	String duplicato;
	char tipoRecord;
	for (int i=0; i < duplicatiList.size(); i++)
	{
		duplicato = duplicatiList.get(i);
		if (!isDiscoSonoro)
		{
			if (duplicato.contains("Disco sonoro"))
				isDiscoSonoro = true;
		}
		
		
		tipoRecord = duplicato. charAt(duplicato.length()-1);
		if (tipoRecord == 'i' || tipoRecord == 'j')
			tipoRecord = 'y'; // y = i or j
		
		
		// Vediamo se abbimo gia' queto tipo record?
		ArrayList<String> ar = (ArrayList<String>)table.get(tipoRecord);
		if (ar == null)
		{
			ar = new ArrayList<String>();
			ar.add(duplicato);
			table.put(tipoRecord, ar);
		}
		else
		{
			ar.add(duplicato);
		}
	}
	
	if (isDiscoSonoro)
		return getGruppiPerDiscoSonoro(table);
	return table;
	
} // End getGruppiPerTipoRecord


Hashtable getGruppiPerDiscoSonoro(Hashtable ht)
{
	
	Hashtable table = new Hashtable();
	String duplicato;	
	
	Enumeration e = ht.keys();
	while (e.hasMoreElements()) {
	  Character key = (Character) e.nextElement();
	  ArrayList<String> dup = (ArrayList<String>)ht.get(key);

	  if (dup.size() > 1)
		{
		char numGiri = '0';  
		for (int i=0; i < dup.size(); i++)
		{
			duplicato = dup.get(i);

			// Troviamo il numer odi giri del disco sonoro
			int ndx = duplicato.indexOf("Disco sonoro :");
			if (ndx == -1)
				numGiri = '0';
			else
			{
				if (duplicato.contains("Disco sonoro : 33"))
					numGiri = '3';
				else if (duplicato.contains("Disco sonoro : 45"))
					numGiri = '4';
				else if (duplicato.contains("Disco sonoro : 78"))
					numGiri = '7';
			}
			
			// Vediamo se abbimo gia' queto tipo record?
			ArrayList<String> ar = (ArrayList<String>)table.get(numGiri);
			if (ar == null)
			{
				ar = new ArrayList<String>();
				ar.add(duplicato);
				table.put(numGiri, ar);
			}
			else
			{
				ar.add(duplicato);
			}
		} // end for
		} // end if dup.size() > 1 
	} // End while
	
	
	
	
	
	return table;
	
} // End getGruppiPerDiscoSonoro






};  // End Statistiche2006
	


