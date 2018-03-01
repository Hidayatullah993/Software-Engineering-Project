/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.LocationInterval;
import entity.Timeline;
import entity.UserGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author shenying
 */
public class CompanionDAO {

   
    /**
     * Gets all the timelines of the mac addresses that are within the
     * timeframe.
     *
     * @param date The date that is queried by the user.
     * @return a HashMap of String and Timeline that maps users and their
     * timelines.
     */
    public static HashMap<String, Timeline> getAllUserTimeLines(String date) {
        date = date.replace("T", " ");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryWindowEnd = formatter.parseDateTime(date);
        DateTime queryWindowStart = queryWindowEnd.minusMinutes(15);
        queryWindowEnd = queryWindowEnd.minusSeconds(1);

        HashMap<String, Timeline> userTimelineMap = new HashMap<>();

        try {
            Connection conn = ConnectionManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("select mac_address, location_id, timestamp from location where "
                    + "timestamp between ? and ? order by mac_address, timestamp");

            stmt.setString(1, queryWindowStart.toString(formatter));
            stmt.setString(2, queryWindowEnd.toString(formatter));

            ResultSet rs = stmt.executeQuery();

            String currentUser = "";
            Timeline currentUserTimeline = new Timeline();
            String currentLocation = "";

            DateTime minDate = null;
            DateTime maxDate = null;

            if (rs.next()) {
                String macAddress = rs.getString(1);
                String locationId = rs.getString(2);
                String timestamp = rs.getString(3);

                timestamp = timestamp.substring(0, timestamp.lastIndexOf("."));

                currentUser = macAddress;
                currentLocation = locationId;
                minDate = formatter.parseDateTime(timestamp);
            } else {
                return userTimelineMap;
            }

            while (rs.next()) {
                String macAddress = rs.getString(1);
                String locationId = rs.getString(2);
                String timestamp = rs.getString(3);

                timestamp = timestamp.substring(0, timestamp.lastIndexOf("."));

                if (!macAddress.equals(currentUser)) { //user is different, so add their interval immediately
                    if (maxDate == null) {
                        maxDate = minDate.plusMinutes(5);
                    }

                    if (maxDate.isAfter(queryWindowEnd)) {
                        maxDate = queryWindowEnd.plusSeconds(1);
                    }
                    LocationInterval interval = new LocationInterval(currentLocation, minDate, maxDate);
                    currentUserTimeline.addInterval(interval);
                    userTimelineMap.put(currentUser, currentUserTimeline);

                    //now reset all variables
                    currentUserTimeline = new Timeline();
                    currentUser = macAddress;
                    currentLocation = locationId;
                    minDate = formatter.parseDateTime(timestamp);
                    maxDate = null;
                } else { //user is the same, handle a few other different cases now
                    if (!(locationId.equals(currentLocation))) { //same user but different location, add interval to timeline
                        maxDate = formatter.parseDateTime(timestamp);

                        if (maxDate.isAfter(minDate.plusMinutes(5))) {
                            maxDate = minDate.plusMinutes(5);
                        } else if (maxDate.isAfter(queryWindowEnd)) {
                            maxDate = queryWindowEnd.plusSeconds(1);
                        }

                        LocationInterval interval = new LocationInterval(currentLocation, minDate, maxDate);
                        currentUserTimeline.addInterval(interval);

                        //now reset all variables except for user
                        currentLocation = locationId;
                        minDate = formatter.parseDateTime(timestamp);
                        maxDate = null;
                    } else { //same user, same location
                        maxDate = formatter.parseDateTime(timestamp);

                        if (maxDate.isAfter(minDate.plusMinutes(5))) {
                            maxDate = minDate.plusMinutes(5);

                            LocationInterval interval = new LocationInterval(currentLocation, minDate, maxDate);
                            currentUserTimeline.addInterval(interval);

                            minDate = formatter.parseDateTime(timestamp);
                            maxDate = null;
                        } else {
                            LocationInterval interval = new LocationInterval(currentLocation, minDate, maxDate);
                            currentUserTimeline.addInterval(interval);

                            minDate = formatter.parseDateTime(timestamp);
                            maxDate = null;
                        }

                    }
                }
            }

            //handle last user
            maxDate = minDate.plusMinutes(5);

            if (maxDate.isAfter(queryWindowEnd)) {
                maxDate = queryWindowEnd.plusSeconds(1);
            }

            LocationInterval interval = new LocationInterval(currentLocation, minDate, maxDate);
            currentUserTimeline.addInterval(interval);
            userTimelineMap.put(currentUser, currentUserTimeline);


            ConnectionManager.close(conn, stmt, rs);
        } catch (SQLException ex) {
            Logger.getLogger(CompanionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userTimelineMap;
    }
    
   /**
     * Takes in a mac address and searches the database for an email associated
     * with the mac address.
     *
     * @param macAddress The mac address to query in the database.
     * @return email if it exists in database, empty string otherwise.
     */
    public static String getEmail(String macAddress) {
        String result = "";
        try {
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select email from demographics where mac_address = ?");
            stmt.setString(1, macAddress);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = rs.getString("email");
             
            }

            ConnectionManager.close(conn, stmt, rs);

        } catch (SQLException ex) {
            Logger.getLogger(CompanionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

      /**
     * Finds the top k companions of a selected mac address.
     *
     * @param userTimelines A HashMap of all users and their timelines found
     * within the query window.
     * @param macAddress Mac address for which the top k companions must be
     * found.
     * @param date The date that is queried by the user.
     * @return a JSONObject containing the top k companion results.
     */
    public static JSONObject getTopKCompanion(HashMap<String, Timeline> userTimelines, String macAddress, String date) {
        JSONObject jsonOutput = new JSONObject();

        Timeline timeframeOfQueriedUser = userTimelines.get(macAddress);
        if (timeframeOfQueriedUser == null) {
            TreeMap<Integer, ArrayList<String>> emptyMap = new TreeMap<>();
            jsonOutput.put("results", emptyMap);
            return jsonOutput;
        }
        userTimelines.remove(macAddress); //remove queried user from the list.

        TreeMap<Integer, ArrayList<String>> tempMap = new TreeMap<>();
        //key will be the seconds tgt, value will be the macAddress;

        for (String otherUser : userTimelines.keySet()) {
            Timeline otherUserTimeline = userTimelines.get(otherUser);

            int secondsTgt = timeframeOfQueriedUser.retrieveTogetherTime(otherUserTimeline);
            if (secondsTgt != 0) {
                if (tempMap.containsKey(secondsTgt)) {
                    ArrayList<String> userList = tempMap.get(secondsTgt);
                    userList.add(otherUser);
                } else {
                    ArrayList<String> userList = new ArrayList<>();
                    userList.add(otherUser);
                    tempMap.put(secondsTgt, userList);
                }
            }

        }
        TreeMap<Integer, ArrayList<String>> resultMap = new TreeMap<>(Collections.reverseOrder());
        resultMap.putAll(tempMap);

        jsonOutput.put("results", resultMap);

        return jsonOutput;
    }
    
    /**
     * Finds all the groups of students found within the query window.
     *
     * @param date The date that is queried by the user.
     * @return a JSONObject containing the group detection results.
     */
    public static JSONObject detectGroups(String date) {
        JSONObject jsonOutput = new JSONObject();

        HashMap<String, Timeline> allUserTimelines = getAllUserTimeLines(date);
        //size of allUsertimeline is the totalNumberOfUsers.
        jsonOutput.put("total-users", allUserTimelines.size());

        //filter out users that don't spend 720 seconds in the building
        Iterator<String> userIter = allUserTimelines.keySet().iterator();
        while (userIter.hasNext()) {
            String user = userIter.next();
            Timeline t = allUserTimelines.get(user);

            TreeMap<String, Integer> togetherTime = new TreeMap<>();

            t.calculateTimeSpent(togetherTime);
            int totalTime = 0;
            for (String place : togetherTime.keySet()) {
                totalTime += togetherTime.get(place);
            }

            if (totalTime < 720) {
                userIter.remove();
            }
        }

        HashSet<String> leaders = new HashSet<>();
        ArrayList<UserGroup> groups = new ArrayList<>();

        for (String user : allUserTimelines.keySet()) {
            Timeline t = allUserTimelines.get(user);
            t.compress();

            leaders.add(user);
            ArrayList<UserGroup> subgroups = new ArrayList<>();

            for (String otherUser : allUserTimelines.keySet()) {
                //don't process the user if it's already a leader
                if (!leaders.contains(otherUser)) {
                    UserGroup temp = new UserGroup();

                    temp.add(user);
                    temp.add(otherUser);

                    temp.formSharedTimeline(allUserTimelines);

                    if (temp.isEligibleGroup()) {
                        subgroups.add(temp);
//                        companions.add(otherUser);
                    }
                }
            }

            for (UserGroup ug : subgroups) {
                Timeline tl1 = ug.getSharedTimeline();
                HashSet<String> companions = new HashSet<>();
                companions.add(ug.getUsers().get(1));

                for (UserGroup otherUg : subgroups) {
                    //process if the two groups aren't the same
                    if (ug != otherUg) {
                        //form a larger group
                        Timeline tl2 = otherUg.getSharedTimeline();
                        
                        Timeline merged = tl1.merge(tl2);
                        
                        UserGroup temp = new UserGroup();
                        temp.setSharedTimeline(merged);
                        
                        if(temp.isEligibleGroup()){
                            tl1 = merged;
                            companions.add(otherUg.getUsers().get(1));
                        }
                    }
                }

                if (companions.size() >= 1) {
                    UserGroup group = new UserGroup();
                    companions.add(user);

                    for (String s : companions) {
                        group.add(s);
                    }

                    boolean groupInList = false;
                    for (UserGroup g : groups) {
                        if (g.isSupersetOf(group)) {
                            groupInList = true;
                        }
                    }

                    if (!groupInList) {

                        group.formSharedTimeline(allUserTimelines);
                        groups.add(group);
                    }

                }
            }

        }

        Collections.sort(groups);
        jsonOutput.put("total-groups", groups.size());
        jsonOutput.put("groups", processGroupsToJSON(groups));
        jsonOutput.put("status", "success");
        return jsonOutput;
    }
    
 /**
     * Detects the users that share space with the users already in the group
     *
     * @param userGroup Existing user group.
     * @param usersAndCompanions HashMap of users and their companions.
     * @return ArrayList of users (mac addresses) that are common to all users
     * in the group.
     */
    public static ArrayList<String> detectCommonUsers(UserGroup userGroup, HashMap<String, ArrayList<String>> usersAndCompanions) {
        ArrayList<String> usersInCommon = new ArrayList<>();
        //for each user group, find their people in common
        ArrayList<String> existingGroup = userGroup.getUsers();

        for (String user : existingGroup) {
            ArrayList<String> userCompanions = usersAndCompanions.get(user);
            boolean common = true;
            for (String companion : userCompanions) {
                for (String otherUser : existingGroup) {
                    ArrayList<String> otherCompanions = usersAndCompanions.get(otherUser);
                    if (!otherCompanions.contains(companion)) {
                        common = false;
                    }
                }
                if (common && !usersInCommon.contains(companion)) {
                    usersInCommon.add(companion);
                }
            }
        }

        return usersInCommon;
    }

      /**
     * Takes in the existing group and the users in common. Determines if there
     * is any overlap in their timeline, and if yes, returns a bigger group.
     *
     * @param userGroup Existing user group.
     * @param userInCommon Potential groupmate.
     * @param allUserTimelines A HashMap of all users and their timelines found
     * within the query window.
     * @param date The date that is queried by the user.
     * @return a new group consisting of the original group members and the new
     * user, null if there is no overlap in their timelines.
     */
    public static UserGroup formGroup(UserGroup userGroup, String userInCommon,
            HashMap<String, Timeline> allUserTimelines, String date) {
        //iterate through existingGroup (using iterator) to see if the user in common should be added to the group
        ArrayList<String> existingGroup = userGroup.getUsers();


        UserGroup trialGroup = new UserGroup();
        for (String user : existingGroup) {
            trialGroup.add(user);
        }
        trialGroup.add(userInCommon);

        Timeline existingTimeline = userGroup.getSharedTimeline();
        Timeline potentialUserTimeline = allUserTimelines.get(userInCommon);

        Timeline mergedTimeline = existingTimeline.merge(potentialUserTimeline);

        trialGroup.setSharedTimeline(mergedTimeline);

        if (trialGroup.isEligibleGroup()) {
            return trialGroup;
        } else {
            return null;
        }
    }

       /**
     * Processes the detected user groups into JSON format.
     *
     * @param userGroups User groups to be converted into JSON.
     * @return JSONArray containing JSONObjects that represent user groups.
     */
    public static JSONArray processGroupsToJSON(ArrayList<UserGroup> userGroups) {
        //1 group is 1 Json object, and the group will contain a json array of member and location.
        //each member is a json object 
        // {} is object [] is array
        JSONArray result = new JSONArray();

        for (UserGroup userGroup : userGroups) {
           
            result.add(userGroup.toJSON());
        }
        return result; //should return jsonArray
    }
}

