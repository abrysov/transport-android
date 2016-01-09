/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

public class PointKey {
	private double lat;
	private double lon;
	private float radius;
	
	public PointKey(double lat, double lon, float radius) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.radius = radius;
	}
	
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public float getRadius() {
		return radius;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(radius);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointKey other = (PointKey) obj;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		if (Float.floatToIntBits(radius) != Float.floatToIntBits(other.radius))
			return false;
		return true;
	}
	
}