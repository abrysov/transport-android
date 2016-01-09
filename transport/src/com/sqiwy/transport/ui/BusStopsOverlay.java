/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.content.Context;
import android.graphics.Canvas;

import com.sqiwy.transport.R;
import com.sqiwy.transport.data.Point;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.safecanvas.ISafeCanvas;

import java.util.ArrayList;
import java.util.List;

public class BusStopsOverlay extends ItemizedIconOverlay<BusStopOverlayItem> {
	
    public BusStopsOverlay(Context context, List<Point> busStops, Point nextBusStop) {
        super(context, new ArrayList<BusStopOverlayItem>(), null);

        List<BusStopOverlayItem> items = new ArrayList<BusStopOverlayItem>(busStops.size());
        for (Point busStop : busStops) {
            boolean isNextBusStop = busStop.equals(nextBusStop);
            BusStopOverlayItem item = new BusStopOverlayItem(context, busStop, isNextBusStop);
            item.setMarker(context.getResources().getDrawable(
                    isNextBusStop ? R.drawable.ic_next_bus_stop : R.drawable.ic_bus_stop));
            item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
            items.add(item);
        }
        addItems(items);
    }

    @Override
    protected void onDrawItem(ISafeCanvas canvas, final BusStopOverlayItem item,
                              final android.graphics.Point curScreenCoords, final float aMapOrientation) {
        super.onDrawItem(canvas, item, curScreenCoords, aMapOrientation);

        if (isUsingSafeCanvas()) {
            item.drawBubble(canvas.getSafeCanvas(), curScreenCoords.x, curScreenCoords.y, aMapOrientation);
        } else {
            canvas.getUnsafeCanvas(new ISafeCanvas.UnsafeCanvasHandler() {
                @Override
                public void onUnsafeCanvas(Canvas canvas) {
                    item.drawBubble(canvas, curScreenCoords.x, curScreenCoords.y, aMapOrientation);
                }
            });
        }
    }
}
