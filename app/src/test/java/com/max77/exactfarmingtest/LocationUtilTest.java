package com.max77.exactfarmingtest;

import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180131.
 */

public class LocationUtilTest {
    @Test
    public void intersection1Correct() {
        LocationInfo l1a = LocationInfo.of(0, 0);
        LocationInfo l1b = LocationInfo.of(2, 2);
        LocationInfo l2a = LocationInfo.of(0, 2);
        LocationInfo l2b = LocationInfo.of(2, 0);
        LocationInfo intersection = LocationUtil.getIntersectionBetweenSegments(l1a, l1b, l2a, l2b);

        assertNotNull(intersection);
        assertEquals(intersection.getLatitude(), 1, 1e-8);
        assertEquals(intersection.getLongitude(), 1, 1e-8);
    }

    @Test
    public void intersection2Correct() {
        LocationInfo l1a = LocationInfo.of(0, 0);
        LocationInfo l1b = LocationInfo.of(2, 0);
        LocationInfo l2a = LocationInfo.of(1, 1);
        LocationInfo l2b = LocationInfo.of(1, -1);
        LocationInfo intersection = LocationUtil.getIntersectionBetweenSegments(l1a, l1b, l2a, l2b);

        assertNotNull(intersection);
        assertEquals(intersection.getLatitude(), 1, 1e-8);
        assertEquals(intersection.getLongitude(), 0, 1e-8);
    }

    @Test
    public void intersectionParallelCorrect() {
        LocationInfo l1a = LocationInfo.of(0, 0);
        LocationInfo l1b = LocationInfo.of(2, 0);
        LocationInfo l2a = LocationInfo.of(0, 1);
        LocationInfo l2b = LocationInfo.of(2, 1);
        LocationInfo intersection = LocationUtil.getIntersectionBetweenSegments(l1a, l1b, l2a, l2b);

        assertNull(intersection);
    }

    @Test
    public void intersectionNotIntersectingCorrect() {
        LocationInfo l1a = LocationInfo.of(0, 0);
        LocationInfo l1b = LocationInfo.of(2, 2);
        LocationInfo l2a = LocationInfo.of(0, 1);
        LocationInfo l2b = LocationInfo.of(-2, -1);
        LocationInfo intersection = LocationUtil.getIntersectionBetweenSegments(l1a, l1b, l2a, l2b);

        assertNull(intersection);
    }
}
