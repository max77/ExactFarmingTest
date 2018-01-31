package com.max77.exactfarmingtest.tracker;

import com.max77.exactfarmingtest.location.ILocationFilter;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationPath;
import com.max77.exactfarmingtest.location.ILocationTracker;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180131.
 */

public interface IAreaTrackerService {
    void initialize(ILocationTracker locationTracker, ILocationFilter locationFilter);

    void setStateListener(StateListener listener);

    void forceStateListenerForCurrentState(StateListener listener);

    void startInitialLocationFix();

    void startTracking();

    void recoverAfterSelfIntersection();

    void finishTracking();

    interface StateListener {
        void isCreated();

        void isInitialized();

        void isLocationNotAvailable();

        void isFixingInitialLocation(LocationInfo currentLocation, long startTime, long timeout);

        void isTimedOutFixingInitialLocation(LocationInfo lastLocation);

        void isTrackingNormally(LocationPath currentPath, boolean isRecoveringAfterSelfIntersection);

        void isTimedOutTracking(LocationPath currentPath);

        void isTrackingComplete(LocationPath currentPath);
    }

}
