package com.max77.exactfarmingtest.area;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180129.
 */

public class AreaTrackerState {
    enum Status {
        STARTED,
        WAITING_FOR_LOCATION,
        READY_TO_TRACK,
        TRACKING,
        TRACKING_ERROR,
        TRACKING_FINISHED,
        FINISHED,
        ERROR;
    }

//    private Status mStatus;
//    private Collection<LocationInfo> mPath = new LinkedList<>();
//    private LocationInfo mLastValidLocation;
//
//    public AreaTrackerState(Status status) {
//        mStatus = status;
//    }
//
//    public AreaTrackerState(AreaTrackerState other) {
//        mStatus = other.mStatus;
//        mPath = new LinkedList<>(other.mPath);
//        mLastValidLocation;
//    }
//
//    public Collection<LocationInfo> getPath() {
//        return Collections.unmodifiableCollection(mPath);
//    }
//
//    public AreaTrackerState addLocation(LocationInfo locationInfo) {
//        mPath.add(locationInfo);
//        return this;
//    }
//
//    public void truncateToLastValidLocation() {
//
//    }
}
