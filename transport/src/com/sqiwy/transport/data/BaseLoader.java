/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

/**
 * Base loader to load data asynchronously.
 *
 * @param <T> result type.
 */
public abstract class BaseLoader<T> extends AsyncTaskLoader<T> {
	protected final Loader<T>.ForceLoadContentObserver mObserver = new ForceLoadContentObserver();
	private T mResult;
	
	public BaseLoader(Context context) {
		super(context);
	}

	@Override
	public void deliverResult(T data) {
		if (isReset()) {
			mResult = null;
		} else {
            mResult = data;
            if (isStarted()) {
                super.deliverResult(data);
            }
        }
	}
	
	@Override
	protected void onStartLoading() {
		if (mResult != null) {
			deliverResult(mResult);
		}

		if (takeContentChanged() || mResult == null) {
			forceLoad();
		}
	}
	
	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		cancelLoad();
		getContext().getContentResolver().unregisterContentObserver(mObserver);
		mResult = null;
	}
}
