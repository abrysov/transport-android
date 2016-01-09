/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.util.Log;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.api.GetRouteResponse;
import com.sqiwy.transport.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {

    private static final String TAG = Vehicle.class.getSimpleName();
	
	private long mId = -1;
	private String mName;
	private String mDescription;
	private String mGuid;
	private int mVersion;
	private float mAdsRatio;
	private float mMapRatio;
	private float mContentRatio;
	private int mStopsRadius;

	private List<Route> mRoutes;
	
	private final List<Point> mBusStops = new ArrayList<Point>();
	private final List<String> mBusStopsDirections = new ArrayList<String>();
	
	/**
	 * @return the id
	 */
	public long getId() {
		return mId;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.mId = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.mName = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return mDescription;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.mDescription = description;
	}
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return mGuid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.mGuid = guid;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return mVersion;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.mVersion = version;
	}

    public boolean isVersionMatch(Vehicle vehicle) {
        return vehicle != null && vehicle.mGuid.equals(mGuid) && vehicle.mVersion == mVersion;
    }

	/**
	 * @return the routes
	 */
	public List<Route> getRoutes() {
		return mRoutes;
	}
	/**
	 * @param routes the routes to set
	 */
	public void setRoutes(List<Route> routes) {
		mRoutes = routes;
		mBusStops.clear();
		mBusStopsDirections.clear();
        if (mRoutes != null) {
        	for (Route route : mRoutes) {
	            for (Point point : route.getBusStops()) {
                    mBusStops.add(point);
	                mBusStopsDirections.add(route.getDirection());
	            }
        	}
        }
	}

    public static Vehicle fromResponse(GetRouteResponse response) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(response.number);
        vehicle.setDescription(response.description);
        vehicle.setGuid(response.guid);
        vehicle.setVersion(response.version);
        vehicle.setRoutes(response.routes);
        vehicle.setAdsRatio((float) response.ads_ratio / 100);
        vehicle.setMapRatio((float) response.map_ratio / 100);
        vehicle.setContentRatio((float) response.content_ratio / 100);
        vehicle.setStopsRadius(response.stops_radius);
        return vehicle;
    }

    public boolean isRouteUndefined() {
        return getActiveRoute() == null;
    }

    public Route getActiveRoute() {
    	int activeRouteIndex = PrefUtils.getActiveRouteIndex();
    	if (activeRouteIndex == -1) {
    		return null;
    	} else {
    		return mRoutes.get(activeRouteIndex);
    	}
    }

    public int getRouteIndexFromDirection(String direction) {
        for (int i = 0; i < mRoutes.size(); i++) {
            if (mRoutes.get(i).getDirection().equalsIgnoreCase(direction)) {
                return i;
            }
        }
        return -1; // :FIXME calculation of direct/revert route should be rewritten
    }
    
	private String determineDirection(Point previousBusStop, Point currentBusStop) {
		if (currentBusStop == null || previousBusStop == null || currentBusStop == previousBusStop) {
			return null;
		}
		for (Route route : this.getRoutes()) {
			List<Point> points = route.getBusStops();
			if (points.contains(currentBusStop) 
					&& points.contains(previousBusStop)) {
				if (currentBusStop.getOrder() > previousBusStop.getOrder()) {
					return route.getDirection();
				}
			}
		}
		return null;
	}

    public boolean changeActiveRouteIfNeeded(Point busStop) {
    	boolean isChanged = false;
        // If the previous bus stop of the active route is the last one,
        // then switch to the other route.
        if (getActiveRoute() != null && getActiveRoute().getBusStops().indexOf(busStop) >= getActiveRoute().getBusStops().size() - 1) {
            Log.d(TAG, "Trying to switch the Active Route. Previous[" + PrefUtils.getPreviousBusStopIndex() + "], Active Route["+getActiveRoute().getDirection()+"], routes < " + mRoutes + " >");
            PrefUtils.setPreviousBusStopIndex(0);
            int activeRouteIndex = PrefUtils.getActiveRouteIndex();
            PrefUtils.setActiveRouteIndex(activeRouteIndex == 0 ? 1 : 0);
            isChanged = true;
        } else {
        	int previousBusStopIndex = PrefUtils.getPreviousBusStopIndex();

            if (getActiveRoute() == null && previousBusStopIndex != -1) {
                String direction = determineDirection(mBusStops.get(PrefUtils.getPreviousBusStopIndex()), busStop);
                if (direction != null) {
                    PrefUtils.setActiveRouteIndex(getRouteIndexFromDirection(direction));
                    isChanged = true;
                }
                if (BuildConfig.DEBUG) {
                    Log.d(this.getClass().getName(), "direction detecting attempt: " + direction);
                }
            }

      		changePreviousBusStop(busStop);
        }
        return isChanged;
    }
    
    public List<Point> getBusStops() {
        return mBusStops;
    }

    public int getBusStopCount() {
        return mBusStops.size();
    }
    
    public int getBusStopCountForCurrentDirection() {
    	int count = 0;
		String currentDirection = getActiveRoute().getDirection();
		for (String direction : mBusStopsDirections) {
			if (direction.equals(currentDirection)) {
				count ++;
			}
		}
		return count;
	}
	
	public List<Point> getBusStopsForCurrentDirection() {
		ArrayList<Point> result = new ArrayList<Point>();
		String currentDirection = getActiveRoute().getDirection();
		for (int i = 0; i < mBusStops.size(); i++) {
			if (mBusStopsDirections.get(i).equals(currentDirection)) {
				result.add(mBusStops.get(i));
			}
		}
		return result;
	}

    public Point getPreviousBusStop() {
        int previousBusStopIndex = PrefUtils.getPreviousBusStopIndex();
        if (previousBusStopIndex > -1) {
        	return previousBusStopIndex < mBusStops.size() ? mBusStops.get(previousBusStopIndex) : null;
        } else {
        	return null;
        }
    }

    public Point getNextBusStop() {
        int previousBusStopIndex = PrefUtils.getPreviousBusStopIndex();
        if (previousBusStopIndex > -1) {
        	return previousBusStopIndex < mBusStops.size() - 1 ? mBusStops.get(previousBusStopIndex + 1) : null;
        } else {
        	return null;
        }
    }

    public void changePreviousBusStop(Point busStop) {
    	PrefUtils.setPreviousBusStopIndex(mBusStops.indexOf(busStop));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vehicle vehicle = (Vehicle) o;

        if (mId != vehicle.mId) return false;
        if (mVersion != vehicle.mVersion) return false;
        if (mDescription != null ? !mDescription.equals(vehicle.mDescription) : vehicle.mDescription != null)
            return false;
        if (mGuid != null ? !mGuid.equals(vehicle.mGuid) : vehicle.mGuid != null) return false;
        if (mName != null ? !mName.equals(vehicle.mName) : vehicle.mName != null) return false;
        if (mRoutes != null ? !mRoutes.equals(vehicle.mRoutes) : vehicle.mRoutes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
        result = 31 * result + (mGuid != null ? mGuid.hashCode() : 0);
        result = 31 * result + mVersion;
        result = 31 * result + (mRoutes != null ? mRoutes.hashCode() : 0);
        return result;
    }

	@Override
	public String toString() {
		return "Vehicle [id=" + mId + ", name=" + mName + ", description=" + mDescription
                + ", guid=" + mGuid + ", version=" + mVersion + ", routes=" + mRoutes + "]";
	}
	
	public float getAdsRatio() {
		return mAdsRatio;
	}
	
	public float getMapRatio() {
		return mMapRatio;
	}
	
	public int getStopsRadius() {
		return mStopsRadius;
	}
	
	public float getContentRatio() {
		return mContentRatio;
	}
	
	public void setContentRatio(float contentRatio) {
		this.mContentRatio = contentRatio;
	}
	
	public void setMapRatio(float mapRatio) {
		this.mMapRatio = mapRatio;
	}
	
	public void setAdsRatio(float adsRatio) {
		this.mAdsRatio = adsRatio;
	}
	
	public void setStopsRadius(int stopsRadius) {
		this.mStopsRadius = stopsRadius;
	}

}
