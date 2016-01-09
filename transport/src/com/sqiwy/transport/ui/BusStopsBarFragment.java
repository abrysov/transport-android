package com.sqiwy.transport.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.sqiwy.transport.R;
import com.sqiwy.transport.data.Point;
import com.sqiwy.transport.data.Vehicle;
import com.sqiwy.transport.data.VehicleLoader;
import com.sqiwy.transport.ui.view.BusStopLayout;
import com.sqiwy.transport.util.PrefUtils;

import java.util.List;

public class BusStopsBarFragment extends Fragment implements LoaderManager.LoaderCallbacks<Vehicle>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private View mBusStopsLayout;
    private BusStopLayout mBusStopLayout1;
    private BusStopLayout mBusStopLayout2;
    private BusStopLayout mBusStopLayout3;
    private BusStopLayout mBusStopLayout4;
    private ImageView mBusImageView;
    private Vehicle mVehicle;

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus_stops_bar, container, false);
        mBusStopLayout1 = (BusStopLayout) view.findViewById(R.id.bus_stop_layout_1);
        mBusStopLayout2 = (BusStopLayout) view.findViewById(R.id.bus_stop_layout_2);
        mBusStopLayout3 = (BusStopLayout) view.findViewById(R.id.bus_stop_layout_3);
        mBusStopLayout4 = (BusStopLayout) view.findViewById(R.id.bus_stop_layout_4);
        mBusImageView = (ImageView) view.findViewById(R.id.iv_bus);

        // Add a space for the hidden bus stop layout for the correct animation.
        mBusStopsLayout = view.findViewById(R.id.layout_bus_stops);
        mBusStopsLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int busStopLayoutWidth = mBusStopLayout1.getWidth();
                mBusStopLayout4.getLayoutParams().width = busStopLayoutWidth;
                mBusStopsLayout.getLayoutParams().width = mBusStopsLayout.getWidth() + busStopLayoutWidth;
                mBusStopsLayout.requestLayout();
                mBusStopsLayout.removeOnLayoutChangeListener(this);
            }
        });

        getLoaderManager().initLoader(0, null, this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefUtils.registerOnPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtils.unregisterOnPreferenceChangeListener(this);
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
        if (vehicle == null) {
            mVehicle = null;
            return;
        }

        if (mVehicle != vehicle) {
            mVehicle = vehicle;
            if (!mVehicle.isRouteUndefined()) {
                updateBusStopLayouts();
            }
        }
    }

    private void updateBusStopLayouts() {
        if (mVehicle == null) {
            return;
        }

        int busStopCount = mVehicle.getBusStopCountForCurrentDirection();
        if (busStopCount < 3) {
            return;
        }

        List<Point> busStops = mVehicle.getBusStopsForCurrentDirection();
        String arriveMin;
        int i = PrefUtils.getPreviousBusStopIndex();
        if (i == -1) {
        	return;
        }
        if (i < busStopCount - 2) {
            arriveMin = "" + busStops.get(i + 1).getArrivalTime().getMinutes();

            updateBusStopLayout(mBusStopLayout1, BusStopLayout.Type.PREVIOUS, busStops.get(i).getName(), "");
            updateBusStopLayout(mBusStopLayout2, BusStopLayout.Type.NEXT, busStops.get(i + 1).getName() + (arriveMin.equals("0") ? "" : "("+arriveMin+" мин)"), "");
            updateBusStopLayout(mBusStopLayout3, BusStopLayout.Type.GENERAL, busStops.get(i + 2).getName(), "");
            if (i < busStopCount - 3) {
                arriveMin = "" + busStops.get(i + 3).getArrivalTime().getMinutes();
                updateBusStopLayout(mBusStopLayout4, BusStopLayout.Type.GENERAL, busStops.get(i + 3).getName() + (arriveMin.equals("0") ? "" : "("+arriveMin+" мин)"), "");
            }
            mBusImageView.setTranslationX(0);
        } else if (i < busStopCount - 1) {
            arriveMin = "" + busStops.get(i+1).getArrivalTime().getMinutes();
            updateBusStopLayout(mBusStopLayout1, BusStopLayout.Type.GENERAL, busStops.get(i - 1).getName(), "");
            updateBusStopLayout(mBusStopLayout2, BusStopLayout.Type.PREVIOUS, busStops.get(i).getName(), "");
            updateBusStopLayout(mBusStopLayout3, BusStopLayout.Type.NEXT, busStops.get(i + 1).getName() + (arriveMin.equals("0") ? "" : "("+arriveMin+" мин)"), "");
            mBusImageView.setTranslationX(mBusStopLayout1.getWidth());
        } else if (i < busStopCount) {
            updateBusStopLayout(mBusStopLayout1, BusStopLayout.Type.GENERAL, busStops.get(i - 2).getName(), "");
            updateBusStopLayout(mBusStopLayout2, BusStopLayout.Type.GENERAL, busStops.get(i - 1).getName(), "");
            updateBusStopLayout(mBusStopLayout3, BusStopLayout.Type.PREVIOUS, busStops.get(i).getName(), "");
            mBusImageView.setTranslationX(2 * mBusStopLayout1.getWidth());
        }
    }

    private void updateBusStopLayout(BusStopLayout busStopLayout,
                                     BusStopLayout.Type busStopLayoutType, String busStopName, String busStopTime) {
        busStopLayout.setType(busStopLayoutType);
        busStopLayout.setBusStopName(busStopName);
        busStopLayout.setBusStopTime(busStopTime);
    }

    private void animateBusStopLayouts() {
        Animator anim = ObjectAnimator.ofFloat(mBusStopsLayout, View.TRANSLATION_X,
                -mBusStopLayout1.getWidth());
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBusStopsLayout.setTranslationX(0);
                updateBusStopLayouts();
            }
        });
        anim.start();
    }

    private void animateBus(int busStopCount) {
        int busStopLayoutWidth = mBusStopLayout1.getWidth();
        Animator anim = ObjectAnimator.ofFloat(mBusImageView, View.TRANSLATION_X,
                busStopCount * busStopLayoutWidth);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                updateBusStopLayouts();
            }
        });
        anim.start();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(PrefUtils.PREF_ACTIVE_ROUTE_INDEX)) {
            updateBusStopLayouts();
        } else if (key.equals(PrefUtils.PREF_PREVIOUS_BUS_STOP_INDEX)) {
        	
        	if (null != mVehicle) { 
        	
	            int busStopCount = mVehicle.getBusStopCount();
	            int i = PrefUtils.getPreviousBusStopIndex();
	            if (i < busStopCount - 2) {
	                animateBusStopLayouts();
	            } else {
	                animateBus(i < busStopCount - 1 ? 1 : 2);
	            }
        	}
        }
    }
}
