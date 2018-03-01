/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import dao.CompanionDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author Gerald
 */
public class UserGroup implements Comparable<UserGroup>{

    /**
     * An ArrayList of users (represented by their mac addresses) in the group.
     */
    private ArrayList<String> users;

    /**
     * A Timeline object that represents the shared timeline of the group.
     */
    private Timeline sharedTimeline;

    /**
     * A TreeMap of places and the time spent together by the group in each
     * place.
     */
    private TreeMap<String, Integer> togetherTime; //key is place, value is the time spent in seconds

    /**
     * A TreeMap of emails and their associated mac addresses.
     */
    private TreeMap<String, String> emailAndMac;

    /**
     * An ArrayList storing the mac address that do not have an email associated
     * with them.
     */
    private ArrayList<String> macsWithoutEmail;

    /**
     * Default constructor for the UserGroup class. Instantiates all attributes
     * with the defaults.
     */
    public UserGroup() {
        users = new ArrayList<>();
        togetherTime = new TreeMap<>();
        emailAndMac = new TreeMap<>();
        macsWithoutEmail = new ArrayList<>();
        sharedTimeline = new Timeline();
    }

    /**
     * Retrieves the TreeMap of places and time spent together in each place.
     *
     * @return TreeMap of place and time spent, in seconds.
     */
    public TreeMap<String, Integer> getTogetherTime() {
        return togetherTime;
    }

    /**
     * Retrieves the Timeline object representing the shared timeline of the
     * group.
     *
     * @return Timeline of the group.
     */
    public Timeline getSharedTimeline() {
        return sharedTimeline;
    }

    /**
     * Retrieves the ArrayList of users in the group.
     *
     * @return ArrayList of users (represented by mac addresses).
     */
    public ArrayList<String> getUsers() {
        return users;
    }

    /**
     * Takes in a Timeline, and sets it as the shared timeline of the group.
     *
     * @param t new shared Timeline.
     */
    public void setSharedTimeline(Timeline t) {
        sharedTimeline = t;
        sharedTimeline.calculateTimeSpent(togetherTime);
    }

    /**
     * Retrieves the size of the group.
     *
     * @return size of the group.
     */
    public int size() {
        return users.size();
    }

    /**
     * Adds the given user into the group.
     *
     * @param user User to be added into the group.
     */
    public void add(String user) {
        users.add(user);
    }

    /**
     * Takes in a TreeMap of togetherTime and sets it as the group's
     * togetherTime.
     *
     * @param togetherTime TreeMap of places and the time spent in each place.
     */
    public void updateTogetherTime(TreeMap<String, Integer> togetherTime) {
        this.togetherTime = togetherTime;
    }

    /**
     * Takes all the users (mac addresses) in the group and tags their emails to
     * them in the emailAndMac attribute. If there is no email associated with
     * the mac address, add the mac address to the macsWithoutEmail attribute.
     */
    private void mapEmailsToMacs() {
        for (String mac : users) {
            String email = CompanionDAO.getEmail(mac);
            if (!email.equals("")) {
                emailAndMac.put(email, mac);
            } else {
                macsWithoutEmail.add(mac);
            }
        }
    }

    /**
     * Converts the UserGroup object into its JSON representation, following the
     * Project Wiki conventions.
     *
     * @return JSONObject that represents this UserGroup.
     */
    public JSONObject toJSON() {
        JSONObject group = new JSONObject();
        group.put("size", users.size());

        int timeTogether = 0;
        for (String place : togetherTime.keySet()) {
            timeTogether += togetherTime.get(place);
        }
        group.put("total-time-spent", timeTogether);

        mapEmailsToMacs();
        JSONArray members = new JSONArray();
        for (String email : emailAndMac.keySet()) {
            JSONObject userJson = new JSONObject();
            userJson.put("mac-address", emailAndMac.get(email));
            userJson.put("email", email);
            members.add(userJson);
        }
        for (String mac : macsWithoutEmail) {
            JSONObject userJson = new JSONObject();
            userJson.put("mac-address", mac);
            userJson.put("email", "");
            members.add(userJson);
        }
        group.put("members", members);

        JSONArray locations = new JSONArray();
        //System.out.println(locationTimeSpent.get("total"));
        for (String location : togetherTime.keySet()) {
            //System.out.println("time spent in " + location + " is " + togetherTime.get(location));
            JSONObject locationResult = new JSONObject();
            locationResult.put("location", location);
            locationResult.put("time-spent", togetherTime.get(location));
            locations.add(locationResult);
        }
        group.put("locations", locations);

        return group;
    }

    /**
     * This method creates a shared Timeline for the users in the group. Should
     * only be used when the group is a pair.
     *
     * @param allUserTimelines HashMap containing all users and their timelines
     * within the queried time window.
     */
    public void formSharedTimeline(HashMap<String, Timeline> allUserTimelines) {
        Timeline base = allUserTimelines.get(users.get(0));
        for (String user : users) {
            Timeline userTimeline = allUserTimelines.get(user);
            base = base.merge(userTimeline);
        }

        sharedTimeline = base;
        sharedTimeline.calculateTimeSpent(togetherTime);
    }

    /**
     * Checks if this group is an eligible group (spends more than 12 minutes
     * together).
     *
     * @return true if the group is eligible, false otherwise.
     */
    public boolean isEligibleGroup() {

        int timeTogether = 0;
        for (String place : togetherTime.keySet()) {
            timeTogether += togetherTime.get(place);
        }
        return timeTogether >= 12 * 60;
    }

    /**
     * Takes in another UserGroup and checks if this group is a superset
     * (completely contained within) of the other group.
     *
     * @param other UserGroup that is to be checked as a subset.
     * @return true if this UserGroup is a superset of the other UserGroup, false
     * otherwise.
     */
    public boolean isSupersetOf(UserGroup other) {
        for (String user : other.users) {
            if (!users.contains(user)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Sum up the time together
     * @return total time 
     */
    public int calculateTotalTime() {

        int timeTogether = 0;
        for (String place : togetherTime.keySet()) {
            timeTogether += togetherTime.get(place);
        }
        return timeTogether;
    }

    @Override
    public int compareTo(UserGroup o) {
        if (users.size() > o.users.size()) {
            return -1;
        } else if (users.size() < o.users.size()) {
            return 1;
        }
        
        if(calculateTotalTime() > o.calculateTotalTime()){
            return -1;
        } else if(calculateTotalTime() > o.calculateTotalTime()){
            return 1;
        }
        
        return 0;

    }
}
