package com.sqiwy.transport.advertisement;

import java.text.SimpleDateFormat;
import java.util.*;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.controller.Screen;
import com.sqiwy.transport.controller.ScreenController;
import com.sqiwy.transport.controller.screen.AdScreen;
import com.sqiwy.transport.data.TransportProvider.Table;
import com.sqiwy.transport.data.TransportProviderHelper;
/**
 * Created by abrysov
 */
public class AdvertisementManager {
	
	private static final String TAG = AdvertisementManager.class.getName();
	private static final AdvertisementManager INSTANCE = new AdvertisementManager();
	private final List<AdvertisementResource> mOngoingAds;
	private final long TIME_INTERVAL_MONITOR = 60*60*1000;

    private static int sOngoingAdsRotator;
    private SimpleDateFormat mSdf = new SimpleDateFormat("dd-MM-yyyy", TransportApplication.getAppContext().getResources().getConfiguration().locale);
    private Date mDateToday;
	private Date mEndViewDate;
    private int mShowsCountPicture = 0;
    private int mShowsCountVideo = 0;
    private int mShowsCountText = 0;
    private long mStartViewTime;

	private AdvertisementManager() {
		mOngoingAds = new ArrayList<AdvertisementResource>();
        mDateToday = new Date();
        mStartViewTime = System.currentTimeMillis();

		// Observer for ads. Will refresh ongoing ads if they were updated in the DB.
		ContentObserver adsRefresher = new ContentObserver(new Handler()) {
			public void onChange(boolean selfChange) {
				this.onChange(selfChange, null);
			}     
		  
		    @Override
		    public void onChange(boolean selfChange, Uri uri) {
		    	refreshOngoingAds();
		    }   
		};
		
		ContentResolver contentResolver = TransportApplication.getAppContext().getContentResolver();
		contentResolver.registerContentObserver(Table.Advertisement.URI, true, adsRefresher);
		contentResolver.registerContentObserver(Table.Resources.URI, true, adsRefresher);
	}
	
	public static AdvertisementManager getInstance() {
		return INSTANCE;
	}
	
	public ArrayList<AdvertisementResource> getAds() {
		return getAdsByType(null);
	}
	
	public ArrayList<AdvertisementResource> getAdsByType(Advertisement.Type type) {
		ArrayList<AdvertisementResource> result = new ArrayList<AdvertisementResource>();
		
		Context appContext = TransportApplication.getAppContext();

        long deltaTime;
		
		String selection = null;
		String[] selectionArgs = null;
		if (type != null) {
			selection = Table.Advertisement.TYPE + " = ?";
			selectionArgs = new String[] {type.name()};
		}
		
		if (BuildConfig.DEBUG) {
			Log.d(this.getClass().getName(), "selection: " + selection + " selectionArgs: " + selectionArgs);
		}
		
		ContentResolver contentResolver = appContext.getContentResolver();
		Cursor cursor = contentResolver.query(Table.Advertisement.URI, null, selection, selectionArgs, null);

		while (cursor.moveToNext()) {
			Advertisement ad = TransportProviderHelper.parseAd(contentResolver, cursor);
			if (BuildConfig.DEBUG) {
    			Log.d(this.getClass().getName(), "getAdsByType: resources.size()=" + ad.getResources().size());
    		}

            try{
                mEndViewDate = mSdf.parse(ad.getEndDate());
            }catch (Exception e){
                Log.d(TAG, e.getMessage());
            }

            // set time filter for view ad
            if (mEndViewDate.getTime() >= mDateToday.getTime()) {

                for (AdvertisementResource resource : ad.getResources()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "getAdsByType: resource.status=" + resource.getStatus());
                    }
                    if (resource.getStatus() == AdvertisementResource.DOWNLOADED) {

                        String resType = resource.getAd().getType();

                        deltaTime = System.currentTimeMillis() - mStartViewTime;

                        if ((deltaTime < TIME_INTERVAL_MONITOR)) {
                            if (resType.equals("VIDEO")) {
                                if (mShowsCountVideo < resource.getAd().getMaxHourShows()) {
                                    result.add(resource);  // at least one was downloaded
                                    mShowsCountVideo++;
                                    if (BuildConfig.DEBUG) {
                                        Log.d(TAG, "adsVideoCount= " + mShowsCountVideo);
                                    }
                                }
                            } else if (resType.equals("BANNER")) {
                                if (mShowsCountPicture < resource.getAd().getMaxHourShows()) {
                                    result.add(resource);  // at least one was downloaded
                                    mShowsCountPicture++;
                                    if (BuildConfig.DEBUG) {
                                        Log.d(TAG, "adsBannerCount= " + mShowsCountPicture);
                                    }
                                }
                            } else if (resType.equals("TEXT")) {
                                if (mShowsCountText < resource.getAd().getMaxHourShows()) {
                                    result.add(resource);  // at least one was downloaded
                                    mShowsCountText++;
                                    if (BuildConfig.DEBUG) {
                                        Log.d(TAG, "adsTextCount= " + mShowsCountText);
                                    }
                                }
                            }

                        } else {
                            if (resType.equals("VIDEO")) {
                                mShowsCountVideo = 0;
                            } else if (resType.equals("BANNER")) {
                                mShowsCountPicture = 0;
                            } else if (resType.equals("TEXT")) {
                                mShowsCountText = 0;
                            }
                            mStartViewTime = System.currentTimeMillis();
                        }
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "ad is old");
                }
            }
		}
		cursor.close();
		if (BuildConfig.DEBUG) {
			Log.d(this.getClass().getName(), "getAdsByType: " + result.size());
		}
		
		return result;
	}
	
	public long refreshOngoingAds(long minTimeToShowAds) {

        List<AdvertisementResource> ads = getAds();

        // TODO this init not correct, break data from TransportApplication.class init, need to correct
        // default 10 seconds
        long timeToShowAds = 20 * 1000; // default = 0;

		synchronized (mOngoingAds) {
			sOngoingAdsRotator += mOngoingAds.size();

			Collections.rotate(ads, sOngoingAdsRotator);
			//Collections.shuffle(ads, mRandom);
			
			mOngoingAds.clear();
			for (AdvertisementResource ad : ads) {

                mOngoingAds.add(ad);

                timeToShowAds += ad.getAd().getShowDuration();
                if (minTimeToShowAds <= timeToShowAds) {
                    break;
                }

			}
			if (BuildConfig.DEBUG) {
				Log.d(this.getClass().getName(), "refreshOngoingAds: " + mOngoingAds.size());
			}
		}
		
		return timeToShowAds;
	}
	
	public ArrayList<AdvertisementResource> getOngoingAds() {
		synchronized (mOngoingAds) {
			return new ArrayList<AdvertisementResource>(mOngoingAds);
		}
	}
	
	public long getOngoingAdsDuration() {
        // TODO this init not correct, break data from TransportApplication.class init, need to correct
        // default 10 seconds
		long result = 1 * 1000;

        for (AdvertisementResource ad : getOngoingAds()) {
			result += ad.getAd().getShowDuration();
		}
		
		return result;
	}

	public void refreshOngoingAds() {
		ScreenController screenController = TransportApplication.getScreenController();
		List<Screen> screens = screenController.getScreens();

		long minTime = 0;
		for (Screen screen : screens) {
			if (screen instanceof AdScreen) {
				minTime = screen.getMinShowTime();
			}
		}
		
		refreshOngoingAds(minTime);
	}

	public boolean isReady() {
		if (mOngoingAds.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
}
