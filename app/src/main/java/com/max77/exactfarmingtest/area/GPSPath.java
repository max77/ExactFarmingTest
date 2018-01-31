package com.max77.exactfarmingtest.area;

import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public final class GPSPath {
    private double mPathClosureThreshold;
    private int[] mIntersectionPointsIdx;
    private List<LocationInfo> mPoints = new ArrayList<>();

    public GPSPath(double pathClosureThreshold) {
        if (pathClosureThreshold <= 0)
            throw new IllegalArgumentException("Closure threshold must be positive!");

        mPathClosureThreshold = pathClosureThreshold;
    }

    public boolean isClosed() {
        return mIntersectionPointsIdx != null &&
                mIntersectionPointsIdx[0] == 0 &&
                mIntersectionPointsIdx[1] == size() - 1;
    }

    public boolean hasSelfIntersection() {
        return mIntersectionPointsIdx != null &&
                mIntersectionPointsIdx[0] > 0 &&
                mIntersectionPointsIdx[1] < size() - 1;
    }

    public List<LocationInfo> getAllPoints() {
        return Collections.unmodifiableList(mPoints);
    }

    public List<LocationInfo> getClosedPart() {
        if (mIntersectionPointsIdx != null) {
            return Collections.unmodifiableList(mPoints.subList(mIntersectionPointsIdx[0],
                    mIntersectionPointsIdx[1] + 1));
        } else {
            return Collections.unmodifiableList(new ArrayList<LocationInfo>());
        }
    }

    public int size() {
        return mPoints.size();
    }

    public void addPoint(LocationInfo newPoint) {
        if (size() >= 3) {
            LocationInfo lastPoint = mPoints.get(size() - 1);
            LocationInfo intersectionPoint = null;

            // check for path self-intersection
            int idx = 0;
            while (idx < size() - 2 && intersectionPoint == null) {
                LocationInfo pointA = mPoints.get(idx);
                LocationInfo pointB = mPoints.get(++idx);

                intersectionPoint =
                        LocationUtil.getIntersectionBetweenSegments(pointA, pointB, lastPoint, newPoint);
            }

            if (intersectionPoint == null || idx == 1) {
                // check for closure
                LocationInfo startPoint = mPoints.get(0);
                if (LocationUtil.distanceBetween(newPoint, startPoint) <
                        mPathClosureThreshold) {
                    // replace the new point coordinates with ones of the starting point of the path
                    newPoint = LocationInfo.of(startPoint)
                            .setTime(newPoint.getTime())
                            .setAccuracy(newPoint.getAccuracy());
                    mIntersectionPointsIdx = new int[]{0, size()};
                }
            }

            if (intersectionPoint != null && mIntersectionPointsIdx == null) {
                // add copies of the intersection point to the end of the path
                // and between the endpoints of the intersected segment
                mPoints.add(LocationInfo.of(intersectionPoint)
                        .setTime((lastPoint.getTime() + newPoint.getTime()) / 2));
                mPoints.add(idx, LocationInfo.of(intersectionPoint)
                        .setTime((mPoints.get(idx - 1).getTime() + mPoints.get(idx).getTime()) / 2));
                mIntersectionPointsIdx = new int[]{idx, size() - 1};
            }
        }

        mPoints.add(newPoint);
    }
}
