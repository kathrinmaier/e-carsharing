package reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.matsim.core.utils.misc.Time;
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

	public static void main(String[] args) {
		new ReadData().run();
	}

	void run() {
		shape = readShapeFileAndExtractGeometry(
				"/Users/kathrinmaier/Desktop/e-carsharing/LOR_SHP_EPSG_25833/Planungsraum.shp", "SCHLUESSEL");
		for (String zone : shape.keySet()) {
			standzeitenProZone.put(zone, new ArrayList<Long>());
		}

		TabularFileParserConfig c = new TabularFileParserConfig();
		c.setDelimiterTags(new String[] { "\t" });
		c.setFileName(
				"/Users/kathrinmaier/Desktop/e-carsharing/rohdaten/cs daten ba/carsharingRun2/CG_B-GO2001_WME4513341K566515.txt");
		final List<CarsharingRide> rides = new ArrayList<>();
		new TabularFileParser().parse(c, new TabularFileHandler() {

			@Override
			public void startRow(String[] row) {
				try {
					String id = row[0];
					String x = row[1].split(",")[1];
					String y = row[1].split(",")[0];
					Coord start = new Coord(Double.parseDouble(x), Double.parseDouble(y));
					x = row[2].split(",")[1];
					y = row[2].split(",")[0];
					Coord end = new Coord(Double.parseDouble(x), Double.parseDouble(y));
					;
					Date startTime = sdf.parse(row[3]);
					Date endTime = sdf.parse(row[4]);
					;
					int tankVor = Integer.parseInt(row[5]);
					int tankNach = Integer.parseInt(row[6]);
					int distance = Integer.parseInt(row[9]);
					int zeitFrei = Integer.parseInt(row[10]);
					int zeitStau = Integer.parseInt(row[11]);
					int umstiege = Integer.parseInt(row[12]);
					int zeitPT = Integer.parseInt(row[13]);
					CarsharingRide ride = new CarsharingRide(id, start, end, startTime, endTime, tankVor, tankNach,
							distance, zeitFrei, zeitStau, umstiege, zeitPT);
					rides.add(ride);
				} catch (Exception e) {

					System.err.println("Could not parse line: " + row);
				}
			}
		});
		Date lastEnd = null;
		List<Long> standzeiten = new ArrayList<>();
		for (CarsharingRide r : rides) {
			if (lastEnd != null) {
				String standzone = getZoneForCoord(r.start);
				long standzeit = (r.startTime.getTime() - lastEnd.getTime()) / 1000;
				lastEnd = r.endTime;
				if (standzone != null) {
					standzeitenProZone.get(standzone).add(standzeit);
				}
				standzeiten.add(standzeit);
			} else {
				lastEnd = r.endTime;

			}
		}

		writeStandzeiten("/Users/kathrinmaier/Desktop/e-carsharing/standzeitenprozone.csv");

		System.out.println(Time.writeTime(DoubleMath.mean(standzeiten)));

		for (Entry<String, ArrayList<Long>> entry : this.standzeitenProZone.entrySet()) {
			if (entry.getValue().size() > 0) {
				System.out.println(entry.getKey() + "\t" + Time.writeTime(DoubleMath.mean(entry.getValue())));
			}
		}

	}

	private void writeStandzeiten(String filename) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		try {
			bw.write("Zone;MittlereStandZeit");
			for (Entry<String, ArrayList<Long>> entry : this.standzeitenProZone.entrySet()) {
				if (entry.getValue().size() > 0) {
					bw.newLine();
					
					bw.write(entry.getKey() + ";" + (DoubleMath.mean(entry.getValue())));
				}
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
