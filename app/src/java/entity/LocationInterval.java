/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.HashMap;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author shenying
 */
public class LocationInterval {

    /**
     * LocationId associated with the location interval.
     */
    private String locationId;

    /**
     * Time interval associated with the location.
     */
    private Interval interval;

    /**
     * This constructor takes in locationId, start and end time of the location
     * interval.
     *
     * @param locationId locationId associated with the location interval.
     * @param startTime start time of the time interval
     * @param endTime end time of the time interval.
     */
    public LocationInterval(String locationId, DateTime startTime, DateTime endTime) {
        this.locationId = locationId;
        this.interval = new Interval(startTime, endTime);
    }

    /**
     * This constructor takes in locationId and the time interval object
     * associated with the locationId.
     *
     * @param locationId locationId associated with the location interval.
     * @param interval the time interval spent in the location.
     */
    public LocationInterval(String locationId, Interval interval) {
        this.locationId = locationId;
        this.interval = interval;
    }

    /**
     * Retrieve the locationId associated with the location interval.
     *
     * @return locationId associated with the location interval.
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * Retrieves the time interval spent in the location.
     *
     * @return interval of the time spent in location.
     */
    public Interval getInterval() {
        return interval;
    }

    /**
     * This method takes in another LocationInterval and calculates the overlap
     * time between the two intervals if the locationId is the same. Returns
     * overlap time and also updates the overlap time inside the passed in
     * HashMap of locations and time spent in each location.
     *
     * @param other LocationInterval to be checked against.
     * @param locationAndTimeSpent HashMap of places and the time spent in each
     * location.
     * @return overlap time in seconds, returns 0 if lcoationId does not match
     * or no overlap time detected.
     */
    public int getOverlap(LocationInterval other, HashMap<String, Integer> locationAndTimeSpent) {
        if (!locationId.equals(other.locationId)) {
            return 0;
        }
        Interval overlapInterval = interval.overlap(other.interval);

        if (overlapInterval == null) {
            return 0;
        } else {
            int overlapTime = (int) overlapInterval.toDuration().getStandardSeconds();
            if (!locationAndTimeSpent.containsKey(locationId)) {
                locationAndTimeSpent.put(locationId, overlapTime);
            } else {
                int originalTime = locationAndTimeSpent.get(locationId);
                locationAndTimeSpent.put(locationId, originalTime + overlapTime);
            }
            return overlapTime;
        }
    }

    /**
     * This method takes in another LocationInterval and calculates the overlap
     * time between the two intervals if the locationId is the same. Returns
     * overlap time.
     *
     * @param other other LocationInterval to be checked against.
     * @return overlap time in seconds, returns 0 if lcoationId does not match
     * or no overlap time detected.
     */
    public int getOverlap(LocationInterval other) {
        if (!locationId.equals(other.locationId)) {
            return 0;
        }
        Interval overlapInterval = interval.overlap(other.interval);

        if (overlapInterval == null) {
            return 0;
        } else {
            return (int) overlapInterval.toDuration().getStandardSeconds();
        }
    }

    @Override
    public String toString() {
        String result = "";
        result = "location: " + this.locationId + " | interval: " + this.interval;
        return result;
    }

    /**
     * This method takes in another LocationInterval and returns the overlap
     * location interval if there is any overlap.
     *
     * @param other LocationInterval to be checked against.F
     * @return LocationInterval if overlap exist, return null otherwise. 
     */
    public LocationInterval getOverlapInterval(LocationInterval other) {
        if (!locationId.equals(other.locationId)) {
            return null;
        }

        if (interval.overlap(other.interval) == null) {
            return null;
        }
//        System.out.println("overlap: " + interval.overlap(other.interval));
        return new LocationInterval(locationId, interval.overlap(other.interval));
    }
}
