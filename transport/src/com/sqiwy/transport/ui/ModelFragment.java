/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.advertisement.Advertisement;
import com.sqiwy.transport.advertisement.AdvertisementManager;
import com.sqiwy.transport.advertisement.AdvertisementResource;
import com.sqiwy.transport.api.GetRouteResponse;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.event.EventEnterAdGeofence;
import com.sqiwy.transport.controller.event.EventEnterBusStopGeofence;
import com.sqiwy.transport.controller.event.EventExitBusStopGeofence;
import com.sqiwy.transport.data.GeoArea;
import com.sqiwy.transport.data.GeoAd;
import com.sqiwy.transport.data.GeoManager;
import com.sqiwy.transport.data.Point;
import com.sqiwy.transport.data.TransportProvider.Table;
import com.sqiwy.transport.data.TransportProviderHelper;
import com.sqiwy.transport.data.Vehicle;
import com.sqiwy.transport.data.VehicleLoader;
import com.sqiwy.transport.mocklocations.LocationUtils;
import com.sqiwy.transport.util.GeofencesHelper;
import com.sqiwy.transport.util.GeofencesHelperImpl;
import com.sqiwy.transport.util.PointKey;
import com.sqiwy.transport.util.PrefUtils;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Vehicle>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = ModelFragment.class.getSimpleName();

    private static final String ACTION_GEOFENCE_TRIGGERED
            = "com.sqiwy.transport.ACTION_GEOFENCE_TRIGGERED";
    private static final int UPDATE_ROUTE_INTERVAL = 5 * 60 * 1000;

    private final Handler mHandler = new Handler();
    private final UpdateRouteRunnable mUpdateRouteRunnable = new UpdateRouteRunnable();
    private GeofencesHelper mGeofencesHelper;
    private GeofenceTransitionReceiver mGeofenceTransitionReceiver;
    private int mRequestCode;
    private Vehicle mVehicle;
    private ContentObserver mAdsPointsObserver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        PrefUtils.registerOnPreferenceChangeListener(this);
        
        if (BuildConfig.DEBUG) {
        	Log.d(this.getClass().getName(), "onCreate");
        }

        mGeofencesHelper = new GeofencesHelperImpl();
