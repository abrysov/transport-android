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
import android.widget.TextView;

import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.api.GetNewsResponse;
import com.sqiwy.transport.api.TransportApiHelper;
import com.sqiwy.transport.data.News;
import com.sqiwy.transport.data.NewsLoader;
import com.sqiwy.transport.data.TransportProviderHelper;
import com.sqiwy.transport.util.PrefUtils;

import java.util.List;

public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<News>> {
    private static final int NEWS_TIMER_INTERVAL = 10 * 1000;

    private final Handler mHandler = new Handler();
    private final NewsTimerRunnable mNewsTimerRunnable = new NewsTimerRunnable();
    private TextView mNewsTextView;
    private List<News> mNews;

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mNewsTextView = (TextView) view.findViewById(R.id.tv_news);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction().add(
                    R.id.fragment_bus_stops_bar, new BusStopsBarFragment()).commit();
        }

        getLoaderManager().initLoader(0, null, this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startNewsTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNewsTimer();
    }

    private void startNewsTimer() {
        mHandler.removeCallbacks(mNewsTimerRunnable);
        if (mNews != null && !mNews.isEmpty()) {
            mHandler.post(mNewsTimerRunnable);
        }
    }

    private void stopNewsTimer() {
        mHandler.removeCallbacks(mNewsTimerRunnable);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        return new NewsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        changeNews(news);
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        changeNews(null);
    }

    private void changeNews(List<News> news) {
        if (mNews != news) {
            mNews = news;
            startNewsTimer();
        }
    }

    public static void updateNews() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                GetNewsResponse response = TransportApiHelper.getNews();
                if (response != null && response.success) {
                    PrefUtils.setNextNewsIndex(0);

                    ContentResolver resolver = TransportApplication.getAppContext().getContentResolver();
                    TransportProviderHelper.deleteAllNews(resolver);
                    if (response.news != null) {
                        TransportProviderHelper.insertNews(resolver, response.news);
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class NewsTimerRunnable implements Runnable {
        @Override
        public void run() {
            if (mNews != null && !mNews.isEmpty()) {
                int nextNewsIndex = PrefUtils.getNextNewsIndex();
                if (nextNewsIndex < mNews.size()) {
                    setNewsTitle(mNews.get(nextNewsIndex).getTitle());
                    PrefUtils.setNextNewsIndex(nextNewsIndex + 1);
                } else {
                    updateNews();
                }
            }

            mHandler.postDelayed(this, NEWS_TIMER_INTERVAL);
        }

        private void setNewsTitle(final String title) {
            Animator fadeOutAnim = ObjectAnimator.ofFloat(mNewsTextView, View.ALPHA, 0);
            Animator fadeInAnim = ObjectAnimator.ofFloat(mNewsTextView, View.ALPHA, 1);
            fadeOutAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mNewsTextView.setText(title);
                }
            });

            AnimatorSet animSet = new AnimatorSet();
            animSet.playSequentially(fadeOutAnim, fadeInAnim);
            animSet.start();
        }
    }
}
