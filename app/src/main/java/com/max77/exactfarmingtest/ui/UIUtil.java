package com.max77.exactfarmingtest.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
}
