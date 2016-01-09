package com.sqiwy.weather;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.sqiwy.weather.WeatherService.WeatherBinder;
/**
 * Created by abrysov
 */
public class WeatherServiceHelper {

	WeatherService mService;
    boolean mBound = false;
    
    public void bind(Context context) {
		Intent intent = new Intent(context, WeatherService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unbind(Context context) {
		// Unbind from the service
        if (mBound) {
            context.unbindService(mConnection);
            mBound = false;
        }
	}
	
	public WeatherData getWeatherData(Context context) {
		return WeatherProvider.getWeatherData(context);
	}
	
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
    	
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WeatherBinder binder = (WeatherBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}