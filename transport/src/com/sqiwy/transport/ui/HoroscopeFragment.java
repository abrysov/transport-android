/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.api.GetHoroscopeResponse;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.data.Horoscope;
import com.sqiwy.transport.data.HoroscopeLoader;
import com.sqiwy.transport.data.TransportProviderHelper;
import com.sqiwy.transport.ui.view.AutoResizeTextView;
import com.sqiwy.transport.util.PrefUtils;

import java.util.List;

public class HoroscopeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Horoscope>> {

    private static final int HOROSCOPE_TIMER_INTERVAL = 5 * 1000;
    private static final String TAG = HoroscopeFragment.class.getName();

    private final Handler mHandler = new Handler();
    private final HoroscopeTimerRunnable mHoroscopesTimerRunnable = new HoroscopeTimerRunnable();
    private AutoResizeTextView mHoroscopesTextView;
    private TextView mNameSign;
    private ImageView mHoroscopePic;
    private List<Horoscope> mHoroscopes;
    private Sign mSign;

    public static enum Sign{
        VODOLEY,
        RIBI,
        OVEN,
        TELEC,
        BLIZNEC,
        RAK,
        LEV,
        DEVA,
        VESI,
        SCORPION,
        STRELEC,
        KOZEROG
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_horoscope, container, false);
        mHoroscopesTextView = (AutoResizeTextView) view.findViewById(R.id.horoscope_tv_text);
        mNameSign = (TextView) view.findViewById(R.id.horoscope_tv_sign_name);

        mHoroscopePic = (ImageView) view.findViewById(R.id.horoscope_iv_pic);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_bus_stops_bar, new BusStopsBarFragment()).commit();
        }

        getLoaderManager().initLoader(0, null, this);

        //updateHoroscope();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startHoroscopesTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNewsTimer();
    }

    private void startHoroscopesTimer() {
        mHandler.removeCallbacks(mHoroscopesTimerRunnable);
        if (mHoroscopes != null && !mHoroscopes.isEmpty()) {
            mHandler.post(mHoroscopesTimerRunnable);
        }
    }

    private void stopNewsTimer() {
        mHandler.removeCallbacks(mHoroscopesTimerRunnable);
    }

    @Override
    public Loader<List<Horoscope>> onCreateLoader(int id, Bundle args) {
        return new HoroscopeLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Horoscope>> loader, List<Horoscope> horoscopes) {
        changeHoroscopes(horoscopes);
    }

    @Override
    public void onLoaderReset(Loader<List<Horoscope>> loader) {
        changeHoroscopes(null);
    }

    private void changeHoroscopes(List<Horoscope> horoscopes) {
        if (mHoroscopes != horoscopes) {
            mHoroscopes = horoscopes;
            startHoroscopesTimer();
        }
    }

    public static void updateHoroscope() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GetHoroscopeResponse response = TransportApiHelper.getHoroscope();

                if (response != null && response.success) {
                    PrefUtils.setNextHoroscopesIndex(0);

                    ContentResolver resolver = TransportApplication.getAppContext().getContentResolver();
                    TransportProviderHelper.deleteAllHoroscopes(resolver);
                    if (response.horoscope != null) {
                        TransportProviderHelper.insertHoroscope(resolver, response.horoscope);
                    }

                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class HoroscopeTimerRunnable implements Runnable {
        @Override
        public void run() {
            if (mHoroscopes != null && !mHoroscopes.isEmpty()) {
                int nextHoroscopesIndex = PrefUtils.getNextHoroscopeIndex();
                if (nextHoroscopesIndex < mHoroscopes.size()) {
                    setHoroscopesDescription(mHoroscopes.get(nextHoroscopesIndex));

                    PrefUtils.setNextHoroscopesIndex(nextHoroscopesIndex + 1);
                } else {
                    updateHoroscope();
                }
            }

            mHandler.postDelayed(this, HOROSCOPE_TIMER_INTERVAL);
        }

        private void setHoroscopesDescription(final Horoscope horpscope) {
            Animator fadeOutAnim = ObjectAnimator.ofFloat(mHoroscopesTextView, View.ALPHA, 0);
            Animator fadeInAnim = ObjectAnimator.ofFloat(mHoroscopesTextView, View.ALPHA, 1);
            fadeOutAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    mHoroscopesTextView.setText(horpscope.getDescription());

                    if (horpscope.getCode().equals("aries")) {
                        mSign = Sign.OVEN;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_03_oven_aries);
                    } else if (horpscope.getCode().equals("taurus")) {
                        mSign = Sign.TELEC;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_02_telec_taurus);
                    } else if (horpscope.getCode().equals("gemini")) {
                        mSign = Sign.BLIZNEC;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_04_bliznec_gemini);
                    } else if (horpscope.getCode().equals("cancer")) {
                        mSign = Sign.RAK;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_05_rak_cancer);
                    } else if (horpscope.getCode().equals("leo")) {
                        mSign = Sign.LEV;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_06_lev_leo);
                    } else if (horpscope.getCode().equals("virgo")) {
                        mSign = Sign.DEVA;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_07_deva_virgo);
                    } else if (horpscope.getCode().equals("aquarius")) {
                        mSign = Sign.VODOLEY;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_00_vodoley_aquarius);
                    } else if (horpscope.getCode().equals("capricorn")) {
                        mSign = Sign.KOZEROG;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_11_kozerog_capricorn);
                    } else if (horpscope.getCode().equals("libra")) {
                        mSign = Sign.VESI;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_08_vesi_libra);
                    } else if (horpscope.getCode().equals("pisces")) {
                        mSign = Sign.RIBI;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_01_ribi_piscis);
                    } else if (horpscope.getCode().equals("scorpio")) {
                        mSign = Sign.SCORPION;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_09_scorpion_scorpio);
                    } else if (horpscope.getCode().equals("sagittarius")) {
                        mSign = Sign.STRELEC;
                        mHoroscopePic.setBackgroundResource(R.drawable.ic_10_strelec_sagittarius);
                    } else {
                    }
                    mNameSign.setText(horpscope.getTitle());
                    mNameSign.invalidate();
                }
            });

            AnimatorSet animSet = new AnimatorSet();
            animSet.playSequentially(fadeOutAnim, fadeInAnim);
            animSet.start();

        }

    }

}
