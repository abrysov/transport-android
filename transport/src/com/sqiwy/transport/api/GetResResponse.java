/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import java.util.List;

import com.sqiwy.transport.advertisement.AdvertisementResource;

public class GetResResponse {
	List<AdvertisementResource> resources;

	public List<AdvertisementResource> getResources() {
		return resources;
	}

	public void setResources(List<AdvertisementResource> resources) {
		this.resources = resources;
	}

}
