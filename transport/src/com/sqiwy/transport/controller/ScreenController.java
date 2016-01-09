/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller;

import java.util.List;

public abstract class ScreenController {
	
	public interface OnScreenControllerListener {
		public void onNextScreen(Screen screen, Event event);
	}
	
	private OnScreenControllerListener mOnScreenControllerListener;
	
	public abstract void start();
	
	public abstract void stop();
	
	public abstract void resetScreens();
	
	public abstract void addScreen(Screen screen);
	
	public abstract void addEvent(Event event);
	
	public abstract List<Screen> getScreens();
	
	public void setOnScreenControllerListener(OnScreenControllerListener listener) {
		mOnScreenControllerListener = listener;
	}
	
	public OnScreenControllerListener getOnScreenControllerListener() {
		return mOnScreenControllerListener;
	}
}
