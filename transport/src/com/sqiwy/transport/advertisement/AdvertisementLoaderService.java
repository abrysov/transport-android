package com.sqiwy.transport.advertisement;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sqiwy.ljmenu.dmanager.DownloadManagerService;
import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.api.GetAdsResponse;
import com.sqiwy.transport.api.TransportApi;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.data.TransportProviderHelper;
import com.sqiwy.transport.util.PrefUtils;

/**
 * Created by abrysov
 */

public class AdvertisementLoaderService extends IntentService {

	private static final boolean DBG = BuildConfig.DEBUG;
	private static final String TAG = AdvertisementLoaderService.class.getName();
	
	public AdvertisementLoaderService() {
		super(AdvertisementLoaderService.class.getName());
	}

	public static void start(Context context) {
		if (DBG) {
			Log.d(TAG, "AdvertisementLoaderService start");
		}
		context.startService(new Intent(context, AdvertisementLoaderService.class));
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			load(intent);
		} catch (Exception e) {
			Log.e(TAG, "Error while loading ads", e);
		} finally {
			// Schedule next load using AlarmManager
			AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			PendingIntent operation = PendingIntent.getBroadcast(getApplicationContext(), 0,
					new Intent(getApplicationContext(), ScheduledLoadReceiver.class),
					PendingIntent.FLAG_UPDATE_CURRENT);
			// TODO: change to appropriate time
			am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5* 60 * 1000, operation);
		}
	}

	private boolean load(Intent intent) {
		Log.i(TAG, "load");
		boolean changed = false;
		
		TransportApi api = TransportApiHelper.getApi();

		String deviceGuid = PrefUtils.getDeviceGuid();
		
		// Get ads
		GetAdsResponse adsResponse = api.getAds(deviceGuid);
		
		if (adsResponse.getAds() == null) {
			return changed;
		}
		
		if (DBG) {
			Log.d(TAG, "loading ads: " + adsResponse.getAds().size());
		}
		
		for (Advertisement ad : adsResponse.getAds()) {
			if (DBG) {
				Log.d(TAG, "loading ad: " + ad.getGuid() + "[version:" + ad.getVersion() + "]");
			}
			Advertisement oldAd = TransportProviderHelper.queryAd(getContentResolver(), ad.getGuid());
			
			if (DBG) {
				boolean adExist = oldAd != null;
				Log.d(TAG, ad.getGuid() + " : exist = " + adExist);
				if (adExist) {
					Log.d(TAG, ad.getGuid() + " : [verson:" + oldAd.getVersion() + "]");
				}
			}
			if (null != oldAd && oldAd.getVersion() == ad.getVersion()) {
				// No need for ad update. Continue to next ad.
				continue;
			}
			
			// Insert or override ad
			TransportProviderHelper.insertAd(getContentResolver(), ad);
			changed = true;
			
			// Cancel and delete old resource
			if (null != oldAd) {

                for (AdvertisementResource resource : oldAd.getResources()) {
                    // Cancel old resource download
                    if (AdvertisementResource.DOWNLOADING == resource.getStatus()) {
                        DownloadManagerService.cancelDownload(
                                getApplicationContext(), resource.getUri());
                    }

                    // Delete old resource from the external storage.
                    String path = Uri.parse(resource.getStorageUri()).getPath();

                    File file = new File(path);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            try {
                                FileUtils.deleteDirectory(file);
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to delete directory: " + String.valueOf(file));
                            }
                        } else {
                            FileUtils.deleteQuietly(file);
                        }
                    }

                    // Delete old resource from the db
                    TransportProviderHelper.deleteRes(getContentResolver(), resource.getId());
                }
                TransportProviderHelper.deleteAd(getContentResolver(), oldAd.getId());
			}

            for (AdvertisementResource resource : ad.getResources()) {
                // Insert and download new resource
                resource.setStatus(AdvertisementResource.DOWNLOADING);

                // Generate access and storage uri (backend doesn't return these values)
                // TODO: introduct Resources/Storage Manager
                String name = System.currentTimeMillis() + ".ad";
                File adFolder = new File(getApplicationContext().getExternalFilesDir(null), "ads");
                if (!adFolder.exists()) {
                    adFolder.mkdir();
                }
                String uri = Uri.fromFile(new File(adFolder, name)).toString();
                resource.setStorageUri(uri);
                resource.setAccessUri(uri);

                // Insert new resource.
                TransportProviderHelper.insertRes(getContentResolver(), resource, ad.getGuid());
                
                if (DBG) {
                	Log.d(TAG, "Start downloading resource: " + resource.getUri() + " -> " + resource.getStorageUri());
                }
                
                // Start downloading resource
                DownloadManagerService.downloadFile(getApplicationContext(),
                        resource.getUri(), resource.getStorageUri());

            }
		}
		
		return changed;
	}
	
	/**
	 * Is intended to launch service (load ads operation) in response to pending intent, fired
	 * by AlarmManager.
	 */
	public static class ScheduledLoadReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			AdvertisementLoaderService.start(context);
		}
	}
}
