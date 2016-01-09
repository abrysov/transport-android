/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.sqiwy.transport.R;
import com.sqiwy.transport.data.Point;
import com.sqiwy.transport.util.UIUtils;

import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.safecanvas.SafeTranslatedCanvas;

public class BusStopOverlayItem extends OverlayItem {
    private static final int BUBBLE_MARGIN_DP = 20;
    private static final int BUBBLE_TEXT_SIZE_SP = 65;

    private static final Rect sTempBounds = new Rect();
    private static Typeface sProximaRegularTypeface;
    private static Typeface sProximaSemiboldTypeface;

    private final Context mContext;
    private final String mBusStopName;
    private final Drawable mBubbleBackground;
    private final Rect mBubbleBounds = new Rect();
    private final Rect mBubblePadding = new Rect();
    private final TextPaint mBubbleTextPaint;

    public BusStopOverlayItem(Context context, Point busStop, boolean isNextBusStop) {
        super(null, null, busStop.getLocation());

        if (sProximaRegularTypeface == null || sProximaSemiboldTypeface == null) {
            AssetManager am = context.getAssets();
            sProximaRegularTypeface = Typeface.createFromAsset(am, "fonts/Proxima Nova Condensed Regular.otf");
            sProximaSemiboldTypeface = Typeface.createFromAsset(am, "fonts/Proxima Nova Condensed Semibold.otf");
        }

        mContext = context;
        mBusStopName = busStop.getName();
        Resources res = context.getResources();
        mBubbleBackground = res.getDrawable(isNextBusStop
                ? R.drawable.next_bus_stop_bubble_background : R.drawable.bus_stop_bubble_background);
        mBubbleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBubbleTextPaint.setTextSize(UIUtils.spToPixels(context, BUBBLE_TEXT_SIZE_SP));
        mBubbleTextPaint.setColor(isNextBusStop ? Color.WHITE : res.getColor(R.color.bus_stop_text_color));
        mBubbleTextPaint.setTypeface(isNextBusStop ? sProximaSemiboldTypeface : sProximaRegularTypeface);
    }

    @Override
    public void setMarker(Drawable marker) {
        super.setMarker(marker);

        // Calculate the bubble bounds to place it to the right of the marker.
        mBubbleBackground.getPadding(mBubblePadding);
        mBubbleTextPaint.getTextBounds(mBusStopName, 0, mBusStopName.length(), mBubbleBounds);
        mBubbleBounds.left = mBubbleBounds.bottom = 0;
        mBubbleBounds.right = mBubbleBounds.width() + mBubblePadding.left + mBubblePadding.right;
        mBubbleBounds.top -= mBubblePadding.top + mBubblePadding.bottom;
        mBubbleBounds.offsetTo(marker.getIntrinsicWidth() / 2
                + UIUtils.dpToPixels(mContext, BUBBLE_MARGIN_DP), mBubbleBounds.top);
    }

    public void drawBubble(Canvas canvas, int x, int y, float aMapOrientation) {
        canvas.save();
        canvas.rotate(-aMapOrientation, x, y);

        // Draw the bubble background. Note that we use the workaround with SafeTranslatedCanvas
        // to correctly draw the nine-patch drawable due to Canvas#drawPatch() is hidden
        // and not defined in SafeTranslatedCanvas.
        SafeTranslatedCanvas safeCanvas = (SafeTranslatedCanvas) canvas;
        sTempBounds.set(mBubbleBounds);
        sTempBounds.offset(x + safeCanvas.xOffset, y + safeCanvas.yOffset);
        mBubbleBackground.setBounds(sTempBounds);
        mBubbleBackground.draw(safeCanvas.getWrappedCanvas());

        // Draw the bubble text.
        safeCanvas.getWrappedCanvas().drawText(mBusStopName, sTempBounds.left + mBubblePadding.left,
                sTempBounds.bottom - mBubblePadding.bottom, mBubbleTextPaint);

        canvas.restore();
    }
}
