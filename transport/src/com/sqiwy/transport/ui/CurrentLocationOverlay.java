/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;

import com.sqiwy.transport.R;
import com.sqiwy.transport.location.CurrentLocationProvider;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;

public class CurrentLocationOverlay extends MyLocationNewOverlay {
    public CurrentLocationOverlay(Context context, MapView mapView) {
        this(context, mapView, new CustomResourceProxyImpl(context));
    }
    
    private CurrentLocationOverlay(Context context, MapView mapView,
                                   CustomResourceProxyImpl resourceProxy) {
        super(new CurrentLocationProvider(context), mapView, resourceProxy);
        Point hotspot = resourceProxy.getBusHotspot();
        setPersonHotspot(hotspot.x, hotspot.y);
    }

    @Override
    protected void drawMyLocation(ISafeCanvas canvas, MapView mapView, Location lastFix) {
        // Remove bearing from the location to not draw the arrow.
        lastFix.removeBearing();
        super.drawMyLocation(canvas, mapView, lastFix);
    }

    @Override
    protected Rect getMyLocationDrawingBounds(int zoomLevel, Location lastFix, Rect reuse) {
        // Remove bearing from the location as we don't draw the arrow.
        lastFix.removeBearing();
        return super.getMyLocationDrawingBounds(zoomLevel, lastFix, reuse);
    }

    private static class CustomResourceProxyImpl extends DefaultResourceProxyImpl {
        private final Bitmap mBusBitmap;

        @SuppressWarnings("ConstantConditions")
        public CustomResourceProxyImpl(Context context) {
            super(context);
            mBusBitmap = ((BitmapDrawable) context.getResources().getDrawable(
                    R.drawable.ic_big_bus)).getBitmap();
        }

        @Override
        public Bitmap getBitmap(bitmap pResId) {
            return pResId == bitmap.person ? mBusBitmap : super.getBitmap(pResId);
        }

        public Point getBusHotspot() {
            return new Point(mBusBitmap.getWidth() / 2, mBusBitmap.getHeight() / 2);
        }
    }
}
