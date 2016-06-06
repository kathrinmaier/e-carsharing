package reader;

import java.util.Date;

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
	
	
	public String toString(){
		return id+" "+carId+" "+zoneId+" "+coord.getX()+"-"+coord.getY()+" "+startTime+" "+endTime;
	}
}
