/**
 * Created by abrysov
 */
package com.sqiwy.transport.advertisement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sqiwy.ljmenu.dmanager.DownloadManagerService;
import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.data.TransportProviderHelper;

/**
 * Marks resources with corresponding URIs as downloaded.
 */
public class AdvertisementLoaderReceiver extends BroadcastReceiver {

	private static final String TAG = AdvertisementLoaderReceiver.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String downloadUri = intent.getStringExtra(DownloadManagerService.ARG_DOWNLOAD_URI);
		String localUri = intent.getStringExtra(DownloadManagerService.ARG_LOCAL_URI);
		
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Downloaded: " + downloadUri + " to " + localUri);
		}
		
		TransportProviderHelper.markResourcesDownloaded(context.getContentResolver(), downloadUri, localUri);
	}

}
