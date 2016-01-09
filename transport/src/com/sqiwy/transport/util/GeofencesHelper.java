/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

import com.google.android.gms.location.Geofence;

public interface GeofencesHelper {

    public void addGeofences(Activity activity, List<Geofence> geofences, PendingIntent pendingIntent);

    public void removeGeofences(Activity activity, List<Geofence> geofences);

    public void handleActivityResult(int requestCode, int resultCode, Intent data);

}
