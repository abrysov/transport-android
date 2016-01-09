/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 */
public final class DownloadManagerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		
		if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			
			DownloadManagerService.downloadComplete(context, intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
		}
	}
}
