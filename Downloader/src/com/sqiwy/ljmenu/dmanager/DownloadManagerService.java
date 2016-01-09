/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import java.io.File;
import java.util.List;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import android.util.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sqiwy.ljmenu.BuildConfig;
import com.sqiwy.ljmenu.dmanager.DownloadEntity.Status;
import com.sqiwy.ljmenu.dmanager.DownloadManagerContract.Downloads;
import com.sqiwy.ljmenu.dmanager.utils.CursorUtils;
import com.sqiwy.ljmenu.dmanager.utils.CursorUtils.CursorRunnable;
import com.sqiwy.ljmenu.dmanager.utils.DbOptional;
import com.sqiwy.ljmenu.dmanager.utils.Logger;

public class DownloadManagerService extends IntentService {

	/**
	 * consts
	 */
	private static final String TAG = "DownloadManagerService";
	private static final String ACTION_DOWNLOAD = "com.sqiwy.ljmenu.action.DOWNLOAD";
	private static final String ACTION_CANCEL_DOWNLOAD = "com.sqiwy.ljmenu.action.CANCEL_DOWNLOAD";
	private static final String ACTION_CHECK_DOWNLOADS = "com.sqiwy.ljmenu.action.CHECK_DOWNLOAD";
	private static final String ACTION_DOWNLOAD_COMPLETE = "com.sqiwy.ljmenu.action.DOWNLOAD_COMPLETE";
	private static final String ACTION_REMOVE_DOWNLOAD = "com.sqiwy.ljmenu.action.REMOVE_DOWNLOAD";
	
	/**
	 * You should subscribe to this event via broadcast receiver.
	 * <p>
	 * It will contain {@link DownloadManagerService#ARG_DOWNLOAD_URI} and {@link DownloadManagerService#ARG_LOCAL_URI}.
	 */
	public static final String ACTION_DOWNLOAD_COMPLETED = "com.sqiwy.intent.action.DOWNLOAD_COMPLETED";
	
	public static final String ARG_DOWNLOAD_URI = "uri";
	public static final String ARG_LOCAL_URI = "local_uri";
	public static final String ARG_DOWNLOAD_ID = "download_id";
	
	/**
	 * 
	 */
	public DownloadManagerService() {
		
		super(TAG);
		
		setIntentRedelivery(true);
	}

	/**
	 * 
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		final String action = intent.getAction();
		
		if(action.equals(ACTION_DOWNLOAD)) {
		
			handleActionDownload(intent);
		}
		else
		if(action.equals(ACTION_CANCEL_DOWNLOAD)) {
			
			handleActionCancelDownload(intent);
		}
		else
		if(action.equals(ACTION_REMOVE_DOWNLOAD)) {
			
			handleActionRemoveDownload(intent);
		}
		else
		if(action.equals(ACTION_DOWNLOAD_COMPLETE)) {
			
			handleActionDownloadComplete(intent);
		}
		else
		if(action.equals(ACTION_CHECK_DOWNLOADS)) {
			
			handleActionCheckDownloads(intent);
		}
		else {
			
			throw new IllegalArgumentException("Invalid action: " + action);
		}
	}
	
	/*********************************************************************************************
	 * 
	 * 											HANDLERS
	 * 
	 *********************************************************************************************/
	/**
	 * 
	 * @param intent
	 */
	private void handleActionDownload(Intent intent) {
		
		final Context context = getApplicationContext();
		final ContentResolver resolver = context.getContentResolver();
		final String uri = intent.getStringExtra(ARG_DOWNLOAD_URI);
		final String localUri = intent.getStringExtra(ARG_LOCAL_URI);
		final Cursor cursor;
		final boolean downloadExists[] = new boolean[]{false};
		final DownloadEntity downloadEntity = new DownloadEntity();
		final List<DownloadEntity> downloadsToRemove = Lists.newArrayList();
		
		Logger.i(TAG, String.format("Downloading request from '%s' to '%s'", uri, localUri));
		
		// check if we already have such a download
		cursor = resolver.query(Downloads.CONTENT_URI, 
				null, Downloads.COLUMN_URI + " = ? AND " + Downloads.COLUMN_LOCAL_URI + " = ?", 
				new String[]{uri, localUri}, null);
		
		CursorUtils.handleCursorInCycle(cursor, new CursorRunnable() {
			
			@Override
			public void run(Cursor jobCursor) {
				
				downloadEntity.fromCursor(jobCursor);
				
				// if download is failed then restart it
				if(Status.FAILED == downloadEntity.getStatus().get()) {
					
					downloadsToRemove.add(new DownloadEntity(downloadEntity));
				}
				else {
				
					downloadExists[0] = true;
				}
			}
		});
		
		// remove failed downloads
		removeDownloads(context, downloadsToRemove);
		
		if(false == downloadExists[0]) {
			
			final DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
	        Request request = new Request(Uri.parse(uri))
        		.setDestinationUri(Uri.parse(localUri));
        		//.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
	        long dmDownloadId = dm.enqueue(request);
	        
	        resolver.insert(Downloads.CONTENT_URI, 
	        		BaseEntity.prepareForInsert(
	        				new DownloadEntity(DbOptional.of(uri), DbOptional.of(localUri), DbOptional.of(dmDownloadId), DbOptional.of(Status.DOWNLOADING))
	        				.toContentValues(new ContentValues())));
	        
	        Logger.i(TAG, String.format("Downloading started, DownloadManager id = '%s'", String.valueOf(dmDownloadId)));
		}
		else {
			
			Logger.i(TAG, "Downloading not started since we already have such a pending download");
			//
			//resolver.notifyChange(Downloads.CONTENT_URI, null);
		}
	}
	
