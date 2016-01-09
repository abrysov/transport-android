/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;

public final class PrefUtils {
    public static final String PREF_DEVICE_GUID = "pref_device_guid";
    public static final String PREF_DEMO_MODE = "pref_demo_mode";
    public static final String PREF_MAP_ONLY = "pref_map_only";
    public static final String PREF_PAUSE = "pref_pause";
    public static final String PREF_SEND_INTERVAL = "pref_send_interval";
    public static final String PREF_SERVER_ADDRESS = "pref_server_address";
    public static final String PREF_DEVICE_NUMBER = "pref_device_number";
    public static final String PREF_ACTIVE_ROUTE_INDEX = "pref_active_route_index";
    public static final String PREF_PREVIOUS_BUS_STOP_INDEX = "pref_previous_bus_stop_index";
    public static final String PREF_NEXT_NEWS_INDEX = "pref_next_news_index";
    public static final String PREF_NEXT_HOROSCOPE_INDEX = "pref_next_horoscope_index";
    public static final String PREF_NEXT_CURRENCY_INDEX = "pref_next_currency_index";

    static {
        PreferenceManager.setDefaultValues(TransportApplication.getAppContext(),
                R.xml.preferences, false);
    }

    private PrefUtils() {
    }

    private static SharedPreferences getDefaultPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(TransportApplication.getAppContext());
    }

    public static void registerOnPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDefaultPrefs().registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getDefaultPrefs().unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static String getDeviceGuid() {
        return getDefaultPrefs().getString(PREF_DEVICE_GUID, "");
    }
    
    public static boolean getMapOnly() {
        return getDefaultPrefs().getBoolean(PREF_MAP_ONLY, false);
    }

    public static void setDeviceGuid(String guid) {
        getDefaultPrefs().edit().putString(PREF_DEVICE_GUID, guid).commit();
    }

    public static String getServerAddress() {
        return getDefaultPrefs().getString(PREF_SERVER_ADDRESS, "");
    }
    
    public static boolean getDemoMode() {
        return getDefaultPrefs().getBoolean(PREF_DEMO_MODE, false);
    }

    public static String getDeviceNumber() {
        return getDefaultPrefs().getString(PREF_DEVICE_NUMBER, "");
    }
    
    public static int getPause() {
        return Integer.parseInt(getDefaultPrefs().getString(PREF_PAUSE, "5"));
    }
    
    public static int getSendInterval() {
        return Integer.parseInt(getDefaultPrefs().getString(PREF_SEND_INTERVAL, "5"));
    }

    public static int getActiveRouteIndex() {
        return getDefaultPrefs().getInt(PREF_ACTIVE_ROUTE_INDEX, -1);
    }

    public static void setActiveRouteIndex(int index) {
        getDefaultPrefs().edit().putInt(PREF_ACTIVE_ROUTE_INDEX, index).commit();
    }

    public static int getPreviousBusStopIndex() {
        return getDefaultPrefs().getInt(PREF_PREVIOUS_BUS_STOP_INDEX, -1);
    }

    public static void setPreviousBusStopIndex(int index) {
        getDefaultPrefs().edit().putInt(PREF_PREVIOUS_BUS_STOP_INDEX, index).commit();
    }

    public static int getNextNewsIndex() {
        return getDefaultPrefs().getInt(PREF_NEXT_NEWS_INDEX, -1);
    }

    public static void setNextNewsIndex(int index) {
        getDefaultPrefs().edit().putInt(PREF_NEXT_NEWS_INDEX, index).commit();
    }


    public static void setNextHoroscopesIndex(int index) {
        getDefaultPrefs().edit().putInt(PREF_NEXT_HOROSCOPE_INDEX, index).commit();
    }
    public static int getNextHoroscopeIndex() {
        return getDefaultPrefs().getInt(PREF_NEXT_HOROSCOPE_INDEX, -1);
    }

    public static void setNextCurrencyIndex(int index) {
        getDefaultPrefs().edit().putInt(PREF_NEXT_CURRENCY_INDEX, index).commit();
    }
    public static int getNextCurrencyIndex() {
        return getDefaultPrefs().getInt(PREF_NEXT_CURRENCY_INDEX, -1);
    }
}
