package reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.core.utils.io.IOUtils;

/**
 * 
 * @author kathrinmaier
 *
 */
public class WriteData {

	/**
	 * 
	 * @param filename
	 * @param mittlereStandzeitProZone
	 */
	public static void writeMittlereStandzeitenProZone(String filename, Map<String, Double> mittlereStandzeitProZone) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		BufferedWriter bwCVST = IOUtils.getBufferedWriter(filename + "t");
		try {

			bwCVST.write("\"String\",\"Real\"");

			bw.write("Zone,MittlereStandZeit");
			for (Entry<String, Double> entry : mittlereStandzeitProZone.entrySet()) {
				if (entry.getValue() != null) {
					bw.newLine();
					bw.write(entry.getKey() + "," + (entry.getValue()));
				}
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

	/**
	 * 
	 * @param filename
	 * @param mittlereAnzahlFzProZone
	 */
	public static void writeMittlereAnzahlFzProZone(String filename, Map<String, Double> mittlereAnzahlFzProZone) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		BufferedWriter bwCVST = IOUtils.getBufferedWriter(filename + "t");
		try {

			bwCVST.write("\"String\",\"Real\"");

			bw.write("Zone,MittlereAnzahlFz");
			for (Entry<String, Double> entry : mittlereAnzahlFzProZone.entrySet()) {
				if (entry.getValue() != null) {
					bw.newLine();
					bw.write(entry.getKey() + "," + (entry.getValue()));
				}
			}
			bw.flush();
			bw.close();
			bwCVST.flush();
			bwCVST.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param filename
	 * @param ladebedarfProZone
	 */
	public static void writeLadebedarfProZone(String filename, Map<String, Double> ladebedarfProZone) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		BufferedWriter bwCVST = IOUtils.getBufferedWriter(filename + "t");
		try {

			bwCVST.write("\"String\",\"Real\"");

			bw.write("Zone,Ladebedarf");
			for (Entry<String, Double> entry : ladebedarfProZone.entrySet()) {
				if (entry.getValue() != null) {
					bw.newLine();
					bw.write(entry.getKey() + "," + (entry.getValue()));
				}
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

	/**
	 * 
	 * @param standorteProZone
	 */
	public static void writeStandorteProZone(Map<String, ArrayList<Standort>> standorteProZone) {
		for (Entry<String, ArrayList<Standort>> entry : standorteProZone.entrySet()) {
			if (entry.getValue() != null) {
				writeStandorteInZone("/Users/kathrinmaier/Desktop/e-carsharing/StandorteProZonen/standorte_in_"
						+ entry.getKey() + ".csv", entry.getKey(), entry.getValue());
			}

		}
	}

	/**
	 * Internal Method for writeStandorteProZone.
	 * 
	 * @param filename
	 * @param zone
	 * @param standorte
	 */
	private static void writeStandorteInZone(String filename, String zone, List<Standort> standorte) {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		BufferedWriter bwCVST = IOUtils.getBufferedWriter(filename + "t");
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
			e.printStackTrace();
		}
	}
}
