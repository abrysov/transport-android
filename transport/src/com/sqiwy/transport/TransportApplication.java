/**
 * Created by abrysov
 */
package com.sqiwy.transport;


import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.sqiwy.transport.controller.ScreenController;
import com.sqiwy.transport.controller.ScreenControllerImpl;
import com.sqiwy.transport.controller.screen.*;
import com.sqiwy.transport.data.Vehicle;
import com.sqiwy.transport.data.VehicleLoader;
import com.sqiwy.transport.util.SystemControllerHelper;

public class TransportApplication extends Application {

    private static Context sContext;
    private static ScreenController sScreenController;

    private static volatile Location sLocation;
    private static volatile Vehicle sVehicle;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sScreenController = new ScreenControllerImpl();
        
        VehicleLoader vehicleLoader = new VehicleLoader(this);
        Vehicle vehicle = vehicleLoader.loadInBackground();
        if (vehicle != null) {
        	if (BuildConfig.DEBUG) {
            	Log.d(this.getClass().getName(), "vehicle.getGuid() = " + vehicle.getGuid());
            }
	        sScreenController.addScreen(new MapScreen(0, 10000, vehicle.getMapRatio()));
	        sScreenController.addScreen(new AdScreen(2, 5000, vehicle.getAdsRatio()));
	        sScreenController.addScreen(new NewsScreen(1, 5000, vehicle.getContentRatio() / 3));
	        sScreenController.addScreen(new HoroscopeScreen(3, 5000, vehicle.getContentRatio() / 3));
            sScreenController.addScreen(new MultiCurrencyScreen(4, 5000, vehicle.getContentRatio() / 3));
	        sScreenController.start();
        } else {
        	mWaitVehicle.cancel(true);
        	mWaitVehicle = new WaitVehicleTask();
        	mWaitVehicle.execute(vehicleLoader);
        }
        SystemControllerHelper.setSystemUiMode(sContext,
                SystemControllerHelper.SYSTEM_UI_MODE_DISABLE_ALL);
    }
    
    private WaitVehicleTask mWaitVehicle = new WaitVehicleTask();
    
    private class WaitVehicleTask extends AsyncTask<VehicleLoader, Void, Vehicle> {
    	
        protected Vehicle doInBackground(VehicleLoader... loaders) {
        	VehicleLoader vehicleLoader = loaders[0];
            Vehicle vehicle = vehicleLoader.loadInBackground();
            while (vehicle == null && !isCancelled()) {
            	if (BuildConfig.DEBUG) {
                	Log.d(this.getClass().getName(), "Waiting for vehicle");
                }
            	try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	vehicle = vehicleLoader.loadInBackground();
            }
        	return vehicle;
        }
        
        protected void onPostExecute(Vehicle vehicle) {
        	if (vehicle != null) {
		        sScreenController.addScreen(new MapScreen(0, 10000, vehicle.getMapRatio()));
		        sScreenController.addScreen(new AdScreen(2, 5000, vehicle.getAdsRatio()));
		        sScreenController.addScreen(new NewsScreen(1, 5000, vehicle.getContentRatio() / 3));
		        sScreenController.addScreen(new HoroscopeScreen(3, 5000, vehicle.getContentRatio() / 3));
                sScreenController.addScreen(new MultiCurrencyScreen(4, 5000, vehicle.getContentRatio() / 3));
		        sScreenController.start();
        	}
        	mWaitVehicle.cancel(true);
        }
    }
    
    @Override
    public void onTerminate() {
    	super.onTerminate();
    	if (!mWaitVehicle.isCancelled()) {
    		mWaitVehicle.cancel(true);
    	}
    }

    public static Context getAppContext() {
        return sContext;
    }
    
    public static ScreenController getScreenController() {
    	return sScreenController;
    }

    public static void setCurrentLocation(Location mLocation) {
        sLocation = mLocation;
    }

    public static Location getCurrentLocation() {
        return sLocation;
    }

    public static Vehicle getVehicle() {
        return sVehicle;
    }

    public static void setVehicle(Vehicle mVehicle) {
        sVehicle = mVehicle;
    }
}
