/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.content.Context;

import java.util.List;

public class HoroscopeLoader extends BaseLoader<List<Horoscope>> {
    public HoroscopeLoader (Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        getContext().getContentResolver().registerContentObserver(TransportProvider.Table.Horoscopes.URI, true, mObserver);
    }

    @Override
    public List<Horoscope> loadInBackground() {
        return TransportProviderHelper.queryHoroscopes(getContext().getContentResolver());
    }
}
