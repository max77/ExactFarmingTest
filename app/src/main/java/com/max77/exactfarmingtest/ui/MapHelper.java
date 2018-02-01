package com.max77.exactfarmingtest.ui;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.max77.exactfarmingtest.R;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationUtil;

import java.util.List;

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
    private Polyline mPathPolyline;
    private Polygon mAreaPolygon;

    private double mGoodAccuracy;

    public MapHelper(Context context, GoogleMap map, double goodAccuracy) {
        mContext = context;
        mMap = map;
        mGoodAccuracy = goodAccuracy;
    }

    public void showMyLocation(LocationInfo locationInfo) {

        if (mMap == null) {
            return;
        }

        LatLng latLng = LocationUtil.locationInfoToLatLng(locationInfo);

        if (mMyLocationMarker == null) {
            mMyLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .zIndex(1)
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            UIUtil.getBitmapFromVector(mContext, R.drawable.ic_my_location)))
                    .anchor(0.5f, 0.5f));
        } else {
            mMyLocationMarker.setPosition(latLng);
        }

        if (mAccuracyCircle == null) {
            mAccuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .zIndex(2)
                    .strokeWidth(UIUtil.dpToPx(mContext.getResources(), ACCURACY_CIRCLE_STROKE_WIDTH_DP)));
        } else {
            mAccuracyCircle.setCenter(latLng);
        }

        mAccuracyCircle.setRadius(locationInfo.getAccuracy());
        mAccuracyCircle.setFillColor(UIUtil.getColorForAccuracy(locationInfo.getAccuracy(), mGoodAccuracy, 64,
                1.0f));
        mAccuracyCircle.setStrokeColor(UIUtil.getColorForAccuracy(locationInfo.getAccuracy(), mGoodAccuracy, 128,
                0.5f));
    }

    public void centerOnLocation(LocationInfo location, float zoom) {
        if (mMap == null) {
            return;
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LocationUtil.locationInfoToLatLng(location), zoom));
    }

    public void drawPath(List<LocationInfo> points, int color) {
        List<LatLng> latLngPoints = LocationUtil.locationInfoListToLatLngList(points);

        if (mPathPolyline == null) {
            mPathPolyline = mMap.addPolyline(new PolylineOptions()
                    .endCap(new RoundCap())
                    .startCap(new RoundCap())
                    .zIndex(3)
                    .width(UIUtil.dpToPx(mContext.getResources(), ACCURACY_CIRCLE_STROKE_WIDTH_DP))
                    .color(color)
                    .jointType(JointType.ROUND)
                    .addAll(latLngPoints));
        } else {
            mPathPolyline.setPoints(latLngPoints);
        }
    }

    public void drawArea(List<LocationInfo> points, int color) {
        List<LatLng> latLngPoints = LocationUtil.locationInfoListToLatLngList(points);

        if (mAreaPolygon == null) {
            mAreaPolygon = mMap.addPolygon(new PolygonOptions()
                    .zIndex(4)
                    .strokeWidth(0)
                    .fillColor(color)
                    .addAll(latLngPoints));
        } else {
            mAreaPolygon.setPoints(latLngPoints);
        }
    }

    public void clear() {
        if (mMyLocationMarker != null) {
            mMyLocationMarker.remove();
            mMyLocationMarker = null;
        }

        if (mAccuracyCircle != null) {
            mAccuracyCircle.remove();
            mAccuracyCircle = null;
        }

        if (mPathPolyline != null) {
            mPathPolyline.remove();
            mPathPolyline = null;
        }

        if (mAreaPolygon != null) {
            mAreaPolygon.remove();
            mAreaPolygon = null;
        }
    }
}
