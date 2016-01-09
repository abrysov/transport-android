package com.sqiwy.transport.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sqiwy.transport.R;
import com.sqiwy.transport.api.GetCurrencyResponse;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.data.Currency;
import com.sqiwy.transport.data.CurrencyLoader;

import java.util.List;

/**
* Created by abrysov
*/
public class SingleCourseFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Currency>>{
    private static final int COURSE_TIMER_INTERVAL = 10 * 1000;

    private final Handler mHandler = new Handler();
    private final CourseTimerRunnable mCourseTimerRunnable = new CourseTimerRunnable();
    private TextView mCourseSell;
    private TextView mCourseBuy;
    private List<Currency> mCurrencies;

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_single_course, container, false);

        mCourseBuy = (TextView) view.findViewById(R.id.single_course_tv_buy);
        mCourseBuy.setText(mCourseBuy.getText() + " 34.90");
        mCourseSell = (TextView) view.findViewById(R.id.single_course_tv_sell);
        mCourseSell.setText(mCourseSell.getText() + " 35.70");

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_bus_stops_bar, new BusStopsBarFragment()).commit();
        }

        getLoaderManager().initLoader(0, null, this);
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
        mHandler.removeCallbacks(mCourseTimerRunnable);
        if (mCurrencies != null) {
            mHandler.post(mCourseTimerRunnable);
        }
    }

    private void stopNewsTimer() {
        mHandler.removeCallbacks(mCourseTimerRunnable);
    }

    @Override
    public Loader<List<Currency>> onCreateLoader(int id, Bundle args) {
        return new CurrencyLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Currency>> loader, List<Currency> currencies) {
        changeCourse(currencies);
    }

    @Override
    public void onLoaderReset(Loader<List<Currency>> loader) {
        changeCourse(null);
    }

    private void changeCourse(List<Currency> currency) {
        if (mCurrencies != currency) {
            mCurrencies = currency;
            startHoroscopesTimer();
        }
    }

    public static void updateCourse() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GetCurrencyResponse response = TransportApiHelper.getCourse();

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CourseTimerRunnable implements Runnable {
        @Override
        public void run() {
            if (mCurrencies != null && !mCurrencies.isEmpty()) { //

                    updateCourse();

            }

            mHandler.postDelayed(this, COURSE_TIMER_INTERVAL);
        }

//        private void setCourseTitle(final String title) {
//            Animator fadeOutAnim = ObjectAnimator.ofFloat(mCourseTextView, View.ALPHA, 0);
//            Animator fadeInAnim = ObjectAnimator.ofFloat(mCourseTextView, View.ALPHA, 1);
//            fadeOutAnim.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mCourseTextView.setText(title);
//                }
//            });
//
//            AnimatorSet animSet = new AnimatorSet();
//            animSet.playSequentially(fadeOutAnim, fadeInAnim);
//            animSet.start();
//        }
    }

}