//            mGeofencesHelper = new GeofencesHelperMockRandomImpl(1 * 60 * 1000, 4 * 60 * 1000);
//            mGeofencesHelper = new GeofencesHelperMockFixedImpl(
//            		new long[] {1 * 60 * 1000, 4 * 60 * 1000, 17 * 60 * 1000});
        mGeofenceTransitionReceiver = new GeofenceTransitionReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GEOFENCE_TRIGGERED);
        Context appContext = TransportApplication.getAppContext();
		appContext.registerReceiver(mGeofenceTransitionReceiver, intentFilter);

        mAdsPointsObserver = new ContentObserver(new Handler()) {
        	 // Implement the onChange(boolean) method to delegate the change notification to
        	 // the onChange(boolean, Uri) method to ensure correct operation on older versions
        	 // of the framework that did not have the onChange(boolean, Uri) method.
        	 @Override
        	 public void onChange(boolean selfChange) {
        	     onChange(selfChange, null);
        	 }

        	 // Implement the onChange(boolean, Uri) method to take advantage of the new Uri argument.
        	 @Override
        	 public void onChange(boolean selfChange, Uri uri) {
        	     setGeofences();
        	 }
		};
		appContext.getContentResolver()
			.registerContentObserver(Table.AdvertisementPoint.URI, true, mAdsPointsObserver);
        
        // Start the timer to update the route. Note that we don't need to update the route
        // in the background, so use Handler instead of AlarmManager for simplicity.
        mHandler.post(mUpdateRouteRunnable);

        getLoaderManager().initLoader(0, null, this);
        
        if (PrefUtils.getDemoMode() && !mMockServiceStarted) {
    		mMockServiceStarted = LocationUtils.startMockLocationsService(getActivity().getApplicationContext());
    	}
    }
    
    private OnGeofenceTriggered mGeofenceListener;
    
    public void setGeofenceListener(OnGeofenceTriggered listener) {
    	mGeofenceListener = listener;
    }
    
    public interface OnGeofenceTriggered {
    	public void geofenceTriggered(String geofenceType, Point point);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefUtils.unregisterOnPreferenceChangeListener(this);
        Context appContext = TransportApplication.getAppContext();
        appContext.getContentResolver().unregisterContentObserver(mAdsPointsObserver);
        appContext.unregisterReceiver(mGeofenceTransitionReceiver);
        mHandler.removeCallbacks(mUpdateRouteRunnable);
        mUpdateRouteTask.cancel(true);
        if (PrefUtils.getDemoMode()) {
        	mMockServiceStarted = LocationUtils.stopMockLocationsService(getActivity().getApplicationContext());
        }
        removeGeofences();
    }

	public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        mGeofencesHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Vehicle> onCreateLoader(int id, Bundle args) {
        return new VehicleLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Vehicle> loader, Vehicle vehicle) {
        changeVehicle(vehicle);
    }

    @Override
    public void onLoaderReset(Loader<Vehicle> loader) {
        changeVehicle(null);
    }

    private void changeVehicle(Vehicle vehicle) {
        if (mVehicle != vehicle) {
            mVehicle = vehicle;
            setGeofences();
        }
    }

    private void setGeofences() {
        removeGeofences();
        if (mVehicle != null) {
            addGeofences();
        }
    }

    private void addGeofences() {
    	GeoManager geoManager = GeoManager.getInstance();

        if (mVehicle.getBusStopCount() != 0) {
		    for (Point busStopPoint : mVehicle.getBusStops()) {
		    	busStopPoint.setRadius(mVehicle.getStopsRadius());
		        geoManager.addGeoArea(busStopPoint);
		    }
        }

        // Get and group ads by their locations
        // TODO: we may want to move it to AdvertisementManager
        // TODO: we may want to utilize SQLite query to group ads by location
        Map<PointKey, Set<AdvertisementResource>> groupedAds
			= new LinkedHashMap<PointKey, Set<AdvertisementResource>>();
        
        ArrayList<AdvertisementResource> ads = AdvertisementManager.getInstance().getAds();
        for (AdvertisementResource resource : ads) {
        	
        	boolean isGeoAd = false;
//            for (String trigger : resource.getAd().getTrigger()) {
//
//            }

            if (Advertisement.TRIGGER_ANY.equals(resource.getAd().getTrigger())) {
                isGeoAd = true;
                break;
            }else if (Advertisement.TRIGGER_GEO.equals(resource.getAd().getTrigger())) {
                isGeoAd = true;
                break;
            }else{
                isGeoAd = false;
            }

        	boolean hasPoints = null != resource.getAd().getPoints() && !resource.getAd().getPoints().isEmpty();
        	
        	if (isGeoAd && hasPoints) {
        		
        		for (Point point : resource.getAd().getPoints()) {
        			PointKey t = new PointKey(
        					point.getLatitude(), point.getLongitude(), point.getRadius());
        			
        			Set<AdvertisementResource> group = groupedAds.get(t);
        			if (null == group) {
        				group = new LinkedHashSet<AdvertisementResource>();
        				groupedAds.put(t, group);
        			}
        			group.add(resource);
        		}
        	}
        }
        
        for (Map.Entry<PointKey, Set<AdvertisementResource>> entry : groupedAds.entrySet()) {
        	
        	PointKey key = entry.getKey();
			
        	GeoAd geoAd = new GeoAd(
        			new GeoPoint(key.getLat(), key.getLon()),
        			key.getRadius(),
        			new ArrayList<AdvertisementResource>(entry.getValue()));
        	
        	geoManager.addGeoArea(geoAd);
        }
        
        if (null != geoManager.getGeofences() && !geoManager.getGeofences().isEmpty()) {
        	Intent intent = new Intent(ACTION_GEOFENCE_TRIGGERED);
            // mRequestCode should be different so we can have multiple request codes
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), ++mRequestCode,
                    intent, 0);
            mGeofencesHelper.addGeofences(getActivity(), geoManager.getGeofences(), pendingIntent);
        }
    }

    private void removeGeofences() {
        GeoManager geoManager = GeoManager.getInstance();
        if (null != geoManager.getGeofences() && !geoManager.getGeofences().isEmpty()) {
        	mGeofencesHelper.removeGeofences(getActivity(), geoManager.getGeofences());
        	geoManager.removeAllGeofences();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(PrefUtils.PREF_ACTIVE_ROUTE_INDEX)) {
            setGeofences();
        }
    }

    private class GeofenceTransitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int geofenceTransition = LocationClient.getGeofenceTransition(intent);
            final List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
            if (geofenceTransition != -1 && geofences != null && !geofences.isEmpty()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "geofence triggered: transition: " + geofenceTransition + ", geofences: " + geofences);
                }
                // TODO: what to do if several geofences have been triggered? what priority?
                GeoArea geo = GeoManager.getInstance().findGeoArea(geofences.get(0));
                Event event = null;
                if (geo instanceof Point) {
                    // TODO: correspondent data needs to be populated (bus stop name for instance etc.)
                    Point point = (Point) geo;
                    if (point.isBusStop()) {
                        setTrackingMarkerDetails(point, geofenceTransition);
                        if (Geofence.GEOFENCE_TRANSITION_ENTER == geofenceTransition) {
                        	mGeofenceListener.geofenceTriggered("GEOFENCE_TRANSITION_ENTER", point);
                            event = new EventEnterBusStopGeofence();
                        } else if (Geofence.GEOFENCE_TRANSITION_EXIT == geofenceTransition) {
                        	mGeofenceListener.geofenceTriggered("GEOFENCE_TRANSITION_EXIT", point);
                            event = new EventExitBusStopGeofence();
                            boolean isChanged = mVehicle.changeActiveRouteIfNeeded(point);
                            if (isChanged) {
                            	mGeofenceListener.geofenceTriggered("RESET", null);
                            }
                        }
                    }
                } else if (geo instanceof GeoAd) {
                    if (Geofence.GEOFENCE_TRANSITION_ENTER == geofenceTransition) {
                        event = new EventEnterAdGeofence(((GeoAd) geo).getAds());
                    }
                }
                if (event != null) {
                    TransportApplication.getScreenController().addEvent(event);
                } else {
                    Log.w(TAG, "Something went wrong and we failed to create required event.");
                }
            } else {
                Log.w(TAG, "Receiver has been triggered but intent has incorrect state: transition: "
                        + geofenceTransition + ", goefences: " + geofences);
            }
        }
    }

    private void setTrackingMarkerDetails(Point point, int geofenceTransition) {
        Location currentLocation = TransportApplication.getCurrentLocation();
        if (currentLocation != null) {
            point.setLatitude(currentLocation.getLatitude());
            point.setLongitude(currentLocation.getLongitude());
        }

        if (!mVehicle.isRouteUndefined()) {
            String sign = Geofence.GEOFENCE_TRANSITION_ENTER == geofenceTransition ? "+" : "-";
            point.setName(sign + mVehicle.getActiveRoute().getDirection().substring(0, 1).toUpperCase() + point.getOrder());
        }
    }

    private class UpdateRouteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            GetRouteResponse response = TransportApiHelper.getRoute();
            while (response == null) {
            	if (isCancelled()) {
            		return null;
            	}
            	if (BuildConfig.DEBUG) {
                	Log.d(this.getClass().getName(), "Waiting for vehicle");
                }
            	try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                response = TransportApiHelper.getRoute();
            }
            Vehicle vehicle = Vehicle.fromResponse(response);
            if (mVehicle == null || !mVehicle.isVersionMatch(vehicle)) {
                ContentResolver resolver = getActivity().getContentResolver();
                TransportProviderHelper.deleteAllVehicles(resolver);
                if (vehicle != null) {
                    TransportProviderHelper.insertVehicle(resolver, vehicle);
                }
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	if (BuildConfig.DEBUG) {
            	Log.d(this.getClass().getName(), "vehicle loaded");
            }
        	this.cancel(true);
        }
    }
    
    private UpdateRouteTask mUpdateRouteTask = new UpdateRouteTask();

    private class UpdateRouteRunnable implements Runnable {
        @Override
        public void run() {
        	if (BuildConfig.DEBUG) {
        		Log.d("ModelFragment", "mUpdateRouteTask");
        	}
        	mUpdateRouteTask.cancel(true);
        	mUpdateRouteTask = new UpdateRouteTask();
        	mUpdateRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mHandler.postDelayed(this, UPDATE_ROUTE_INTERVAL);
        }
    }
    
    private boolean mMockServiceStarted = false;

}
