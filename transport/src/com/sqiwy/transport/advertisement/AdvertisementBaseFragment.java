/**
 * Created by abrysov
 */
package com.sqiwy.transport.advertisement;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.sqiwy.transport.BuildConfig;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;


public abstract class AdvertisementBaseFragment extends Fragment implements Target {
	
	private String TAG = getClass().getName();
	
	public interface AdvertisemenFragmentListener {
		public void onAdFinished(AdvertisementResource ad);
		public void onAdShown(AdvertisementResource ad);
	}
	
	public static final String EXTRA_AD = "extra-ad";
	
	protected static final int MSG_WHAT_COMPLETED = 0x01;
	protected static final int MSG_WHAT_SHOWN = 0x02;
	
	protected Handler mHandler;
	
	protected class AdHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "handleMessage: " + msg.what);
			}
			Fragment fragment = getTargetFragment();
			Activity activity = getActivity();
			AdvertisementResource ad = getRes();
			
			switch (msg.what) {
			case MSG_WHAT_COMPLETED:

				if (fragment instanceof AdvertisemenFragmentListener) {
					((AdvertisemenFragmentListener) fragment).onAdFinished(ad);
				}
				
				if (activity instanceof AdvertisemenFragmentListener) {
					((AdvertisemenFragmentListener) activity).onAdFinished(ad);
				}
				
				break;
			case MSG_WHAT_SHOWN:
				
				if (fragment instanceof AdvertisemenFragmentListener) {
					((AdvertisemenFragmentListener) fragment).onAdShown(ad);
				}
				
				if (activity instanceof AdvertisemenFragmentListener) {
					((AdvertisemenFragmentListener) activity).onAdShown(ad);
				}
				
				break;
			}
		};
	};
	
	public AdvertisementBaseFragment() {
		setArguments(new Bundle());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new AdHandler();
	}
	
	public final void showAd(AdvertisementResource ad) {
		setRes(ad);
		
		/*if (null != ad.getDetailBackground()) {
			Picasso.with(getActivity().getApplicationContext()).load(
					AssetsProvider.getUri(ad.getDetailBackground())).into(this);
		}*/
		
		showAdImpl(ad);
	}
	
	@Override
	public void onBitmapLoaded(Bitmap b, LoadedFrom from) {
		getView().setBackground(new BitmapDrawable(getResources(), b));
	}
	
	@Override
	public void onBitmapFailed(Drawable d) {}	
	@Override
	public void onPrepareLoad(Drawable d) {}
	
	protected abstract void showAdImpl(AdvertisementResource ad);
	
	public AdvertisementResource getRes() {
		return (AdvertisementResource) getArguments().getSerializable(EXTRA_AD);
	}
	
	public void setRes(AdvertisementResource ad) {
		getArguments().putSerializable(EXTRA_AD, ad);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		clearMessages();
	}
	
	public void clearMessages() {
		mHandler.removeCallbacksAndMessages(null);
	}
}
