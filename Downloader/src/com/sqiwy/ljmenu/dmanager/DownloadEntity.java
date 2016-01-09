/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import java.util.Date;

import com.sqiwy.ljmenu.dmanager.utils.DbOptional;
import com.sqiwy.ljmenu.dmanager.utils.OptionalUtils;
import com.sqiwy.ljmenu.dmanager.utils.ParcelableUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public final class DownloadEntity extends BaseEntity<DownloadEntity> 
	implements Parcelable{

	/**
	 *
	 */
	public static enum Status {
		
		PENDING(DownloadManagerContract.STATUS_PENDING),
		DOWNLOADING(DownloadManagerContract.STATUS_DOWNLOADING),
		DOWNLOADED(DownloadManagerContract.STATUS_DOWNLOADED),
		FAILED(DownloadManagerContract.STATUS_FAILED);
		
		private long mStatusCode;
		
		private Status(long statusCode) {
			
			mStatusCode = statusCode;
		}
		
		public long getStatusCode() {
			
			return mStatusCode;
		}
		
		static Status fromStatusCode(long statusCode) {
			
			for(Status status : Status.values()) {
			
				if(status.getStatusCode() == statusCode) {
					
					return status;
				}
			}
			
			return Status.PENDING;
		}
	}
	
	/**
	 * variables
	 */
	private DbOptional<String> mUri = DbOptional.<String>absent();
	private DbOptional<String> mLocalUri = DbOptional.<String>absent();
	private DbOptional<Long> mDownloadmanagerDownloadId = DbOptional.<Long>absent();
	private DbOptional<Status> mStatus = DbOptional.<Status>absent();
	
	/**
	 * 
	 */
	public DownloadEntity() {
		
		this(DbOptional.<Long>absent(), DbOptional.<Long>absent(), DbOptional.<Long>absent(), 
				DbOptional.<String>absent(), DbOptional.<String>absent(), DbOptional.<Long>absent(), 
				DbOptional.<Status>absent());
	}
	
	/**
	 * 
	 * @param title
	 * @param icon
	 */
	public DownloadEntity(DbOptional<String> uri, DbOptional<String> localUri, DbOptional<Long> downloadManagerDownloadId,  
			DbOptional<Status> status) {
		
		this(DbOptional.<Long>absent(), DbOptional.<Long>absent(), DbOptional.<Long>absent(), 
				uri, localUri, downloadManagerDownloadId, status);
	}
	
	/**
	 * 
	 * @param id
	 * @param createDate
	 * @param modofyDate
	 * @param title
	 * @param icon
	 */
	public DownloadEntity(DbOptional<Long> id, DbOptional<Long> createDate, DbOptional<Long> modifyDate, 
			DbOptional<String> uri, DbOptional<String> localUri, DbOptional<Long> downloadManagerDownloadId,  
			DbOptional<Status> status) {
	
		super(id, createDate, modifyDate);
				
		mUri = uri;
		mLocalUri = localUri;
		mDownloadmanagerDownloadId = downloadManagerDownloadId;
		mStatus = status;
	}
	
	/**
	 * 
	 * @param other
	 */
	public DownloadEntity(DownloadEntity other) {
		
		setId(other.getId());
		setCreateDate(getCreateDate());
		setModifyDate(getModifyDate());
		mUri = other.mUri;
		mLocalUri = other.mLocalUri;
		mDownloadmanagerDownloadId = other.mDownloadmanagerDownloadId;
		mStatus = other.mStatus;
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<String> getUri() {
		
		return mUri;
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<String> getLocalUri() {
		
		return mLocalUri;
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<Long> getDownloadManagerDownloadId() {
		
		return mDownloadmanagerDownloadId;
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<Status> getStatus() {
		
		return mStatus;
	}
	
	/**
	 * 
	 * @param title
	 */
	public DownloadEntity setUri(DbOptional<String> uri) {
		
		mUri = uri;
		
		return this;
	}
	
	/**
	 * 
	 * @param icon
	 */
	public DownloadEntity setLocalUri(DbOptional<String> localUri) {
		
		mLocalUri = localUri;
		
		return this;
	}
	
	/**
	 * 
	 * @param order
	 * @return
	 */
	public DownloadEntity setDownloadmanagerDownloadId(DbOptional<Long> downloadmanagerDownloadId) {
		
		mDownloadmanagerDownloadId = downloadmanagerDownloadId;
		
		return this;
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 */
	public DownloadEntity setStatus(DbOptional<Status> status) {
		
		mStatus = status;
		
		return this;
	}
	
	/**
	 * 
	 */
	@Override
	public ContentValues toContentValues(ContentValues cv) {
		
		super.toContentValues(cv);
		
		OptionalUtils.toStringContentValue(mUri, DownloadManagerContract.Downloads.COLUMN_URI, cv);
		OptionalUtils.toStringContentValue(mLocalUri, DownloadManagerContract.Downloads.COLUMN_LOCAL_URI, cv);
		OptionalUtils.toLongContentValue(mDownloadmanagerDownloadId, DownloadManagerContract.Downloads.COLUMN_DOWNLOAD_MANAGER_DOWNLOAD_ID, cv);
		OptionalUtils.toLongContentValue(mStatus.isPresent() ? DbOptional.fromNullable(mStatus.get().getStatusCode()) : DbOptional.<Long>absent(), DownloadManagerContract.Downloads.COLUMN_STATUS, cv);
		
		return cv;
	};
	
	/**
	 * 
	 */
	@Override
	public DownloadEntity fromCursor(Cursor c) {

		super.fromCursor(c);
		
		mUri = OptionalUtils.getDbOptionalStringValue(c, DownloadManagerContract.Downloads.COLUMN_URI);
		mLocalUri = OptionalUtils.getDbOptionalStringValue(c, DownloadManagerContract.Downloads.COLUMN_LOCAL_URI);
		mDownloadmanagerDownloadId = OptionalUtils.getDbOptionalLongValue(c, DownloadManagerContract.Downloads.COLUMN_DOWNLOAD_MANAGER_DOWNLOAD_ID);
		
		DbOptional<Long> status = OptionalUtils.getDbOptionalLongValue(c, DownloadManagerContract.Downloads.COLUMN_STATUS);
		
		if(status.isPresent()) {
			
			mStatus = DbOptional.of(Status.fromStatusCode(status.get()));
		}
		else
		if(status.isNull()) {
		
			mStatus = DbOptional.<Status>nullValue();
		}
		else {
			
			mStatus = DbOptional.<Status>absent();
		}
		
		return this;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		
		return String.format("DownloadEntity(id = %s, create date = %s, modify date = %s, uri = %s, local uri = %s, download manager download id = %s, status = %s)",  
				getId().isPresent() ? getId().get() : "<no ID>", 
				getCreateDate().isPresent() ? sDateFormatter.format(new Date(getCreateDate().get())) : "<no create date>", 
				getModifyDate().isPresent() ? sDateFormatter.format(new Date(getModifyDate().get())) : "<no modify date>",						
				getUri().isPresent() ? getUri().get() : "<no uri>",
				getLocalUri().isPresent() ? getLocalUri().get() : "<no local uri>",
				getDownloadManagerDownloadId().isPresent() ? getDownloadManagerDownloadId().get().toString() : "<no dm download id>",
				getStatus().isPresent() ? getStatus().get().name() : "<no status>");
	}
	
	/****************************************************************************************************
	 * 
	 * 												PARCELABLE
	 * 
	 ****************************************************************************************************/

	/**
	 * 
	 */
	@Override
	public int describeContents() {

		return 0;
	}

	/**
	 * 
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {

		super.writeToParcel(dest, flags);
		
		ParcelableUtils.writeString(dest, mUri);
		ParcelableUtils.writeString(dest, mLocalUri);
		ParcelableUtils.writeLong(dest, mDownloadmanagerDownloadId);
		ParcelableUtils.writeEnum(dest, mStatus);
	}
	
	/**
	 * 
	 * @param parcel
	 */
	private DownloadEntity(Parcel parcel){
		
		super(parcel);
		
		mUri = ParcelableUtils.readDbOptionalString(parcel);
		mLocalUri = ParcelableUtils.readDbOptionalString(parcel);
		mDownloadmanagerDownloadId = ParcelableUtils.readDbOptionalLong(parcel);
		mStatus = ParcelableUtils.readDbOptionalEnum(parcel, Status.class, Status.PENDING);
	}
	
	/**
	 * 
	 */
    public static final Parcelable.Creator<DownloadEntity> CREATOR = new Parcelable.Creator<DownloadEntity>() {
    	
        public DownloadEntity createFromParcel(Parcel in) {
        	
            return new DownloadEntity(in); 
        }

        public DownloadEntity[] newArray(int size) {
        	
            return new DownloadEntity[size];
        }
    };
}
