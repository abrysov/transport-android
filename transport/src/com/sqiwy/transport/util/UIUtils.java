/**
 * Created by abrysov
 */
package com.sqiwy.transport.util;

import android.content.Context;
import android.util.TypedValue;

public final class UIUtils {
    private UIUtils() {
    }

    public static int dpToPixels(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static int spToPixels(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                context.getResources().getDisplayMetrics());
    }
}
