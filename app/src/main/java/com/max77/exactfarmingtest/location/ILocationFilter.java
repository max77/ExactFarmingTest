package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public interface ILocationFilter {
    boolean isValidLocation(LocationInfo location);

    void reset();
}
