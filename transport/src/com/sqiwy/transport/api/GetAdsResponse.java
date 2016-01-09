/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import java.util.List;

import com.sqiwy.transport.advertisement.Advertisement;

public class GetAdsResponse extends BaseResponse {
	List<Advertisement> ads;

	public List<Advertisement> getAds() {
		return ads;
	}

	public void setAds(List<Advertisement> ads) {
		this.ads = ads;
	}
	
}
