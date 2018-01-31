package com.max77.exactfarmingtest.location;

/**
 * ExactFarmingTest project
 * Created by max77 on 20180130.
 */

public final class LocationInfo {
    private double latitude;
    private double longitude;
    private double accuracy;
    private long time;

    public double getLatitude() {
        return latitude;
    }

    public LocationInfo setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public LocationInfo setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public LocationInfo setAccuracy(double accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public long getTime() {
        return time;
    }

    public LocationInfo setTime(long time) {
        this.time = time;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationInfo that = (LocationInfo) o;

        if (Double.compare(that.getLatitude(), getLatitude()) != 0) return false;
        if (Double.compare(that.getLongitude(), getLongitude()) != 0) return false;
        if (Double.compare(that.getAccuracy(), getAccuracy()) != 0) return false;
        return getTime() == that.getTime();
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getLatitude());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getLongitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getAccuracy());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (getTime() ^ (getTime() >>> 32));
        return result;
    }

    public static LocationInfo of(double lat, double lon) {
        return new LocationInfo()
                .setLatitude(lat)
                .setLongitude(lon);
    }

    public static LocationInfo of(LocationInfo other) {
        return other != null ?
                new LocationInfo()
                        .setLatitude(other.latitude)
                        .setLongitude(other.longitude)
                        .setAccuracy(other.accuracy)
                        .setTime(other.time) :
                null;
    }
}
