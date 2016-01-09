/**
 * Created by abrysov
 */
package com.sqiwy.transport.advertisement;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sqiwy.transport.R;

public class AdvertisementTextFragment extends AdvertisementBaseFragment {

	private static final String TAG = AdvertisementTextFragment.class.getName();
	
	private TextView mTextView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.advertisement_text_fragment, container, false);
		mTextView = (TextView) view.findViewById(R.id.ad_text);
		mTextView.setVisibility(View.INVISIBLE);
		return view;
	}

	@Override
	protected void showAdImpl(AdvertisementResource ad) {
		mTextView.setVisibility(View.INVISIBLE);

		Uri uri = Uri.parse(ad.getAccessUri());
		
		InputStream stream = null;
		try {
			stream = new FileInputStream(new File(uri.getPath()));
		
			String text = IOUtils.toString(stream);
			mTextView.setText(text);
			mTextView.setVisibility(View.VISIBLE);
			
			mHandler.sendEmptyMessage(MSG_WHAT_SHOWN);
			mHandler.sendEmptyMessageDelayed(MSG_WHAT_COMPLETED, getRes().getAd().getShowDuration());
			
		} catch (Exception e) {
			
			Log.e(TAG, "Error while showing text ad.", e);
			
			mHandler.sendEmptyMessage(MSG_WHAT_COMPLETED);
			
		} finally {
			IOUtils.closeQuietly(stream);
		}
		
	}

}
