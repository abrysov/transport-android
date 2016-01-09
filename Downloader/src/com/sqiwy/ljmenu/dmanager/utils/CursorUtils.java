/**
 * Created by abrysov
 */
package com.sqiwy.ljmenu.dmanager.utils;

import android.database.Cursor;

public class CursorUtils {

	/**
	 *
	 */
	public interface CursorRunnable {
		
		public void run(Cursor jobCursor);
	}
	
	/**
	 * 
	 * @param cursor
	 * @param closeCursor
	 * @return
	 */
	public final static boolean isEmpty(Cursor cursor, boolean closeCursor) {
		
		boolean res = true;
		
		if( (null != cursor) &&
			(cursor.getCount() > 0) ) {
			
			res = false;
		}
			
		if(closeCursor) {
			
			close(cursor);
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param cursor
	 * @return
	 */
	public final static boolean isEmpty(Cursor cursor) {
		
		return isEmpty(cursor, false);
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	public final static boolean prepareForUse(Cursor c) {
		
		if( (null != c) && 
			(!c.isClosed()) &&
			(true == c.moveToFirst()) ) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param c
	 */
	public static void close(Cursor c) {
		
		if( (null != c) &&
			(!c.isClosed()) ) {
			
			try {
				
				c.close();
			}
			catch(Throwable error) {
				
				error.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param cursor
	 * @param job
	 */
	public static void handleCursorInCycle(Cursor cursor, CursorRunnable job) {
		
		if (cursor != null) {
			
			if (cursor.moveToFirst()) {
				
				do {
					
					job.run(cursor);
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
	}
	
	/**
	 * 
	 * @param cursor
	 * @param job
	 * @param closeCursor
	 */
	public static void handleCursorInCycle(Cursor cursor, CursorRunnable job, boolean closeCursor) {
		
		if (cursor != null) {
			
			if (cursor.moveToFirst()) {
				
				do {
					
					job.run(cursor);
				} while (cursor.moveToNext());
			}
			
			if(closeCursor) {
			
				cursor.close();
				cursor = null;
			}			
		}
	}
}

