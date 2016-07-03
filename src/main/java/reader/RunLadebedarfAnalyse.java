package reader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.crypto.Data;

/**
 * 
 * @author kathrinmaier
 *
 */
public class RunLadebedarfAnalyse {
	
	final static int QUANTIL = 21;
	final static int P = 11;
	
	final int DifferenzQuantil1 = 1;
	final int DifferenzQuantil2 = 5;

	private List<Standort> standorte;
	private Map<String, ArrayList<Long>> standzeitenProZone;
	private Map<String, ArrayList<Standort>> standorteProZone;
	private Map<String, Double> mittlereStandzeitProZone;

	private Map<String, Double> mittlereStandzeitProZoneBereinigt;

	private Map<String, Double> mittlereAnzahlFzProZone;

	private Map<String, Double> mittlereAnzahlFzProZoneBereinigt;

	private Map<String, Double> ladebedarfProZoneGewichtet;
	private Map<String, Double> ladebedarfProZoneUngewichtet;
	private Map<String, Double> ladebedarfsDifferenz;

	/**
	 * Default Constructor Initialisieren aller Listen und Maps.
	 */
	public RunLadebedarfAnalyse() {
		standorteProZone = new LinkedHashMap<String, ArrayList<Standort>>();
		mittlereStandzeitProZone = new LinkedHashMap<String, Double>();
		mittlereAnzahlFzProZone = new LinkedHashMap<String, Double>();
		ladebedarfProZoneGewichtet = new LinkedHashMap<String, Double>();
		ladebedarfProZoneUngewichtet = new LinkedHashMap<String, Double>();

		mittlereStandzeitProZoneBereinigt = new LinkedHashMap<String, Double>();
		mittlereAnzahlFzProZoneBereinigt = new LinkedHashMap<String, Double>();

	}

	public static void main(String[] args) {
		new RunLadebedarfAnalyse().run();
	}

	/**
	 * Run enthält das Auslesen, Auswerten und speichern der Analyseergebnisse
	 */
	void run() {
		ReadData reader = new ReadData();
		this.standorte = reader.getStandorte();
		this.standzeitenProZone = reader.getStandzeitenProZone();

		runAnalysis(reader);
		//standortStandzeitRatio();
		write();
	}

	/**
	 * Starten aller Analysemethoden
	 * 
	 * @param reader
	 *            Zum Übergeben des Pointers an AnzahlFzProZeitintervallProZone
	 */
	void runAnalysis(ReadData reader) {
		System.out.println("---Starte Analyse---");

		System.out.println("Berechne mittlere Standzeiten pro Zone...");
		mittlereStandzeitProZone = DataAnalysis.berechneMittlereStandzeitProZone(standzeitenProZone);
		System.out.println("Berechne Standorte pro Zone...");
		standorteProZone = DataAnalysis.berechneStandorteProZone(standorte, mittlereStandzeitProZone);

		System.out.println("Berechne Anzahl Fahrzeuge pro Zeitintervall pro Zone...");
		Map<String, short[]> a = DataAnalysis.berechneAnzahlFzProZeitintervallProZone(reader, standorteProZone);
		System.out.println("Berechne mittlere Anzahl Fahrzeuge pro Zone...");
		mittlereAnzahlFzProZone = DataAnalysis.berechneMittlereAnzahlFzProZone(a);
		System.out.println("Berechne Ladebedarf pro Zone...");
		// GEWICHTET
		ladebedarfProZoneGewichtet = DataAnalysis.berechneLadebedarfProZoneGewichtet(mittlereAnzahlFzProZone,
				mittlereStandzeitProZone,P, QUANTIL);
		ladebedarfProZoneGewichtet = DataAnalysis.sortByValue(ladebedarfProZoneGewichtet);
		System.out.println("mittlerer Ladebedarf Gewichtet bei Quantil = " + QUANTIL + " : "
				+ DataAnalysis.computeMean(ladebedarfProZoneGewichtet) + " kWh");
		System.out.println("gesamter Ladebedarf Gewichtet bei Quantil = " + QUANTIL + " : "
				+ DataAnalysis.computeSum(ladebedarfProZoneGewichtet) + " kWh");
		// UNGEWICHTET
		ladebedarfProZoneUngewichtet = DataAnalysis.berechneLadebedarfProZoneUngewichtet(mittlereAnzahlFzProZone,
				mittlereStandzeitProZone,P);
		ladebedarfProZoneUngewichtet = DataAnalysis.sortByValue(ladebedarfProZoneUngewichtet);
		System.out.println("mittlerer Ladebedarf Ungewichtet bei Quantil = " + QUANTIL + " : "
				+ DataAnalysis.computeMean(ladebedarfProZoneUngewichtet) + " kWh");
		System.out.println("gesamter Ladebedarf Ungewichtet bei Quantil = " + QUANTIL + " : "
				+ DataAnalysis.computeSum(ladebedarfProZoneUngewichtet) + " kWh");
		//DIFFERENZBERECHNUNG

		ladebedarfsDifferenz = DataAnalysis.berechneDifferenzenImLadebedarf(mittlereAnzahlFzProZone,
				mittlereStandzeitProZone,P, DifferenzQuantil1, DifferenzQuantil2);
		
	}

