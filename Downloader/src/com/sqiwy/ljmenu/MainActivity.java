package com.sqiwy.ljmenu;

import java.io.File;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.sqiwy.ljmenu.dmanager.DownloadManagerService;
import com.sqiwy.ljmenu.dmanager.utils.Logger;
/**
 * Created by abrysov
 */
public class MainActivity extends Activity {

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Logger.initialize("Downloader", true);
		
		setContentView(R.layout.activity_main);
		
		DownloadManagerService.cancelDownload(this, "https://dl.dropbox.com/s/rih4gstq9ws2fn8/1000K.dat");
		DownloadManagerService.removeDownload(this, "https://dl.dropbox.com/s/rih4gstq9ws2fn8/1000K.dat");
		DownloadManagerService.downloadFile(this, "https://dl.dropbox.com/s/rih4gstq9ws2fn8/1000K.dat", Uri.fromFile(new File(getLocalFile())).toString());
		DownloadManagerService.cancelDownload(this, "https://dl.dropbox.com/s/rih4gstq9ws2fn8/1000K.dat");
	}

	/**
	 * 
	 * @return
	 */
	private String getLocalFile() {
		
		return new File(Environment.getExternalStorageDirectory() + File.separator + "lj-menu-downloader", "download.dat").getAbsolutePath();
	}
}
