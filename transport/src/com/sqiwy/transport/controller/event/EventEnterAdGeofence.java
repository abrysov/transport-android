/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller.event;

import java.util.ArrayList;

import com.sqiwy.transport.advertisement.Advertisement;
import com.sqiwy.transport.advertisement.AdvertisementResource;
import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.Event.Source;

public class EventEnterAdGeofence extends Event {

	private final ArrayList<AdvertisementResource> ads;
	
	public EventEnterAdGeofence(ArrayList<AdvertisementResource> ads) {
		type = Event.Type.FORCING;
		source = Source.GEO;
		this.ads = ads;
	}
	
	public ArrayList<AdvertisementResource> getAds() {
		return ads;
	}
}
