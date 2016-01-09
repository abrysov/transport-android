/**
 * Created by abrysov
 */
package com.sqiwy.transport.advertisement;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sqiwy.transport.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class AdvertisementImageFragment extends AdvertisementBaseFragment implements Target {

	private ImageView mImageView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.advertisement_image_fragment, container, false);
		mImageView = (ImageView) view.findViewById(R.id.ad_image);
		mImageView.setVisibility(View.INVISIBLE);
		return view;
	}

	@Override
	protected void showAdImpl(AdvertisementResource ad) {
		mImageView.setVisibility(View.INVISIBLE);

		Picasso.with(getActivity()).load(Uri.parse(ad.getAccessUri())).noFade().into(this);
	}

	@Override
	public void onBitmapFailed(Drawable arg) {
		mHandler.sendEmptyMessage(MSG_WHAT_COMPLETED);
	}

	@Override
	public void onBitmapLoaded(Bitmap arg, LoadedFrom from) {
		mImageView.setImageBitmap(arg);
		mImageView.setVisibility(View.VISIBLE);

		mHandler.sendEmptyMessage(MSG_WHAT_SHOWN);
		mHandler.sendEmptyMessageDelayed(MSG_WHAT_COMPLETED, getRes().getAd().getShowDuration());
	}

	@Override
	public void onPrepareLoad(Drawable arg) {}

}
