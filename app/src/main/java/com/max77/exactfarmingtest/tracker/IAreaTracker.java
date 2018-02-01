package com.max77.exactfarmingtest.tracker;

import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationPath;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180131.
 */

public interface IAreaTracker {

    void addStateListener(StateListener listener);

    boolean removeStateListener(StateListener listener);

    void forceStateListenerForCurrentState();

    void startInitialLocationFix();

    void startTracking();

    void finishTracking();

    interface StateListener {
        void isInitialized();

        void isFixingInitialLocation(LocationInfo currentLocation, long startTime, long timeout);

        void isLocationFixed(LocationInfo currentLocation);

        void isTracking(LocationPath currentPath);

        void isFinished(LocationPath currentPath);

        void isLocationNotAvailable();

        void isLocationTimedOut(LocationInfo lastLocation);
    }

}
