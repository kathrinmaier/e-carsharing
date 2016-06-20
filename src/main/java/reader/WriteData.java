package reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.core.utils.io.IOUtils;

public class WriteData {
	
	public static void writeStandzeiten(String filename, Map<String, Double> mittlereStandzeitProZone)  {

		BufferedWriter bw = IOUtils.getBufferedWriter(filename);
		try {
			bw.write("Zone,MittlereStandZeit");
			for (Entry<String, Double> entry : mittlereStandzeitProZone.entrySet()) {
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
	
	public static void writeStandorteProZone(String filename, String zone, List<Standort> standorte) {

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

}