	/**
	 * 
	 * @param intent
	 */
	private void handleActionCancelDownload(Intent intent) {
		
		final Context context = getApplicationContext();
		Cursor cursor = null;
		final DownloadEntity downloadEntity = new DownloadEntity();
		final DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
		final List<DownloadEntity> downloadsToRemove = Lists.newArrayList();

		if(intent.getExtras().containsKey(ARG_DOWNLOAD_ID)) {
			
			Logger.i(TAG, String.format("Downloading cancel request by id '%d'", intent.getLongExtra(ARG_DOWNLOAD_ID, -1)));
			
			cursor = getDownloadsById(context, intent.getLongExtra(ARG_DOWNLOAD_ID, -1));
		}
		else
		if(intent.getExtras().containsKey(ARG_DOWNLOAD_URI)) {
			
			Logger.i(TAG, String.format("Downloading cancel request by uri '%s'", intent.getStringExtra(ARG_DOWNLOAD_URI)));
			
			cursor = getDownloadsByUri(context, intent.getStringExtra(ARG_DOWNLOAD_URI));
		}
		
		if( (null != cursor) &&
			(cursor.getCount() > 0) ) {
			
			CursorUtils.handleCursorInCycle(cursor, new CursorRunnable() {

				@Override
				public void run(Cursor jobCursor) {

					downloadEntity.fromCursor(jobCursor);
					
					// if download is not finished yet then delete it
					if(Status.DOWNLOADED != downloadEntity.getStatus().get()) {
						
						dm.remove(downloadEntity.getDownloadManagerDownloadId().get());
						downloadsToRemove.add(new DownloadEntity(downloadEntity));						
					}				
					else {
						
						Logger.i(TAG, String.format("Skipping finished downloading cancel ('%s' -> '%s')", downloadEntity.getUri().get(), downloadEntity.getLocalUri().get()));
					}
				}				
			});
		}
		
		removeDownloads(context, downloadsToRemove);
	}
	
	/**
	 * 
	 * @param intent
	 */
	private void handleActionRemoveDownload(Intent intent) {
		
		final Context context = getApplicationContext();
		Cursor cursor = null;
		final DownloadEntity downloadEntity = new DownloadEntity();
		final DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
		final List<DownloadEntity> downloadsToRemove = Lists.newArrayList();

		if(intent.getExtras().containsKey(ARG_DOWNLOAD_ID)) {
			
			Logger.i(TAG, String.format("Downloading remove request by id '%d'", intent.getLongExtra(ARG_DOWNLOAD_ID, -1)));
			
			cursor = getDownloadsById(context, intent.getLongExtra(ARG_DOWNLOAD_ID, -1));
		}
		else
		if(intent.getExtras().containsKey(ARG_DOWNLOAD_URI)) {
			
			Logger.i(TAG, String.format("Downloading remove request by uri '%s'", intent.getStringExtra(ARG_DOWNLOAD_URI)));
			
			cursor = getDownloadsByUri(context, intent.getStringExtra(ARG_DOWNLOAD_URI));
		}
		
		if( (null != cursor) &&
			(cursor.getCount() > 0) ) {
			
			CursorUtils.handleCursorInCycle(cursor, new CursorRunnable() {

				@Override
				public void run(Cursor jobCursor) {

					downloadEntity.fromCursor(jobCursor);
					
					if(Status.DOWNLOADED != downloadEntity.getStatus().get()) {
						
						dm.remove(downloadEntity.getDownloadManagerDownloadId().get());
					}					
					
					// all the downloads will be removed
					downloadsToRemove.add(new DownloadEntity(downloadEntity));	
				}				
			});
		}
		
		removeDownloads(context, downloadsToRemove);
	}
	
