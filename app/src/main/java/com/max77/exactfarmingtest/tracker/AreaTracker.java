package com.max77.exactfarmingtest.tracker;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.max77.exactfarmingtest.BuildConfig;
import com.max77.exactfarmingtest.location.ILocationFilter;
import com.max77.exactfarmingtest.location.ILocationTracker;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180201.
 */

public class AreaTracker implements IAreaTracker {
    private static final String TAG = BuildConfig.TAG + AreaTracker.class.getSimpleName();

    private enum State {
        ZERO,
        INITIALIZED,
        WAITING_FOR_INITIAL_FIX,
        LOCATION_FIXED,
        TRACKING,
        FINISHED,
        LOCATION_TIMED_OUT,
        LOCATION_NOT_AVAILABLE
    }

    private State mState = State.ZERO;

    private final long mLocationTimeout;

    private ILocationTracker mLocationTracker;
    private ILocationFilter mLocationFilter;
    private Set<StateListener> mStateListeners = new HashSet<>();
    private LocationPath mCurrentPath;
    private LocationInfo mCurrentLocation;
    private long mCountdownStartTime;
    private Handler mLocationTimeoutHandler;

    public AreaTracker(ILocationTracker locationTracker, ILocationFilter locationFilter,
                       long locationTimeout, double pathClosureThreshold) {
        if (locationTracker != null && locationFilter != null) {
            mLocationTracker = locationTracker;
            mLocationFilter = locationFilter;
            mLocationTimeout = locationTimeout;
            mCurrentPath = new LocationPath(pathClosureThreshold);

            updateStateAndReport(State.INITIALIZED);
        } else {
            throw new IllegalArgumentException("locationTracker and locationFilter must be non-null!");
        }
    }

    @Override
    public void addStateListener(StateListener listener) {
        mStateListeners.add(listener);
    }

    @Override
    public boolean removeStateListener(StateListener listener) {
        if (listener != null) {
            return mStateListeners.remove(listener);
        }

        mStateListeners.clear();
        return false;
    }

    @Override
    public void forceStateListenerForCurrentState() {
        if (mStateListeners.isEmpty()) {
            return;
        }

        for (StateListener stateListener : mStateListeners) {
            switch (mState) {
                case INITIALIZED:
                    stateListener.isInitialized();
                    break;
                case WAITING_FOR_INITIAL_FIX:
                    stateListener.isFixingInitialLocation(LocationInfo.of(mCurrentLocation), mCountdownStartTime, mLocationTimeout);
                    break;
                case LOCATION_TIMED_OUT:
                    stateListener.isLocationTimedOut(mCurrentLocation);
                    break;
                case LOCATION_NOT_AVAILABLE:
                    stateListener.isLocationNotAvailable();
                    break;
                case LOCATION_FIXED:
                    stateListener.isLocationFixed(mCurrentLocation);
                    break;
                case TRACKING:
                    stateListener.isTracking(LocationPath.of(mCurrentPath));
                    break;
                case FINISHED:
                    stateListener.isFinished(LocationPath.of(mCurrentPath));
                    break;
            }
        }
    }

    @Override
    public void startInitialLocationFix() {
        if (!checkState(State.INITIALIZED, State.LOCATION_TIMED_OUT, State.LOCATION_NOT_AVAILABLE)) {
            return;
        }

        updateStateAndReportWithTimeout(State.WAITING_FOR_INITIAL_FIX);

        mLocationFilter.reset();
        mLocationTracker.setListener(new ILocationTracker.Listener() {
            @Override
            public void onNewLocation(LocationInfo location) {
                mCurrentLocation = location;
                updateStateAndReport(State.WAITING_FOR_INITIAL_FIX);

                if (mLocationFilter.isValidLocation(location)) {
                    Log.i(TAG, "initial location fixed: " + mCurrentLocation);

                    mLocationTracker.stopTracking();
                    stopLocationTimeoutCountdown();
                    updateStateAndReport(State.LOCATION_FIXED);
                }
            }

            @Override
            public void onLocationNotAvailable() {
                reportLocationNotAvailable();
            }
        });

        mLocationTracker.startTracking();
    }

    @Override
    public void startTracking() {
        if (!checkState(State.LOCATION_FIXED, State.LOCATION_TIMED_OUT)) {
            return;
        }

        updateStateAndReportWithTimeout(State.TRACKING);

        mLocationFilter.reset();
        mLocationTracker.setListener(new ILocationTracker.Listener() {
            @Override
            public void onNewLocation(LocationInfo location) {
                if (mLocationFilter.isValidLocation(location)) {
                    mCurrentLocation = location;
                    mCurrentPath.addPoint(mCurrentLocation);

                    Log.i(TAG, "new location added to path: " + mCurrentLocation);

                    if (mCurrentPath.hasSelfIntersection()) {
                        mCurrentPath.removeLoop();
                    }

                    if (mCurrentPath.isClosed()) {
                        finishTracking();
                    } else {
                        updateStateAndReportWithTimeout(State.TRACKING);
                    }
                }
            }

            @Override
            public void onLocationNotAvailable() {
                reportLocationNotAvailable();
            }
        });

        mLocationTracker.startTracking();
    }

    @Override
    public void finishTracking() {
        stopLocationTimeoutCountdown();
        mLocationTracker.setListener(null);
        mLocationTracker.stopTracking();

        // forced finishing
        if (!mCurrentPath.isClosed()) {
            mCurrentPath.forceClosing();
            if (mCurrentPath.hasSelfIntersection()) {
                mCurrentPath.removeLoop();
            }
            Log.i(TAG, "tracking finished (forced path closing)");
        } else {
            Log.i(TAG, "tracking finished (closed)");
        }

        updateStateAndReport(State.FINISHED);
    }

    @Override
    public void destroy() {
        if (mLocationTracker != null) {
            mLocationTracker.setListener(null);
            mLocationTracker.stopTracking();
            mLocationTracker = null;
        }
    }

    private void updateStateAndReport(State state) {
        mState = state;
        forceStateListenerForCurrentState();
    }

    private void updateStateAndReportWithTimeout(State state) {
        updateStateAndReport(state);
        mCountdownStartTime = System.currentTimeMillis();
        startLocationTimeoutCountdown();
    }

    private void startLocationTimeoutCountdown() {
        stopLocationTimeoutCountdown();

        mLocationTimeoutHandler = new Handler();
        mLocationTimeoutHandler.postDelayed(() -> {
            stopLocationTimeoutCountdown();
            mLocationTracker.stopTracking();
            updateStateAndReport(State.LOCATION_TIMED_OUT);
        }, mLocationTimeout);
    }

    private void stopLocationTimeoutCountdown() {
        if (mLocationTimeoutHandler != null) {
            mLocationTimeoutHandler.removeCallbacksAndMessages(null);
            mLocationTimeoutHandler = null;
        }
    }

    private boolean checkState(State... allowedStates) {
        for (State allowedState : allowedStates) {
            if (mState == allowedState)
                return true;
        }

        List<String> states = new ArrayList<>();
        for (State allowedState : allowedStates) {
            states.add(allowedState.name());
        }

        Log.w(TAG, "not in allowed state: " + TextUtils.join(",", states));
        return false;
    }

    private void reportLocationNotAvailable() {
        stopLocationTimeoutCountdown();
        mLocationTracker.stopTracking();
        updateStateAndReport(State.LOCATION_NOT_AVAILABLE);
    }
}
