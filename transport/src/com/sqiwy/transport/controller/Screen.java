/**
 * Created by abrysov
 */
package com.sqiwy.transport.controller;

public class Screen {
	
	/**
	 * Unique id for the screen.
	 */
	private final int id;
	
	/**
	 * Min time/duration in millis to show the screen
	 */
	private final long minShowTime;
	
	/**
	 * How much in relation to total time we want to display the screen.
	 * Should be in (0, 1]
	 */
	private final float targetShowCoefficient;
	
	public Screen(int id, long minShowTime, float targetShowCoefficient) {
		super();
		this.id = id;
		this.minShowTime = minShowTime;
		this.targetShowCoefficient = targetShowCoefficient;
	}

	public int getId() {
		return id;
	}

	public long getMinShowTime() {
		return minShowTime;
	}

	/**
	 * Override this to implement event dependent timing.
	 * @param event event which is triggering this screen.
	 * @return time/duration to show this screen.
	 */
	public long getShowTime(Event event) {
		return getMinShowTime();
	}
	
	public float getTargetShowCoefficient() {
		return targetShowCoefficient;
	}

	public boolean isEligibleFor(Event event) {
		return false;
	}
	
	public boolean isWithOutTimeLimit(Event event) {
		return false;
	}

	/**
	 * Is called when screen is going to be dismissed.
	 * @param event event caused the screen to be dismissed.
	 */
	public void aboutToBeDissmissed(Event event) {
	}

}
