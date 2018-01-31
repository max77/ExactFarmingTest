package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.max77.exactfarmingtest.BuildConfig;

/**
 * Attempts to get GPS coordinates periodically
 */
public abstract class PeriodicGPSLocationTracker implements LocationTracker {
    private static final String TAG = BuildConfig.TAG + PeriodicGPSLocationTracker.class.getSimpleName();

    private final long mApproxTrackingPeriod;

    private boolean isStarted;
    private final LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Location lastLoc = locationResult.getLastLocation();

            if (lastLoc != null) {
                Log.i(TAG, "new location received: " + lastLoc);
                onNewLocation(LocationUtil.locationInfoFromLocation(lastLoc));
            } else {
                Log.w(TAG, "NULL location received");
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            if (!locationAvailability.isLocationAvailable()) {
                stopTracking();
                onLocationNotAvailable();
            }
        }
    };

    public PeriodicGPSLocationTracker(Context context, long approxTrackingPeriod) {
        mApproxTrackingPeriod = approxTrackingPeriod;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mApproxTrackingPeriod);
        mLocationRequest.setFastestInterval(mApproxTrackingPeriod);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public final void startTracking() throws SecurityException {
        checkForBeingReused();
        if (isStarted)
            return;

        isStarted = true;
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        Log.i(TAG, "Starting tracking approx. every " + mLocationRequest.getInterval() + "ms");
    }


    @Override
    public final void stopTracking() {
        checkForBeingReused();
        if (!isStarted)
            return;

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mFusedLocationClient = null;
        Log.i(TAG, "Stopped tracking");

        isStarted = false;
    }

    private void checkForBeingReused() {
        if (mFusedLocationClient == null)
            throw new IllegalStateException("PeriodicGPSLocationTracker can not be reused!");
    }
}
