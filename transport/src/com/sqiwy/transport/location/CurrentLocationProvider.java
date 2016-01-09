/**
 * Created by abrysov
 */
package com.sqiwy.transport.location;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.sqiwy.transport.TransportApplication;

public class CurrentLocationProvider implements IMyLocationProvider,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private static final String TAG = CurrentLocationProvider.class.getName();
	
	private final Context mContext;
	private IMyLocationConsumer mMyLocationConsumer;
	private LocationClient mLocationClient;
	private Location mLocation;
	
	public CurrentLocationProvider(Context context) {
	    mContext = context;
	}
	
	@Override
	public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
	    mMyLocationConsumer = myLocationConsumer;
	    mLocationClient = new LocationClient(mContext, this, this);
	    mLocationClient.connect();
	    return true;
	}
	
	@Override
	public void stopLocationProvider() {
	    if (mLocationClient != null && mLocationClient.isConnected()) {
	        mLocationClient.removeLocationUpdates(this);
	        mLocationClient.disconnect();
	    }
	}
	
	@Override
	public Location getLastKnownLocation() {
	    return mLocation;
	}
	
	@Override
	public void onConnected(Bundle bundle) {
	    if (mLocationClient.isConnected()) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(500);

            mLocationClient.requestLocationUpdates(locationRequest, this);
        }
	}
	
	@Override
	public void onDisconnected() {
	    reset();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	    reset();
	}
	
	private void reset() {
	    mMyLocationConsumer = null;
	    mLocationClient = null;
	}
	
	@Override
	public void onLocationChanged(Location location) {
	    Log.d(TAG, "*** called onLocationChanged()");
	
	    mLocation = location;
	
	    TransportApplication.setCurrentLocation(mLocation);
	
	    if (mMyLocationConsumer != null) {
	        mMyLocationConsumer.onLocationChanged(location, this);
	    }
	}
}
