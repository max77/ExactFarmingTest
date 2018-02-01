package com.max77.exactfarmingtest.location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public final class LocationPath {
    private double mPathClosureThreshold;
    private int[] mClosedPartEndpointsIdx;
    private List<LocationInfo> mPoints = new ArrayList<>();

    public LocationPath(double pathClosureThreshold) {
        if (pathClosureThreshold <= 0)
            throw new IllegalArgumentException("Closure threshold must be positive!");

        mPathClosureThreshold = pathClosureThreshold;
    }

    public boolean isClosed() {
        return mClosedPartEndpointsIdx != null &&
                mClosedPartEndpointsIdx[0] == 0 &&
                mClosedPartEndpointsIdx[1] == size() - 1;
    }

    public boolean hasSelfIntersection() {
        return mClosedPartEndpointsIdx != null &&
                mClosedPartEndpointsIdx[0] > 0 &&
                mClosedPartEndpointsIdx[1] < size() - 1;
    }

    public List<LocationInfo> getAllPoints() {
        return Collections.unmodifiableList(mPoints);
    }

    public List<LocationInfo> getClosedPart() {
        if (mClosedPartEndpointsIdx != null) {
            return Collections.unmodifiableList(mPoints.subList(mClosedPartEndpointsIdx[0],
                    mClosedPartEndpointsIdx[1] + 1));
        } else {
            return Collections.unmodifiableList(new ArrayList<LocationInfo>());
        }
    }

    public int size() {
        return mPoints.size();
    }

    public void addPoint(LocationInfo newPoint) {
        if (size() >= 3 && mClosedPartEndpointsIdx == null) {
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
                    mClosedPartEndpointsIdx = new int[]{0, size()};
                }
            }

            if (intersectionPoint != null && mClosedPartEndpointsIdx == null) {
                // add copies of the intersection point to the end of the path
                // and between the endpoints of the intersected segment
                mPoints.add(LocationInfo.of(intersectionPoint)
                        .setTime((lastPoint.getTime() + newPoint.getTime()) / 2));
                mPoints.add(idx, LocationInfo.of(intersectionPoint)
                        .setTime((mPoints.get(idx - 1).getTime() + mPoints.get(idx).getTime()) / 2));
                mClosedPartEndpointsIdx = new int[]{idx, size() - 1};
            }
        }

        mPoints.add(LocationInfo.of(newPoint));
    }

    public void forceClosing() {
        addPoint(LocationInfo.of(mPoints.get(0)));
    }

    public void removeTailAfterIntersection() {
        List<LocationInfo> newPoints = mPoints.subList(0, mClosedPartEndpointsIdx[1]);
        mPoints = newPoints;
        mClosedPartEndpointsIdx = null;
    }

    public static LocationPath of(LocationPath other) {
        LocationPath path = new LocationPath(other.mPathClosureThreshold);
        path.mClosedPartEndpointsIdx = Arrays.copyOf(other.mClosedPartEndpointsIdx, 2);
        for (LocationInfo point : other.mPoints) {
            path.mPoints.add(LocationInfo.of(point));
        }

        return path;
    }

    public double area() {
        if (size() <= 3) {
            return 0;
        }

        List<LatLng> latLngPoints = new ArrayList<>();
        for (LocationInfo point : mPoints) {
            latLngPoints.add(LocationUtil.locationInfoToLatLng(point));
        }

        return SphericalUtil.computeArea(latLngPoints);
    }

    public double length() {
        if (size() < 2) {
            return 0;
        }

        double len = 0;

        LocationInfo previous = null;
        for (LocationInfo point : mPoints) {
            if (previous != null)
                len += LocationUtil.distanceBetween(point, previous);
            previous = point;
        }

        return len;
    }
}
