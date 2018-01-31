package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public interface LocationTracker {
    void startTracking();

    void stopTracking();

    void onNewLocation(LocationInfo location);

    void onLocationNotAvailable();
}
