/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import com.sqiwy.ljmenu.dmanager.DownloadManagerContract.Downloads;
import com.sqiwy.ljmenu.dmanager.DownloadManagerContract.Tables;
import com.sqiwy.ljmenu.dmanager.utils.Logger;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public final class DownloadManagerProvider extends ContentProvider {

	/**
	 * consts
	 */
	private final static String TAG = "DownloadManagerProvider";
	
	/** uri matcher codes */
	private static final int DOWNLOADS_CODE = 1;
	private static final int DOWNLOAD_BY_ID_CODE = 2;

	/** uri matcher */
	private static final UriMatcher URI_MATCHER;

	/** uri matcher initialization */
	static {
		
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(DownloadManagerContract.AUTHORITY, Downloads.CONTENT_PATH, DOWNLOADS_CODE);
		URI_MATCHER.addURI(DownloadManagerContract.AUTHORITY, Downloads.CONTENT_PATH + "/#", DOWNLOAD_BY_ID_CODE);
	}
		
	/**
	 * 
	 */
    @Override
	public boolean onCreate() {
    	
		return true;
	}

    /**
     * 
     */
	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		final int code = URI_MATCHER.match(uri);
		final SQLiteDatabase db = DownloadManagerSQLiteOpenHelper.instance(getContext()).getReadableDatabase();		
		Cursor cursor = null;
						
		switch (code) {
		
			case DOWNLOADS_CODE: {
				
				cursor = db.query(Tables.DOWNLOADS, projection, selection, selectionArgs, null, null, sortOrder);
			}
			break;
			
			case DOWNLOAD_BY_ID_CODE: {
	
				String id = uri.getLastPathSegment();
				cursor = db.query(Tables.DOWNLOADS, projection, Downloads.COLUMN_ID + " = ?", new String[] {id}, null, null, sortOrder);
			}
			break;	
	
			default: {
	
				Logger.i(TAG, "query(): unsupported uri " + uri);
				return null;
			}
		}

		if (null != cursor) {

			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return cursor;
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {

		final int code = URI_MATCHER.match(uri);
		final SQLiteDatabase db = DownloadManagerSQLiteOpenHelper.instance(getContext()).getWritableDatabase();
		final ContentResolver resolver = getContext().getContentResolver();
        long rowId = -1;        
        
        switch (code) {
        
        	case DOWNLOADS_CODE: {
        		
                rowId = db.insert(Tables.DOWNLOADS, null, values);
                
                if (-1 != rowId) {
                	
                	Uri insertedUri = ContentUris.withAppendedId(Downloads.CONTENT_URI, rowId);
                    resolver.notifyChange(insertedUri, null);
                    return insertedUri;
                }
                
                throw new SQLException("Failed to insert row into " + uri);
        	}
        	
            default: {
            	
                Logger.i(TAG, "insert(): unsupported uri " + uri);
            }
        }
        
		return null;
	}

	/**
	 * 
	 */
	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		final int code = URI_MATCHER.match(uri);
		final SQLiteDatabase db = DownloadManagerSQLiteOpenHelper.instance(getContext()).getWritableDatabase();
		final ContentResolver resolver = getContext().getContentResolver();
		int count;     
        
        switch (code) {
        
        	case DOWNLOADS_CODE: {
        		
                count = db.update(Tables.DOWNLOADS, values, selection, selectionArgs);
                
                if(count > 0) {
                
                	resolver.notifyChange(Downloads.CONTENT_URI, null);
                }
        	}
        	break;
        	
        	case DOWNLOAD_BY_ID_CODE: {
        		
                String id = uri.getLastPathSegment();
                count = db.update(Tables.DOWNLOADS, values, Downloads.COLUMN_ID + " = ?", new String[] {id});

                if(count > 0) {
                    
                	resolver.notifyChange(Downloads.CONTENT_URI, null);
                	resolver.notifyChange(uri, null);
                }
        	}
        	break;
        	
            default: {
            	
                throw new IllegalArgumentException("update(): unsupported uri " + uri);
            }
        }
        
		return count;
	}

	/**
	 * 
	 */
	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {

		final int code = URI_MATCHER.match(uri);
		final SQLiteDatabase db = DownloadManagerSQLiteOpenHelper.instance(getContext()).getWritableDatabase();
		ContentResolver resolver = getContext().getContentResolver();
		int count;     
        
        switch (code) {
        
        	case DOWNLOADS_CODE: {
        		
                count = db.delete(Tables.DOWNLOADS, selection, selectionArgs);
                
                if(count > 0) {
                
                	resolver.notifyChange(Downloads.CONTENT_URI, null);
                }
        	}
        	break;
        	
        	case DOWNLOAD_BY_ID_CODE: {
        		
                String id = uri.getLastPathSegment();
                count = db.delete(Tables.DOWNLOADS, Downloads.COLUMN_ID + " = ?", new String[] {id});

                if(count > 0) {
                    
                	resolver.notifyChange(Downloads.CONTENT_URI, null);
                	resolver.notifyChange(uri, null);
                }
        	}
        	break;        	
               	
            default: {
            	
                throw new IllegalArgumentException("delete(): unsupported uri " + uri);
            }
        }
        
		return count;
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized String getType(Uri uri) {

		return null;
	}
}
