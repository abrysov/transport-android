package com.sqiwy.weather;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Created by abrysov
 * Simple class to persist weather data.
 */
public class WeatherProvider {

	private static final String WEATHER = "weather";
	
	public static WeatherData getWeatherData(Context context) {
		WeatherData result = null;
		
		if (null != context) {
		
			SharedPreferences prefs = context.getSharedPreferences(WEATHER, Context.MODE_PRIVATE);
			
			String weatherStr = prefs.getString(WEATHER, null);
			
			if (null != weatherStr) {
				Gson gson = new Gson();
				result = gson.fromJson(weatherStr, WeatherData.class);
			}
		}
		
		return result;
	}
	
	public static void setWeatherData(Context context, WeatherData weatherData) {
		
		if (null != context) {
			SharedPreferences prefs = context.getSharedPreferences(WEATHER, Context.MODE_PRIVATE);
			
			String weatherStr = null;
			if (null != weatherData) {
				Gson gson = new Gson();
				weatherStr = gson.toJson(weatherData);
			}
		
			prefs.edit().putString(WEATHER, weatherStr).commit();
		}
	}
	
}