	/**
	 * Ausleiten der in der Analyse generierten Daten in csv Dateien.
	 */
	void write() {
		System.out.println("---Starte Schreibvorgang---");

		System.out.println("Schreibe Datei 'mittlereStandzeitenProZone.csv'...");
		WriteData.writeMittlereStandzeitenProZone(
				"/Users/kathrinmaier/Desktop/e-carsharing/mittlereStandzeitenProZone.csv", mittlereStandzeitProZone);
		
		System.out.println("Schreibe Datei 'mittlereAnzahlFzProZone.csv'...");
		WriteData.writeMittlereAnzahlFzProZone("/Users/kathrinmaier/Desktop/e-carsharing/mittlereAnzahlFzProZone.csv",
				mittlereAnzahlFzProZone);
		
		System.out.println("Schreibe Datei 'LadebedarfProZone_" + QUANTIL + ".csv'...");
		WriteData.writeLadebedarfProZone(
				"/Users/kathrinmaier/Desktop/e-carsharing/LadebedarfProZone_" + QUANTIL + ".csv",
				ladebedarfProZoneGewichtet);
		
		System.out.println("Schreibe Datei 'LadebedarfProZoneUngewichtet.csv'...");
		WriteData.writeLadebedarfProZone("/Users/kathrinmaier/Desktop/e-carsharing/LadebedarfProZoneUngewichtet.csv",
				ladebedarfProZoneUngewichtet);
		
		System.out.println("Schreibe Datei 'LadebedarfDifferenz_"+DifferenzQuantil1+"-"+DifferenzQuantil2 +".csv'...");
		WriteData.writeLadebedarfProZone("/Users/kathrinmaier/Desktop/e-carsharing/LadebedarfDifferenz_" + 
				DifferenzQuantil1+"-"+DifferenzQuantil2 +".csv",
				ladebedarfsDifferenz);

		WriteData.writeStandorteProZone(standorteProZone);

	}

	void standortStandzeitRatio() {
		bereinigeMaps();
		int alpha = 0;
		int beta = 3;
		Map<String, Double> scoringMap = new LinkedHashMap<String, Double>();
		int steps = 10;
		for (int i = 0; i <= steps; i++) {
			alpha = (i / 5);
			beta = (2 - (2 * (i / steps)));
			scoringMap = DataAnalysis.scoringMethod(scoringMap, mittlereAnzahlFzProZoneBereinigt, mittlereStandzeitProZoneBereinigt,
					alpha, beta);

		}
		scoringMap = DataAnalysis.sortByValue(scoringMap);
		WriteData.writeLadebedarfProZone("/Users/kathrinmaier/Desktop/e-carsharing/Scoring.csv", scoringMap);
	}

	/**
	 * Zusätzliche Maps um ausreiser im unteren bereich zu eliminieren 
	 * 20 Minute Standzeit
	 * 4 Fahrzeug
	 */
	void bereinigeMaps() {
		for (Entry<String, Double> entry : mittlereAnzahlFzProZone.entrySet()) {
			if(entry.getValue() > 4){
				mittlereAnzahlFzProZoneBereinigt.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<String, Double> entry : mittlereStandzeitProZone.entrySet()) {
			if(entry.getValue() > 20){
				mittlereStandzeitProZoneBereinigt.put(entry.getKey(), entry.getValue());
			}
		}
	}

}
