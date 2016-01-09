/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

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
import android.widget.TextView;
import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.api.GetCurrencyResponse;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.data.*;
import com.sqiwy.transport.util.PrefUtils;

import java.util.List;

public class MultiCurrencyFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Currency>>{

    private static final int CURRENCY_TIMER_INTERVAL = 10 * 60 * 1000;
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    private final Handler mHandler = new Handler();
    private final CurrencyTimerRunnable mCurrencyTimerRunnable = new CurrencyTimerRunnable();

    private TextView mBuyUSDTextView;
    private TextView mSellUSDTextView;
    private TextView mBuyEUROTextView;
    private TextView mSellEUROTextView;

    private List<Currency> mCurrencies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_multi_course, container, false);

        mBuyUSDTextView = (TextView) view.findViewById(R.id.multi_currency_buy_baks);
        mSellUSDTextView = (TextView) view.findViewById(R.id.multi_currency_sell_baks);
        mBuyEUROTextView = (TextView) view.findViewById(R.id.multi_currency_buy_euro);
        mSellEUROTextView = (TextView) view.findViewById(R.id.multi_currency_sell_euro);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_bus_stops_bar, new BusStopsBarFragment()).commit();
        }

        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startCourseTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNewsTimer();
    }

    private void startCourseTimer() {
        mHandler.removeCallbacks(mCurrencyTimerRunnable);
        if (mCurrencies != null && !mCurrencies.isEmpty()) {
            mHandler.post(mCurrencyTimerRunnable);
        }
    }

    private void stopNewsTimer() {
        mHandler.removeCallbacks(mCurrencyTimerRunnable);
    }

    @Override
    public Loader<List<Currency>> onCreateLoader(int id, Bundle args) {
        return new CurrencyLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Currency>> loader, List<Currency> currency) {
        changeСurrencies(currency);
    }

    @Override
    public void onLoaderReset(Loader<List<Currency>> loader) {
        changeСurrencies(null);
    }

    private void changeСurrencies(List<Currency> currency) {
        if (mCurrencies != currency) {
            mCurrencies = currency;
            startCourseTimer();
        }
    }

    public static void updateCourses() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GetCurrencyResponse response = TransportApiHelper.getCourse();

                if (response != null && response.success) { //
                    PrefUtils.setNextCurrencyIndex(0);

                    ContentResolver resolver = TransportApplication.getAppContext().getContentResolver();
                    TransportProviderHelper.deleteAllCurrencies(resolver);

                    if (response.currency != null) {
                        TransportProviderHelper.insertCurrencies(resolver, response.currency);
                    }
                }

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class CurrencyTimerRunnable implements Runnable {
        @Override
        public void run() {
            if (mCurrencies != null && !mCurrencies.isEmpty()) {

                for(Currency c : mCurrencies){
                    if (c.getCode().equalsIgnoreCase(USD)){
                        setCourseUSD(c.getSell(),c.getBuy());
                    }else if(c.getCode().equalsIgnoreCase(EUR)){
                        setCourseEURO(c.getSell(),c.getBuy());
                    }
                }

            }else{
                updateCourses();
            }

            mHandler.postDelayed(this, CURRENCY_TIMER_INTERVAL);
        }

        private void setCourseUSD(final String buy, final String sell) {

            mBuyUSDTextView.setText(buy);
            mSellUSDTextView.setText(sell);
        }

        private void setCourseEURO(final String buy, final String sell) {
            mBuyEUROTextView.setText(buy);
            mSellEUROTextView.setText(sell);
        }

    }


}
