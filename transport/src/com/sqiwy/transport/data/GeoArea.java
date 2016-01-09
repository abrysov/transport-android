/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

/**
 * Describes geo area/location.
 */
public interface GeoArea extends Serializable {

	GeoPoint getLocation();
	float getRadius();
}
