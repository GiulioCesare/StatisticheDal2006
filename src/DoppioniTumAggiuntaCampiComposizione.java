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


class DoppioniTumAggiuntaCampiComposizione {
//	boolean filtraEditore = true; 

	
//	class GruppoDuplicati{
//		ArrayList<String> editori = new ArrayList<String>(); 
//		ArrayList<String> records = new ArrayList<String>(); 
//	}
	
//	ArrayList<GruppoDuplicati> gruppoDuplicatiAL = new ArrayList<GruppoDuplicati>();
	
	
	String ar[];
	String composizioneAr[];
	BufferedReader in = null;
    BufferedWriter out = null;

//    ArrayList<String> doppioniAL = new ArrayList<String>(); // Elenco doppioni escluso test editore
//    ArrayList<String[]> doppioniAL = new ArrayList<String []>(); // Elenco doppioni escluso test editore

    int	id_gruppo = 0;
    
    
       
    
	RandomAccessFile  	tbComposizioneIn = null;	// tr_tit_bib.out.bytes.srt
    MappedByteBuffer 	tbComposizioneOffsetIn = null;	// tr_tit_bib.out.bytes.srt.off
	long fileOffetLength;
	long bidsInTbComposizione=0;
	long bidNonTrovatiCtr=0;
    
    
    
    
//  String lastRecord = "                                                  �          �    �    �    "; // Non comprende l'editore
//    String lastRecord = "                                                  |          |    |    |    "; // Non comprende l'editore
//	String lastEditore = ""; 
	

	int rowCtr = 0;
	int droppedCtr=0;
	int writtenCtr=0;
//	int lastRecordCtr=1;
	
	
	
	enum Fields {
		id_gruppo,
		vid,
		cles1_2,
		bid,
		cd_livello,
		localizzazioni,
		isbd,
		// Info da tb musica
		organico_sintetico,
		organico_analitico

		// Info da tb+composizione
//		cd_forma_1,
//		numero_ordine,
//		numero_opera,
//		numero_cat_tem,
//		cd_tonalita,
	
	};	
	
