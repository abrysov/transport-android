/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager.utils;

import com.google.common.base.Optional;

import android.content.ContentValues;
import android.database.Cursor;

public final class OptionalUtils {

	/**
	 * 
	 */
	private OptionalUtils() {
		
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static Optional<Long> getLongValue(Cursor c, String columnName) {
		
		try {
			
			return Optional.of(Long.parseLong(c.getString(c.getColumnIndex(columnName))));
		}
		catch(Throwable error) {
			
			error.printStackTrace();
		}
		
		return Optional.<Long>absent();
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static Optional<Double> getDoubleValue(Cursor c, String columnName) {
		
		try {
			
			return Optional.of(Double.parseDouble(c.getString(c.getColumnIndex(columnName))));
		}
		catch(Throwable error) {
			
			error.printStackTrace();
		}
		
		return Optional.<Double>absent();
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static Optional<Boolean> getBooleanValue(Cursor c, String columnName) {
		
		try {
			
			return Optional.<Boolean>of((0 == Integer.parseInt(c.getString(c.getColumnIndex(columnName)))) ? Boolean.FALSE : Boolean.TRUE);
		}
		catch(Throwable error) {

			error.printStackTrace();
		}
		
		return Optional.<Boolean>absent();
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static Optional<String> getStringValue(Cursor c, String columnName) {
		
		try {
			
			return Optional.of(c.getString(c.getColumnIndex(columnName)));
		}
		catch(Throwable error) {

			error.printStackTrace();
		}
		
		return Optional.<String>absent();
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static Optional<byte[]> getBlobValue(Cursor c, String columnName) {
		
		try {
			
			return Optional.of(c.getBlob(c.getColumnIndex(columnName)));
		}
		catch(Throwable error) {

			error.printStackTrace();
		}
		
		return Optional.<byte[]>absent();
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static DbOptional<Long> getDbOptionalLongValue(Cursor c, String columnName) {
		
		int columnIndex = c.getColumnIndex(columnName);
		
		if(columnIndex >= 0) {
			
			if(!c.isNull(columnIndex)) {
				
				try {
					
					return DbOptional.of(c.getLong(columnIndex));
				}
				catch(Throwable error) {
					error.printStackTrace();
				}
			}
		
			return DbOptional.<Long>nullValue();			
		}
		
		return DbOptional.<Long>absent();		
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static DbOptional<Double> getDbOptionalDoubleValue(Cursor c, String columnName) {
		
		int columnIndex = c.getColumnIndex(columnName);
		
		if(columnIndex >= 0) {
			
			if(!c.isNull(columnIndex)) {
				
				try {
					
					return DbOptional.of(c.getDouble(columnIndex));
				}
				catch(Throwable error) {
	
					error.printStackTrace();
				}
			}
		
			return DbOptional.<Double>nullValue();			
		}
		
		return DbOptional.<Double>absent();	
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static DbOptional<Boolean> getDbOptionalBooleanValue(Cursor c, String columnName) {
		
		int columnIndex = c.getColumnIndex(columnName);
		
		if(columnIndex >= 0) {
			
			if(!c.isNull(columnIndex)) {
				
				try {
					
					return DbOptional.of((0 == c.getInt(columnIndex) ? Boolean.FALSE : Boolean.TRUE));
				}
				catch(Throwable error) {
	
					error.printStackTrace();
				}
			}
		
			return DbOptional.<Boolean>nullValue();			
		}
		
		return DbOptional.<Boolean>absent();	
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static DbOptional<String> getDbOptionalStringValue(Cursor c, String columnName) {
		
		int columnIndex = c.getColumnIndex(columnName);
		
		if(columnIndex >= 0) {
			
			if(!c.isNull(columnIndex)) {
				
				try {
					
					return DbOptional.of(c.getString(columnIndex));
				}
				catch(Throwable error) {
	
					error.printStackTrace();
				}
			}
		
			return DbOptional.<String>nullValue();			
		}
		
		return DbOptional.<String>absent();	
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @return
	 */
	public static DbOptional<byte[]> getDbOptionalBlobValue(Cursor c, String columnName) {
		
		int columnIndex = c.getColumnIndex(columnName);
		
		if(columnIndex >= 0) {
			
			if(!c.isNull(columnIndex)) {
				
				try {
					
					return DbOptional.of(c.getBlob(columnIndex));
				}
				catch(Throwable error) {
	
					error.printStackTrace();
				}
			}
			
			return DbOptional.<byte[]>nullValue();			
		}
		
		return DbOptional.<byte[]>absent();	
	}
	
	/**
	 * 
	 * @param c
	 * @param columnName
	 * @param defValue
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Enum<?>> DbOptional<T> getDbOptionalEnumValue(Cursor c, String columnName, Class<T> clazz, T defValue) {
		
		int columnIndex = c.getColumnIndex(columnName);
		
		if(columnIndex >= 0) {
			
			if(!c.isNull(columnIndex)) {
				
				try {

					return DbOptional.of((T)Enum.valueOf((Class)clazz, c.getString(columnIndex)));
				}
				catch(Throwable error) {
	
					return DbOptional.<T>fromNullable(defValue);
				}
			}
		
			return DbOptional.<T>nullValue();			
		}
		
		return DbOptional.<T>absent();	
	}

	/**
	 * 
	 * @param dbOptional
	 * @param columnName
	 * @param cv
	 */
	public static void toLongContentValue(DbOptional<Long> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get());
		}
		else {
			
			if(dbOptional.isNull()) {
			
				cv.putNull(columnName);
			}			
		}
	}
	
	/**
	 * 
	 * @param dbOptional
	 * @param columnName
	 * @param cv
	 */
	public static void toDoubleContentValue(DbOptional<Double> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get());
		}
		else {
			
			if(dbOptional.isNull()) {
			
				cv.putNull(columnName);
			}			
		}
	}
	
	/**
	 * 
	 * @param dbOptional
	 * @param columnName
	 * @param cv
	 */
	public static void toStringContentValue(DbOptional<String> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get());
		}
		else {
			
			if(dbOptional.isNull()) {
			
				cv.putNull(columnName);
			}			
		}
	}
	
	/**
	 * 
	 * @param dbOptional
	 * @param columnName
	 * @param cv
	 */
	public static void toBooleanContentValue(DbOptional<Boolean> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get());
		}
		else {
			
			if(dbOptional.isNull()) {
			
				cv.putNull(columnName);
			}			
		}
	}
	
	public static void toIntegerContentValue(DbOptional<Integer> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get());
		}
		else {
			
			if(dbOptional.isNull()) {
				cv.putNull(columnName);
			}			
		}
	}
	

	/**
	 * 
	 * @param dbOptional
	 * @param columnName
	 * @param cv
	 */
	public static void toBlobContentValue(DbOptional<byte[]> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get());
		}
		else {
			
			if(dbOptional.isNull()) {
			
				cv.putNull(columnName);
			}			
		}
	}
	
	/**
	 * 
	 * @param dbOptional
	 * @param columnName
	 * @param cv
	 */
	public static <T extends Enum<?>> void toEnumContentValue(DbOptional<T> dbOptional, String columnName, ContentValues cv) {
		
		if(dbOptional.isPresent()) {
			
			cv.put(columnName, dbOptional.get().name());
		}
		else {
			
			if(dbOptional.isNull()) {
			
				cv.putNull(columnName);
			}			
		}
	}
}
