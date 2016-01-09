/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.data.Point;
import com.sqiwy.transport.data.Vehicle;
import com.sqiwy.weather.WeatherData;
import com.sqiwy.weather.WeatherServiceHelper;

import java.util.List;

public class ActionBarFragment extends Fragment {

    private static final String TAG = ActionBarFragment.class.getName();

    private enum InfoFragments {
		TIME,
		WEATHER,
		WIFI
	}

	/** Values of info fragments enum. [DO NOT EDIT] */
	private static final InfoFragments[] INFO_FRAGMENT_VALUES = InfoFragments.values();
	
	/** Number of available different info fragments. */
	private static final int INFO_FRAGMENTS_NUMBER = INFO_FRAGMENT_VALUES.length;
	
	private WeatherServiceHelper mWeatherHelper;
	
	/**
	 * Is used to schedule info (weather, wifi, time) showing.
	 */
	private Handler mInfoHandler;
	
	private String mVehicle;
	private String mVehicleStop;

    //private List<>
	
	private TextView mVehicleTextView;
	private TextView mVehicleStopTextView;

	private String[] mWeatherConditions;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		mWeatherConditions = getResources().getStringArray(R.array.weather_conditions);
		
		mWeatherHelper = new WeatherServiceHelper();
		mInfoHandler = new InfoHandler();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_action_bar, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mVehicleTextView = (TextView) view.findViewById(R.id.action_bar_vehicle);
        mVehicleStopTextView = (TextView) view.findViewById(R.id.action_bar_vehicle_stop);
	}

	@Override
	public void onResume() {
		super.onResume();
		mWeatherHelper.bind(getActivity());
		mInfoHandler.sendEmptyMessage(InfoHandler.WHAT_SHOW_NEXT_INFO);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mInfoHandler.removeCallbacksAndMessages(null);
		mWeatherHelper.unbind(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public void setVehicle(String vehicle, boolean animate) {
		mVehicle = vehicle;
		setText(mVehicleTextView, vehicle, animate);
	}
	
	public void setVehicleStop(final String vehicleStop, boolean animate) {
		mVehicleStop = vehicleStop;
		setText(mVehicleStopTextView, vehicleStop, animate);
	}
	
	protected void setText(final TextView textView, final String value, boolean animate) {
		
		// Finish animator set which may be running currently.
		Object oldSet = textView.getTag(R.id.text_view_animator_set);
		if (null != oldSet) {
			((AnimatorSet) oldSet).end();
		}
		
		if (animate) {
			// Create and schedule animator set which changes text.
			ObjectAnimator alphaMinus = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
			alphaMinus.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					textView.setText(value);
				}
			});
			
			ObjectAnimator alphaPlus = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
			alphaPlus.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					textView.setTag(R.id.text_view_animator_set, null);
				}
			});
			
			AnimatorSet set = new AnimatorSet();
			set.playSequentially(alphaMinus, alphaPlus);
			set.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
			set.start();
			
			textView.setTag(R.id.text_view_animator_set, set);
			
		} else {
			textView.setText(value);
		}
	}
	
	class InfoHandler extends Handler {
		
		private static final int WHAT_SHOW_NEXT_INFO = 0x01;
        private static final double DELTA = 0.002;
        private int counterOfShownFragments;
        private String busStopName = "";


		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

            double curLatPositioMap = 0.0;
            double curLonPositioMap = 0.0;

			switch (msg.what) {
                case WHAT_SHOW_NEXT_INFO:

                    if(TransportApplication.getVehicle() != null) {
                        Vehicle vehicle = TransportApplication.getVehicle();
                        setVehicle(vehicle.getName(), false);

                        List<Point> busStops = vehicle.getBusStops();


                        if (TransportApplication.getCurrentLocation() != null) {
                            curLatPositioMap = TransportApplication.getCurrentLocation().getLatitude();
                            curLonPositioMap = TransportApplication.getCurrentLocation().getLongitude();
                        }


                        Log.d(TAG, "*** Current location is :: lat:" + curLatPositioMap + ", lon:" + curLonPositioMap);

                        for (int i=0; i < busStops.size(); i++){
                            if(TransportApplication.getCurrentLocation() != null) {

                                if ((busStops.get(i).getLatitude() > curLatPositioMap - DELTA && busStops.get(i).getLatitude() < curLatPositioMap + DELTA) &&
                                        (busStops.get(i).getLongitude() > curLonPositioMap - DELTA && busStops.get(i).getLongitude() < curLonPositioMap + DELTA)) {

                                    if(i != busStops.size()-1){
                                        busStopName = busStops.get(i + 1).getName();
                                    } else {
                                        busStopName = busStops.get(busStops.size()-1).getName();
                                    }

                                }
                                setVehicleStop(busStopName, true);
                            }
                        }

                    }

                    Fragment fragmentToAdd = null;
                    Bundle args = null;

                    switch (INFO_FRAGMENT_VALUES[counterOfShownFragments++ % INFO_FRAGMENTS_NUMBER]) {
                        case TIME:

                            fragmentToAdd = new ActionBarDateFragment();

                            break;

                        case WEATHER:

                            args = new Bundle();

                            WeatherData weatherData = mWeatherHelper.getWeatherData(getActivity());

                            // TODO: add expiration, for instance if weather has timestamp more than 2 hours ago - don't show weather
                            if (null != weatherData && weatherData.conditionCode < mWeatherConditions.length) {
                                args.putInt(ActionBarInfoFragment.ARG_INFO_ICON_RESOURCE,
                                        WeatherData.getConditionIconId(weatherData.conditionCode));
                                args.putString(ActionBarInfoFragment.ARG_INFO_TEXT,
                                        weatherData.temperature + "°, " + mWeatherConditions[weatherData.conditionCode]);

                                fragmentToAdd = new ActionBarInfoFragment();
                            }

                            break;

                        case WIFI:

                            args = new Bundle();
                            args.putInt(ActionBarInfoFragment.ARG_INFO_ICON_RESOURCE, R.drawable.ic_wifi);
                            args.putString(ActionBarInfoFragment.ARG_INFO_TEXT, "Бесплатный WiFi");

                            fragmentToAdd = new ActionBarInfoFragment();

                            break;
                    }

                    long delay;
                    if (null != fragmentToAdd) {
                        if (null != args) {
                            fragmentToAdd.setArguments(args);
                        }

                        Fragment fragmentToRemove = getFragmentManager().findFragmentByTag("info");

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.info_in, R.anim.info_out);

                        if (null != fragmentToRemove) {
                            transaction.remove(fragmentToRemove);
                        }

                        transaction.add(R.id.action_bar_info_container, fragmentToAdd, "info");

                        transaction.commitAllowingStateLoss();

                        // TODO: get requirements for delay
                        // Arbitrary delay for now
                        delay = 5000;
                    } else {
                        delay = 0;
                    }

                    // Schedule next info
                    removeMessages(WHAT_SHOW_NEXT_INFO);
                    sendEmptyMessageDelayed(WHAT_SHOW_NEXT_INFO, delay);

                    break;
            }

		}
	}
	
}
