package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public interface ILocationTracker {
    void startTracking();

    void stopTracking();

    void setListener(Listener listener);

    interface Listener {
        void onNewLocation(LocationInfo location);

        void onLocationNotAvailable();
    }
}
