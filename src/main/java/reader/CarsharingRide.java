package reader;

import java.util.Date;

import org.matsim.api.core.v01.Coord;

public class CarsharingRide {

	String id;
	Coord start;
	Coord end;
	Date startTime;
	Date endTime;
	int tankVor;
	int tankNach;
	int distance;
	int zeitFrei;
	int zeitStau;
	int umstiege;
	int zeitPT;
	
	public CarsharingRide(String id, Coord start, Coord end, Date startTime, Date endTime, int tankVor, int tankNach,
			int distance, int zeitFrei, int zeitStau, int umstiege, int zeitPT) {
	
		this.id = id;
		this.start = start;
		this.end = end;
		this.startTime = startTime;
		this.endTime = endTime;
		this.tankVor = tankVor;
		this.tankNach = tankNach;
		this.distance = distance;
		this.zeitFrei = zeitFrei;
		this.zeitStau = zeitStau;
		this.umstiege = umstiege;
		this.zeitPT = zeitPT;
	}
	
	
}
