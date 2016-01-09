/**
 * Created by abrysov
 */
package com.sqiwy.transport.advertisement;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;

public class AssetsProvider extends ContentProvider {

	/**
	 * 
	 */
	@Override
	public boolean onCreate() {

		return false;
	}
	
	/**
	 * 
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		return null;
	}
	
	/**
	 * 
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		return 0;
	}

	/**
	 * 
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		return 0;
	}

	/**
	 * 
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {

		return null;
	}
	
	/**
	 * 
	 */
	@Override
	public String getType(Uri uri) {

		return null;
	}
	
	/**
	 * 
	 */
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
    	
        AssetManager am = getContext().getAssets();
        AssetFileDescriptor afd = null;
        
    	try {
			
    		String assetFile = uri.getPath();
    		
    		if(assetFile.startsWith("/")) {
    			
    			assetFile = assetFile.substring(1);
    		}
    		
    		afd = am.openFd(assetFile);
		} 
    	catch (IOException e) {

			e.printStackTrace();
		}
    	
        if(null == afd) {
        
        	throw new FileNotFoundException(uri.toString());
        }
        
        return afd;
    }
    
    /**
	 * @param resource
	 * @return
	 */
	public static boolean isInternalResource(String resource) {
		
		if(resource.startsWith("content://")) {
			
			return true;
		}
		
		return false;
	}
	
	public static Uri getUri(String name) {
		return Uri.parse(name);
	}
}

