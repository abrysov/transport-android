/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.advertisement.AdvertisementLoaderService;
import com.sqiwy.transport.advertisement.AdvertisementResource;
import com.sqiwy.transport.advertisement.AdvertisementsFragment;
import com.sqiwy.transport.controller.Event;
import com.sqiwy.transport.controller.Screen;
import com.sqiwy.transport.controller.ScreenController.OnScreenControllerListener;
import com.sqiwy.transport.controller.screen.*;
import com.sqiwy.transport.data.Vehicle;
import com.sqiwy.transport.data.VehicleLoader;
import com.sqiwy.transport.util.PrefUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Vehicle>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private ModelFragment mModelFragment;
    private MapFragment mMapFragment;
    private ProgressBar mBusStopsProgressBar;
    private Vehicle mVehicle;
    private Screen mCurrentScreen;
    private View mTitleBar;

    public static void start(Context context) {
    	if (BuildConfig.DEBUG) {
    		Log.d("MainActivity", "start");
    	}
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        AdvertisementLoaderService.start(context);
        NewsFragment.updateNews();
        HoroscopeFragment.updateHoroscope();
        MultiCurrencyFragment.updateCourses();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("fonts/Proxima Nova Regular.otf");
        setContentView(R.layout.activity_main);
        mBusStopsProgressBar = (ProgressBar) findViewById(R.id.pb_bus_stops);
        mBusStopsProgressBar.setVisibility(View.GONE);

        mTitleBar = (View) findViewById(R.id.fragment_action_bar);

        FragmentManager fm = getSupportFragmentManager();
        mMapFragment = (MapFragment) fm.findFragmentById(R.id.fragment_map);
        mModelFragment = (ModelFragment) fm.findFragmentByTag(ModelFragment.TAG);
        if (mModelFragment == null) {
            mModelFragment = new ModelFragment();
            fm.beginTransaction().add(mModelFragment, ModelFragment.TAG).commit();
        }
        mModelFragment.setGeofenceListener(mMapFragment);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
    	super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.registerOnPreferenceChangeListener(this);
        TransportApplication.getScreenController().setOnScreenControllerListener(new ScreenControllerListener());
        checkSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrefUtils.unregisterOnPreferenceChangeListener(this);
        TransportApplication.getScreenController().setOnScreenControllerListener(null);
    }

    private void checkSettings() {
        // Check if Google Play services are available.
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment.show(getSupportFragmentManager(), dialog);
            }
        } else {
            // Check Location settings.
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isGpsProviderEnabled = lm.getProvider(LocationManager.GPS_PROVIDER) != null
                    && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkProviderEnabled = lm.getProvider(LocationManager.NETWORK_PROVIDER) != null
                    && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                LocationSettingsDialogFragment.show(getSupportFragmentManager());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mModelFragment.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Vehicle> onCreateLoader(int id, Bundle args) {
        return new VehicleLoader(this);
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
            setupBusStopsProgress();
        }
    }

    private void setupBusStopsProgress() {
        mBusStopsProgressBar.setMax( mVehicle != null ? mVehicle.getBusStopCount() - 1 : 0);
        mBusStopsProgressBar.setProgress(0);
    }

    private void updateBusStopsProgress() {
        mBusStopsProgressBar.setProgress(PrefUtils.getPreviousBusStopIndex());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(PrefUtils.PREF_ACTIVE_ROUTE_INDEX)) {
            setupBusStopsProgress();
        } else if (key.equals(PrefUtils.PREF_PREVIOUS_BUS_STOP_INDEX)) {
            updateBusStopsProgress();
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public static void show(FragmentManager fm, Dialog dialog) {
            ErrorDialogFragment fragment = new ErrorDialogFragment();
            fragment.mDialog = dialog;
            fragment.show(fm, (String) null);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public static class LocationSettingsDialogFragment extends DialogFragment {
        public static void show(FragmentManager fm) {
            LocationSettingsDialogFragment fragment = new LocationSettingsDialogFragment();
            fragment.setCancelable(false);
            fragment.show(fm, null);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            return builder.setTitle(R.string.title_location_settings)
                    .setMessage(R.string.message_location_settings)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(R.string.btn_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.btn_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    })
                    .create();
        }
    }

    private class ScreenControllerListener implements OnScreenControllerListener {
        @Override
        public void onNextScreen(Screen screen, Event event) {
            if (screen == mCurrentScreen) {
                return;
            }

            mCurrentScreen = screen;
            //mActionBar.setVisibility(View.VISIBLE);
            if (screen instanceof MapScreen) {
            	mTitleBar.animate().translationY(0).setDuration(3000).withLayer();
            } else {
            	mTitleBar.animate().translationY(0).setDuration(0).withLayer();
            }
            mTitleBar.setVisibility(View.VISIBLE);
            //mBusStopsProgressBar.animate().alpha(1).setDuration(1000).withLayer();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            if (screen instanceof MapScreen) {

                //mActionBar.animate().translationY(-300).setDuration(2000).withLayer();
                //mBusStopsProgressBar.animate().alpha(0).setDuration(1000).withLayer();
                hideFragment(fm.findFragmentById(R.id.fragment_ads), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_news), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_horoscopes), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_single_course), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_multi_course), transaction);
                showFragment(mMapFragment, transaction, 0);

            } else if (screen instanceof NewsScreen) {

                hideFragment(mMapFragment, transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_ads), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_horoscopes), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_single_course), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_multi_course), transaction);
                showFragment(new NewsFragment(), transaction, R.id.fragment_news);

            } else if (screen instanceof HoroscopeScreen) {

                hideFragment(mMapFragment, transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_ads), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_news), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_single_course), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_multi_course), transaction);
                showFragment(new HoroscopeFragment(), transaction, R.id.fragment_horoscopes);

            } else if (screen instanceof SingleCourseScreen) {

                hideFragment(mMapFragment, transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_ads), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_news), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_single_course), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_multi_course), transaction);
                showFragment(new SingleCourseFragment(), transaction, R.id.fragment_horoscopes);

            } else if (screen instanceof MultiCurrencyScreen) {

                hideFragment(mMapFragment, transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_ads), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_news), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_single_course), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_horoscopes), transaction);
                showFragment(new MultiCurrencyFragment(), transaction, R.id.fragment_multi_course);

            } else if (screen instanceof AdScreen) {
//
                mTitleBar.animate().translationY(-300).setDuration(3000).withLayer();
                mTitleBar.setVisibility(View.GONE);
                //mBusStopsProgressBar.animate().alpha(0).setDuration(1000).withLayer();
                hideFragment(mMapFragment, transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_news), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_horoscopes), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_single_course), transaction);
                hideFragment(fm.findFragmentById(R.id.fragment_multi_course), transaction);
                
                ArrayList<AdvertisementResource> ads = ((AdScreen) screen).getAds(event);
                if (BuildConfig.DEBUG) {
                	Log.d(this.getClass().getName(), "showing ads: " + ads.size());
                }
                showFragment(AdvertisementsFragment.newInstance(ads),
                        transaction, R.id.fragment_ads);

            } else {
                throw new IllegalArgumentException("Screen: " + screen + " is unknown.");
            }
            transaction.commit();
        }

        private void showFragment(Fragment fragment, FragmentTransaction transaction, int id) {
            if (fragment == mMapFragment) {
                transaction.show(fragment);
            } else {
                transaction.add(id, fragment, null);
            }
        }

        private void hideFragment(Fragment fragment, FragmentTransaction transaction) {
            if (fragment == mMapFragment) {
                transaction.hide(fragment);
            } else if (fragment != null) {
                transaction.remove(fragment);
            }
        }
    }
}
