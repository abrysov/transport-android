package com.sqiwy.weather;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by abrysov
 */
public class WeatherActivityExample extends Activity {
	
	private WeatherServiceHelper weather;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		weather = new WeatherServiceHelper();
		
		handler = new Handler();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        
        weather.bind(this);
        
        handler.postDelayed(new Runnable() {
			
			public void run() {
				
				WeatherData weatherData = WeatherProvider.getWeatherData(WeatherActivityExample.this);
				if (null != weatherData) {
					Toast.makeText(WeatherActivityExample.this, "Weather: " + weatherData.location + " " + weatherData.temperature, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(WeatherActivityExample.this, "Weather not available.", Toast.LENGTH_LONG).show();
				}
				
				handler.postDelayed(this, 5000);
			}
			
		}, 5000);
    }
	
    @Override
    protected void onStop() {
        super.onStop();
        
        handler.removeCallbacksAndMessages(null);
        
        weather.unbind(this);
    }
}
