package reader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.matsim.api.core.v01.Coord;

public class Standort {

	private int id;
	private String carId;
	private String zoneId;
	private Coord coord;
	private Date startTime;
	private Date endTime;

	public Standort(int id, String carId, String zoneId, Coord coord, Date startTime, Date endTime) {
		super();
		this.id = id;
		this.carId = carId;
		this.zoneId = zoneId;
		this.coord = coord;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * 
	 * @param standorte
	 * @param radius
	 *            in Meter
	 * @return
	 */
	public List<Standort> hotspotStandort(List<Standort> standorte, int radius) {

		List<Standort> standorteImRadius = new ArrayList<Standort>();
		double distance;
		
		// jeder Standort wird verglichen mit den übergebenen Standorten
		for (Standort standort : standorte) {
			distance = distFrom(coord.getX(), coord.getY(), standort.getCoord().getX(), standort.getCoord().getY());
			
			if(distance <= radius){
				standorteImRadius.add(standort);
			}
			
		}
		return standorteImRadius;
	}

	/**
	 * Berechne den Abstand zwischen zwei in Koordinaten angegebenen Punkten in
	 * Metern
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return double in Metern
	 */
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; // meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (double) (earthRadius * c);
		return dist;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCarId() {
		return carId;
	}

	public void setCarId(String carId) {
		this.carId = carId;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}

	public Coord getCoord() {
		return coord;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String toString() {
		return id + " " + carId + " " + zoneId + " " + coord.getX() + "-" + coord.getY() + " " + startTime + " "
				+ endTime;
	}
}
