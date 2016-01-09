/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.sqiwy.ljmenu.dmanager.DownloadManagerContract.BaseEntityColumns;
import com.sqiwy.ljmenu.dmanager.utils.DbOptional;
import com.sqiwy.ljmenu.dmanager.utils.OptionalUtils;
import com.sqiwy.ljmenu.dmanager.utils.ParcelableUtils;

@SuppressLint("SimpleDateFormat")
abstract class BaseEntity <T extends BaseEntity<T>> implements Parcelable {
	
	/**
	 * variables
	 */
	protected static SimpleDateFormat sDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private DbOptional<Long> mId = DbOptional.<Long>absent();
	private DbOptional<Long> mCreateDate = DbOptional.<Long>absent();
	private DbOptional<Long> mModifyDate = DbOptional.<Long>absent();
	
	/**
	 * 
	 */
	protected BaseEntity() {
		
		this(DbOptional.<Long>absent(), DbOptional.<Long>absent(), DbOptional.<Long>absent());
	}
	
	/**
	 * 
	 * @param id
	 * @param createDate
	 * @param modifyDate
	 */
	protected BaseEntity(DbOptional<Long> id, DbOptional<Long> createDate, DbOptional<Long> modifyDate) {
		
		long currTime = System.currentTimeMillis();
		
		mId = id;
		mCreateDate = createDate.isPresent() ? createDate : DbOptional.<Long>of(currTime);
		mModifyDate = modifyDate.isPresent() ? modifyDate : DbOptional.<Long>of(currTime);
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<Long> getId() {
	
		return mId;
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<Long> getCreateDate() {
		
		return mCreateDate;
	}
	
	/**
	 * 
	 * @return
	 */
	public DbOptional<Long> getModifyDate() {
		
		return mModifyDate;
	}
	
	/**
	 * 
	 * @param date
	 */
	public T setId(DbOptional<Long> id) {
		
		mId = id;
		
		return getThis();
	}
	
	/**
	 * 
	 * @param date
	 */
	public T setCreateDate(DbOptional<Long> date) {
		
		mCreateDate = date;
		
		return getThis();
	}
	
	/**
	 * 
	 * @param date
	 */
	public T setModifyDate(DbOptional<Long> date) {
		
		mModifyDate = date;
		
		return getThis();
	}
	
	/**
	 * 
	 * @param cv
	 */
	
	public ContentValues toContentValues(ContentValues cv) {
	
		OptionalUtils.toLongContentValue(mId, BaseEntityColumns.COLUMN_ID, cv);
		OptionalUtils.toLongContentValue(mCreateDate, BaseEntityColumns.COLUMN_CREATE_DATE, cv);
		OptionalUtils.toLongContentValue(mModifyDate, BaseEntityColumns.COLUMN_MODIFY_DATE, cv);
		
		return cv;
	}
	
	/**
	 * 
	 * @param c
	 */
	public T fromCursor(Cursor c) {
		
		mId = OptionalUtils.getDbOptionalLongValue(c, BaseEntityColumns.COLUMN_ID);
		mCreateDate = OptionalUtils.getDbOptionalLongValue(c, BaseEntityColumns.COLUMN_CREATE_DATE);
		mModifyDate = OptionalUtils.getDbOptionalLongValue(c, BaseEntityColumns.COLUMN_MODIFY_DATE);
	
		return getThis();
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T getThis() {
	
		return (T) this;
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

		ParcelableUtils.writeLong(dest, getId());
		ParcelableUtils.writeLong(dest, getCreateDate());
		ParcelableUtils.writeLong(dest, getModifyDate());
	}
	
	/**
	 * 
	 * @param parcel
	 */
	protected BaseEntity(Parcel parcel){
		
		super();
		
		mId = ParcelableUtils.readDbOptionalLong(parcel);
		mCreateDate = ParcelableUtils.readDbOptionalLong(parcel);
		mModifyDate = ParcelableUtils.readDbOptionalLong(parcel);
	}
    
	/****************************************************************************************************
	 * 
	 * 												UTILS
	 * 
	 ****************************************************************************************************/
    
    /**
	 * 
	 * @param cv
	 * @return
	 */
	public static ContentValues prepareForUpdate(ContentValues cv) {
		
		return prepareForUpdate(cv, true);
	}
	
	/**
	 * 
	 * @param cv
	 * @return
	 */
	public static ContentValues prepareForUpdate(ContentValues cv, boolean forceSettUpdateDate) {
		
		cv.remove(BaseEntityColumns.COLUMN_CREATE_DATE);
		
		if((true == forceSettUpdateDate) || (!cv.containsKey(BaseEntityColumns.COLUMN_MODIFY_DATE)) ){
			
			cv.put(BaseEntityColumns.COLUMN_MODIFY_DATE, System.currentTimeMillis());
		}

		return cv;
	}
	
	/**
	 * 
	 * @param cv
	 * @return
	 */
	public static ContentValues prepareForInsert(ContentValues cv) {
		
		return prepareForInsert(cv, true);
	}
	
	/**
	 * 
	 * @param cv
	 * @return
	 */
	public static ContentValues prepareForInsert(ContentValues cv, boolean forceSetCreateDate) {
		
		long currTime = System.currentTimeMillis();
		long createDate = (true == forceSetCreateDate) ? currTime : 
			cv.containsKey(BaseEntityColumns.COLUMN_CREATE_DATE) ? cv.getAsLong(BaseEntityColumns.COLUMN_CREATE_DATE) : currTime;
		
		cv.put(BaseEntityColumns.COLUMN_CREATE_DATE, createDate);
		cv.put(BaseEntityColumns.COLUMN_MODIFY_DATE, createDate);
		
		return cv;
	}
}
