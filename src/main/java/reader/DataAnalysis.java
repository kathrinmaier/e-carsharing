package reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.math.DoubleMath;

/**
 * 
 * @author kathrinmaier
 *
 */
public class DataAnalysis {



	/**
	 * 
	 * @param value
	 * @return
	 */
	public static Double computeMean(ArrayList<Long> value) {
		return DoubleMath.mean(value);
	}

	public static Double computeMean(Map<String, Double> map) {
		ArrayList<Double> list = new ArrayList<Double>();

		for (Entry<String, Double> entry : map.entrySet()) {
			list.add(entry.getValue());
		}
		return DoubleMath.mean(list);
	}
	
	public static double computeSum(Map<String, Double> map) {
		double sum = 0;
		for (Entry<String, Double> entry : map.entrySet()) {
			sum += entry.getValue();
		}
		return sum;
	}

	/**
	 * 
	 * @return
	 */
	public static Map<String, Double> berechneMittlereStandzeitProZone(
			Map<String, ArrayList<Long>> standzeitenProZone) {

		// Berechne mittlere Standzeit pro Zone
		Map<String, Double> mittlereStandzeitProZoneUnsortiert = new LinkedHashMap<>();
		for (Entry<String, ArrayList<Long>> entry : standzeitenProZone.entrySet()) {
			if (entry.getValue().size() > 0) {
				mittlereStandzeitProZoneUnsortiert.put(entry.getKey(), DataAnalysis.computeMean(entry.getValue()));
				// System.out.println(mittlereStandzeitProZoneUnsortiert.get(entry.getKey()));
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
	public static Map<String, Double> berechneMittlereAnzahlFzProZone(Map<String, short[]> anzahlFzProZone) {
		Map<String, Double> mittlereAnzahlFzProZone = new LinkedHashMap<String, Double>();
		for (Entry<String, short[]> entry : anzahlFzProZone.entrySet()) {
			if (entry.getValue() == null)
				continue;
			int sum = 0;
			for (int i = 0; i < entry.getValue().length; i++) {
				sum += entry.getValue()[i];
			}
			double durchschnittlicheFz = sum * 1.0 / entry.getValue().length * 1.0;
			mittlereAnzahlFzProZone.put(entry.getKey(), durchschnittlicheFz);

		}
		mittlereAnzahlFzProZone = (LinkedHashMap<String, Double>) sortByValue(mittlereAnzahlFzProZone);
		// for (Entry<String, Double> entry :
		// mittlereAnzahlFzProZone.entrySet()) {
		// System.out.println(entry.getKey() + " : " + entry.getValue());
		// }
		return mittlereAnzahlFzProZone;
	}

	/**
	 * 
	 * @param standorte
	 * @return
	 */
	public static Map<String, ArrayList<Standort>> berechneStandorteProZone(List<Standort> standorte,
			Map<String, Double> mittlereStandzeitProZone) {

		int startIndex = 0;
		int endIndex = mittlereStandzeitProZone.keySet().size();

		Map<String, ArrayList<Standort>> standorteProZone = new LinkedHashMap<String, ArrayList<Standort>>();
		// iteriere über "mittleStandzeitProZone von startIndex bis endIndex

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
		return standorteProZone;
	}

	/**
	 * 
	 * @param arrayLength
	 * @param earliestDate
	 * @return
	 */
	public static Map<String, short[]> berechneAnzahlFzProZeitintervallProZone(ReadData reader,
			Map<String, ArrayList<Standort>> standorteProZone) {
		// Map <ZonenId, AnzahlFzgProZeitIntervall>
		int arrayLength = DataAnalysis.safeLongToInt((reader.getLatestDate() - reader.getEarliestDate()) / 60000);

		Map<String, short[]> anzahlFzProZone = new HashMap<String, short[]>();

		for (Entry<String, ArrayList<Standort>> entry : standorteProZone.entrySet()) {
			anzahlFzProZone.put(entry.getKey(), initArray(arrayLength));

			if (entry.getValue() != null) {
				for (Standort standort : entry.getValue()) {
					try {
						int startIndex = safeLongToInt(
								(standort.getStartTime().getTime() - reader.getEarliestDate()) / 60000);
						int endIndex = safeLongToInt(
								(standort.getEndTime().getTime() - reader.getEarliestDate()) / 60000);

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
	 * Ladebedarf pro Zone d. Erst nach Aufruf von mittlereStandzeitProZone()
	 * und berechenMittlereAnzahlFzProZone möglich!!!
	 * 
	 * @return Map mit mittleren Ladebedarfen pro Zone
	 */
	public static Map<String, Double> berechneLadebedarfProZoneGewichtet(Map<String, Double> mittlereAnzahlFzProZone,
			Map<String, Double> mittlereStandzeitProZone,
			final int P, final int QUANTIL) {
		if (QUANTIL == 1) {
			return berechneLadebedarfProZoneUngewichtet(mittlereAnzahlFzProZone, mittlereStandzeitProZone, P);
		}
		
		Map<String, Double> ladebedarfProZone = new LinkedHashMap<String, Double>();
		double gamma = 0.0;
		int increment = 0;
		int counter = 0;
		int anzahlZonen = mittlereStandzeitProZone.size();

		for (Entry<String, Double> entry : mittlereAnzahlFzProZone.entrySet()) {

			String zonenID = entry.getKey();

			double n = entry.getValue();
			double t = mittlereStandzeitProZone.get(zonenID) / 60.0; // t in
																		// Stunden

			// Berechnungsformel für den Ladebedarf pro Zone
			double d = P * n * t * gamma;
			//System.out.println(zonenID+ " : "+d +" = "+n+" * "+t+" * "+gamma);

			ladebedarfProZone.put(zonenID, d);

			counter++;
			// Gamma != 2, um bei einer Teilefremden Anzahl der Zonen im Bereich
			// [0,2] zu bleiben.
			if (counter >= (anzahlZonen / QUANTIL) && gamma < 2) {
				counter = 0;
				increment += 2;
				gamma = increment / (QUANTIL - 1.0);
				
			}

		}

		return ladebedarfProZone;

	}
	
	public static  Map<String, Double> berechneLadebedarfProZoneUngewichtet(Map<String, Double> mittlereAnzahlFzProZone,
			Map<String, Double> mittlereStandzeitProZone, final int P) {
		Map<String, Double> ladebedarfProZone = new LinkedHashMap<String, Double>();

		for (Entry<String, Double> entry : mittlereAnzahlFzProZone.entrySet()) {

			String zonenID = entry.getKey();

			double n = entry.getValue();
			double t = mittlereStandzeitProZone.get(zonenID) / 60.0; // t in
																		// Stunden

			// Berechnungsformel für den Ladebedarf pro Zone
			double d = P * n * t;
			//System.out.println(d +" = "+n+" * "+t+" * "+gamma);

			ladebedarfProZone.put(zonenID, d);

		}

		return ladebedarfProZone;
	}
	
	/**
	 * Berechnung des Ladebedarfs für zwei quantile. Es wird die Differenz beider berechnter Werte pro Zone gespeichert.
	 * @param mittlereAnzahlFzProZone
	 * @param mittlereStandzeitProZone
	 * @param p
	 * @param quantil1
	 * @param quantil2
	 * @return
	 */
	public static Map<String, Double> berechneDifferenzenImLadebedarf(Map<String, Double> mittlereAnzahlFzProZone, Map<String, Double> mittlereStandzeitProZone, int p, int quantil1, int quantil2){
		Map<String,Double> differenz = new LinkedHashMap<String,Double>();
		
		Map<String,Double> map1 = berechneLadebedarfProZoneGewichtet(mittlereAnzahlFzProZone, mittlereStandzeitProZone, p, quantil1);
		Map<String,Double> map2 = berechneLadebedarfProZoneGewichtet(mittlereAnzahlFzProZone, mittlereStandzeitProZone, p, quantil2);
		
		for (Entry<String,Double> entry1 : map1.entrySet()) {
			double d1 = entry1.getValue();
			double d2 = map2.get(entry1.getKey());
			differenz.put(entry1.getKey(), d1-d2);
		}
		
		return differenz;
	}
	
	/**
	 * Median fahrzeuge = 3.5 / Median Standzeit = 230 min
	 * => 1 Fahrhezug - 1 Stunde == n - t
	 * @param scoringMap
	 * @param mittlereAnzahlFzProZone
	 * @param mittlereStandzeitProZone
	 * @param alpha
	 * @param beta
	 * @return
	 */
	public static Map<String, Double> scoringMethod(Map<String, Double> scoringMap,
			Map<String, Double> mittlereAnzahlFzProZone, Map<String, Double> mittlereStandzeitProZone, double alpha, double beta) {
		
		for (Entry<String, Double> entry : mittlereAnzahlFzProZone.entrySet()) {

			String zonenID = entry.getKey();

			double n = entry.getValue();
			if(mittlereStandzeitProZone.get(zonenID) == null) continue;
			double t = mittlereStandzeitProZone.get(zonenID) / 60.0; // t in
																		// Stunden

			
			double score = (alpha*n) + (t*beta);
			//System.out.println(d +" = "+n+" * "+t+" * "+gamma);
			if(scoringMap.get(zonenID)!=null){
				score += scoringMap.get(zonenID);
			}
			scoringMap.put(zonenID, score);

		}

		
		
		return scoringMap;
		
	}


	/**
	 * Initializes a array of length l with values zero
	 * 
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
	 * 
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
	 * 
	 * @paramn unsorted map
	 * @return sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
