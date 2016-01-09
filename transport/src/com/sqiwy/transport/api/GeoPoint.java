/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

public class GeoPoint {

    public GeoPoint() {
    }

    public GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    private double lat;
    private double lon;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
