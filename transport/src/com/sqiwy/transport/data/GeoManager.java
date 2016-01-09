package com.sqiwy.transport.data;

import com.google.android.gms.location.Geofence;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by abrysov
 */
public class GeoManager {

	private static final GeoManager INSTANCE = new GeoManager(); 
	
	private final Map<Geofence, GeoArea> mGeofences = new LinkedHashMap<Geofence, GeoArea>();
	
	public static GeoManager getInstance() {
		return INSTANCE;
	}
	
	public List<Geofence> getGeofences() {
		return new ArrayList<Geofence>(mGeofences.keySet());
	}
	
	public GeoArea findGeoArea(Geofence geofence) {
		return mGeofences.get(geofence);
	}
	
	public void addGeoArea(GeoArea area) {
		GeoPoint point = area.getLocation();
		Geofence geofence = new Geofence.Builder()
			.setRequestId(UUID.randomUUID().toString())
			.setCircularRegion(point.getLatitude(), point.getLongitude(), area.getRadius())
			.setExpirationDuration(Geofence.NEVER_EXPIRE)
			.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
			.build();
		if (mGeofences.containsKey(geofence)) {
			return;
		}
		mGeofences.put(geofence, area);
	}

    public void removeAllGeofences() {
        mGeofences.clear();
    }
}
