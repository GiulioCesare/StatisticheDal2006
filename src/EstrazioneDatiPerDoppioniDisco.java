import it.finsiel.misc.Misc;
import it.finsiel.misc.MiscString;
import it.finsiel.misc.MiscStringTokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import DoppioniAutoriResp1Disco.Fields;



public class EstrazioneDatiPerDoppioniDisco {
	ArrayList<Pattern> patternList = new ArrayList<Pattern>(); 

    String tbTitoloFilename;
	String filenameOut;

	BufferedReader tbTitoloIn = null;
	BufferedWriter fileOut = null;
	
	char charSepArray[] = { 0x01 }; //'�' C0 '�' 
	char charSepArray2[] = {  }; // C0
//	char charSepOut = '|';
	char charSepOut = 0x01; //'�';
	
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
	

	



	EstrazioneDatiPerDoppioniDisco (String[] args)
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
		String volume = ""; 
		
		compilePatterns();		
		
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
						{
							System.out.println("\nLetti " + rowCtr + " record");
							System.out.println("Scritti " + (rowCtr-droppedCtr) + " record");
							System.out.println("Scartati " + droppedCtr + " record");
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
						
						// E' una natura valida?
						char natura = ar[Fields.cd_natura.ordinal()].charAt(0);
						if (natura == 'W')
						{
							deletedCtr++;
							continue;
						}
							
						
						char tipoRecord = '?';
						
						String tpRecord = ar[Fields.tp_record_uni.ordinal()];
						
						if (tpRecord != null && tpRecord.length() > 0) 
							tipoRecord = tpRecord.charAt(0);
						
//						if ( (natura == 'M' || natura == 'W') && (tipoRecord == 'g' || tipoRecord == 'j' || tipoRecord == 'i'))
						if ( (natura == 'M' ) && (tipoRecord == 'g' || tipoRecord == 'j' || tipoRecord == 'i')) // Rimossa natura W (riunione del 02/09/2015)
						{
							
							// E' una data valida?
							if (ar[Fields.aa_pubb_1.ordinal()].length() == 0 || ar[Fields.aa_pubb_1.ordinal()].charAt(0) == ' ')
							{
								droppedCtr++;
								continue;
							}

						volume = getVolume(ar[Fields.isbd.ordinal()]);	
						if (volume.length() > 0)
						{
							droppedCtr++;
							continue;
						}
//						else
//							System.out.println ("Volume: " + volume);
						
						
							String editore = "";
							// Prendiamo l'area di pubblicazione
							String indiceAree = ar[Fields.indice_isbd.ordinal()];
							int areaPub = indiceAree.indexOf("210-");
							if (areaPub != -1)
							{
								int pos = Integer.parseInt(indiceAree.substring(areaPub+4, areaPub+4+4));
								
								int posNextArea;
								
								// Abbiamo un-altra area dopo la 210
								if (indiceAree.length() > areaPub + 9)
									posNextArea = Integer.parseInt(indiceAree.substring(areaPub+4+9, areaPub+4+9+4));
								else
									posNextArea = ar[Fields.isbd.ordinal()].length();
								
								if (posNextArea > ar[Fields.isbd.ordinal()].length() 
									|| posNextArea < 0 // 15/07/13 
									)
								{
									String errore = "Errore in indice aree per editore del bid " + ar[Fields.bid.ordinal()];  
									//editore = errore;
									System.out.println(errore);
									
								}
								else
								{
									if (pos -1 < 0 || posNextArea  < 0)
									{
										String errore = "Errore in indice aree per editore del bid " + ar[Fields.bid.ordinal()];  
										//editore = errore;
										System.out.println(errore);
									}
									else
									{
										if (posNextArea <=  pos-1)
										{
											String errore = "Errore in posNextArea: " + posNextArea + " pos-1=" +(pos-1);   
											//editore = errore;
											System.out.println(errore);
										}
										else
										{
											String areaPubblicazione = ar[Fields.isbd.ordinal()].substring(pos-1, posNextArea);
											// Troviamo ora l'editore
											int areaEditore = areaPubblicazione.indexOf(":");
											editore = areaPubblicazione.substring(areaEditore+1);
											// Ripuliamo dati dopo editore

											if (editore.indexOf(",") != -1) // data pubblicazione
												editore = editore.substring(0, editore.indexOf(","));
											else 
											if (editore.indexOf("[") != -1) // funzione distributore
												editore = editore.substring(0, editore.indexOf("["));
											else 
											if (editore.indexOf("(") != -1) // luogo di stampa
												editore = editore.substring(0, editore.indexOf("("));
											
										}
									}
									
								}
								
							}
							
							
							// Scriviamo il record
							fileOut.write(
									ar[Fields.ky_cles1_t.ordinal()] + ar[Fields.ky_cles2_t.ordinal()] + charSepOut 
									+ ar[Fields.cd_livello.ordinal()] + charSepOut 
									+ ar[Fields.aa_pubb_1.ordinal()] + charSepOut 
									+ ar[Fields.bid.ordinal()] + charSepOut 
									+ ar[Fields.cd_natura.ordinal()] + charSepOut 
									+ volume  + charSepOut 
									+ editore  + charSepOut 
									+ ar[Fields.isbd.ordinal()]  + charSepOut 
									+ ar[Fields.tp_record_uni.ordinal()] // 03/09/2015 per filtraggio
											
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
	System.out.println("Scartati " + droppedCtr + " record");
	System.out.println("Cancellati " + deletedCtr + " record");
	System.out.println("Fine ");
	
	
	
	} // End run
		

	
















String getTestVolume(String isbd)
{

		ArrayList<Pattern> patternList = new ArrayList<Pattern>(); 
		String volume = "";

		

//		patternList.add(Pattern.compile("^\\d+"));
//		patternList.add(Pattern.compile("^\\d+"));
//		patternList.add(Pattern.compile("^\\d+"));
//		patternList.add(Pattern.compile("^\\d+"));
//		patternList.add(Pattern.compile("^\\d+"));

		for (Pattern pattern : patternList) {
	      	  Matcher matcher = pattern.matcher(isbd);
	         if(matcher.find())
	         {
	     		System.out.println (isbd);
	     		break;
//	        	volume = matcher.group(0);
//	     		System.out.println (volume);
	         }
	        }

		return volume;
} // End getTestVolume

/*	
1 / music by Marco Tutino. - \S.l.! : ASdisc, p1991. - 1 compact disc (ca 46 min.) : DDD ; 12 cm. ((NE: ASdisc AS 5014

1: *Music of the later Middle Ages for Court and Church / Gothic voices \int.! ; with Pavlo Beznosiuk, medieval fiddle ; Christopher Page, director. - London : Hyperion, p1994. - 1 compact disc (62'40 min.) : DDD, stereo ; 12 cm + 1 fasc. programma (23 p). ((Fasc. programma in ingl., franc., ted. - Contiene: Part 1, The 14th and 15th centuries . Part 2., The 12th and 13th centuries. - NE: Hyperion CDA66739.
1 / Ockeghem. - London : Decca, \dopo il 1982!. - 1 compact disc (ca 63 min.) : DDD, stereo ; 12 cm. ((NE: L'Oiseau Lyre 436195-2.
*/
	

/**
 * @param args
 */
public static void main(String[] args) {
	// 
	
	
	
	if(args.length < 2)
    {
        System.out.println("Uso: EstrazioneDatiPerDoppioni tbTitoloFilenameIN tbTitoloFilenameOut"); 
        System.exit(1);
    }
    
//	logFileOut = args[2];
    
    String start="EstrazioneDatiPerDoppioni tool - (c) Almaviva S.p.A 2011-2020"+
	 "\n====================================="+
	 "\nTool di estrazione dati per la gestione dei doppioni (fusioni)";

    System.out.println(start);

    EstrazioneDatiPerDoppioniDisco estrazioneDatiPerDoppioniDisco = new EstrazioneDatiPerDoppioniDisco(args);
    
    estrazioneDatiPerDoppioniDisco.run();
    

//    System.out.println("Righe elaborate: " + Integer.toString(rowCounter));
    System.exit(0);
	
	
} // End main


/*

Common matching symbols
======================= 
. 			Matches any character
^regex 		Finds regex that must match at the beginning of the line.
regex$ 		Finds regex that must match at the end of the line.
[abc] 		Set definition, can match the letter a or b or c.
[abc][vz] 	Set definition, can match a or b or c followed by either v or z.
[^abc] 	When a caret appears as the first character inside square brackets, it negates the pattern. This ccontent/an match any character except a or b or c.
[a-d1-7] 	Ranges: matches a letter between a and d and figures from 1 to 7, but not d1.
X|Z 		Finds X or Z.
XZ 			Finds X directly followed by Z.
$ 			Checks if a line end follows.

Meta characters 
===============
\d 	Any digit, short for [0-9]
\D 	A non-digit, short for [^0-9]
\s 	A whitespace character, short for [ \t\n\x0b\r\f]
\S 	A non-whitespace character, short for [^\s]
\w 	A word character, short for [a-zA-Z_0-9]
\W 	A non-word character [^\w]
\S+ 	Several non-whitespace characters
\b 	Matches a word boundary where a word character is [a-zA-Z0-9_]. 

Quantifier
==========
* 		Occurs zero or more times, is short for {0,} 	X* finds no or several letter X,
.* 		finds any character sequence
+ 		Occurs one or more times, is short for {1,} 	X+ - Finds one or several letter X
? 		Occurs no or one times, ? is short for {0,1}. 	X? finds no or exactly one letter X
{X} 	Occurs X number of times, {} describes the order of the preceding liberal 	\d{3} searches for three digits, .{10} for any character sequence of length 10.
{X,Y} 	Occurs between X and Y times, 	\d{1,4} means \d must occur at least once and at a maximum of four.
*? 	? 	after a quantifier makes it a reluctant quantifier. It tries to find the smallest match. This makes the regular expression stop at the first match. 	 

Grouping and Backreference 
==========================
You can group parts of your regular expression. In your pattern you group elements with round brackets, e.g., (). 
This allows you to assign a repetition operator to a complete group. 
 */
	
String getVolume(String isbd)
{
// isbd = "Napoletana : Antologia cronologica della canzone partenopea : dal 1820 al 1880 : vol. 3 / Roberto Murolo. - Milano : DURIUM, 1963 . - 1 Disco sonoro : 33 1/3 rpm, Elettrica/analogica, Mono ; 12 in. (30 cm.). ((Note ill. e testi in cop.";
	String volume = "";
	
	for (Pattern pattern : patternList) {
	      	  Matcher matcher = pattern.matcher(isbd);
		         if(matcher.find())
		         {
		        	volume = matcher.group(0);
		        	break;
		         }
	        }
	return volume;
} // End getVolume
	

void compilePatterns()
{
	
	  
patternList.add(Pattern.compile("^\\\\[0-9]+\\!:"));
patternList.add(Pattern.compile("^°[0-9]+\\!:"));
patternList.add(Pattern.compile("^\\d+\\."));
patternList.add(Pattern.compile("^ *\\d+ *\\/"));// 18/04/15  4 / Jovanotti, Enrico Ruggeri,
patternList.add(Pattern.compile("^\\d+ *:"));	
patternList.add(Pattern.compile("^\\d+\\.\\d+ +/"));	
patternList.add(Pattern.compile("^\\d+\\.\\d+:"));
patternList.add(Pattern.compile("^\\d+ [a-zA-Z]+ *\\*")); // 18/04/15 11 I *Capuleti e i Montecchi
patternList.add(Pattern.compile("^\\d+\\-\\d+ */"));	
patternList.add(Pattern.compile("^\\d+\\-\\d+ *:"));	
patternList.add(Pattern.compile("^\\d+\\-\\d+\\."));//	18/04/15 1-2. - Novara : De Agostini junior, c2000
patternList.add(Pattern.compile("^\\d+ +\\-"));
patternList.add(Pattern.compile("^\\d+ +\\. +\\-"));
patternList.add(Pattern.compile("^Act \\d+"));
patternList.add(Pattern.compile("^Vol\\. \\d+\\."));


patternList.add(Pattern.compile("^Compact disc \\d+"));
patternList.add(Pattern.compile("^\\*\\d+ /"));
patternList.add(Pattern.compile("^\\*\\d+ :"));
patternList.add(Pattern.compile("^\\*\\d+\\. *\\-"));
patternList.add(Pattern.compile("^\\*\\[\\d+\\]\\. *\\-"));
patternList.add(Pattern.compile("^\\*\\d+\\. +puntata"));// 18/04/15 *1. puntata. - 1 DVD (70 min,) : b/n, son.
patternList.add(Pattern.compile("^\\[[\\d+\\-]+\\]"));// 18/04/15 [3-4]: Parte 3.a ; conclus. / R. Strauss.
patternList.add(Pattern.compile("^\\[\\d+\\!"));	// 18/04/15 [1!. - Milano : Twentieth century fox home

patternList.add(Pattern.compile("^\\: disc +[A-Z],", Pattern.CASE_INSENSITIVE));// 20/04/15 : disc A, Chicago 1926. - London :	
patternList.add(Pattern.compile("^Disc +[a-zA-Z]+", Pattern.CASE_INSENSITIVE));// 18/04/15 Disc four. - [S.n.! : Sony music, c
patternList.add(Pattern.compile("^Disc +\\d+", Pattern.CASE_INSENSITIVE));
patternList.add(Pattern.compile("^Dischi [0-9]+ [e&] [0-9]+", Pattern.CASE_INSENSITIVE)); // 18/04/15 Dischi 1 e 2. - Italia : Universal Studios, c2008
patternList.add(Pattern.compile("^Disque \\d+"));// 18/04/15 Disque 1

				// Vol.13. - Hamburg : Teldec, c1998. - 1 compact dis
		// 18/04/15 vol. 16 / regia di Daisuke Nishio ; tratto da
patternList.add(Pattern.compile("^[a-zA-Z]+ *stagione\\."));
patternList.add(Pattern.compile("^Parte? \\d+"));	// 18/04/15 Part 1 /
			// 18/04/15 Parte 1. - [Roma] : Fonit Cet	 	
patternList.add(Pattern.compile("^/\\d+\\!")); // 18/04/15 /1!: *Atto 1 : 1. parte : conclusione. - [Italia! : 	
patternList.add(Pattern.compile("^Cassette \\d+"));
patternList.add(Pattern.compile("^n\\. *\\d+")); // 18/04/15 n. 4 / Mina. - [Italy] : EMI, [2001] 	
patternList.add(Pattern.compile("^Folgen [\\d\\-]+")); // 18/04/15 Folgen 6-10. - [Bonn] : Inter Nationes ; 	
patternList.add(Pattern.compile("^Units [\\d\\-]+")); // 18/04/15 Units 13-18. - Novara : De Agostini, c1991. - 1 audiocassetta  (ca 1 ora).
patternList.add(Pattern.compile("^\\*A\\d+ \\: CD \\d+"));// 19/04/15 *A1 : CD 1. Unita 1-6 / Maurizi	
patternList.add(Pattern.compile("^Lez\\. [\\d\\-]+")); // 19/04/15 Lez. 17-18: *Analisi del comportamento 	
patternList.add(Pattern.compile("^\\*Anno \\d+"));// 19/04/15 *Anno 1. : l'Appennino come ecosistema. 1: Inaugurazione : giovedì 	
patternList.add(Pattern.compile("^\\[[ 0-9a-z]+\\]"));// 19/04/15 [2 bis] / R. Wagner. - Milano	
patternList.add(Pattern.compile("^\\: disco? +\\d+", Pattern.CASE_INSENSITIVE));// 19/04/15 : disc 1 / directed by Ron Howard	
patternList.add(Pattern.compile("^\\\\[0-9\\.]+\\!"));// 19/04/15 \2.1!: *Atto primo : prima parte, seconda part	
patternList.add(Pattern.compile("^Cloth \\d+")); // 19/04/15 Cloth 1. - Milano : Yamato, 2008. 	
patternList.add(Pattern.compile("^I \\*cd dei Fiati \\: vol\\. \\d+"));// 19/04/15 I *cd dei Fiati : vol. 1 / 	
patternList.add(Pattern.compile("^\\*\\d+\\-\\d+"));// 20/04/15 *1-2 :  July '40 to February '64.  	
patternList.add(Pattern.compile("^\\*[0-9]+ [&] [0-9]+"));// 20/04/15 *3 & 4 :  February '64 to August '65	
patternList.add(Pattern.compile("^\\*DJzone \\: Best Classic [0-9\\.]+", Pattern.CASE_INSENSITIVE)); // 20/04/15  - Italy : 	
patternList.add(Pattern.compile("^\\: corso [0-9\\.]+", Pattern.CASE_INSENSITIVE));// 20/04/15 : corso 1. - Milano : Boroli ; Mon	
patternList.add(Pattern.compile("^Dettati [0-9\\.]+", Pattern.CASE_INSENSITIVE));// 20/04/15 Dettati 1. - Novara : D	
patternList.add(Pattern.compile("^Speciale Italia [0-9\\.]+", Pattern.CASE_INSENSITIVE));// 20/04/15 Speciale Italia 1. - Roma : Editoriale	
patternList.add(Pattern.compile("^\\*Cinema graffiti : grandi temi da grandi film degli anni [0-9\\.]+", Pattern.CASE_INSENSITIVE));// 20/04/15 *Cinema graffiti : grandi temi da grandi film degli anni 30. - [Germania] :	
patternList.add(Pattern.compile("^Atto [0-9\\.]+", Pattern.CASE_INSENSITIVE));// 20/04/15 Atto 1. : / Gaetano Donizetti. - London : 	

patternList.add(Pattern.compile("^Gli \\*anni")); // 21/04/15 Gli *anni d'oro : vol. 2 / Mina. -   	
patternList.add(Pattern.compile("^The \\*animation show of shows : box set \\d+"));// 21/04/15 The *animation show of shows : box set 1.	
patternList.add(Pattern.compile("^L'\\*ape Magà : episodi \\d+")); // 21/04/15 L'*ape Magà : episodi 10	
patternList.add(Pattern.compile("^Quadro \\d+")); // 21/04/15 Quadro 1.-Quadro 2. - Milano : Amadeus 	
patternList.add(Pattern.compile("^Echo \\d+")); // 21/04/15 Echo 1 / tratto dal fumetto di Satoru Ozawa	

//patternList.add(Pattern.compile("^Puntate [0-9 \\-]\\+ \\d+"));
//patternList.add(Pattern.compile("^Puntate [0-9]"));// 18/04/15 Puntate 1-2. - [Roma! : Rai Trade
//patternList.add(Pattern.compile("^Puntata \\d+"));
patternList.add(Pattern.compile("^\\**Puntat[ae] [0-9]", Pattern.CASE_INSENSITIVE));// 21/04/15 *Puntate 3-4. - [Roma] : Rai Tr	




patternList.add(Pattern.compile("^Acte \\d+")); // 21/04/15 Acte 1. / Mozart. - [Paris] : Diapason,	
patternList.add(Pattern.compile("^\\*Grateful Dead: dicks picks : volume [a-zA-Z]+")); // 21/04/15 *Grateful Dead: dicks picks : volume two. - London : Grateful Dead Records, 1995.	
patternList.add(Pattern.compile("^\\*Giorgio Gaber : gli \\*anni [a-zA-Z]+")); // 21/04/15 *Giorgio Gaber : gli *anni Sessanta. - Roma : Radiofandango	
patternList.add(Pattern.compile("^La \\*famiglia Barbapapa : n. [0-9]+")); // 21/04/15 La *famiglia Barbapapa : n. 8 / [creators Talus 	

//patternList.add(Pattern.compile("^\\**Episodio [0-9\\.]+", Pattern.CASE_INSENSITIVE)); // 20/04/15 Episodio 2.20 / guest star: Errol Flynn, 	
//patternList.add(Pattern.compile("^\\*?Episodi[o]* [0-9]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 Episodi 5-8. - Edizione da collezione 	

patternList.add(Pattern.compile("^[\\*:]? ?episodi[o]? [0-9]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 : episodi 1-8. - Milano : Buena vista	

patternList.add(Pattern.compile("^\\d+ \\*Cantiones sacrae quatuor vocum, \\d+ : SWV [0-9-]+ vol. \\d+", Pattern.CASE_INSENSITIVE));// 21/04/15 1 *Cantiones sacrae quatuor vocum, 1625 : SWV 53-93 vol. 1 / Schutz 	
patternList.add(Pattern.compile("^\\\\Cassetta! [1.]+", Pattern.CASE_INSENSITIVE));// 21/04/15 \Cassetta! 1.2. - Sankt-Peterburg : Zlatoust	
patternList.add(Pattern.compile("^\\*Dance invasion : vol\\. [0-9]+", Pattern.CASE_INSENSITIVE));// 21/04/15 *Dance invasion : vol. 51. - [Milano] : Do it yourself, 2008	
patternList.add(Pattern.compile("^\\(episodes [0-9]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 (Episodes 1, 2 & 3) 1. - [Italia] : Home box office	
patternList.add(Pattern.compile("^\\*ER : medici in prima linea. Anno [0-9]+", Pattern.CASE_INSENSITIVE));// 21/04/15 *ER : medici in prima linea. Anno 5 / created by Michael Crichton. 	
patternList.add(Pattern.compile("^Les \\*diapason d'or : [A-Za-z]+ [0-9]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 Les *diapason d'or : Mars 2006 : (extraits). - France 	
patternList.add(Pattern.compile("^\\*Elektro beat : shock [0-9]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 *Elektro beat : shock 18. - [Milano] :	
//patternList.add(Pattern.compile("^\\[[0-9\\.]+\\] *")); // 18/04/15 [1] programmi dall'1-al 5. - Roma : Prel, [2000?
//patternList.add(Pattern.compile("^\\*\\[[0-9\\.]+\\]")); // 21/04/15 *[1]: Film. - Milano : Mondadori [distributore
patternList.add(Pattern.compile("^\\*?\\[[0-9\\.]+\\]")); // 21/04/15 [1.1]. - La Habana : Egrem, dopo il 1964. - 1 disco sonoro	
patternList.add(Pattern.compile("^\\*DJzone : Best Session \\d+")); // 21/04/15 *DJzone : Best Session 01. - Italy : Time, 	
patternList.add(Pattern.compile("^\\*Friends : l[ 'a-z]+ stagione completa", Pattern.CASE_INSENSITIVE)); // 21/04/15 *Friends : la settima stagione completa 	
patternList.add(Pattern.compile("^Pack \\d+")); // 21/04/15 Pack 1. - Tres Cantos : Metrovideo, 1994.	
patternList.add(Pattern.compile("^I \\*grandi successi"));	// 21/04/15 I *grandi successi degli Anni 90 : vol. 1. - [Milano] :	
				// 21/04/15 I *grandi successi / Luigi Tenco. - [	
patternList.add(Pattern.compile("^: disc [a-z]+")); // 21/04/15 : disc one (episode 1, 2, 3). - [Italia]	

patternList.add(Pattern.compile("^Vol\\. *\\d+", Pattern.CASE_INSENSITIVE));// 18/04/15  Vol. 1: 1947-1952. - [Italia] : Warner Music, 2006. - 1 compact disc ; 12 cm.
//patternList.add(Pattern.compile("^Volume \\d+")); // Volume 2. - Bresso : Hobby & Work, c2001. - 1 compact disc : stereo, DDD ; 12 cm. ((Contiene:
patternList.add(Pattern.compile("^\\**Volume \\d+", Pattern.CASE_INSENSITIVE)); // 21/04/15 *Volume 1. - Bresso : Hobby & Work,	

//patternList.add(Pattern.compile("^C[dD] \\d+ *\\."));
//patternList.add(Pattern.compile("^C[dD] \\d+ *\\:"));
//patternList.add(Pattern.compile("^C[dD] \\d+ */"));

//patternList.add(Pattern.compile("^cd *\\d+", Pattern.CASE_INSENSITIVE));// 18/04/15 CD1 : act one (1). - Hamburg :
patternList.add(Pattern.compile("^cd \\d+\\-\\d+\\.", Pattern.CASE_INSENSITIVE));
patternList.add(Pattern.compile("^cd [a-zA-Z]+ *\\., Pattern.CASE_INSENSITIVE"));
patternList.add(Pattern.compile("^cd\\-audio \\d+ *\\."));// 18/04/15 cd-audio 1. - Novara : De Agostini,
patternList.add(Pattern.compile("^:* *cd *\\d+", Pattern.CASE_INSENSITIVE));// 22/04/15 : CD 1 / Ray Charles. - Burbank :


patternList.add(Pattern.compile("^Dexter : [l 'a-z]+ stagione", Pattern.CASE_INSENSITIVE));// 2/04/15 Dexter : quarta stagione : dischi 1 & 2 / [developed 
												// 22/04/15 Dexter : la quinta stagione : dischi 1 & 2 / [developed	
patternList.add(Pattern.compile("^<<Il mio amico cane>> \\d+")); // 22/04/15 <<Il mio amico cane>> 1. - Milano : Cinehollywood
patternList.add(Pattern.compile("^The \\*masterpieces")); // 22/04/15 The *masterpieces : *sonate A-dur
patternList.add(Pattern.compile("^\\*Mai dire gol")); // 22/04/15 *Mai dire gol :  uno sport, un perché /  regia 
patternList.add(Pattern.compile("^\\*Mai dire Grande Fratello")); // 22/04/15 *Mai dire Grande Fratello : le origini / regia e montaggio
patternList.add(Pattern.compile("^\\*Matematica ")); // 22/04/15 *Matematica : I anno liceo scientifico : 2 / regia F. Caravello

//patternList.add(Pattern.compile("^Disco [a-z]+ *\\. *\\-")); // 18/04/15 Disco ventuno. -
//patternList.add(Pattern.compile("^disco [a-z]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 Disco uno : episodi 1-4. - Milano: Twentyeth C-
//patternList.add(Pattern.compile("^\\*Disc \\d+ */"));// 18/04/15  DISC 1 / Ludwig van Beethoven ; 
//patternList.add(Pattern.compile("^\\*Disc \\d+ *\\:"));
//patternList.add(Pattern.compile("^Disco \\d+"));
patternList.add(Pattern.compile("^disc[ohi]+ [a-z]+", Pattern.CASE_INSENSITIVE)); // 21/04/15 Dischi uno e due. - Milano : Twentieth Century Fox, 2008. - 2
patternList.add(Pattern.compile("^\\**disco* [0-9]+", Pattern.CASE_INSENSITIVE)); // 22/04/15 disco 2 / Rumiko Takahashi. - Milano :


patternList.add(Pattern.compile("^Il \\*mondo di Patty")); // 22/04/15 Il *mondo di Patty : la storia più bella.
patternList.add(Pattern.compile("^\\*Mare e Miniere Duemila7")); // 22/04/15 *Mare e Miniere Duemila7 : *"Passione liberoamericana"
patternList.add(Pattern.compile("^Il \\*maresciallo Rocca")); // 22/04/15 Il *maresciallo Rocca : episodio 1-2 / regia Giorgio
patternList.add(Pattern.compile("^\\*Maria Callas")); // 22/04/15 *Maria Callas. - \Italia! : Fonit Cetra, ℗
patternList.add(Pattern.compile("^\\*Messico")); // 22/04/15 *Messico : Le citta' coloniali. - Città del 
patternList.add(Pattern.compile("^\\*Pimpa \\[film\\]")); // 22/04/15 *Pimpa [film] : Bombo ippopotamo e l

//patternList.add(Pattern.compile("^Dvd \\d+", Pattern.CASE_INSENSITIVE)); // 18/04/15 dvd 4. - Novara : De Agostini, 2008
patternList.add(Pattern.compile("^DVD n\\. +\\d+", Pattern.CASE_INSENSITIVE));// 18/04/15 DVD n. 1 / Bach. - Hamburg :
patternList.add(Pattern.compile("^\\[*dvd \\d+\\]*", Pattern.CASE_INSENSITIVE)); // 22/04/15 [DVD 1] / regia di Luigi Perelli. - [Roma] :

patternList.add(Pattern.compile("^\\*Orchestral Works : volume \\d+")); // 22/04/15 *Orchestral Works : volume 1 / Elisabet
patternList.add(Pattern.compile("^\\*Patlabor")); // 22/04/15 *Patlabor : VS (versus) / regia Naoyuki Yosh
patternList.add(Pattern.compile("^Le \\*piu belle canzoni")); // 22/04/15 Le *piu belle canzoni / Franco Califano. 
patternList.add(Pattern.compile("^The \\*Platinum collection")); // 22/04/15 The *Platinum collection / The Beach Boys. - [S. l.! : E
patternList.add(Pattern.compile("^L'\\*olocausto")); // 22/04/15 L'*olocausto : l'incendio divampa nel ghetto. - [B
patternList.add(Pattern.compile("^\\*Sex and the city")); // 22/04/15 *Sex and the city : stagione 2. - [Italia] 
patternList.add(Pattern.compile("^*Prison break : stagione \\d+")); // 22/04/15 *Prison break : stagione 3. - [Milano] : T

patternList.add(Pattern.compile("[dD]al [0-9]+ al [0-9]+ \\: vol\\. [0-9]+")); // 04/09/15
patternList.add(Pattern.compile("meglio di \\: vol\\. [0-9]+")); // 04/09/15
patternList.add(Pattern.compile("supercompilation \\: vol\\. [0-9]+")); // 04/09/15

patternList.add(Pattern.compile("\\: disco n\\. [0-9]+ ")); // 04/09/15
//	patternList.add(Pattern.compile("^")); // 22/04/15 
//	patternList.add(Pattern.compile("^")); // 22/04/15 
	
	
	
} // End compilePatterns()




} // End EstrazioneDatiPerDoppioniDisco
