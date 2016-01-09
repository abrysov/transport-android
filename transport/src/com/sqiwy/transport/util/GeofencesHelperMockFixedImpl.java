/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.location.Geofence;

/**
 * Mock implementation of geofences helper in order to be able to test geofences functionality in office.
 * 
 * It's assumed here that sequential geofences (they have correct order);
 * 
 * NOTE: number of geofences should not exceed number of items in timing array.
 */
public class GeofencesHelperMockFixedImpl extends GeofencesHelperMockBaseImpl {

	private long[] mTiming;
	private int mIndex = 0;
	private long mCreationUpTime;
	
	/**
	 * @param timing for instance [ 1 * 60 * 1000, 3 * 60 * 1000, .... ], items in array are time in millis.
	 */
	public GeofencesHelperMockFixedImpl(long[] timing) {
		mTiming = timing;
		mCreationUpTime = SystemClock.uptimeMillis();
	}
	
	@Override
	protected long getTimeForNextGeofence(Geofence geofence, PendingIntent pendingIntent) {
		return mCreationUpTime + mTiming[mIndex++];
	}

	@Override
	public synchronized void removeGeofences(Activity activity, List<Geofence> geofences) {
		super.removeGeofences(activity, geofences);
		mIndex = 0;
	}

}
