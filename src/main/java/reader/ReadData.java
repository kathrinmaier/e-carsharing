package reader;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author kathrinmaier
 *
 */
public class ReadData {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	static CoordinateTransformation trans = TransformationFactory
			.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:25833");
	Map<String, Geometry> shape;
	static String carData = "/Users/kathrinmaier/Desktop/e-carsharing/rohdaten/fahrzeugdaten/";
	int nullZoneCounter = 0;

	private static int standortId = 1;

	private List<Standort> standorte;
	private Map<String, ArrayList<Long>> standzeitenProZone;
	private long earliestDate = Long.MAX_VALUE;
	private long latestDate = Long.MIN_VALUE;

	public ReadData() {
		init();
		File folder = new File(carData);
		File[] allCars = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.equals(".DS_Store");
			}
		});
		iterateOverAllCars(allCars);
	}

	/**
	 * 
	 * @param args
	 */
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
		// standorte werden erzeugt
		reader.iterateOverAllCars(allCars);
		// StandortDaten werden zerlegt und analyisiert

	}

	/**
	 */
	void init() {
		shape = readShapeFileAndExtractGeometry(
				"/Users/kathrinmaier/Desktop/e-carsharing/LOR_SHP_EPSG_25833/Planungsraum.shp", "SCHLUESSEL");
		standzeitenProZone = new LinkedHashMap<String, ArrayList<Long>>();
		for (String zone : shape.keySet()) {
			standzeitenProZone.put(zone, new ArrayList<Long>());
		}

	}

	/**
	 */
	private void iterateOverAllCars(File[] allCars) {
		standorte = new ArrayList<Standort>();
		for (int i = 0; i < allCars.length; i++) {
			if (allCars[i].isFile()) {
				erzeugeStandorteProAuto(allCars[i].getPath(),
						allCars[i].getName().substring(0, allCars[i].getName().length() - 4));
			}
		}
		System.out.println("Anzahl der Standort mit zone == null :" + nullZoneCounter);

	}

	/**
	 * 
	 * @param filePath
	 * @param carName
	 */
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
					if (startTime.getTime() < earliestDate) {
						earliestDate = startTime.getTime();
					}
					Date endTime = sdf.parse(row[4]);
					if (endTime.getTime() > latestDate) {
						latestDate = endTime.getTime();
					}

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
				// Wenn Fahrten außerhalb von Zonen stattgefunden haben, werden
				// sie mit zone -1 deklariert.
				if (zone == null) {
					nullZoneCounter++;
					zone = "-1";
				}

				Standort standort = new Standort(standortId++, carName, zone, r.start, previousEnd, r.startTime);
				standorte.add(standort);

				String standzone = getZoneForCoord(r.start);
				long standzeit = (r.startTime.getTime() - previousEnd.getTime()) / 60000; // in
																							// Minuten
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

	/**
	 * 
	 * @param start
	 * @return
	 */
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

	/**
	 * 
	 * @param filename
	 * @param key
	 * @return
	 */
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

	public long getEarliestDate() {
		return earliestDate;
	}

	public long getLatestDate() {
		return latestDate;
	}

	public List<Standort> getStandorte() {
		return standorte;
	}

	public Map<String, ArrayList<Long>> getStandzeitenProZone() {
		return standzeitenProZone;
	}

	public void setEarliestDate(long earliestDate) {
		this.earliestDate = earliestDate;
	}

}
