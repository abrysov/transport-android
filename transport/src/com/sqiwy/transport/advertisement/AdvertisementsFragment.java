package com.sqiwy.transport.advertisement;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.R;
import com.sqiwy.transport.StatsUtils;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.advertisement.Advertisement.Type;
import com.sqiwy.transport.advertisement.AdvertisementBaseFragment.AdvertisemenFragmentListener;
import com.sqiwy.transport.ui.view.TransitionAnimationFrameLayout;
/**
 * Created by abrysov
 */
public class AdvertisementsFragment extends Fragment implements AdvertisemenFragmentListener {

	private static final String ARG_ADS = "arg-ads";
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String TAG = AdvertisementsFragment.class.getName();
	
	private List<AdvertisementResource> mAds;
	private int mCurrentAdPosition = -1;
	private long mCurrentAdStartTime;
	private AdvertisementResource mCurrentAd;
	
	private AdvertisementBaseFragment mVideoAdFragment;
	private AdvertisementBaseFragment mImageAdFragment;
	private AdvertisementTextFragment mTextAdFragment;
	private TransitionAnimationFrameLayout mContainer;
	DateFormat mServerExpectedISOTime;

	public static AdvertisementsFragment newInstance(ArrayList<AdvertisementResource> resources) {
		AdvertisementsFragment adFragment = new AdvertisementsFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_ADS, resources);
		adFragment.setArguments(args);
		return adFragment;
	}
	
	@SuppressWarnings("unchecked")
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Bundle args = getArguments();
		mAds = (ArrayList<AdvertisementResource>) args.getSerializable(ARG_ADS);
		mServerExpectedISOTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.advertisements, container, false);
		mContainer = (TransitionAnimationFrameLayout) view.findViewById(R.id.ad_container);
		
		FragmentManager fm = getChildFragmentManager();
		
		mImageAdFragment = new AdvertisementImageFragment();
		mVideoAdFragment = new AdvertisementVideoFragment();
		mTextAdFragment = new AdvertisementTextFragment();
		
		mImageAdFragment.setTargetFragment(this, 0);
		mVideoAdFragment.setTargetFragment(this, 0);
		mTextAdFragment.setTargetFragment(this, 0);
		
		fm.beginTransaction()
			.add(R.id.ad_image_fragment, mImageAdFragment)
			.add(R.id.ad_video_fragment, mVideoAdFragment)
			.add(R.id.ad_text_fragment, mTextAdFragment).commit();
		
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContainer.setMaskVisibility(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();		
		showNextAd();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mContainer.setMaskVisibility(true);
		//mContainer.startMaskTransition(true);
		reportAd(mCurrentAd);
	}
	
	public void showNextAd() {
		// There can be cases when ads are not available.
		if (mAds != null && !mAds.isEmpty()) {
			int positionOfAdToDisplay = ++mCurrentAdPosition % mAds.size();
			AdvertisementResource adToDisplay = mAds.get(positionOfAdToDisplay);
			showAd(adToDisplay);
		}
	}
	
	public void showAd(AdvertisementResource ad) {
		if (isAdded() && getChildFragmentManager() != null && ad != null) {
			mCurrentAd = ad;
			if (DEBUG) {
				Log.d(TAG, "showAd " + ad.getUri() + " " + ad.getAd().getType().toUpperCase());
			}
			AdvertisementBaseFragment fragment = null;
			final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			Type type = Type.valueOf(ad.getAd().getType().toUpperCase());
			if (type == Advertisement.Type.BANNER) {
				hide(mVideoAdFragment, transaction);
				hide(mTextAdFragment, transaction);
				show(mImageAdFragment, transaction);
				fragment = mImageAdFragment;
			} else if (type == Advertisement.Type.VIDEO) {
				hide(mImageAdFragment, transaction);
				hide(mTextAdFragment, transaction);
				show(mVideoAdFragment, transaction);
				fragment = mVideoAdFragment;
			} else if (type == Advertisement.Type.TEXT) {
				hide(mImageAdFragment, transaction);
				hide(mVideoAdFragment, transaction);
				show(mTextAdFragment, transaction);
				fragment = mTextAdFragment;
			}
			transaction.commitAllowingStateLoss();
			// Set approximate start time. More precise time will be set in onAdShown.
			mCurrentAdStartTime = System.currentTimeMillis();
			fragment.showAd(ad);
		}
	}
	
	protected void hide(Fragment fragment, FragmentTransaction transaction) {
		if (null != fragment) {
			transaction.hide(fragment);
		}
	}
	
	protected void show(Fragment fragment, FragmentTransaction transaction) {
		if (null != fragment) {
			transaction.show(fragment);
		}
	}
	
	@Override
	public void onAdShown(AdvertisementResource res) {
		if (DEBUG) {
			Log.d(TAG, "onAdShown " + res.getUri());
		}
		if (mCurrentAd.equals(res)) {
			// Disable mask transition if ad shown is what we scheduled previously. 
			mContainer.startMaskTransition(false);
			// Set precise start time.
			mCurrentAdStartTime = System.currentTimeMillis();
		} else {
			Log.w(TAG, "Wrong ad shown reported");
		}
	}
	
	@Override
	public void onAdFinished(AdvertisementResource res) {
		if (DEBUG) {
			Log.d(TAG, "onAdFinished " + res.getUri() + " " + isResumed());
		}
		if (mCurrentAd.equals(res)) {
			reportAd(res);
			mContainer.setMaskVisibility(true);
			showNextAd();
		} else {
			Log.w(TAG, "Wrong ad finished reported");
		}
	}

	public void reportAd(AdvertisementResource res) {
		if (DEBUG) {
			Log.d(TAG, "reportAd: " + String.valueOf(res != null));
		}
		if (res != null) {
			// TODO fill with right data
			StatsUtils.reportAdvertisementStats(res.getAdsGuid(), res.getGuid(), 					 
					String.valueOf(TransportApplication.getVehicle().getGuid()), 
					mServerExpectedISOTime.format(mCurrentAdStartTime), 
					System.currentTimeMillis() - mCurrentAdStartTime);
		}
	}

}
