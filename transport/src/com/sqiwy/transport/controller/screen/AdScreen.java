/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller.screen;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.advertisement.AdvertisementManager;
import com.sqiwy.transport.advertisement.AdvertisementResource;
import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.Screen;
import com.sqiwy.transport.controller.event.EventEnterAdGeofence;
import com.sqiwy.transport.controller.event.EventEnterBusStopGeofence;

public class AdScreen extends Screen {

	public AdScreen(int id, long minShowTime, float targetShowCoefficient) {
		super(id, minShowTime, targetShowCoefficient);
	}

	@Override
	public boolean isEligibleFor(Event event) {
		if (!AdvertisementManager.getInstance().isReady()) {
			return false;
		}
		if (event instanceof EventEnterBusStopGeofence) {
        	return false;
        } else {
        	return true;
        }
	}
	
	@Override
	public boolean isWithOutTimeLimit(Event event) {
		return event instanceof EventEnterAdGeofence;
	}
	
	@Override
	public long getShowTime(Event event) {
		long time = 0;
		if (event instanceof EventEnterAdGeofence) {
			List<AdvertisementResource> resources = ((EventEnterAdGeofence) event).getAds();
			for (AdvertisementResource res : resources) {
				time += res.getAd().getShowDuration();
			}
		} else {
			time = AdvertisementManager.getInstance().getOngoingAdsDuration();
		}
		
		return time;
	}
	
	public ArrayList<AdvertisementResource> getAds(Event event) {
		ArrayList<AdvertisementResource> resources;
		if (event instanceof EventEnterAdGeofence) {
			resources = ((EventEnterAdGeofence) event).getAds();
		} else {
			resources = AdvertisementManager.getInstance().getOngoingAds();
		}
		if (BuildConfig.DEBUG) {
			Log.d(getClass().getName(), "getAds: " + resources.size() + " : " + resources.toString());
			Log.d(getClass().getName(), "event instanceof EventEnterAdGeofence: " + (event instanceof EventEnterAdGeofence));
		}
		
		return resources;
	}
	
	@Override
	public void aboutToBeDissmissed(Event event) {
		super.aboutToBeDissmissed(event);
		
		AdvertisementManager.getInstance().refreshOngoingAds(getMinShowTime());
	}
}
