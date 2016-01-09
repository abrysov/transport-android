/**
 * Created by abrysov
 */
package com.sqiwy.transport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.sqiwy.transport.api.AdReport;
import com.sqiwy.transport.api.TransportApiHelper;
import com.squareup.tape.FileObjectQueue;

public class StatsUtils {

	private static final String TAG = StatsUtils.class.getName();
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static int STATS_DELAY = 10000; // ms
	
	private static ExecutorService mStatsExecutor = null;
	
	public static void reportAdvertisementStats(String adGuId, String contentGuId, String routeGuid, String startTime, long duration) {
		Map<String, String> stats = new HashMap<String, String>();
		stats.put("ad_guid", adGuId);
		stats.put("content_guid", contentGuId);
		stats.put("route_guid", routeGuid);
		stats.put("start_time", startTime);
		stats.put("duration", String.valueOf(duration));
		reportStats(stats);
	}
	
	private static void reportStats(Map<String, String> stats) {
		StatsQueue.add(stats);
		if (mStatsExecutor == null) {
			mStatsExecutor = Executors.newSingleThreadExecutor();
		}
		new SendReportTask().executeOnExecutor(mStatsExecutor, (Void)null);
		new DelayTask().executeOnExecutor(mStatsExecutor, (Void)null);
	}
	
	private static class DelayTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(STATS_DELAY);
			} catch (InterruptedException ex) {}
			return null;
		}
	}
	
	private static class SendReportTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			//TODO send stats to server
			Map<String, String> stats;
			
			if (!isConnectedToNetwork()) {
				return null;
			}
			ArrayList<AdReport> adReports = new ArrayList<AdReport>();
			while (true) {
				try {
					stats = StatsQueue.peek();
				} catch (Throwable ex) {
					break;
				}
				if (stats==null) {
					break;
				}
				adReports.add(new AdReport(stats));
				try {
					// Remove stats from queue.
					StatsQueue.remove();
				} catch (Exception e) {
					Log.d(TAG, "Failed to report stats", e);
					// Just exit, we'll try to report stats next time.
					return null;
				}
			}
			if (DEBUG) {
				Log.d(TAG, "Reporting ads");
			}
			TransportApiHelper.sendAdReports(adReports);
			return null;
		}
	}
	
	private static boolean isConnectedToNetwork() {
		ConnectivityManager connectivityManager;
		NetworkInfo networkInfo;
		boolean isConnected;
		connectivityManager = (ConnectivityManager)TransportApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		networkInfo = connectivityManager.getActiveNetworkInfo(); 
		isConnected = false;
		if (networkInfo != null) {
			isConnected = networkInfo.isConnected();
		}
		return isConnected;
	}
	
	public static final class StatsQueue {
		
		private static FileObjectQueue<MapWrapper> sStatsQueue;
		
		private static synchronized FileObjectQueue<MapWrapper> get() {
			if (null == sStatsQueue) {
				try {
					sStatsQueue = new FileObjectQueue<MapWrapper>(new File(Environment.getExternalStorageDirectory(), ".stats"),
							new GsonConverter<MapWrapper>(new Gson(), MapWrapper.class));
				} catch (IOException e) {
					Log.e(TAG, "Couldn't create queue file for stats.", e);
				}
			}
			return sStatsQueue;
		}
		
		public synchronized static void add(Map<String, String> stats) {
			get().add(new MapWrapper(stats));
		}
		
		public synchronized static Map<String, String> peek() {
			Map<String, String> value = null;
			MapWrapper wrapper = get().peek();
			if (null != wrapper) {
				value = wrapper.value;
			}
			return value;
		}
		
		public synchronized static void remove() {
			get().remove();
		}
		
		private static class MapWrapper {
			Map<String, String> value;
			
			public MapWrapper(Map<String, String> value) {
				this.value = value;
			}
		}
		
		/**
		 * Use GSON to serialize stats.
		 */
		static class GsonConverter<T> implements FileObjectQueue.Converter<T> {
			private final Gson gson;
			private Class<T> type;

			public GsonConverter(Gson gson, Class<T> type) {
				this.gson = gson;
				this.type = type;
			}

			@Override
			public T from(byte[] bytes) {
				Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes));
				return gson.fromJson(reader, type);
			}

			@Override
			public void toStream(T object, OutputStream bytes) throws IOException {
				Writer writer = new OutputStreamWriter(bytes);
				gson.toJson(object, writer);
				writer.close();
			}
		}
	}
}