	/**
	 * 
	 * @param intent
	 */
	private void handleActionDownloadComplete(Intent intent) {
		
		final long downloadId = intent.getLongExtra(ARG_DOWNLOAD_ID, 0);
		final Context context = getApplicationContext();
		final DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
		final Query query = new android.app.DownloadManager.Query().setFilterById(downloadId);				
		final Cursor cursor = dm.query(query);
		final boolean handled[] = new boolean[]{false};
		
		Logger.i(TAG, String.format("Downloading complete notification, DownloadManager id = '%d'", downloadId));
		
		CursorUtils.handleCursorInCycle(cursor, new CursorRunnable() {
			
			@Override
			public void run(Cursor jobCursor) {

				int dmStatus = jobCursor.getInt(jobCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				String uri = jobCursor.getString(jobCursor.getColumnIndex(DownloadManager.COLUMN_URI));
				String localUri = jobCursor.getString(jobCursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
				Status status = (DownloadManager.STATUS_SUCCESSFUL == dmStatus) ? Status.DOWNLOADED :
					(DownloadManager.STATUS_FAILED == dmStatus) ? Status.FAILED :
					(DownloadManager.STATUS_PENDING == dmStatus) ? Status.PENDING :
					Status.DOWNLOADING;				
				ContentValues cv = BaseEntity.prepareForUpdate(new DownloadEntity()
					.setStatus(DbOptional.of(status))
					.setLocalUri(DbOptional.of(localUri))
					.toContentValues(new ContentValues()));
				
				int updated = context.getContentResolver().update(DownloadManagerContract.Downloads.CONTENT_URI, 
						cv, Downloads.COLUMN_DOWNLOAD_MANAGER_DOWNLOAD_ID + " = ?", new String[]{String.valueOf(downloadId)});
				
				if(updated > 0) {
					
					handled[0] = true;
					
					Logger.i(TAG, String.format("Downloading complete: status = %s, uri = '%s', local uri = '%s'", 
							status.name(), uri, localUri));					
				}
				
				if (Status.DOWNLOADED == status) {
					Intent intent = new Intent(DownloadManagerService.ACTION_DOWNLOAD_COMPLETED);
					intent.putExtra(ARG_DOWNLOAD_URI, uri);
					intent.putExtra(ARG_LOCAL_URI, localUri);
					context.sendBroadcast(intent);
				}
			}
		});
		
		if(false == handled[0]) {
			
			Logger.i(TAG, String.format("Downloading complete notification: DownloadManager id = '%d' is not registered in database", downloadId));  
		}
	}
	
	/**
	 * 
	 * @param intent
	 */
	private void handleActionCheckDownloads(Intent intent) {
		
		// TODO: implement me
	}
	
	/*********************************************************************************************
	 * 
	 * 												API
	 * 
	 *********************************************************************************************/
	/**
	 * 
	 * @param context
	 * @param uri
	 * @param localFile
	 */
	public static void downloadFile(Context context, String uri, String localFile) {
		
		Preconditions.checkArgument(!TextUtils.isEmpty(uri), "URI not set");
		Preconditions.checkArgument(!TextUtils.isEmpty(localFile), "Local File not set");

		Intent intent = createServiceIntent(context, ACTION_DOWNLOAD);
		intent.putExtra(ARG_DOWNLOAD_URI, uri);
		intent.putExtra(ARG_LOCAL_URI, localFile);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @param context
	 * @param downloadId
	 */
	public static void cancelDownload(Context context, long downloadId) {
		
		Preconditions.checkArgument((downloadId > 0), "invalid download id");

		Intent intent = createServiceIntent(context, ACTION_CANCEL_DOWNLOAD);
		intent.putExtra(ARG_DOWNLOAD_ID, downloadId);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @param context
	 * @param downloadId
	 */
	public static void cancelDownload(Context context, String uri) {
		
		Preconditions.checkArgument(!TextUtils.isEmpty(uri), "URI not set");

		Intent intent = createServiceIntent(context, ACTION_CANCEL_DOWNLOAD);
		intent.putExtra(ARG_DOWNLOAD_URI, uri);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @param context
	 * @param downloadId
	 */
	public final static void removeDownload(Context context, long downloadId) {
		
		Preconditions.checkArgument((downloadId > 0), "invalid download id");

		Intent intent = createServiceIntent(context, ACTION_REMOVE_DOWNLOAD);
		intent.putExtra(ARG_DOWNLOAD_ID, downloadId);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @param context
	 * @param uri
	 */
	public static void removeDownload(Context context, String uri) {
		
		Preconditions.checkArgument(!TextUtils.isEmpty(uri), "URI not set");

		Intent intent = createServiceIntent(context, ACTION_REMOVE_DOWNLOAD);
		intent.putExtra(ARG_DOWNLOAD_URI, uri);
		context.startService(intent);
	}
	
	/**
	 * 
	 * @param context
	 * @param downloadId
	 */
	static void downloadComplete(Context context, long downloadId) {

		Intent intent = createServiceIntent(context, ACTION_DOWNLOAD_COMPLETE);
		intent.putExtra(ARG_DOWNLOAD_ID, downloadId);
		context.startService(intent);

        if(BuildConfig.DEBUG){
            Log.d(TAG, "download completed");
        }
	}
	
	/**
	 * 
	 * @param context
	 * @param downloadId
	 */
	static void checkDownloads(Context context, long downloadId) {
		
		Intent intent = createServiceIntent(context, ACTION_CHECK_DOWNLOADS);
		context.startService(intent);
	}
	
	/*********************************************************************************************
	 * 
	 * 											HELPERS
	 * 
	 *********************************************************************************************/
	
	/**
	 * 
	 * @param context
	 * @param action
	 * @return
	 */
	private static Intent createServiceIntent(Context context, String action) {
		
		Intent intent = new Intent(context, DownloadManagerService.class);
		intent.setAction(action);		
		return intent;
	}
	
	/**
	 * 
	 * @param context
	 * @param id
	 * @return
	 */
	private static Cursor getDownloadsById(Context context, long id) {
		
		return context.getContentResolver().query(Downloads.CONTENT_URI, 
				null, Downloads.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null);
	}
	
	/**
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	private static Cursor getDownloadsByUri(Context context, String uri) {
		
		return context.getContentResolver().query(Downloads.CONTENT_URI, 
				null, Downloads.COLUMN_URI + " = ?", new String[]{uri}, null);
	}
	
	/**
	 * 
	 * @param context
	 * @param id
	 */
	private static void removeDownloadById(Context context, long id) {
		
		context.getContentResolver().delete(ContentUris.withAppendedId(Downloads.CONTENT_URI, id), null, null);
	}
	
	/**
	 * 
	 * @param context
	 * @param downloads
	 */
	private static void removeDownloads(Context context, List<DownloadEntity> downloads) {
		
		for(DownloadEntity de : downloads) {
			
			Logger.i(TAG, String.format("Removing download from database: uri = '%s', local uri = '%s', status = '%s', DownloadManager id = '%s'", 
					de.getUri().get(), de.getLocalUri().get(), de.getStatus().get().name(), 
					(de.getDownloadManagerDownloadId().isPresent() ? String.valueOf(de.getDownloadManagerDownloadId().get()) : "<not set>")));
			
			removeDownloadById(context, de.getId().get());
			
			try {
			
				if(true == (new File(Uri.parse(de.getLocalUri().get()).getPath())).delete()) {
					
					Logger.i(TAG, String.format("Local file '%s' removed", de.getLocalUri().get()));
				}
				else {
					
					Logger.i(TAG, String.format("Failed to remove local file '%s'", de.getLocalUri().get()));
				}
			}
			catch(Throwable error) {

			}
		}
	}
}
