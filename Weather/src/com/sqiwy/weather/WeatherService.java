package com.sqiwy.weather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sqiwy.weather.YahooWeatherApiClient.LocationInfo;

/**
 * Created by abrysov
 * Bounded service to provide weather data.
 */
public class WeatherService extends Service {
	
	private static final String TAG = WeatherService.class.getName();
	
    private final IBinder mBinder;
    private final Criteria mLocationCriteria;
    
    private WeatherData mWeatherData;
    
    private volatile long mUpdateInterval = 45000; // default is 45 seconds

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    
    
	public WeatherService() {
		mBinder = new WeatherBinder();
		
        mLocationCriteria = new Criteria();
        mLocationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        mLocationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        mLocationCriteria.setCostAllowed(false);
	}

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            
        	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = lm.getBestProvider(mLocationCriteria, true);
            if (!TextUtils.isEmpty(provider)) {
	            final Location lastLocation = lm.getLastKnownLocation(provider);
                if (lastLocation != null) {
                    try {
                        YahooWeatherApiClient.setWeatherUnits("c");
                        LocationInfo locationInfo = YahooWeatherApiClient.getLocationInfo(lastLocation);
                        mWeatherData = YahooWeatherApiClient.getWeatherForLocationInfo(locationInfo);

                        WeatherProvider.setWeatherData(getApplicationContext(), mWeatherData);

                    } catch (CantGetWeatherException e) {
                        Log.e(TAG, "Error while getting weather data: " + e.getMessage(), e);
                    }
                }
            }
        	
    		mServiceHandler.sendEmptyMessageDelayed(0, mUpdateInterval);
        }
    }
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class WeatherBinder extends Binder {
    	WeatherService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WeatherService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        HandlerThread thread = new HandlerThread("WeatherService-Handler");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        
        mServiceHandler.sendEmptyMessage(0);
    }

    @Override
    public void onDestroy() {
    	mServiceHandler.removeCallbacksAndMessages(null);
        mServiceLooper.quit();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /* Methods for clients */
    
    /**
     * Gets latest weather data.
     * @return latest weather data.
     */
    public WeatherData getWeatherData() {
      return mWeatherData;
    }
    
    /**
     * Sets weather update interval.
     * @param updateInterval interval in millis.
     */
    public void setUpdateInterval(long updateInterval) {
    	mUpdateInterval = updateInterval;
    }
	
}
