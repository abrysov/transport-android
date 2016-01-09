/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
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
 */
public abstract class GeofencesHelperMockBaseImpl implements GeofencesHelper {

	private static final String TAG = "GeofencesHelperMockImpl";

	private final Map<String, Pair<Geofence, PendingIntent>> mGeofences;
	private final Map<String, Pair<Geofence, PendingIntent>> mFiredGeofences;
	
	private final Handler mHandler;
	
	private long counter;
	
	public GeofencesHelperMockBaseImpl() {
		mGeofences = new ConcurrentHashMap<String, Pair<Geofence, PendingIntent>>();
		mFiredGeofences = new ConcurrentHashMap<String, Pair<Geofence, PendingIntent>>();
		
		mHandler = new GeoHandler();
	}
	
	@Override
	public synchronized void addGeofences(Activity activity, List<Geofence> geofences, PendingIntent pendingIntent) {
		
		for (Geofence geofence : geofences) {
			
			String id = String.valueOf(++counter);
			mGeofences.put(id, new Pair<Geofence, PendingIntent>(geofence, pendingIntent));
			
			Message message = mHandler.obtainMessage(GeoHandler.WHAT_FIRE);
			message.obj = id;
			
			long time = getTimeForNextGeofence(geofence, pendingIntent);
			
			Log.d(TAG, "PendingIntent [" + id + "] scheduled for " + time);
			long in = time - SystemClock.uptimeMillis();
			long inMillis = in % 1000;
			long inSeconds = in / 1000;
			long inMinutes = inSeconds / 60;
			inSeconds = inSeconds % 60;
			Log.d(TAG, " *** in " + inMinutes + " mins " + inSeconds + " secs " + inMillis + " millis");
			
			mHandler.sendMessageAtTime(message, time);
		}
	}
	
	protected abstract long getTimeForNextGeofence(Geofence geofence, PendingIntent pendingIntent);

	@Override
	public synchronized void removeGeofences(Activity activity, List<Geofence> geofences) {
		mHandler.removeCallbacksAndMessages(null);
		mGeofences.clear();
		mFiredGeofences.clear();
		counter = 0;
	}

	@Override
	public void handleActivityResult(int requestCode, int resultCode, Intent data) {
		// Do nothing
	}

	class GeoHandler extends Handler {
		
		static final int WHAT_FIRE = 1;
		
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case WHAT_FIRE:
				
				String id = (String) msg.obj;
				
				Pair<Geofence, PendingIntent> pair = mGeofences.get(id);
				mFiredGeofences.put(id, pair);
				
				try {
					pair.second.send();
					
					Log.d(TAG, "PendingIntent [" + id + "] sent: " + pair.second);
					
				} catch (CanceledException e) {
					
					Log.e(TAG, "Error while sending PendingIntent.", e);
				}
				
				break;
			}
			
		}
	}
}
