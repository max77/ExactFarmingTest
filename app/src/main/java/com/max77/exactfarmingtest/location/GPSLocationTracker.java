package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.max77.exactfarmingtest.BuildConfig;

/**
 * Attempts to get GPS coordinates periodically
 */
public final class GPSLocationTracker implements ILocationTracker {
    private static final String TAG = BuildConfig.TAG + GPSLocationTracker.class.getSimpleName();

    private final long mApproxSamplingPeriod;
    private LocationManager mLocationManager;
    private final LocationListener mLocationListener;

    private Listener mListener;
    private boolean isStarted;

    @SuppressLint("MissingPermission")
    public GPSLocationTracker(Context context, long approxSamplingPeriod) {
        mApproxSamplingPeriod = approxSamplingPeriod;

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location != null) {
                    Log.i(TAG, "new location received: " + location);

                    if (mListener != null) {
                        mListener.onNewLocation(LocationUtil.locationInfoFromLocation(location));
                    }
                } else {
                    Log.w(TAG, "NULL location received");
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status != LocationProvider.AVAILABLE) {
                    onProviderDisabled(provider);
                }
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                destroy();
                if (mListener != null) {
                    mListener.onLocationNotAvailable();
                }
            }
        };
    }

    @Override
    public final void startTracking() throws SecurityException {
        checkForBeingReused();

        if (isStarted) {
            return;
        }

        isStarted = true;

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null && mListener != null) {
            Log.i(TAG, "last location used: " + location);
            mListener.onNewLocation(LocationUtil.locationInfoFromLocation(location));
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mApproxSamplingPeriod,
                0, mLocationListener);
        Log.i(TAG, "Starting tracking approx. every " + mApproxSamplingPeriod + "ms");
    }

    @Override
    public final void destroy() {
        checkForBeingReused();

        if (!isStarted) {
            return;
        }

        isStarted = false;
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;
        mListener = null;

        Log.i(TAG, "destroyed");
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void checkForBeingReused() {
        if (mLocationManager == null)
            throw new IllegalStateException(getClass().getSimpleName() + " can not be reused!");
    }
}