	enum tbComposizioneFields {
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

	
	public static void main(String[] args) {
		// 

		char charSepArrayEquals[] = { '='};
		char charSepArraySpace[] = { ' '};

		if(args.length < 4)
	    {
	        System.out.println("Uso: DoppioniTumAggiuntaCampiComposizione filenameIn filenameOut nonefileComposizione nomefilenonefileComposizioneOffset"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="DoppioniTumAggiuntaCampiComposizione tool - (C) Almaviva S.p.A 2015-2016"+
	    			 "\n======================================================================"+
	    			 "\nTool di aggiunta campi a doppioni Titoli Unbiformi musicali"
		 ;

	    System.out.println(start);
		
	    DoppioniTumAggiuntaCampiComposizione doppioniTumAggiuntaCampinonefileComposizione = new DoppioniTumAggiuntaCampiComposizione(args);
	    doppioniTumAggiuntaCampinonefileComposizione.run();
	    
	    System.exit(0);
	}// End main		
		

	public


	String doppioniFilenameIn;
	String doppioniFilenameOut;
	String tbComposizioneFilenameIn;
	String tbComposizioneOffsetFilenameIn;
    
	char charSepArray[] = { 0x01 }; // '�' C0 '|' 
	
//	String  sepOut = "�"; // "|"
	char  sepOut = 0x01;
	
	String oldBid="";
	String oldPolo="";

	
	
    BufferedWriter OutLog;
		
    
    
    DoppioniTumAggiuntaCampiComposizione (String[] args)
	{
	    doppioniFilenameIn = args[0];
	    doppioniFilenameOut = args[1];
	    tbComposizioneFilenameIn = args[2];
	    tbComposizioneOffsetFilenameIn = args[3];
	    
	    System.out.println("doppioniFilenameIn " + doppioniFilenameIn);
	    System.out.println("doppioniFilenameOut " + doppioniFilenameOut);
	    System.out.println("tbMusicaFilenameIn " + tbComposizioneFilenameIn);
	    System.out.println("tbMusicaOffsetFilenameIn " + tbComposizioneOffsetFilenameIn);
	    
	}
	
	void run()
	{
		String s="";
		//ConfigTable configTable=null;
		
		int state = 0;
		
		
		try {
			in = new BufferedReader(new FileReader(doppioniFilenameIn));
			out = new BufferedWriter(new FileWriter(doppioniFilenameOut));
			
			fileOffetLength = new File(tbComposizioneOffsetFilenameIn).length();
			bidsInTbComposizione = fileOffetLength/22; 
			
			tbComposizioneIn = new RandomAccessFile(tbComposizioneFilenameIn, "r");
			tbComposizioneOffsetIn = new FileInputStream(tbComposizioneOffsetFilenameIn).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);

			
			
//			String lastRecordAr[] = {"", ""};
//			// lastRecordAr = MiscString.estraiCampi("'�''�''�''�''�''�''�'", charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "
			
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
						
						
						String key = ar[Fields.bid.ordinal()]; //ar[1]+ar[0]; //s.substring(0,62);
//						String keyLast = lastRecordAr[1] + lastRecordAr[0]; //lastRecord.substring(0,62);
//						if (keyLast.equals(key))
//						{
//							doppioniAL.add(lastRecordAr);
//							//editoriAL.add(lastEditore);
//							lastRecordCtr++;
//						
//						}
//						else
//						{	// Cambio chiave
							// Ultimo record era singolo o multiplo?
//							if (lastRecordCtr == 1)
//							{
//								droppedCtr++; // Singolo, droppato
////System.out.println("Chiave univoca" + lastCles);
//							}
//							else
//							{
//								
////								out.write(lastRecord + "\n"); // Scriviamo record precedente in quanto multiplo
////								writtenCtr++;
//								
								writeRecord(key);
//
//								doppioniAL.add(lastRecordAr);
//								//editoriAL.add(lastEditore);
								
//								writeDoppioni();
								
								
//								doppioniAL.clear();
//								//editoriAL.clear();
								
							}
//							lastRecordCtr=1;
//						}						
						//lastRecord = s;
//						lastRecordAr=ar;
						//lastEditore = ar[Fields.editore.ordinal()];
//					}
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
					tbComposizioneIn.close();	
					
					
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
				
				try {
					System.out.println("\n\nInput " + doppioniFilenameIn);
					System.out.println("Output " + doppioniFilenameOut);
					System.out.println("Letti " + rowCtr + " record");
					System.out.println("Scartati  " + droppedCtr + " record");
					System.out.println("Scritti " + writtenCtr + " record");
//					System.out.println("Gruppi " + id_gruppo);
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
	    long upto  = bidsInTbComposizione;
	    long returnOffset = -1;
	    String s, bid, offset;
	    int positionTo;
	    
	    while (first < upto) {
	        long mid = (first + upto) / 2;  // Compute mid point.

	        positionTo = (int)mid*22;
	    	tbComposizioneOffsetIn.position(positionTo);
	    	tbComposizioneOffsetIn.get(keyOffsetTitolo, 0, 21);			
	        
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
	
	
	





	
	
	boolean writeRecord(String key) // record
	{
		
		//String ar[] = MiscString.estraiCampi(record, charSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_TRUE); //  " "

		//int localizzzazioniCtr = 0;
		String stringSepArray[] = { "&$%" };  
		
		// Troviamo le localizzazioni 
		String bid = key; //ar[Fields.bid.ordinal()];
		

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
					tbComposizioneIn.seek(offset);
//				String s;
//				while (true)
//				{
					String s = tbComposizioneIn.readLine();
//	System.out.println("bid="+bid+" offset="+offset+" "+s);
//	if (bid.equals("CMP0196503"))
//		System.out.println("break");
					
//					if (s == null || !s.startsWith(bid))
//						break;
					//System.out.println(t);
					//localizzzazioniCtr++;
					
//				}

				composizioneAr = MiscString.estraiCampi(s, stringSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_FALSE); //  " "

			}
			
//			out.write(lastRecord + "\n");
			String oustString =	//id_gruppo + sepOut +//"�"
							""+ar[Fields.id_gruppo.ordinal()] + sepOut 
							+ ar[Fields.bid.ordinal()] + sepOut	// 
							+ ar[Fields.vid.ordinal()] + sepOut
							+ ar[Fields.cles1_2.ordinal()] + sepOut 
							+ composizioneAr[tbComposizioneFields.cd_forma_1.ordinal()] + sepOut 
							+ composizioneAr[tbComposizioneFields.numero_ordine.ordinal()] + sepOut 
							+ composizioneAr[tbComposizioneFields.numero_opera.ordinal()] + sepOut 
							+ composizioneAr[tbComposizioneFields.numero_cat_tem.ordinal()] + sepOut 
							+ composizioneAr[tbComposizioneFields.cd_tonalita.ordinal()] + sepOut 
							+ ar[Fields.organico_sintetico.ordinal()] + sepOut 
							+ ar[Fields.organico_analitico.ordinal()] + sepOut 
							+ ar[Fields.cd_livello.ordinal()] + sepOut	// 
							+ ar[Fields.localizzazioni.ordinal()] + sepOut	// 
							+ ar[Fields.isbd.ordinal()] + sepOut	// 
							+  "CURRENT_TIMESTAMP" // TS_INS
							+ "\n";

			
			out.write(oustString);
			
		} catch (IOException e) {
			// 
			e.printStackTrace();
		} // Scriviamo record precedente`
	 catch (Exception e) {
		// 
		e.printStackTrace();
	} // Scriviamo record precedente`
		
		writtenCtr++;
//		lastRecordCtr++;
		
		
		return true;
	} // End writeRecord




};  // End Statistiche2006
	


