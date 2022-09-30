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

//import EstrazioneDatiPerDoppioniAutori.Fields;


class DoppioniPuliziaAutore {
//	boolean filtraEditore = true; 

	
//	class GruppoDuplicati{
//		ArrayList<String> simili = new ArrayList<String>(); 
//		ArrayList<String> uguali = new ArrayList<String>(); 
//
//	}
	
//	ArrayList<GruppoDuplicati> gruppoDuplicatiAL = new ArrayList<GruppoDuplicati>();
	
	
	String ar[];
	BufferedReader in = null;
    BufferedWriter out = null;

    ArrayList<String> doppioniAL = new ArrayList<String>(); // Elenco doppioni escluso test editore
//    ArrayList<String> editoriAL = new ArrayList<String>(); // Elenco doppioni escluso test editore

    int	id_gruppo = 0;
    
    
       
    
	long fileOffetLength;
	long vidsInTrAutBib=0;
	long vidNonTrovatiCtr=0;
    
    
    
    
//  String lastRecord = "                                                  �          �    �    �    "; // Non comprende l'editore
    String lastRecord = "                                                  |          |    |    |    "; // Non comprende l'editore
	String lastCles="";
	int rowCtr = 0;
	int droppedCtr=0;
	int writtenCtr=0;
	int lastRecordCtr=1;
	
	
	
