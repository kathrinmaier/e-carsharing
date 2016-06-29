package reader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author kathrinmaier
 *
 */
public class RunLadebedarfAnalyse {

	private List<Standort> standorte;
	private Map<String, ArrayList<Long>> standzeitenProZone;
	private Map<String, ArrayList<Standort>> standorteProZone;
	private Map<String, Double> mittlereStandzeitProZone;
	private Map<String, Double> mittlereAnzahlFzProZone;
	private Map<String, Double> ladebedarfProZoneGewichtet;
	private Map<String, Double> ladebedarfProZoneUngewichtet;

	/**
	 * Default Constructor Initialisieren aller Listen und Maps.
	 */
	public RunLadebedarfAnalyse() {
		standorteProZone = new LinkedHashMap<String, ArrayList<Standort>>();
		mittlereStandzeitProZone = new LinkedHashMap<String, Double>();
		mittlereAnzahlFzProZone = new LinkedHashMap<String, Double>();
		ladebedarfProZoneGewichtet = new LinkedHashMap<String, Double>();
		ladebedarfProZoneUngewichtet = new LinkedHashMap<String, Double>();
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
		//GEWICHTET
		ladebedarfProZoneGewichtet = DataAnalysis.berechneLadebedarfProZoneGewichtet(mittlereAnzahlFzProZone, mittlereStandzeitProZone);
		ladebedarfProZoneGewichtet = DataAnalysis.sortByValue(ladebedarfProZoneGewichtet);
		System.out.println("mittlerer Ladebedarf Gewichtet bei Quantil = " + DataAnalysis.QUANTIL + " : "
				+ DataAnalysis.computeMean(ladebedarfProZoneGewichtet) + " kWh");
		System.out.println("gesamter Ladebedarf Gewichtet bei Quantil = " + DataAnalysis.QUANTIL + " : "
				+ DataAnalysis.computeSum(ladebedarfProZoneGewichtet) + " kWh");
		//UNGEWICHTET
		ladebedarfProZoneUngewichtet = DataAnalysis.berechneLadebedarfProZoneUngewichtet(mittlereAnzahlFzProZone, mittlereStandzeitProZone);
		ladebedarfProZoneUngewichtet = DataAnalysis.sortByValue(ladebedarfProZoneUngewichtet);
		System.out.println("mittlerer Ladebedarf Ungewichtet bei Quantil = " + DataAnalysis.QUANTIL + " : "
				+ DataAnalysis.computeMean(ladebedarfProZoneUngewichtet) + " kWh");
		System.out.println("gesamter Ladebedarf Ungewichtet bei Quantil = " + DataAnalysis.QUANTIL + " : "
				+ DataAnalysis.computeSum(ladebedarfProZoneUngewichtet) + " kWh");
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
		System.out.println("Schreibe Datei 'LadebedarfProZone_" + DataAnalysis.QUANTIL + ".csv'...");
		WriteData.writeLadebedarfProZone(
				"/Users/kathrinmaier/Desktop/e-carsharing/LadebedarfProZone_" + DataAnalysis.QUANTIL + ".csv",
				ladebedarfProZoneGewichtet);
		System.out.println("Schreibe Datei 'LadebedarfProZoneUngewichtet.csv'...");
		WriteData.writeLadebedarfProZone(
				"/Users/kathrinmaier/Desktop/e-carsharing/LadebedarfProZoneUngewichtet.csv",
				ladebedarfProZoneUngewichtet);

		// WriteData.writeStandorteProZone(standorteProZone);

	}

}
