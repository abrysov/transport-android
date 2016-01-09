/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.content.Context;

import java.util.List;

public class NewsLoader extends BaseLoader<List<News>> {
	public NewsLoader(Context context) {
		super(context);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
        getContext().getContentResolver().registerContentObserver(
                TransportProvider.Table.News.URI, true, mObserver);
	}
	
	@Override
	public List<News> loadInBackground() {
        return TransportProviderHelper.queryNews(getContext().getContentResolver());
	}
}