	enum Fields {
		cles,
		qualificazione,
		vid,
		ds_nome_aut,
		tp_nome_aut,
		cd_livello
	};	
	
	
	public static void main(String[] args) {
		// 

		char charSepArrayEquals[] = { '='};
		char charSepArraySpace[] = { ' '};

		if(args.length < 2)
	    {
//	        System.out.println("Uso: DoppioniPuliziaAutore filenameIn filenameOut nonefileTrTitBib nomefileTrTitBibOffset"); 
	        System.out.println("Uso: DoppioniPuliziaAutore filenameIn filenameOut nonefileTrAutBib nomefileTrAutBibOffset"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start=
	       "DoppioniPuliziaAutore tool - (C) Almaviva S.p.A 2015-2020"+
		 "\n========================================================="+
		 "\nTool di pulizia per doppioni autore"+
		 "\nVersione 06.12.01 13/12/2020" 
		 ;

	    System.out.println(start);
		
	    DoppioniPuliziaAutore doppioniPuliziaAutore = new DoppioniPuliziaAutore(args);
	    doppioniPuliziaAutore.run();
	    
	    System.exit(0);
	}// End main		
		

	public


	String filenameIn;
	String filenameOut;

	String trAutBibFilename;
	String trAutBibOffsetFilename;
	RandomAccessFile  	trAutBibIn = null;	// tr_aut_bib.out.bytes.srt.rel
    MappedByteBuffer 	trAutBibOffsetIn = null;	// tr_aut_bib.out.bytes.srt.rel.off


	
	char charSepArray[] = { 0x01 }; // '�'C0 ''
	
	//String  sepOut = "|";
//	String  sepOut = "�";
	char  sepOut = 0x01;
	
	String oldBid="";
	String oldPolo="";

	
	
    BufferedWriter OutLog;
		
    
    
    DoppioniPuliziaAutore (String[] args)
	{
	    filenameIn = args[0];
	    filenameOut = args[1];
	    trAutBibFilename = args[2];
	    trAutBibOffsetFilename = args[3];
	    
	    System.out.println("filenameIn " + filenameIn);
	    System.out.println("filenameOut " + filenameOut);
	    System.out.println("trTitBibFilename " + trAutBibFilename);
	    System.out.println("trTitBibOffsetFilename " + trAutBibOffsetFilename);
	    
	}
	
	void run()
	{
		String s="";
		//ConfigTable configTable=null;
		
		int state = 0;
		
		
		try {
			in = new BufferedReader(new FileReader(filenameIn));
			out = new BufferedWriter(new FileWriter(filenameOut));
			
			fileOffetLength = new File(trAutBibOffsetFilename).length();
			vidsInTrAutBib = fileOffetLength/22; 
			
			trAutBibIn = new RandomAccessFile(trAutBibFilename, "r");
			trAutBibOffsetIn = new FileInputStream(trAutBibOffsetFilename).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);
			
			// String lastRecordAr[];
			// lastRecordAr = MiscString.estraiCampi("'�''�''�''�''�''�''�'", charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
			
			
			lastRecordCtr=0;
			while (true) {
//			while (rowCtr < 10000) {
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
							System.out.println("Gruppi " + id_gruppo);
							System.out.println("Autori senza localizzazioni " + vidNonTrovatiCtr);
						}
						
//if (rowCtr < 396)
//	continue;
//
//if (s.startsWith("1 ARTI"))
//		System.out.println("rowCtr=" + rowCtr);

						if ((	s.length() == 0) 
								||  (s.charAt(0) == '#') 
								|| (Misc.emptyString(s) == true))
							continue;
					
						ar = MiscString.estraiCampi(s, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
						
						// Stessa cles? 
						String cles = ar[Fields.cles.ordinal()];
						if (!lastCles.equals(cles))
						{	// Cambio chiave
							// Ultimo record era singolo o multiplo?
							if (lastRecordCtr < 2)
							{
								if (lastRecordCtr > 0)
									droppedCtr++; // Singolo, droppato
								lastRecord = s;
								lastCles = cles;
								lastRecordCtr = 1;
							}
							else
							{
								doppioniAL.add(lastRecord);

								writeDoppioni();
								doppioniAL.clear();

								lastRecordCtr = 1;
								lastRecord = s;
								lastCles = cles;
								
							}
						}
						else
						{
							doppioniAL.add(lastRecord);
							lastRecord = s;
							lastCles = ar[Fields.cles.ordinal()];
							lastRecordCtr++;
						}
						
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
					trAutBibIn.close();	
//				    trAutBibOffsetIn.close();
					
					
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				try {
					System.out.println("\n\nInput " + filenameIn);
					System.out.println("Output " + filenameOut);
					System.out.println("Totale letti " + rowCtr + " record");
					System.out.println("Totale scartati  " + droppedCtr + " record");
					System.out.println("Totale scritti " + writtenCtr + " record");
					System.out.println("Totale gruppi " + id_gruppo);
					System.out.println("Totale autori senza localizzazioni " + vidNonTrovatiCtr);
					
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
	    long upto  = vidsInTrAutBib;
	    long returnOffset = -1;
	    String s, bid, offset;
	    int positionTo;
	    
	    while (first < upto) {
	        long mid = (first + upto) / 2;  // Compute mid point.

	        positionTo = (int)mid*22;
	    	trAutBibOffsetIn.position(positionTo);
	    	trAutBibOffsetIn.get(keyOffsetTitolo, 0, 21);			
	        
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
	
	
	
	
	
/*
void scriviGruppiDuplicati()
{
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
	
	gruppoDuplicatiAL.clear();
	
}
*/

/*
public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
}
*/




boolean writeRecord(String record, char type)
{
	
	String ar[] = MiscString.estraiCampi(record, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "

	int localizzzazioniCtr = 0;
	
	// Troviamo le localizzazioni 
	String vid = ar[Fields.vid.ordinal()];
	

	// Contiamo le localizzazioni
	long offset = findOffset(vid);
	try {
		if (offset == -1)
		{
			//System.out.println("Bid non trovato: '" + ar[Fields.bid.ordinal()] + "'");
			vidNonTrovatiCtr++;
		//	continue;
		}
		else
		{
			trAutBibIn.seek(offset);
			String s = trAutBibIn.readLine();
			// Splittiamo la riga per contare le localizzazioni
			String[] parts = s.split("\\|");
			localizzzazioniCtr = parts.length-1; 
		
		}
		
		
//		out.write(lastRecord + "\n");
// DEV output
//		String oustString =	id_gruppo + sepOut //"�"
//						+ type + sepOut
//						+ ar[Fields.cles.ordinal()].trim() + sepOut 
//						+ ar[Fields.qualificazione.ordinal()] + sepOut 
//						+ ar[Fields.vid.ordinal()] + sepOut 
//						+ ar[Fields.ds_nome_aut.ordinal()] + sepOut 
//						+ "\n";

		
		String oustString =	
				""+id_gruppo + sepOut 
				+ ar[Fields.ds_nome_aut.ordinal()] + sepOut 
				+ ar[Fields.cles.ordinal()].trim() + sepOut 
				+ ar[Fields.vid.ordinal()] + sepOut 
				+ ar[Fields.tp_nome_aut.ordinal()] + sepOut 
				+ ar[Fields.cd_livello.ordinal()] + sepOut 
				+ localizzzazioniCtr + sepOut 
				+ "CURRENT_TIMESTAMP" + sepOut // TS_INS
				+ type + sepOut	// Tipo gruppo 
				+ "" + sepOut	// FL_MOD
				+ "\n";
		
		
//		String oustString =	ar[Fields.vid.ordinal()]+ "\n";
		
		
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
    
		

//	gruppoDuplicatiAL.clear();
	
	// Gruppo contiene tutti elementi con qualificazione.
	// =================================================
	// Se tutte le qualificazioni sono DIVERSE allora scartare gruppo 
	ArrayList<String> qualificazioniAL = new ArrayList<String>(); // Elenco delle qualificazioni

//if (ar[0].startsWith("ABATE"))	
//	dumpArrayList("doppioniAL", doppioniAL);
	
	
	
	for (int i=0; i < doppioniAL.size(); i++)
	{
		ar = MiscString.estraiCampi(doppioniAL.get(i), charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
		if (ar[Fields.qualificazione.ordinal()].isEmpty())
//			break;
			continue; // 13/12/2020 causa sort diverso in vmexport 
		qualificazioniAL.add(ar[Fields.qualificazione.ordinal()]);
	}
	
	if (qualificazioniAL.size() == doppioniAL.size())
	{ // Tutti record con qualificazioni!!
	  // =================================	

		ArrayList<ArrayList<Integer>> sottogruppiAL = trovaSottogruppiQualificati(qualificazioniAL);
		
		if (sottogruppiAL.size() > 0)
		{ // Scriviamo i sottogruppi
//if (sottogruppiAL.size() > 1)
//	System.out.println("=====");
			for (int i=0; i < sottogruppiAL.size(); i++)
			{
				ArrayList<Integer> sottogruppoAL = sottogruppiAL.get(i);
				id_gruppo ++;
				for (int j=0; j < sottogruppoAL.size(); j++)
				{
					writeRecord(doppioniAL.get(sottogruppoAL.get(j)), 'U');
//System.out.println("UGUALE: "+doppioniAL.get(sottogruppoAL.get(j)));					
				}
//System.out.println("----");
			}
//if (sottogruppiAL.size() > 1)
//	System.out.println("=====");
	
		}
		else
		{
			return true; // scartiamo gruppo perche' non esiste neanche un sottogruppo
		}
	
	}
	
	else if (qualificazioniAL.size() == 0)
	{ // Nessuna Qualificazione e cles uguali
	  // ====================================
	  // Ne ricaviamo un unico gruppo	
		id_gruppo ++;
		for (int i=0; i < doppioniAL.size(); i++)
			writeRecord(doppioniAL.get(i), 'U');
	}
	
	else
	{ // Gruppo simili con e senza qualificazioni ma stessa cles (cles senza qualificazione) 
		id_gruppo ++;
		for (int i=0; i < doppioniAL.size(); i++)
			writeRecord(doppioniAL.get(i), 'S');
	}
		
	
	
// TODO Da rimuovere quando finito!!	
//	id_gruppo ++;
//	for (i=0; i < doppioniAL.size(); i++)
//		writeRecord(doppioniAL.get(i));
		
	return true;
} // End writeDoppioni	


void dumpArrayList(String arrayListName, ArrayList<String> al)
{
	System.out.println ("Dump "+arrayListName);
	for (int i=0; i < al.size(); i++)
		System.out.println (al.get(i));
}


ArrayList<ArrayList<Integer>> trovaSottogruppiQualificati(ArrayList<String> qualificazioniAL)
{
	ArrayList<ArrayList<Integer>> sottogruppiAL =  new ArrayList<ArrayList<Integer>>(); 
	
	ArrayList<Integer> al = null;
	
	for (int j=0; j < qualificazioniAL.size()-1; j++)
	{
		if (qualificazioniAL.get(j).equals(qualificazioniAL.get(j+1)))
		{
			if (al == null)
			{
				al = new ArrayList<Integer>();
				al.add(j);
			}
			al.add(j+1);
		}
		else
		{
			if (al != null)
			{
				sottogruppiAL.add(al);
				al = null;
			}
		}
		
	}
	if (al != null)
		sottogruppiAL.add(al);

	return sottogruppiAL; 
} // End trovaSottogruppiQualificati



};  // End DoppioniPuliziaAutore
	


