/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager.utils;

import android.os.Parcel;

import com.google.common.base.Optional;

public final class ParcelableUtils {

	/**
	 * 
	 */
	private ParcelableUtils() {
		
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeString(Parcel dest, Optional<String> value) {
		
		if(value.isPresent()) {
			
			dest.writeInt(1);
			dest.writeString(value.get());
		}
		else {
			
			dest.writeInt(0);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeLong(Parcel dest, Optional<Long> value) {
		
		if(value.isPresent()) {
			
			dest.writeInt(1);
			dest.writeLong(value.get());
		}
		else {
			
			dest.writeInt(0);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeDouble(Parcel dest, Optional<Double> value) {
		
		if(value.isPresent()) {
			
			dest.writeInt(1);
			dest.writeDouble(value.get());
		}
		else {
			
			dest.writeInt(0);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeBoolean(Parcel dest, Optional<Boolean> value) {
		
		if(value.isPresent()) {
			
			dest.writeInt(1);
			boolean[] b = {value.get()};
			dest.writeBooleanArray(b);
		}
		else {
			
			dest.writeInt(0);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static Optional<String> readString(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present > 0) {
			
			return Optional.of(dest.readString());
		}
				
		return Optional.<String>absent();
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static Optional<Long> readLong(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present > 0) {
			
			return Optional.of(dest.readLong());
		}
				
		return Optional.<Long>absent();
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static Optional<Double> readDouble(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present > 0) {
			
			return Optional.of(dest.readDouble());
		}
				
		return Optional.<Double>absent();
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static Optional<Boolean> readBoolean(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present > 0) {
			boolean[] b = {};
			dest.readBooleanArray(b);
			return Optional.of(b[0]);
		}
				
		return Optional.<Boolean>absent();
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeString(Parcel dest, DbOptional<String> value) {
		
		if(!value.isPresent()) {
			
			dest.writeInt(-1);
		}
		else
		if(value.isNull()) {
			
			dest.writeInt(0);
		}
		else {
			
			dest.writeInt(1);
			dest.writeString(value.get());
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeLong(Parcel dest, DbOptional<Long> value) {
		
		if(!value.isPresent()) {
			
			dest.writeInt(-1);
		}
		else
		if(value.isNull()) {
			
			dest.writeInt(0);
		}
		else {
			
			dest.writeInt(1);
			dest.writeLong(value.get());
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeDouble(Parcel dest, DbOptional<Double> value) {
		
		if(!value.isPresent()) {
			
			dest.writeInt(-1);
		}
		else
		if(value.isNull()) {
			
			dest.writeInt(0);
		}
		else {
			
			dest.writeInt(1);
			dest.writeDouble(value.get());
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static void writeBoolean(Parcel dest, DbOptional<Boolean> value) {
		
		if(!value.isPresent()) {
			
			dest.writeInt(-1);
		}
		else
		if(value.isNull()) {
			
			dest.writeInt(0);
		}
		else {
			
			dest.writeInt(1);
			dest.writeInt(1);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param value
	 */
	public static <T extends Enum<?>> void writeEnum(Parcel dest, DbOptional<T> value) {
		
		if(!value.isPresent()) {
			
			dest.writeInt(-1);
		}
		else
		if(value.isNull()) {
			
			dest.writeInt(0);
		}
		else {
			
			dest.writeString(value.get().name());
			dest.writeInt(1);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static DbOptional<String> readDbOptionalString(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present < 0) {
			
			return DbOptional.<String>absent();
		}
		else
		if(0 == present) {
			
			return DbOptional.<String>nullValue();
		}
		
		return DbOptional.<String>of(dest.readString());
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static DbOptional<Long> readDbOptionalLong(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present < 0) {
			
			return DbOptional.<Long>absent();
		}
		else
		if(0 == present) {
			
			return DbOptional.<Long>nullValue();
		}
		
		return DbOptional.<Long>of(dest.readLong());
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static DbOptional<Double> readDbOptionalDouble(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present < 0) {
			
			return DbOptional.<Double>absent();
		}
		else
		if(0 == present) {
			
			return DbOptional.<Double>nullValue();
		}
		
		return DbOptional.<Double>of(dest.readDouble());
	}
	
	/**
	 * 
	 * @param dest
	 * @return
	 */
	public static DbOptional<Boolean> readDbOptionalBoolean(Parcel dest) {
		
		int present = dest.readInt();
		
		if(present < 0) {
			
			return DbOptional.<Boolean>absent();
		}
		else
		if(0 == present) {
			
			return DbOptional.<Boolean>nullValue();
		}
		
		return DbOptional.<Boolean>of(0 == dest.readInt() ? Boolean.FALSE : Boolean.TRUE);
	}
	
	/**
	 * 
	 * @param dest
	 * @param clazz
	 * @param defValue
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends Enum<?>> DbOptional<T> readDbOptionalEnum(Parcel dest, Class<T> clazz, T defValue) {
		
		int present = dest.readInt();
		
		if(present < 0) {
			
			return DbOptional.<T>absent();
		}
		else
		if(0 == present) {
			
			return DbOptional.<T>nullValue();
		}
		
		try {
			
			return DbOptional.of((T)Enum.valueOf((Class)clazz, dest.readString()));
		}
		catch(Throwable error) {
			
			return DbOptional.<T>fromNullable(defValue);
		}
	}
}
