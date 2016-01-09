/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.controller.Event.Source;
import com.sqiwy.transport.controller.Event.Type;
import com.sqiwy.transport.controller.event.EventEnterAdGeofence;
import com.sqiwy.transport.controller.event.EventEnterBusStopGeofence;
import com.sqiwy.transport.controller.event.EventExitBusStopGeofence;
import com.sqiwy.transport.controller.screen.MapScreen;
import com.sqiwy.transport.util.PrefUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ScreenControllerImpl extends ScreenController {

	private static final String TAG = ScreenControllerImpl.class.getSimpleName();
	
	private long mStartTime;
	
	private Handler mHandler;
	
	private List<Screen> mScreens;
	private List<Event> mEvents;
	
	private Map<Screen, Long> mScreensTotalTimes;
	
	private long mCurrentScreenStartTime;
	private Screen mCurrentScreen;
	
	private long mTotalTime;
	
	public ScreenControllerImpl() {
		mHandler = new ScreenControllerHandler(this);
		mScreens = new ArrayList<Screen>();
		mEvents = new ArrayList<Event>();
		mScreensTotalTimes = new LinkedHashMap<Screen, Long>();
	}
	
	@Override
	public void start() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "start");
		}
		
		if (0 == mStartTime) {
			mStartTime = System.currentTimeMillis();
			nextScreen(true, null);
		}
	}
	
	@Override
	public void stop() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "stop");
		}
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void addScreen(Screen screen) {
		if (mScreens.contains(screen)) {
			throw new IllegalArgumentException("Screen: " + screen + " already added.");
		}
		mScreens.add(screen);
		mScreensTotalTimes.put(screen, Long.valueOf(0));
	}
	
	@Override 
	public void resetScreens() {
		mScreens.clear();
	}
	
	@Override
	public void addEvent(Event event) {
		mEvents.add(event);
		if (BuildConfig.DEBUG) {
			String type = null;
			if (event instanceof EventEnterAdGeofence) {
				type = "EventEnterAdGeofence";
			}
			if (event instanceof EventEnterBusStopGeofence) {
				type = "EventEnterBusStopGeofence";
			}
			if (event instanceof EventExitBusStopGeofence) {
				type = "EventExitBusStopGeofence";
			}
		}
		switch (event.type) {
			case NON_FORCING:
				nextScreen(false, event);
				break;
			case FORCING:
				// Schedule next screen
				nextScreen(true, event);
				break;
		}
	}

	private boolean mInBusStopAreaNow = false;
	
	private void nextScreen(boolean force, Event event) {
		if (event != null) {
			if (event.source == Source.SCHEDULE && mInBusStopAreaNow) {
				return;
			}
			if (event instanceof EventEnterBusStopGeofence) {
				mInBusStopAreaNow = true;
			}
			if (event instanceof EventExitBusStopGeofence) {
				mInBusStopAreaNow = false;
			}
		}

		long totalCurrentScreenTime = 0;
		long currentScreenTime = 0;
		if (mCurrentScreen != null) {
			currentScreenTime = System.currentTimeMillis() - mCurrentScreenStartTime;
			totalCurrentScreenTime = mScreensTotalTimes.get(mCurrentScreen);
			totalCurrentScreenTime += currentScreenTime; 
		}
		long totalTime = mTotalTime + currentScreenTime;
		
		Screen nextScreen = null;		
		// Find screen with minimal time according to ratio
		float minRatio = Float.MAX_VALUE;
		for (Screen screen : mScreens) {
			if (PrefUtils.getMapOnly()) {
				if (screen instanceof MapScreen) {
					nextScreen = screen;
					break;
				}
			} else {
				long time = mScreensTotalTimes.get(screen);
				// Current screen may also be a candidate if we are not forcing
				if (!force && screen == mCurrentScreen) {
					time = totalCurrentScreenTime;
				} else if (screen == mCurrentScreen) {
					// Skip current screen if are forcing the next screen
					continue;
				}
				if (!screen.isEligibleFor(event)) {
					continue;
				}
				if (0 == screen.getShowTime(event) && !screen.isWithOutTimeLimit(event)) {
					continue;
				}
				float currentCoefficient;
				if (0 == totalTime) {
					currentCoefficient = 0;
				} else {
					currentCoefficient = (float) time / totalTime;
				}
				// For instance target is 10/30 but currently only 5/30, ratio = 1/2
				// For instance target is 20/30 but currently only 19/30, ratio 19/20
				float minRatioCandidate = currentCoefficient / screen.getTargetShowCoefficient();
				if (minRatio > minRatioCandidate) {
					minRatio = minRatioCandidate;
					nextScreen = screen;
				}
			}
		}
		
		if (nextScreen != null) {
			// Adjust total time for current screen
			
			if (null != mCurrentScreen) {
				mScreensTotalTimes.put(mCurrentScreen, totalCurrentScreenTime);
				mTotalTime += currentScreenTime;
				
				mCurrentScreen.aboutToBeDissmissed(event);
			}

			mCurrentScreen = nextScreen;
			mCurrentScreenStartTime = System.currentTimeMillis();
			
			if (!nextScreen.isWithOutTimeLimit(event)) {
				mHandler.removeCallbacksAndMessages(null);
				Message msg = mHandler.obtainMessage(ScreenControllerHandler.WHAT_SCHEDULE_SCREEN_TIMEOUT);
				mHandler.sendMessageDelayed(msg, nextScreen.getShowTime(event));
			}
			
			OnScreenControllerListener onScreenControllerListener = getOnScreenControllerListener();
			if (null != onScreenControllerListener) {
				onScreenControllerListener.onNextScreen(nextScreen, event);
			}
		}
		
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Next screen: " + nextScreen);
			Log.d(TAG, "Total time:  " + mTotalTime);
			Log.d(TAG, "Screens total times:");
			for (Map.Entry<Screen, Long> entry : mScreensTotalTimes.entrySet()) {
				Log.d(TAG, "*** " + entry.getKey() + " = " + entry.getValue() + " [" + (0 == mTotalTime ? 0 : ((float) entry.getValue() / mTotalTime)) + "]");
			}
			Log.d(TAG, "\n");
		}
	}
	
	@Override
	public List<Screen> getScreens() {
		return mScreens;
	}
	
	private static class ScreenControllerHandler extends Handler {
		
		private static final int WHAT_SCHEDULE_SCREEN_TIMEOUT = 0x01;
		private ScreenControllerImpl mController;
		
		
		public ScreenControllerHandler(ScreenControllerImpl screenControllerImpl) {
			mController = screenControllerImpl;
		}

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
				case WHAT_SCHEDULE_SCREEN_TIMEOUT:
					
					Event event = new Event();
					event.type = Type.NON_FORCING;
					event.source = Source.SCHEDULE;
					event.timestamp = System.currentTimeMillis();
					mController.addEvent(event);
					
					break;
			}
		}
	}

}
