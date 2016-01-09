/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.Date;

public class Point implements GeoArea {
	private long id;
	private String name;
    @SerializedName("lat")
	private double latitude;
    @SerializedName("lon")
	private double longitude;
	private int order;
	private transient Date arrivalTime;
    private GeoPoint location;
    private float radius;
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
        location = new GeoPoint(latitude, longitude);
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
        location = new GeoPoint(latitude, longitude);
	}
	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	/**
	 * @return the arrivalTime
	 */
	public Date getArrivalTime() {
		return arrivalTime;
	}
	/**
	 * @param arrivalTime the arrivalTime to set
	 */
	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}

    public boolean isBusStop() {
        return !TextUtils.isEmpty(name);
    }

    @Override
    public GeoPoint getLocation() {
        return location;
    }

    @Override
    public float getRadius() {
        return 0 == radius ? 10 : radius;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + order;
		result = prime * result + Float.floatToIntBits(radius);
		return result;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Point)) return false;

        Point other = (Point) obj;
        if (id != other.id)
            return false;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.latitude))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.longitude))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (order != other.order)
            return false;
        if (Float.floatToIntBits(radius) != Float.floatToIntBits(other.radius))
            return false;
        return true;
    }
}
