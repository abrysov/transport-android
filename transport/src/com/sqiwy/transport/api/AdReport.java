/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class AdReport {
	
	@SerializedName("ad_guid")
	private String mAdGuid;
	@SerializedName("content_guid")
	private String mContentGuid;
	@SerializedName("route_guid")
	private String mRouteGuid;
	@SerializedName("start_time")
	private String mStartTime;
	@SerializedName("duration")
	private String mDuration;
	
	public AdReport(Map<String, String> stats) {
		setmAdGuid(stats.get("ad_guid"));
		setmContentGuid(stats.get("content_guid"));
		setmRouteGuid(stats.get("route_guid"));
		setmStartTime(stats.get("start_time"));
		setmDuration(stats.get("duration"));
	}

	public String getmAdGuid() {
		return mAdGuid;
	}

	public void setmAdGuid(String mAdGuid) {
		this.mAdGuid = mAdGuid;
	}

	public String getmContentGuid() {
		return mContentGuid;
	}

	public void setmContentGuid(String mContentGuid) {
		this.mContentGuid = mContentGuid;
	}

	public String getmStartTime() {
		return mStartTime;
	}

	public void setmStartTime(String mStartTime) {
		this.mStartTime = mStartTime;
	}

	public String getmRouteGuid() {
		return mRouteGuid;
	}

	public void setmRouteGuid(String mRouteGuid) {
		this.mRouteGuid = mRouteGuid;
	}

	public String getmDuration() {
		return mDuration;
	}

	public void setmDuration(String mDuration) {
		this.mDuration = mDuration;
	}

}
