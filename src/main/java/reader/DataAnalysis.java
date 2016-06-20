package reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.common.math.DoubleMath;

public class DataAnalysis {

	static LinkedHashMap<String, ArrayList<Standort>> standorteProZone;
	static Map<String, Double> mittlereStandzeitProZone = new LinkedHashMap<>();
	private static LinkedHashMap<String, Double> mittlereAnzahlFzProZone;
	
	/**
	 * Speichern der mittleren Standzeiten in CSV Datei
	 */
	public static void writeStandzeitenProZone() {
		WriteData.writeStandzeiten("/Users/kathrinmaier/Desktop/e-carsharing/standzeitenprozone.csv",
		DataAnalysis.mittlereStandzeitProZone);
	}
	
	/**
	 * speichere die Listen pro Zone in jeweils eigne csv Dateien
	 */
	public static void writeStandorteProZone() {
		for (Entry<String, ArrayList<Standort>> entry : standorteProZone.entrySet()) {
			if (entry.getValue() != null) {
				WriteData.writeStandorteProZone(
						"/Users/kathrinmaier/Desktop/e-carsharing/StandorteProZonen/standorte_in_" + entry.getKey()
								+ ".csv",
						entry.getKey(), entry.getValue());
			}

		}
	}
	

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static Double computeMean(ArrayList<Long> value) {
		return DoubleMath.mean(value);
	}
	
	/**
	 * 
	 * @return
	 */
	public static Map<String, Double> mittlereStandzeitBerechnung(Map<String, ArrayList<Long>> standzeitenProZone) {

		// Berechne mittlere Standzeit pro Zone
		Map<String, Double> mittlereStandzeitProZoneUnsortiert = new LinkedHashMap<>();
		for (Entry<String, ArrayList<Long>> entry : standzeitenProZone.entrySet()) {
			if (entry.getValue().size() > 0) {
				mittlereStandzeitProZoneUnsortiert.put(entry.getKey(), DataAnalysis.computeMean(entry.getValue()));
			}
		}

		// Sortiere Map
		Map<String, Double> mittlereStandzeitProZone = sortByValue(mittlereStandzeitProZoneUnsortiert);

		return mittlereStandzeitProZone;

	}
	
	/**
	 * 
	 * @param anzahlFzProZone
	 * @return
	 */
	public static void berechneMittlereAnzahlFzProZone(Map<String, short[]> anzahlFzProZone) {
		mittlereAnzahlFzProZone = new LinkedHashMap<String, Double>();
		for (Entry<String, short[]> entry : anzahlFzProZone.entrySet()) {
			if(entry.getValue()==null) continue;
			int sum = 0;
			for (int i = 0; i<entry.getValue().length;i++) {
				sum += entry.getValue()[i];
			}
			double durchschnittlicheFz = sum * 1.0 / entry.getValue().length * 1.0;
			mittlereAnzahlFzProZone.put(entry.getKey(), durchschnittlicheFz);
			
		}
		mittlereAnzahlFzProZone = (LinkedHashMap<String, Double>) sortByValue(mittlereAnzahlFzProZone);
		for (Entry<String, Double> entry : mittlereAnzahlFzProZone.entrySet()) {
			System.out.println(entry.getKey()+" : "+entry.getValue());
		}
	}
	
	/**
	 * 
	 * @param standorte
	 * @return
	 */
	public static void berechneStandorteProZone(List<Standort> standorte) {
		
		
		int startIndex = 0;
		int endIndex = mittlereStandzeitProZone.keySet().size();
		
		standorteProZone = new LinkedHashMap<String, ArrayList<Standort>>();
		// iteriere über "mittleStandzeitProZone von startIndex bis endIndex
		System.out.println("Berechne Standorte pro Zone bei ingesamt " + standorte.size() + " Standorten");
		for (int i = startIndex; i < endIndex; i++) {

			String zone = (String) mittlereStandzeitProZone.keySet().toArray()[i];
			// initialisiere die Liste als value für den key "zone"
			standorteProZone.put(zone, new ArrayList<Standort>());
			// Fülle die Liste zu jeder zone
			for (Standort standort : standorte) {
				// Wenn die zone des Standorts gleich der Zone dann füge ihn zur
				// Liste der Zone hinzu

				if (standort.getZoneId().equals(zone)) {
					standorteProZone.get(zone).add(standort);

				}
			}
		}
	}
	
	/**
	 * 
	 * @param arrayLength
	 * @param earliestDate
	 * @return
	 */
	public static Map<String, short[]> berechneAnzahlFzProZeitintervallProZone(int arrayLength, long earliestDate) {
		// Map <ZonenId, AnzahlFzgProZeitIntervall>
		Map<String, short[]> anzahlFzProZone = new HashMap<String, short[]>();
		
		for (Entry<String, ArrayList<Standort>> entry : standorteProZone.entrySet()) {
			anzahlFzProZone.put(entry.getKey(), initArray(arrayLength));
			System.out.println(entry.getKey());
			if (entry.getValue() != null) {
				for (Standort standort : entry.getValue()) {
					try {
						int startIndex = safeLongToInt((standort.getStartTime().getTime() - earliestDate) / 60000);
						int endIndex = safeLongToInt((standort.getEndTime().getTime() - earliestDate) / 60000);

						for (int i = startIndex; i <= endIndex; i++) {
							anzahlFzProZone.get(entry.getKey())[i]++;
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return anzahlFzProZone;
	}
	
	/**
	 * Initializes a array of length l with values zero
	 * @param l
	 * @return
	 */
	public static short[] initArray(int l) {
		short[] array = new short[l];
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
		return array;
	}
	

	/**
	 * Safe conversion of long to int 
	 * @param l
	 * @return int
	 */
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
	
	/**
	 * Util comparable, sorting a map regarding its values
	 * @paramn unsorted map
	 * @return sorted map
	 */
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	

}
