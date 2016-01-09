/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sqiwy.transport.R;
import com.sqiwy.transport.api.RegisterResponse;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.util.EventBus;
import com.sqiwy.transport.util.PrefUtils;
import com.squareup.otto.Subscribe;

public class ConfigurationActivity extends FragmentActivity {
    private Button mRegisterButton;

    public static void start(Context context) {
        Intent intent = new Intent(context, ConfigurationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        mRegisterButton = (Button) findViewById(R.id.btn_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransportApiHelper.register(PrefUtils.getDeviceNumber());
                ProgressDialogFragment.show(getFragmentManager());
            }
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.registerReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.unregisterReceiver(this);
    }

    @Subscribe
    public void onRegisterResponse(RegisterResponse response) {
        ProgressDialogFragment.dismiss(getFragmentManager());
        if (response.success) {
            Toast.makeText(this, R.string.toast_register_success, Toast.LENGTH_SHORT).show();
            PrefUtils.setDeviceGuid(response.guid);
            MainActivity.start(this);
            finish();
        } else {
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        mRegisterButton.setEnabled(!TextUtils.isEmpty(PrefUtils.getServerAddress().trim())
                && !TextUtils.isEmpty(PrefUtils.getDeviceNumber().trim()));
    }

    public static class ConfigurationFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            updatePreferenceSummary(null);
        }

        @SuppressWarnings("ConstantConditions")
        private void updatePreferenceSummary(String key) {
            if (key == null || key.equals(PrefUtils.PREF_SERVER_ADDRESS)) {
                Preference pref = findPreference(PrefUtils.PREF_SERVER_ADDRESS);
                pref.setSummary(PrefUtils.getServerAddress());
            }

            if (key == null || key.equals(PrefUtils.PREF_DEVICE_NUMBER)) {
                Preference pref = findPreference(PrefUtils.PREF_DEVICE_NUMBER);
                pref.setSummary(PrefUtils.getDeviceNumber());
            }
            
            if (key == null || key.equals(PrefUtils.PREF_PAUSE)) {
                Preference pref = findPreference(PrefUtils.PREF_PAUSE);
                pref.setSummary(String.valueOf(PrefUtils.getPause()));
            }
            
            if (key == null || key.equals(PrefUtils.PREF_SEND_INTERVAL)) {
                Preference pref = findPreference(PrefUtils.PREF_SEND_INTERVAL);
                pref.setSummary(String.valueOf(PrefUtils.getSendInterval()));
            }
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
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            updatePreferenceSummary(key);
            ConfigurationActivity activity = (ConfigurationActivity) getActivity();
            if (activity != null) {
                activity.updateUI();
            }
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {
        private static final String TAG = ProgressDialogFragment.class.getSimpleName();

        public static void show(FragmentManager fm) {
            ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
            dialogFragment.setCancelable(false);
            dialogFragment.show(fm, TAG);
        }

        public static void dismiss(FragmentManager fm) {
            DialogFragment dialogFragment = (DialogFragment) fm.findFragmentByTag(TAG);
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getString(R.string.message_wait));
            return dialog;
        }
    }
}
