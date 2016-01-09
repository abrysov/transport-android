/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.SystemClock;

import com.google.android.gms.location.Geofence;

/**
 * Mock implementation of geofences helper in order to be able to test geofences functionality in office.
 * 
 * It's assumed here that sequential geofences (they have correct order);
 */
public class GeofencesHelperMockRandomImpl extends GeofencesHelperMockBaseImpl {

	private final Random mRandom;
	
	private final long mMinInterval;
	private final long mMaxInterval;

	private long lastTime;
	
	/**
	 * @param minIntervalBetweenOccurrences in millis
	 * @param maxIntervalBetweenOccurrences in millis
	 */
	public GeofencesHelperMockRandomImpl(long minIntervalBetweenOccurrences, long maxIntervalBetweenOccurrences) {
		mRandom = new Random(System.nanoTime());
		
		mMinInterval = minIntervalBetweenOccurrences;
		mMaxInterval = maxIntervalBetweenOccurrences;
	}
	
	@Override
	protected long getTimeForNextGeofence(Geofence geofence, PendingIntent pendingIntent) {
		
		if (0 == lastTime) {
			lastTime = SystemClock.uptimeMillis();
		}
		
		long inTime = (long) (mMinInterval + mRandom.nextFloat() * (mMaxInterval - mMinInterval));
		lastTime += inTime;
		
		return lastTime;
	}

	@Override
	public synchronized void removeGeofences(Activity activity, List<Geofence> geofences) {
		super.removeGeofences(activity, geofences);
		lastTime = 0;
	}

}
