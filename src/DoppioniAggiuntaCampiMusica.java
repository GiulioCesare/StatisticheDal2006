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


class DoppioniAggiuntaCampiMusica {
//	boolean filtraEditore = true; 

	
//	class GruppoDuplicati{
//		ArrayList<String> editori = new ArrayList<String>(); 
//		ArrayList<String> records = new ArrayList<String>(); 
//	}
	
//	ArrayList<GruppoDuplicati> gruppoDuplicatiAL = new ArrayList<GruppoDuplicati>();
	
	
	String ar[];
	String musicaAr[];
	BufferedReader in = null;
    BufferedWriter out = null;

//    ArrayList<String> doppioniAL = new ArrayList<String>(); // Elenco doppioni escluso test editore
//    ArrayList<String[]> doppioniAL = new ArrayList<String []>(); // Elenco doppioni escluso test editore

    int	id_gruppo = 0;
    
//	String sortedStopWords[] = {
//			"BY",
//			"CASA",
//			"CO",
//			"COMPANY",
//			"COOP",
//			"DISTRIBUTED",
//			"DISTRIBUTORE",
//			"ED",
//			"EDITEUR",
//			"EDITIONS",
//			"EDITOR",
//			"EDITORE",
//			"EDITRICE",
//			"EDIZIONI",
//			"HERAUSGEBER",
//			"IL",
//			"PUBLISHED",
//			"PUBLISHER",
//			"PUBLISHING",
//			"SOC",
//			"SOCIETA",
//			"TIP",
//			"TIPOGRAFIA",
//			"VERLAG",
//			"VERLAG HAUS",
//			"VERLEGER"			
//			
//	};
    
       
    
	RandomAccessFile  	tbMusicaIn = null;	// tr_tit_bib.out.bytes.srt
    MappedByteBuffer 	tbMusicaOffsetIn = null;	// tr_tit_bib.out.bytes.srt.off
	long fileOffetLength;
	long bidsInTrTitBib=0;
	long bidNonTrovatiCtr=0;
    
    
    
    
//  String lastRecord = "                                                  �          �    �    �    "; // Non comprende l'editore
//    String lastRecord = "                                                  |          |    |    |    "; // Non comprende l'editore
//	String lastEditore = ""; 
	

	int rowCtr = 0;
	int droppedCtr=0;
	int writtenCtr=0;
//	int lastRecordCtr=1;
	
	
	
	enum Fields {
		cles1_2,
		bid,
		vid,
		cd_forma_1,
		numero_ordine,
		numero_opera,
		numero_cat_tem,
		cd_tonalita,
//		// Info da tb musica
//		organico_sintetico,
//		organico_analitico
	};	
	
	enum musicaFields {
		bid,
		cd_livello,
		ds_org_sint,
		ds_org_anal,
		tp_elaborazione,
		cd_stesura,
		fl_composito,
		fl_palinsesto,
		datazione,
		cd_presentazione,
		cd_materia,
		ds_illustrazioni,
		notazione_musicale,
		ds_legatura,
		ds_conservazione,
		tp_testo_letter,
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
	        System.out.println("Uso: DoppioniAggiuntaCampiMusica filenameIn filenameOut nonefileMusica nomefileMusicaOffset"); 
	        System.exit(1);
	    }
	    
//		logFileOut = args[2];
	    
	    String start="DoppioniAggiuntaCampiMusica tool - (C) Almaviva S.p.A 2013"+
		 "\n=========================================================="+
		 "\nTool di aggiunta campi a doppioni Musica"
		 ;

	    System.out.println(start);
		
	    DoppioniAggiuntaCampiMusica doppioniAggiuntaCampiMusica = new DoppioniAggiuntaCampiMusica(args);
	    doppioniAggiuntaCampiMusica.run();
	    
	    System.exit(0);
	}// End main		
		

	public


	String doppioniFilenameIn;
	String doppioniFilenameOut;
	String tbMusicaFilenameIn;
	String tbMusicaOffsetFilenameIn;
    
	char charSepArray[] = { 0x01 }; // '�', C0 '|' 
	
//	String  sepOut = "�"; // "|"
	char  sepOut = 0x01; 
	
	String oldBid="";
	String oldPolo="";

	
	
    BufferedWriter OutLog;
		
    
    
    DoppioniAggiuntaCampiMusica (String[] args)
	{
	    doppioniFilenameIn = args[0];
	    doppioniFilenameOut = args[1];
	    tbMusicaFilenameIn = args[2];
	    tbMusicaOffsetFilenameIn = args[3];
	    
	    System.out.println("doppioniFilenameIn " + doppioniFilenameIn);
	    System.out.println("doppioniFilenameOut " + doppioniFilenameOut);
	    System.out.println("tbMusicaFilenameIn " + tbMusicaFilenameIn);
	    System.out.println("tbMusicaOffsetFilenameIn " + tbMusicaOffsetFilenameIn);
	    
	}
	
	void run()
	{
		String s="";
		//ConfigTable configTable=null;
		
		int state = 0;
		
		
		try {
			in = new BufferedReader(new FileReader(doppioniFilenameIn));
			out = new BufferedWriter(new FileWriter(doppioniFilenameOut));
			
			fileOffetLength = new File(tbMusicaOffsetFilenameIn).length();
			bidsInTrTitBib = fileOffetLength/22; 
			
			tbMusicaIn = new RandomAccessFile(tbMusicaFilenameIn, "r");
			tbMusicaOffsetIn = new FileInputStream(tbMusicaOffsetFilenameIn).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileOffetLength);

			
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
					tbMusicaIn.close();	
					
					
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
	    long upto  = bidsInTrTitBib;
	    long returnOffset = -1;
	    String s, bid, offset;
	    int positionTo;
	    
	    while (first < upto) {
	        long mid = (first + upto) / 2;  // Compute mid point.

	        positionTo = (int)mid*22;
	    	tbMusicaOffsetIn.position(positionTo);
	    	tbMusicaOffsetIn.get(keyOffsetTitolo, 0, 21);			
	        
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
					tbMusicaIn.seek(offset);
//				String s;
//				while (true)
//				{
					String s = tbMusicaIn.readLine();
//	System.out.println("bid="+bid+" offset="+offset+" "+s);
//	if (bid.equals("CMP0196503"))
//		System.out.println("break");
					
//					if (s == null || !s.startsWith(bid))
//						break;
					//System.out.println(t);
					//localizzzazioniCtr++;
					
//				}

				musicaAr = MiscString.estraiCampi(s, stringSepArray, MiscStringTokenizer.RETURN_EMPTY_TOKENS_FALSE); //  " "

			}
			
//			out.write(lastRecord + "\n");
			String oustString =	//id_gruppo + sepOut +//"�"
							  ar[Fields.cles1_2.ordinal()] + sepOut 
							+ ar[Fields.bid.ordinal()] + sepOut	// 
							+ ar[Fields.vid.ordinal()] + sepOut
							+ ar[Fields.cd_forma_1.ordinal()] + sepOut 
							+ ar[Fields.numero_ordine.ordinal()] + sepOut 
							+ ar[Fields.numero_opera.ordinal()] + sepOut 
							+ ar[Fields.numero_cat_tem.ordinal()] + sepOut 
							+ ar[Fields.cd_tonalita.ordinal()] + sepOut 
							+ musicaAr[musicaFields.ds_org_sint.ordinal()] + sepOut 
							+ musicaAr[musicaFields.ds_org_anal.ordinal()] + sepOut 
							
//							+  "CURRENT_TIMESTAMP" // TS_INS
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
	


