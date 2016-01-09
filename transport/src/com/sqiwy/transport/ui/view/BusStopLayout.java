/**
 * Created by abrysov
 */
package com.sqiwy.transport.ui.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sqiwy.transport.R;

public class BusStopLayout extends RelativeLayout {
    public enum Type {
        GENERAL,
        PREVIOUS,
        NEXT
    }

    private static Typeface sProximaRegularTypeface;
    private static Typeface sProximaBoldTypeface;

    private final ImageView mFirstRoadPartImageView;
    private final ImageView mSecondRoadPartImageView;
    private final ImageView mBusStopImageView;
    private final TextView mBusStopNameTextView;
    private final TextView mBusStopTimeTextView;

    public BusStopLayout(Context context) {
        this(context, null);
    }

    public BusStopLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BusStopLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (sProximaRegularTypeface == null || sProximaBoldTypeface == null) {
            AssetManager am = context.getAssets();
            sProximaRegularTypeface = Typeface.createFromAsset(am, "fonts/Proxima Nova Condensed Regular.otf");
            sProximaBoldTypeface = Typeface.createFromAsset(am, "fonts/Proxima Nova Condensed Bold.otf");
        }

        inflate(context, R.layout.layout_bus_stop, this);
        mFirstRoadPartImageView = (ImageView) findViewById(R.id.iv_first_road_part);
        mSecondRoadPartImageView = (ImageView) findViewById(R.id.iv_second_road_part);
        mBusStopImageView = (ImageView) findViewById(R.id.iv_bus_stop);
        mBusStopNameTextView = (TextView) findViewById(R.id.tv_bus_stop_name);
        mBusStopTimeTextView = (TextView) findViewById(R.id.tv_bus_stop_time);
    }

    public void setBusStopName(String name) {
        mBusStopNameTextView.setText(name);
    }

    public void setBusStopTime(String name) {
        mBusStopTimeTextView.setText(name);
    }

    @SuppressWarnings("ConstantConditions")
    public void setType(Type type) {
        Resources res = getResources();
        switch (type) {
            case GENERAL:
                mFirstRoadPartImageView.setBackgroundColor(res.getColor(R.color.road_color));
                mSecondRoadPartImageView.setBackgroundColor(res.getColor(R.color.road_color));
                mBusStopImageView.setImageResource(R.drawable.ic_bus_stop);

                mBusStopNameTextView.setSingleLine(false);
                mBusStopNameTextView.setMaxLines(2);
                mBusStopNameTextView.setTypeface(sProximaRegularTypeface);
                mBusStopNameTextView.setTextColor(res.getColor(R.color.bus_stop_text_color));

                mBusStopTimeTextView.setSingleLine(false);
                mBusStopTimeTextView.setMaxLines(1);
                mBusStopTimeTextView.setTypeface(sProximaRegularTypeface);
                mBusStopTimeTextView.setTextColor(res.getColor(R.color.bus_stop_text_color));
                break;

            case PREVIOUS:
                mFirstRoadPartImageView.setBackgroundColor(res.getColor(R.color.road_color));
                mSecondRoadPartImageView.setBackgroundColor(res.getColor(R.color.active_road_color));
                mBusStopImageView.setImageResource(R.drawable.ic_bus_stop);

                mBusStopNameTextView.setSingleLine(true);
                mBusStopNameTextView.setTypeface(sProximaRegularTypeface);
                mBusStopNameTextView.setTextColor(res.getColor(R.color.bus_stop_text_color));

                mBusStopTimeTextView.setSingleLine(true);
                mBusStopTimeTextView.setTypeface(sProximaRegularTypeface);
                mBusStopTimeTextView.setTextColor(res.getColor(R.color.bus_stop_text_color));

                break;

            case NEXT:
                mFirstRoadPartImageView.setBackgroundColor(res.getColor(R.color.active_road_color));
                mSecondRoadPartImageView.setBackgroundColor(res.getColor(R.color.road_color));
                mBusStopImageView.setImageResource(R.drawable.ic_next_bus_stop);

                mBusStopNameTextView.setSingleLine(false);
                mBusStopNameTextView.setMaxLines(2);
                mBusStopNameTextView.setTypeface(sProximaBoldTypeface);
                mBusStopNameTextView.setTextColor(res.getColor(R.color.next_bus_stop_text_color));

                mBusStopTimeTextView.setSingleLine(false);
                mBusStopTimeTextView.setMaxLines(1);
                mBusStopTimeTextView.setTypeface(sProximaBoldTypeface);
                mBusStopTimeTextView.setTextColor(res.getColor(R.color.next_bus_stop_text_color));

                break;
        }
    }
}
