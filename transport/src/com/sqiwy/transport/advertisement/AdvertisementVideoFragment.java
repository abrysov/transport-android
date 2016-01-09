/**
 * Created by abrysov
 */
package com.sqiwy.transport.advertisement;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.sqiwy.transport.R;

public class AdvertisementVideoFragment extends AdvertisementBaseFragment implements OnPreparedListener, OnErrorListener, OnCompletionListener {

	private static final String TAG = AdvertisementVideoFragment.class.getName();
	
	private VideoView mVideoView;
	//private View mProgress;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.advertisement_video_fragment, container, false);

		mVideoView = (VideoView) v.findViewById(R.id.ad_video);

		//mProgress = v.findViewById(R.id.progress);
		//mProgress.setVisibility(View.GONE);
		
		return v;
	}
	
	@Override
	public void showAdImpl(final AdvertisementResource ad) {
		if (mVideoView.isPlaying()) {
			silentStop();
		}

		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnErrorListener(this);
		
		//mVideoView.setVideoURI(AssetsProvider.getUri(ad.getName()));
		mVideoView.setVideoURI(Uri.parse(ad.getAccessUri()));
		mVideoView.start();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		mHandler.sendEmptyMessage(MSG_WHAT_COMPLETED);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.w(TAG, "Error occured while playing video ad [what = " + what +"], [extra = " + extra + "]");
		mHandler.sendEmptyMessage(MSG_WHAT_COMPLETED);
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
    	//mProgress.setVisibility(View.GONE);
		mHandler.sendEmptyMessage(MSG_WHAT_SHOWN);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		silentStop();
	}

	protected void silentStop() {
		mVideoView.setOnCompletionListener(null);
		mVideoView.setOnPreparedListener(null);
		mVideoView.setOnErrorListener(null);
		mVideoView.stopPlayback();
	}
}
