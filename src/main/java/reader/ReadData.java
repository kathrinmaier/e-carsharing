package reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.math.DoubleMath;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

public class ReadData {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	static CoordinateTransformation trans = TransformationFactory
			.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:25833");
	Map<String, Geometry> shape;
	Map<String, ArrayList<Long>> standzeitenProZone = new TreeMap<>();
	static String carData = "/Users/kathrinmaier/Desktop/e-carsharing/rohdaten/fahrzeugdaten/";
	List<Standort> standorte;
	private static int standortId = 1;

	private int nullZoneCounter = 0;
	
	Map<String, Double> mittlereStandzeitProZone = new LinkedHashMap<>();

	public static void main(String[] args) {
		ReadData reader = new ReadData();
		reader.init();
		File folder = new File(carData);
		File[] allCars = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.equals(".DS_Store");
			}
		});
		reader.iterateOverAllCars(allCars);
		reader.standzeitBerechnungen();
	}

	void init() {
		shape = readShapeFileAndExtractGeometry(
				"/Users/kathrinmaier/Desktop/e-carsharing/LOR_SHP_EPSG_25833/Planungsraum.shp", "SCHLUESSEL");
		for (String zone : shape.keySet()) {
			standzeitenProZone.put(zone, new ArrayList<Long>());
		}

	}

	private void iterateOverAllCars(File[] allCars) {
		standorte = new ArrayList<Standort>();
		for (int i = 0; i < allCars.length; i++) {
			if (allCars[i].isFile()) {
				erzeugeStandorteProAuto(allCars[i].getPath(), allCars[i].getName().substring(0, allCars[i].getName().length() - 4));
			}
		}
		System.out.println("Anzahl der Standort mit zone == null :"+nullZoneCounter);

	}

	private void erzeugeStandorteProAuto(final String filePath, final String carName) {

		TabularFileParserConfig c = new TabularFileParserConfig();
		c.setDelimiterTags(new String[] { "\t" });
		c.setFileName(filePath);
		final List<CarsharingRide> rides = new ArrayList<>();
		

		new TabularFileParser().parse(c, new TabularFileHandler() {

			@Override
			public void startRow(String[] row) {
				try {
					String rideId = row[0];
					String x = row[1].split(",")[1];
					String y = row[1].split(",")[0];
					Coord start = new Coord(Double.parseDouble(x), Double.parseDouble(y));
					x = row[2].split(",")[1];
					y = row[2].split(",")[0];
					Coord end = new Coord(Double.parseDouble(x), Double.parseDouble(y));

					Date startTime = sdf.parse(row[3]);
					Date endTime = sdf.parse(row[4]);

					int tankVor = Integer.parseInt(row[5]);
					int tankNach = Integer.parseInt(row[6]);
					int distance = Integer.parseInt(row[9]);
					int zeitFrei = Integer.parseInt(row[10]);
					int zeitStau = Integer.parseInt(row[11]);
					int umstiege = Integer.parseInt(row[12]);
					int zeitPT = Integer.parseInt(row[13]);
					CarsharingRide ride = new CarsharingRide(rideId, start, end, startTime, endTime, tankVor, tankNach,
							distance, zeitFrei, zeitStau, umstiege, zeitPT);
					rides.add(ride);

				} catch (Exception e) {

					System.err.println("Could not parse line: " + row);
				}
			}
		});
		Date previousEnd = null;
		List<Long> standzeiten = new ArrayList<>();
		for (CarsharingRide r : rides) {
			if (previousEnd != null) {
				String zone = getZoneForCoord(r.start);
				//Wenn Fahrten außerhalb von Zonen stattgefunden haben, werden sie mit zone -1 deklariert.
				if ( zone == null){
					nullZoneCounter++;
					zone = "-1";
				}
				
				Standort standort = new Standort(standortId++, carName, zone, r.start, previousEnd,
						r.startTime);
				standorte.add(standort);

				String standzone = getZoneForCoord(r.start);
				long standzeit = (r.startTime.getTime() - previousEnd.getTime()) / 1000;
				previousEnd = r.endTime;
				if (standzone != null) {
					standzeitenProZone.get(standzone).add(standzeit);
				}
				standzeiten.add(standzeit);
			} else {
				previousEnd = r.endTime;

			}
		}
	}
	
	
	private void standzeitBerechnungen(){
		
		// Berechne mittlere Standzeit pro Zone
		Map<String, Double> mittlereStandzeitProZoneUnsortiert = new LinkedHashMap<>();
		for (Entry<String, ArrayList<Long>> entry : this.standzeitenProZone.entrySet()) {
			if (entry.getValue().size() > 0) {
				mittlereStandzeitProZoneUnsortiert.put(entry.getKey(), computeMean(entry.getValue()));
			}
		}

		//Sortiere Map
		mittlereStandzeitProZone = sortByValue(mittlereStandzeitProZoneUnsortiert);

		//Berechne alle Standorte von n Zonen mit den höchsten durschnittlichen Standzeiten
		
		berechneStandorteProZone(0, 10);
		
		//Printe Map
		for (Entry<String, Double> entry : this.mittlereStandzeitProZone.entrySet()) {
			if (entry.getValue() != null) {
				System.out.println(entry.getKey()+" : "+entry.getValue());
			}
		}

		// Speichern der mittleren Standzeiten in CSV Datei
		writeStandzeiten("/Users/kathrinmaier/Desktop/e-carsharing/standzeitenprozone.csv");



	}

	private void berechneStandorteProZone(int startIndex, int endIndex) {
		Map<String, ArrayList<Standort>> standorteProZone = new LinkedHashMap<String, ArrayList<Standort>>();
		//iteriere über "mittleStandzeitProZone von startIndex bis endIndex
		System.out.println("Berechne Standorte pro Zone bei ingesamt "+standorte.size()+" Standorten");
		for(int i = startIndex; i <= endIndex; i++){
			
			String zone = (String) mittlereStandzeitProZone.keySet().toArray()[i];
			System.out.println("Schleife für "+zone);
			//initialisiere die Liste als value für den key "zone"
			standorteProZone.put(zone, new ArrayList<Standort>());
			//Fülle die Liste zu jeder zone
			for (Standort standort : standorte) {
				//Wenn die zone des Standorts gleich der Zone dann füge ihn zur Liste der Zone hinzu
				
				if(standort.getZoneId().equals(zone)){
					standorteProZone.get(zone).add(standort);
					
				}
			}
		}
		
		//speichere die Listen pro Zone in jeweils eigne csv Dateien
		for (Entry<String, ArrayList<Standort>> entry : standorteProZone.entrySet()) {
			if (entry.getValue() != null) {
				writeStandorteProZone("/Users/kathrinmaier/Desktop/e-carsharing/StandorteProZonen/standorte_in_"+entry.getKey()+".csv", entry.getKey(), entry.getValue());
			}
			
		}
			
		
	}

	private void writeStandzeiten(String filename) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		try {
			bw.write("Zone,MittlereStandZeit");
			for (Entry<String, Double> entry : this.mittlereStandzeitProZone.entrySet()) {
				if (entry.getValue() != null) {
					bw.newLine();
					bw.write(entry.getKey() + "," + (entry.getValue()));
				}
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeStandorteProZone(String filename, String zone, List<Standort> standorte) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		BufferedWriter bwCVST = IOUtils.getBufferedWriter(filename+"t");
		try {
			bwCVST.write("\"String\",\"Real\",\"Real\"");
			
			bw.write("Zone,x_coord,y_coord");
			for (Standort standort : standorte) {
					bw.newLine();
					bw.write(zone + "," + standort.getCoord().getX() + "," + standort.getCoord().getY());
			}
			
			bwCVST.flush();
			bwCVST.close();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Double computeMean(ArrayList<Long> value) {
		Double mean = DoubleMath.mean(value);
		
		return mean;
	}

	private String getZoneForCoord(Coord start) {
		start = trans.transform(start);
		for (Entry<String, Geometry> e : shape.entrySet()) {
			Geometry geo = e.getValue();
			String zone = e.getKey();
			if (geo.contains(MGC.coord2Point(start))) {
				return zone;
			}
		}

		return null;
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
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

	public static Map<String, Geometry> readShapeFileAndExtractGeometry(String filename, String key) {

		Map<String, Geometry> geometry = new TreeMap<>();
		for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {

			GeometryFactory geometryFactory = new GeometryFactory();
			WKTReader wktReader = new WKTReader(geometryFactory);

			try {
				Geometry geo = wktReader.read((ft.getAttribute("the_geom")).toString());
				String lor = ft.getAttribute(key).toString();
				geometry.put(lor, geo);
			} catch (com.vividsolutions.jts.io.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return geometry;
	}

}
