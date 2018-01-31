package com.max77.exactfarmingtest.ui;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.max77.exactfarmingtest.R;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationUtil;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180131.
 */

public class MapHelper {
    private static final float ACCURACY_CIRCLE_STROKE_WIDTH_DP = 3.0f;

    private Context mContext;
    private GoogleMap mMap;

    private Marker mMyLocationMarker;
    private Circle mAccuracyCircle;

    private double mGoodAccuracy;
    private double mBadAccuracy;

    private boolean isFirstLocation;
    private LocationInfo mMyLastLocation;

    public MapHelper(Context context, GoogleMap map) {
        mContext = context;
        mMap = map;
    }

    public void setAccuracyRange(double goodAccuracy, double badAccuracy) {
        mGoodAccuracy = Math.min(goodAccuracy, badAccuracy);
        mBadAccuracy = Math.max(goodAccuracy, badAccuracy);
    }

    public void setMyLocation(LocationInfo locationInfo) {

        if (mMap == null) {
            return;
        }


        isFirstLocation = mMyLastLocation == null;
        mMyLastLocation = locationInfo;

        LatLng latLng = LocationUtil.locationInfoToLatLng(locationInfo);

        if (mMyLocationMarker == null) {
            mMyLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .zIndex(1)
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            UIUtil.getBitmapFromVector(mContext, R.drawable.ic_my_location)))
                    .anchor(0.5f, 0.5f));
        }

        mMyLocationMarker.setPosition(latLng);

        if (mAccuracyCircle == null) {
            mAccuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .zIndex(2)
                    .strokeWidth(UIUtil.dpToPx(mContext.getResources(), ACCURACY_CIRCLE_STROKE_WIDTH_DP)));
        }

        mAccuracyCircle.setCenter(latLng);
        mAccuracyCircle.setRadius(locationInfo.getAccuracy());
        mAccuracyCircle.setFillColor(getColorForAccuracy(64, 1.0f, locationInfo.getAccuracy()));
        mAccuracyCircle.setStrokeColor(getColorForAccuracy(128, 0.5f, locationInfo.getAccuracy()));
    }

    private static final int MAX_HUE = 150;

    private int getColorForAccuracy(int alpha, float saturation, double accuracy) {
        float hue = (float) (MAX_HUE * (1 - (accuracy - mGoodAccuracy) / (mBadAccuracy - mGoodAccuracy)));
        return Color.HSVToColor(alpha, new float[]{hue, saturation, 1});
    }

    public void zoomOnMyLocationIfNeeded() {
        if (!isFirstLocation || mMap == null || mMyLastLocation == null) {
            return;
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LocationUtil.locationInfoToLatLng(mMyLastLocation), 15));
    }
}
