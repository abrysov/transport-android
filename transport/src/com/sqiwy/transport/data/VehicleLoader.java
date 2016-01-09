/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.content.Context;
import android.util.Log;

public class VehicleLoader extends BaseLoader<Vehicle> {
    private static final String TAG = Vehicle.class.getSimpleName();

	public VehicleLoader(Context context) {
		super(context);
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
        getContext().getContentResolver().registerContentObserver(
                TransportProvider.Table.Vehicle.URI, true, mObserver);
	}
	
	@Override
	public Vehicle loadInBackground() {
        return TransportProviderHelper.queryVehicle(getContext().getContentResolver());
	}
}
