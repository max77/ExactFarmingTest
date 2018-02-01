package com.max77.exactfarmingtest.location;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public final class LocationUtil {
    public static LocationInfo locationInfoFromLocation(Location location) {
        return new LocationInfo()
                .setLatitude(location.getLatitude())
                .setLongitude(location.getLongitude())
                .setAccuracy(location.getAccuracy())
                .setTime(location.getTime());
    }

    public static LatLng locationInfoToLatLng(LocationInfo locationInfo) {
        return new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
    }

    public static List<LatLng> locationInfoListToLatLngList(List<LocationInfo> locationInfos) {
        List<LatLng> result = new ArrayList<>();

        if (locationInfos != null) {
            for (LocationInfo locationInfo : locationInfos) {
                result.add(locationInfoToLatLng(locationInfo));
            }
        }

        return result;
    }

    public static double distanceBetween(LocationInfo l1, LocationInfo l2) {
        return SphericalUtil.computeDistanceBetween(locationInfoToLatLng(l1), locationInfoToLatLng(l2));
    }

    public static LocationInfo getIntersectionBetweenSegments(LocationInfo l1a, LocationInfo l1b,
                                                              LocationInfo l2a, LocationInfo l2b) {

        double x1 = l1a.getLatitude(), y1 = l1a.getLongitude(),
                x2 = l1b.getLatitude(), y2 = l1b.getLongitude(),
                x3 = l2a.getLatitude(), y3 = l2a.getLongitude(),
                x4 = l2b.getLatitude(), y4 = l2b.getLongitude();

        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) {
            return null;
        }

        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            return new LocationInfo()
                    .setLatitude(x1 + ua * (x2 - x1))
                    .setLongitude(y1 + ub * (y2 - y1));
        }

        return null;
    }
}
