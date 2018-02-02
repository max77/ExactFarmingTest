package com.max77.exactfarmingtest;

import com.max77.exactfarmingtest.location.LocationPath;
import com.max77.exactfarmingtest.location.LocationInfo;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LocationPathTest {
    private void assertLatLonTime(LocationInfo locationInfo, double lat, double lon, long time) {
        assertEquals(locationInfo.getLatitude(), lat, 1e-8);
        assertEquals(locationInfo.getLongitude(), lon, 1e-8);
        assertEquals(locationInfo.getTime(), time);
    }

    @Test
    public void shortPathCorrect() {
        LocationPath path = new LocationPath(0.5f);

        assertEquals(path.size(), 0);
        assertFalse(path.hasSelfIntersection());
        assertFalse(path.isClosed());
        assertTrue(path.getLoop().isEmpty());
        assertTrue(path.getAllPoints().isEmpty());

        path.addPoint(LocationInfo.of(0, 0));
        assertEquals(path.size(), 1);
        assertFalse(path.hasSelfIntersection());
        assertFalse(path.isClosed());
        assertTrue(path.getLoop().isEmpty());
        assertFalse(path.getAllPoints().isEmpty());

        path.addPoint(LocationInfo.of(1, 0));
        path.addPoint(LocationInfo.of(1, 1));

        assertEquals(path.size(), 3);
        assertFalse(path.hasSelfIntersection());
        assertFalse(path.isClosed());
        assertTrue(path.getLoop().isEmpty());
        assertFalse(path.getAllPoints().isEmpty());
    }

    @Test
    public void longOpenPathCorrect() {
        LocationPath path = new LocationPath(0.5f);

        for (int i = 0; i < 10; i++)
            path.addPoint(LocationInfo.of(i, i));

        assertEquals(path.size(), 10);
        assertFalse(path.hasSelfIntersection());
        assertFalse(path.isClosed());
        assertTrue(path.getLoop().isEmpty());
        assertFalse(path.getAllPoints().isEmpty());
    }

    @Test
    public void closedPathCorrect() {
        LocationPath path = new LocationPath(0.5f);

        path.addPoint(LocationInfo.of(0, 0).setTime(1));
        path.addPoint(LocationInfo.of(2, 0).setTime(2));
        path.addPoint(LocationInfo.of(2, 2).setTime(3));
        path.addPoint(LocationInfo.of(0, 2).setTime(4));
        path.addPoint(LocationInfo.of(0, 0).setTime(5));

        assertEquals(path.size(), 5);
        assertFalse(path.hasSelfIntersection());
        assertTrue(path.isClosed());
        assertFalse(path.getLoop().isEmpty());
        assertFalse(path.getAllPoints().isEmpty());
        List<LocationInfo> allPoints = path.getAllPoints();
        assertEquals(allPoints.get(0).getLatitude(), allPoints.get(path.size() - 1).getLatitude(), 0);
        assertEquals(allPoints.get(0).getLongitude(), allPoints.get(path.size() - 1).getLongitude(), 0);
        assertEquals(path.getLoop().get(4).getTime(), 5);
    }

    @Test
    public void selfIntersection1Correct() {
        LocationPath path = new LocationPath(0.5f);

        path.addPoint(LocationInfo.of(0, 0).setTime(10));
        path.addPoint(LocationInfo.of(2, 0).setTime(20));
        path.addPoint(LocationInfo.of(2, 2).setTime(30));
        path.addPoint(LocationInfo.of(0, -2).setTime(40));

        assertEquals(path.size(), 6);
        assertTrue(path.hasSelfIntersection());
        assertFalse(path.isClosed());
        List<LocationInfo> closedPart = path.getLoop();
        assertEquals(closedPart.size(), 4);
        assertLatLonTime(closedPart.get(0), 1, 0, 15);
        assertLatLonTime(closedPart.get(1), 2, 0, 20);
        assertLatLonTime(closedPart.get(2), 2, 2, 30);
        assertLatLonTime(closedPart.get(3), 1, 0, 35);

        assertEquals(path.getAllPoints().size(), 6);
    }
}