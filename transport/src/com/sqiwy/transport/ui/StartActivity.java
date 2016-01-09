/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.util.PrefUtils;

public class StartActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If the device is not registered yet, start the configuration activity.
        // Otherwise, start the main activity.
        if (BuildConfig.DEBUG) {
    		Log.d("StartActivity", "onCreate");
    	}
        if (TextUtils.isEmpty(PrefUtils.getDeviceGuid())) {
            ConfigurationActivity.start(this);
        } else {
            MainActivity.start(this);
        }
        finish();
    }
}
