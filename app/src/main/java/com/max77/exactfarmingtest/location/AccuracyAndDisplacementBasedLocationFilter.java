package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

/**
 * Filters incoming location according to maximum horizontal
 * accuracy and the displacement from the previous accepted location
 */
public final class AccuracyAndDisplacementBasedLocationFilter implements ILocationFilter {
    private final double mMaxAccuracy;
    private final double mMinDisplacement;

    private LocationInfo mLastGoodLocation;

    public AccuracyAndDisplacementBasedLocationFilter(double maxAccuracy, double minDisplacement) {
        mMaxAccuracy = maxAccuracy;
        mMinDisplacement = minDisplacement;
    }

    @Override
    public boolean isValidLocation(LocationInfo location) {
        if (location == null || location.getAccuracy() > mMaxAccuracy) {
            return false;
        }

        if (mLastGoodLocation != null && LocationUtil.distanceBetween(mLastGoodLocation, location) > mMinDisplacement) {
            return false;
        }

        mLastGoodLocation = location;
        return true;
    }
}
