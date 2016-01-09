/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager.utils;

import android.content.Context;
import android.util.Log;

/**
 * Helper class for easy logging
 */
public class Logger {
	
	/**
	 * 
	 */
	protected static String APP_TAG = "APP_TAG";
	protected static boolean LOG_ENABLED = false;
	
	/**
	 * Initializes Logger
	 * @param appTag Tag which indentifies app in android log
	 */
	public final static void initialize(String appTag, boolean logEnabled) {
		
		APP_TAG = appTag;
		LOG_ENABLED = logEnabled;
	}
	
	/**
	 * Initializes Logger. As log tag the last part of application package 
	 * name is used 
	 * @param context Android context
	 */
	public final static void initialize(Context context, boolean logEnabled) {
		
		initialize(context, false, logEnabled);
	}

	/**
	 * Initializes Logger.
	 * @param context Android context
	 * @param usePackageName true if log tag is full app package name,
	 * 		false if log tag is last part of application package name
	 */
	public final static void initialize(Context context, boolean usePackageName, boolean logEnabled) {

		String appTag = context.getPackageName();
		
		if(false == usePackageName) {
		
			appTag = appTag.substring(appTag.lastIndexOf('.') + 1);
		}
		
		APP_TAG = appTag;
		LOG_ENABLED = logEnabled;
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void wtf(String tag, String msg) {
		
		if(LOG_ENABLED) {
		
			Log.wtf(APP_TAG, formatMessage(tag, msg));
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param throwable
	 */
	public static void wtf(String tag, String msg, Throwable throwable) {
		
		if(LOG_ENABLED) {
		
			Log.wtf(APP_TAG, formatMessage(tag, msg), throwable);
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void e(String tag, String msg) {
		
		if(LOG_ENABLED) {
		
			Log.e(APP_TAG, formatMessage(tag, msg));
		}
	}	
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param throwable
	 */
	public static void e(String tag, String msg, Throwable throwable) {
		
		if(LOG_ENABLED) {
		
			Log.e(APP_TAG, formatMessage(tag, msg), throwable);
		}
	}	
	
	/**
	 * 	
	 * @param tag
	 * @param msg
	 */
	public static void d(String tag, String msg) {
		
		if(LOG_ENABLED) {
		
			Log.d(APP_TAG, formatMessage(tag, msg));
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void d(String tag, String msg, Throwable throwable) {
		
		if(LOG_ENABLED) {
		
			Log.d(APP_TAG, formatMessage(tag, msg), throwable);
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void w(String tag, String msg) {
		
		if(LOG_ENABLED) {
		
			Log.w(APP_TAG, formatMessage(tag, msg));
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void w(String tag, String msg, Throwable throwable) {
		
		if(LOG_ENABLED) {
		
			Log.w(APP_TAG, formatMessage(tag, msg), throwable);
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void v(String tag, String msg) {
		
		if(LOG_ENABLED) {
		
			Log.v(APP_TAG, formatMessage(tag, msg));
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param throwable
	 */
	public static void v(String tag, String msg, Throwable throwable) {
		
		if(LOG_ENABLED) {
		
			Log.v(APP_TAG, formatMessage(tag, msg), throwable);
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void i(String tag, String msg) {
		
		if(LOG_ENABLED) {
		
			Log.i(APP_TAG, formatMessage(tag, msg));
		}
	}
	
	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param throwable
	 */
	public static void i(String tag, String msg, Throwable throwable) {
		
		if(LOG_ENABLED) {
		
			Log.i(APP_TAG, formatMessage(tag, msg), throwable);
		}
	}
	
	/**
	 * 
	 * @param msg
	 * @param tag
	 * @param sensitive
	 * @param throwable
	 * @return
	 */
	private static String formatMessage(String tag, String msg) {
		
		return "[" + tag + "] " + msg;
	}
}
