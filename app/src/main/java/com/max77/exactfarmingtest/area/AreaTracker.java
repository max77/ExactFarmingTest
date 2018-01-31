package com.max77.exactfarmingtest.area;

import android.content.Context;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180129.
 */

public interface AreaTracker {
    void init(Context context);

    void registerCallback(AreaTrackerCallback callback);

    void unregisterCallback();

    interface AreaTrackerCallback {
        void onAreaTrackerStateUpdated(AreaTrackerState state);
    }
}
