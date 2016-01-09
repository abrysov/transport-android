/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

public class GeofencesHelperImpl implements GeofencesHelper, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener, LocationClient.OnRemoveGeofencesResultListener {
    private static final String TAG = GeofencesHelperImpl.class.getSimpleName();
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 1000;

    private enum RequestType {
        ADD_GEOFENCES,
        REMOVE_GEOFENCES
    }

    private boolean mAreGeofencesAdded;
    private boolean mIsRequestInProgress;
    private RequestType mRequestType;
    private Activity mActivity;
    private List<Geofence> mGeofences;
    private PendingIntent mPendingIntent;
    private LocationClient mLocationClient;

    @Override
    public void addGeofences(Activity activity, List<Geofence> geofences, PendingIntent pendingIntent) {
        if (!mAreGeofencesAdded && !mIsRequestInProgress) {
            startRequest(RequestType.ADD_GEOFENCES, activity, geofences, pendingIntent);
        }
    }
    
    @Override
    public void removeGeofences(Activity activity, List<Geofence> geofences) {
        if (mAreGeofencesAdded && !mIsRequestInProgress) {
            startRequest(RequestType.REMOVE_GEOFENCES, activity, geofences, null);
        }
    }

    private void startRequest(RequestType requestType, Activity activity, List<Geofence> geofences,
                              PendingIntent pendingIntent) {
        mIsRequestInProgress = true;
        mRequestType = requestType;
        mActivity = activity;
        mGeofences = geofences;
        mPendingIntent = pendingIntent;
        mLocationClient = new LocationClient(activity, this, this);
        mLocationClient.connect();
    }

    private void clearRequest() {
        mIsRequestInProgress = false;
        mRequestType = null;
        mActivity = null;
        mGeofences = null;
        mPendingIntent = null;
        mLocationClient = null;
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Try to reconnect to the LocationClient.
                mLocationClient.connect();
            } else {
                clearRequest();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (mRequestType) {
            case ADD_GEOFENCES:
                mLocationClient.addGeofences(mGeofences, mPendingIntent, this);
                break;

            case REMOVE_GEOFENCES:
            	if (mGeofences != null && !mGeofences.isEmpty()) {
	            	List<String> ids = new ArrayList<String>(mGeofences.size());
	            	for (Geofence geofence : mGeofences) {
	            		ids.add(geofence.getRequestId());
	            	}
                    mLocationClient.removeGeofences(ids, this);
            	}
                break;
        }
    }

    @Override
    public void onDisconnected() {
        clearRequest();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        mActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                clearRequest();
                Log.w(TAG, "Failed to start resolution for connection failure", e);
            }
        } else {
            clearRequest();
        }
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (statusCode == LocationStatusCodes.SUCCESS) {
            mAreGeofencesAdded = true;
        } else {
            Log.w(TAG, "Failed to add geofences, status code = " + statusCode);
        }
        mLocationClient.disconnect();
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
        if (statusCode != LocationStatusCodes.SUCCESS) {
            Log.w(TAG, "Failed to remove geofences, status code = " + statusCode);
        }
        mLocationClient.disconnect();
    }
}
