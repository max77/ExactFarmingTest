package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

import android.content.Context;
import android.location.Location;
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
public final class FusedLocationTracker implements ILocationTracker {
    private static final String TAG = BuildConfig.TAG + FusedLocationTracker.class.getSimpleName();

    private final long mApproxSamplingPeriod;

    private Listener mListener;
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
                if (mListener != null) {
                    mListener.onNewLocation(LocationUtil.locationInfoFromLocation(lastLoc));
                }
            } else {
                Log.w(TAG, "NULL location received");
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

            if (!locationAvailability.isLocationAvailable()) {
                Log.w(TAG, "location not available");

                stopTracking();
                if (mListener != null) {
                    mListener.onLocationNotAvailable();
                }
            }
        }
    };

    public FusedLocationTracker(Context context, long approxSamplingPeriod) {
        mApproxSamplingPeriod = approxSamplingPeriod;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mApproxSamplingPeriod);
        mLocationRequest.setFastestInterval(mApproxSamplingPeriod);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context.getApplicationContext());
    }

    @Override
    public final void startTracking() throws SecurityException {
        if (isStarted) {
            return;
        }

        isStarted = true;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        Log.i(TAG, "Starting tracking approx. every " + mLocationRequest.getInterval() + "ms");
    }

    @Override
    public final void stopTracking() {
        if (!isStarted) {
            return;
        }

        isStarted = false;

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mListener = null;

        Log.i(TAG, "tracking stopped");
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }
}
