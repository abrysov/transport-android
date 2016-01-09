package com.sqiwy.transport.data;

import com.sqiwy.transport.advertisement.AdvertisementResource;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by abrysov
 */
public class GeoAd implements GeoArea {

	private GeoPoint location;
	private float radius;
	private ArrayList<AdvertisementResource> ads;
	
	public GeoAd(GeoPoint location, float radius, ArrayList<AdvertisementResource> ads) {
		this.location = location;
		this.radius = radius;
		this.ads = ads;
	}
	
	@Override
	public GeoPoint getLocation() {
		return location;
	}

	@Override
	public float getRadius() {
		return radius;
	}

	public ArrayList<AdvertisementResource> getAds() {
		return ads;
	}

}
