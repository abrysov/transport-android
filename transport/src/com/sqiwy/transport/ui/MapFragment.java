/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.data.Point;
import com.sqiwy.transport.data.Route;
import com.sqiwy.transport.data.Vehicle;
import com.sqiwy.transport.data.VehicleLoader;
import com.sqiwy.transport.ui.ModelFragment.OnGeofenceTriggered;
import com.sqiwy.transport.util.PrefUtils;
import com.sqiwy.transport.util.UIUtils;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PathOverlay;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Vehicle>,
        SharedPreferences.OnSharedPreferenceChangeListener, OnGeofenceTriggered {

    private static final String TAG = MapFragment.class.getSimpleName();
    private static final int ROAD_WIDTH_DP = 20;

    private MapView mMapView;
    private CurrentLocationOverlay mCurrentLocationOverlay;
    private Vehicle mVehicle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.map_view);
        mMapView.getController().setZoom(mMapView.getMaxZoomLevel());
        mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mMapView.setMultiTouchControls(true);
        mMapView.setUseDataConnection(true);
        mMapView.getController().setCenter(new GeoPoint(55.730681, 37.486725));
        mMapView.getController().setZoom(17); // 18
        mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        addCurrentLocationOverlay(); // to enable geo tracking
        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefUtils.registerOnPreferenceChangeListener(this);

        if (mCurrentLocationOverlay != null) {
            mCurrentLocationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtils.unregisterOnPreferenceChangeListener(this);

        if (mCurrentLocationOverlay != null) {
            mCurrentLocationOverlay.disableMyLocation();
        }
    }

    @Override
    public Loader<Vehicle> onCreateLoader(int id, Bundle args) {
        return new VehicleLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Vehicle> loader, Vehicle vehicle) {
        changeVehicle(vehicle);
    }

    @Override
    public void onLoaderReset(Loader<Vehicle> loader) {
        changeVehicle(null);
    }

    private void changeVehicle(Vehicle vehicle) {
        if (mVehicle != vehicle) {
            mVehicle = vehicle;
            TransportApplication.setVehicle(mVehicle);
            setMapOverlays();
        }
    }

    private void setMapOverlays() {
        //Log.d(TAG, "*** called setMapOverlays()");
        mMapView.getOverlays().clear();
        if (mVehicle != null && !mVehicle.isRouteUndefined()) {
            addRoadOverlay();
            addBusStopsOverlay();
            addTestOverlay();
        }
        addCurrentLocationOverlay();
        mMapView.invalidate();
    }

    private void addRoadOverlay() {
        // Add the overlay for the whole road.
        Log.d(TAG, "*** called addRoadOverlay()");

        Context context = getActivity();
        Resources res = context.getResources();

        Route route = mVehicle.getActiveRoute();

        Paint roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (route.getPoints() != null) {

            //roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            roadPaint.setColor(res.getColor(R.color.road_color));
            roadPaint.setStrokeWidth(UIUtils.dpToPixels(context, ROAD_WIDTH_DP));
            roadPaint.setStyle(Paint.Style.STROKE);

            PathOverlay roadOverlay = new PathOverlay(0, context);
            roadOverlay.setPaint(roadPaint);

            Log.d(TAG, "*** Points in path : " + route.getPoints().size());
            for (Point point : route.getPoints()) {
                roadOverlay.addPoint(point.getLocation());
                Log.d(TAG, "*** add point :: Latitude - " + point.getLocation().getLatitude() + ", Longitude - " + point.getLocation().getLongitude());
            }

            mMapView.getOverlays().add(roadOverlay);
        } else {
            Log.e(TAG, "The Route points are empty:" + mVehicle.toString());
        }


        //Add the overlay for the active part of the road (between the previous and next bus stops).
        Point previousBusStop = mVehicle.getPreviousBusStop();
        Point nextBusStop = mVehicle.getNextBusStop();

        if (previousBusStop != null && nextBusStop != null) {
            boolean isStarted = false;
            Paint activeRoadPaint = new Paint(roadPaint);
            activeRoadPaint.setColor(res.getColor(R.color.active_road_color));
            PathOverlay activeRoadOverlay = new PathOverlay(0, context);
            activeRoadOverlay.setPaint(activeRoadPaint);

            // :FIXME calculate points on route between bus stops
/*
            for (Point point : route.getPoints()) {
                if (!isStarted) {
                    if (point.equals(previousBusStop)) {
                        activeRoadOverlay.addPoint(point.getLocation());
                        isStarted = true;
                    }
                } else {
                    activeRoadOverlay.addPoint(point.getLocation());
                    if (point.equals(nextBusStop)) {
                        break;
                    }
                }
            }
*/
            mMapView.getOverlays().add(activeRoadOverlay);
        }
    }

    private void addBusStopsOverlay() {
        mMapView.getOverlays().add(new BusStopsOverlay(getActivity(), mVehicle.getBusStopsForCurrentDirection(),
                mVehicle.getNextBusStop()));
    }

    private Overlay mTestOverlay = null;
    private List<Point> mTestPoints = new ArrayList<Point>();

    private void addTestOverlay() {
    	if (PrefUtils.getMapOnly()) {
    		mTestOverlay = new TestOverlay(getActivity(), mTestPoints);
            mMapView.getOverlays().add(mTestOverlay);
    	}
    }

    private void addCurrentLocationOverlay() {
        if (mCurrentLocationOverlay == null) {
            mCurrentLocationOverlay = new CurrentLocationOverlay(getActivity(), mMapView);
            mCurrentLocationOverlay.enableFollowLocation();
            mCurrentLocationOverlay.enableMyLocation();
            mCurrentLocationOverlay.setDrawAccuracyEnabled(false);
        }
        mMapView.getOverlays().add(mCurrentLocationOverlay);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(PrefUtils.PREF_ACTIVE_ROUTE_INDEX)
                || key.equals(PrefUtils.PREF_PREVIOUS_BUS_STOP_INDEX)) {
            setMapOverlays();
        }
    }

    @Override
    public void geofenceTriggered(String geofenceType, Point point) {
        if (PrefUtils.getMapOnly()) {
            if (geofenceType == null || point == null) {
                Log.d(TAG, "reset test overlay");
                mTestPoints.clear();
            } else {
                Log.d(TAG, geofenceType + " [order:" + point.getOrder() + ", direction:" + (!mVehicle.isRouteUndefined() ? mVehicle.getActiveRoute().getDirection() : "NULL") + "]");
                mTestPoints.add(point);
            }
        }
    }

}
