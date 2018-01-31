package com.max77.exactfarmingtest.tracker;

import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationPath;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180131.
 */

public abstract class TrivialAreaTrackerServiceStateListener implements IAreaTrackerService.StateListener {
    @Override
    public void isCreated() {

    }

    @Override
    public void isInitialized() {

    }

    @Override
    public void isLocationNotAvailable() {

    }

    @Override
    public void isFixingInitialLocation(LocationInfo currentLocation, long startTime, long timeout) {

    }

    @Override
    public void isTimedOutFixingInitialLocation(LocationInfo lastLocation) {

    }

    @Override
    public void isTrackingNormally(LocationPath currentPath, boolean isRecoveringAfterSelfIntersection) {

    }

    @Override
    public void isTimedOutTracking(LocationPath currentPath) {

    }

    @Override
    public void isTrackingComplete(LocationPath currentPath) {

    }
}
