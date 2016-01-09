/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import com.sqiwy.ljmenu.dmanager.DownloadManagerContract.Downloads;
import com.sqiwy.ljmenu.dmanager.DownloadManagerContract.Tables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class DownloadManagerSQLiteOpenHelper extends SQLiteOpenHelper {

	/**
	 * consts
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "DownloadManagerSQLiteOpenHelper";
	private final static int DB_VERSION = 1;
	private final static String DB_NAME = "downloads.db";

	/**
	 * variables
	 */
	private static DownloadManagerSQLiteOpenHelper sInstance = null;
	
	/**
	 * 
	 * @return
	 */
	public static synchronized DownloadManagerSQLiteOpenHelper instance(Context context) {
		
		if(null == sInstance) {
			
			sInstance = new DownloadManagerSQLiteOpenHelper(context.getApplicationContext());
		}
		
		return sInstance;
	}
	
	/**
	 * 
	 * @param context
	 * @param userId
	 */
	private DownloadManagerSQLiteOpenHelper(Context context) {
		
		super(context, DB_NAME, null, DB_VERSION);
	}

	/**
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		// Downloads table
		db.execSQL("CREATE TABLE " + Tables.DOWNLOADS + " ( " +
				Downloads.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				Downloads.COLUMN_CREATE_DATE + " INTEGER NOT NULL, " +
				Downloads.COLUMN_MODIFY_DATE + " INTEGER NOT NULL, " +
				Downloads.COLUMN_URI + " TEXT NOT NULL, " +
				Downloads.COLUMN_LOCAL_URI + " TEXT NOT NULL, " +
				Downloads.COLUMN_DOWNLOAD_MANAGER_DOWNLOAD_ID + " INTEGER, " +
				Downloads.COLUMN_STATUS + " INTEGER NOT NULL" +
				");");		
	}

	/**
	 * 
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + Tables.DOWNLOADS);

		onCreate(db);
	}
}
