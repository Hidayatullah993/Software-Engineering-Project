/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author shenying
 */
public class Timeline {

     /**
     * ArrayList of location intervals, which represents a user's timeline.
     */
    ArrayList<LocationInterval> locationIntervals;

    /**
     * Default constructor for the Timeline class.
     */
    public Timeline() {
        locationIntervals = new ArrayList<>();
    }
    
    public Timeline(Timeline originalTimeline) {
        locationIntervals = new ArrayList<>(originalTimeline.locationIntervals);
    }

    /**
     * Adds a LocationInterval to the existing timeline.
     *
     * @param locationInterval to be added.
     */
    public void addInterval(LocationInterval locationInterval) {
        locationIntervals.add(locationInterval);
    }

    /**
     * Takes in another timeline and a HashMap of locations and time spent
     * together in each place and calculates the amount of time for which this
     * timeline and the other timeline overlap (i.e. spend time together) and
     * puts the overlap time (and place) in the input HashMap. Keeps track of
     * the places where the timelines overlap.
     *
     * @param other Timeline to be checked against.
     * @param locationAndTimeSpent HashMap of places and time spent in each
     * location.
     * @return overlap/together time, in seconds.
     */
    public int retrieveTogetherTime(Timeline other, HashMap<String, Integer> locationAndTimeSpent) {
        int totalTogetherTime = 0;
        for (LocationInterval thisInterval : locationIntervals) {
            for (LocationInterval otherInterval : other.locationIntervals) {
                totalTogetherTime += thisInterval.getOverlap(otherInterval, locationAndTimeSpent);
            }
        }
        locationAndTimeSpent.put("total", totalTogetherTime);
        return totalTogetherTime;
    }

 /**
     * Takes in another timeline and calculates the amount of time for which
     * this timeline and the other timeline overlap (i.e. spend time together).
     * Does not keep track of the places where the timelines overlap.
     *
     * @param other Timeline to be checked against.
     * @return overlap/together time, in seconds.
     */
    public int retrieveTogetherTime(Timeline other) {

        int totalTogetherTime = 0;
        for (LocationInterval thisInterval : locationIntervals) {
            Interval temp1 = thisInterval.getInterval();
            if (temp1.toDuration().getStandardSeconds() > 300) {
                DateTime start1 = temp1.getStart();
                DateTime end1 = start1.plusMinutes(5);

                thisInterval = new LocationInterval(thisInterval.getLocationId(), start1, end1);
            }
            for (LocationInterval otherInterval : other.locationIntervals) {
                Interval temp2 = otherInterval.getInterval();
                if (temp2.toDuration().getStandardSeconds() > 300) {
                    DateTime start2 = temp2.getStart();
                    DateTime end2 = start2.plusMinutes(5);

                    otherInterval = new LocationInterval(otherInterval.getLocationId(), start2, end2);
                }


                if (thisInterval.getOverlap(otherInterval) > 300) {
                    totalTogetherTime += 300;
                } else {
                    totalTogetherTime += thisInterval.getOverlap(otherInterval);
                }

            }
        }

        return totalTogetherTime;
    }

    @Override
    public String toString() {
        String result = "";
        result = locationIntervals.toString();
        return result;
    }

    /**
     * Takes in another timeline and merges it with this timeline. Merging
     * happens when there is overlap in the timelines (i.e. there are
     * LocationIntervals that have the same location and have time interval
     * overlaps).
     *
     * @param other Timeline to be checked against.
     * @return merged Timeline of the two input timelines.
     */
    public Timeline merge(Timeline other) {
        Timeline merged = new Timeline();

        for (LocationInterval thisInterval : locationIntervals) {
            for (LocationInterval otherInterval : other.locationIntervals) {
                LocationInterval overlap = thisInterval.getOverlapInterval(otherInterval);

                if (overlap != null) {

                    merged.addInterval(overlap);
                }
            }
        }

        return merged;
    }

    /**
     * Calculates the time spent in each location on the timeline and maps it
     * into the input map. If the map already contains the place, add to the
     * time spent to the existing time. If not, create new key-value pair.
     *
     * @param togetherTime HashMap of places and time spent together in each
     * location.
     */
    public void calculateTimeSpent(TreeMap<String, Integer> togetherTime) {

        for (LocationInterval interval : locationIntervals) {

            String place = interval.getLocationId();
            if (togetherTime.containsKey(place)) {
                togetherTime.put(place, togetherTime.get(place) + (int) interval.getInterval().toDuration().getStandardSeconds());
            } else {
                togetherTime.put(interval.getLocationId(), (int) interval.getInterval().toDuration().getStandardSeconds());
            }
        }
    }
    
    public void compress() {

        ArrayList<LocationInterval> temp = new ArrayList<>();
        Iterator<LocationInterval> iter = locationIntervals.iterator();

        LocationInterval first = iter.next();

        while (iter.hasNext()) {
   
            LocationInterval second = iter.next();

            if (first.getLocationId().equals(second.getLocationId())) {
                if (first.getInterval().abuts(second.getInterval())) {
                    first = new LocationInterval(first.getLocationId(), first.getInterval().getStart(), second.getInterval().getEnd());
                    iter.remove();

                } else {
                    temp.add(first);
                    first = second;
                }

            } else {
                first = new LocationInterval(first.getLocationId(), first.getInterval().getStart(), second.getInterval().getStart());

                temp.add(first);
                first = second;
            }
            
        }
        temp.add(first);
        locationIntervals = temp;

    }
}
