package com.max77.exactfarmingtest.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.TypedValue;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180131.
 */

public final class UIUtil {
    public static float dpToPx(Resources res, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static Bitmap getBitmapFromVector(Context context, @DrawableRes int drawableResId) {
        VectorDrawableCompat vectorDrawable =
                VectorDrawableCompat.create(context.getResources(), drawableResId, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static final int GOOD_HUE_1 = 75;
    private static final int GOOD_HUE_2 = 140;
    private static final int BAD_HUE_1 = 0;
    private static final int BAD_HUE_2 = 40;

    public static int getColorForAccuracy(double accuracy, double goodAccuracy, int alpha, float saturation) {
        float hue = (float) (accuracy <= goodAccuracy ?
                GOOD_HUE_1 + (1 - accuracy / goodAccuracy) * (GOOD_HUE_2 - GOOD_HUE_1) :
                BAD_HUE_1 + Math.min(1, (1 - (accuracy - goodAccuracy) / (goodAccuracy * 4))) * (BAD_HUE_2 - BAD_HUE_1));

        return Color.HSVToColor(alpha, new float[]{hue, saturation, 1});
    }
}
