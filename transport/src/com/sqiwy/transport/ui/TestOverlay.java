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

public class TestOverlay extends ItemizedIconOverlay<TestOverlayItem> {
	
    public TestOverlay(Context context, List<Point> points) {
        super(context, new ArrayList<TestOverlayItem>(), null);

        List<TestOverlayItem> items = new ArrayList<TestOverlayItem>(points.size());
        for (Point point : points) {
        	TestOverlayItem item = new TestOverlayItem(context, point);
            item.setMarker(context.getResources().getDrawable(R.drawable.ic_bus_stop));
            item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
            items.add(item);
        }
        addItems(items);
    }

    @Override
    protected void onDrawItem(ISafeCanvas canvas, final TestOverlayItem item,
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
