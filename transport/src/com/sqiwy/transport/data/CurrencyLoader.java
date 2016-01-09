/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.content.Context;

import java.util.List;

public class CurrencyLoader extends BaseLoader<List<Currency>> {
    public CurrencyLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        getContext().getContentResolver().registerContentObserver(TransportProvider.Table.Currencies.URI, true, mObserver);
    }

    @Override
    public List<Currency> loadInBackground() {
        return TransportProviderHelper.queryCurrencies(getContext().getContentResolver());
    }
}
