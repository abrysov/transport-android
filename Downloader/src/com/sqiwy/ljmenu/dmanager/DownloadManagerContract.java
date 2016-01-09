/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DownloadManagerContract {

	/** authority */
	public static final String AUTHORITY = "com.sqiwy.ljmenu.dmanager.DownloadManagerProvider";
	
	/** content uri */
	public static final Uri CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).build();
	
	/** download item */
	public interface Downloads extends BaseEntityColumns {
		
		public static final String CONTENT_PATH = "downloads";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(DownloadManagerContract.CONTENT_URI, CONTENT_PATH);
		
		public static final String COLUMN_URI = "download_uri";
		public static final String COLUMN_LOCAL_URI = "download_local_uri";
		public static final String COLUMN_DOWNLOAD_MANAGER_DOWNLOAD_ID = "download_dm_download_id";
		public static final String COLUMN_STATUS = "download_status";
	}
	
	/** download status */	
	public static final int STATUS_PENDING = 0;
	public static final int STATUS_DOWNLOADING = 1;
	public static final int STATUS_DOWNLOADED = 2;
	public static final int STATUS_FAILED = 3;
	
	/** base entity columns */
	public interface BaseEntityColumns {
		
		/**
	     * The unique ID for a row.
	     * <P>Type: TEXT</P>
	     */
		String COLUMN_ID = BaseColumns._ID;
		
		/**
		 * Creation date
		 * <p>Type: LONG
		 */
		String COLUMN_CREATE_DATE = "create_date";
		
		/**
		 * Modification date
		 * <p>Type: LONG
		 */
		String COLUMN_MODIFY_DATE = "modify_date";
	}
	
	/** tables */
    public interface Tables {
    	
		String DOWNLOADS = "downloads";
    }
}
