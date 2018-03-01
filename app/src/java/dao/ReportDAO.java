package dao;

import entity.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * ReportDAO consists of methods for Basic Location Reports - Top-k popular
 * places, Top k companions, Top-k next places
 *
 * @author User
 */
public class ReportDAO {

    /**
     * This method retrieves all students in SIS at the specified timeframe.
     *
     *
     * @param date date to be queried
     * @return student object of mac address, email and gender.
     */
    private static final String DATE_TIME_QUERY_AS_STRING = "(day(timestamp)=? and month(timestamp) = ? "
            + "and year(timestamp) = ?) and time(timestamp) between ? and ?";

    /**
     *
     * @param date
     * @return
     */
    public static StudentDAO retrieveAllStudentsInSISInTimeframe(String date) {
        StudentDAO studentDao = new StudentDAO();

        date = date.replace("T", " ");
        org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryWindowEnd = formatter.parseDateTime(date);
        DateTime queryWindowStart = queryWindowEnd.minusMinutes(15);
        queryWindowEnd = queryWindowEnd.minusSeconds(1);

        try {
            Connection conn = ConnectionManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("select distinct d.mac_address as address, email, gender "
                    + "from location l, demographics d, location_lookup ll where l.mac_address = d.mac_address and "
                    + "l.location_id = ll.location_id and timestamp between ? and ? and semantic_place like "
                    + "'%SIS%' group by address");

            stmt.setString(1, queryWindowStart.toString(formatter));
            stmt.setString(2, queryWindowEnd.toString(formatter));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String address = rs.getString("address");
                String email = rs.getString("email");
                String gender = rs.getString("gender");

                studentDao.addStudent(new Student(address, email, gender));
            }

            ConnectionManager.close(conn, stmt, rs);

        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return studentDao;

    }

    /**
     * This method finds the top-k popular places in the whole SIS building at a
     * specified time.
     *
     * @param date date to be queried
     * @return JSONArray of top-k popular places with user count.
     */
    public static JSONArray getTopKPopularPlaces(String date) {
        JSONArray result = new JSONArray();

        date = date.replace("T", " ");
        org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryWindowEnd = formatter.parseDateTime(date);
        DateTime queryWindowStart = queryWindowEnd.minusMinutes(15);
        queryWindowEnd = queryWindowEnd.minusSeconds(1);

        try {
            Connection conn = ConnectionManager.getConnection();
//            PreparedStatement stmt = conn.prepareStatement("select semantic_place as place, "
//                    + "count(distinct mac_address) as count_address from location as l "
//                    + "inner join location_lookup as ll on l.location_id = ll.location_id where "
//                    + DATE_TIME_QUERY_AS_STRING + " group by place order by count_address desc");

            PreparedStatement stmt = conn.prepareStatement("select semantic_place as place, "
                    + "count(distinct mac_address) as count_address from (select timestamp, mac_address, "
                    + "semantic_place from location as l inner join location_lookup as ll on "
                    + "l.location_id = ll.location_id) as B, (select mac_address as mac, max(timestamp) "
                    + "as time from location where timestamp between ? and ? group by mac_address) as A where A.mac = B.mac_address and A.time = B.timestamp "
                    + "group by place order by count_address desc");

            stmt.setString(1, queryWindowStart.toString(formatter));
            stmt.setString(2, queryWindowEnd.toString(formatter));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject row = new JSONObject();
                String location = rs.getString("place");
                String count = rs.getString("count_address");
                int countMacAddress = Integer.parseInt(count);

                row.put("semantic-place", location);
                row.put("count", countMacAddress);
                result.add(row);
            }

            ConnectionManager.close(conn, stmt, rs);

        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * This method returns the top-k next places that users located at a
     * semantic place (passed in as an argument) are likely to visit in the next
     * 15 minutes.
     *
     * An example query can be, 'where are the top 3 places that users located
     * at seminar room 1 at 11:15 on March 1st visited next'. From this, a user
     * can understand the potential relationship between the two different
     * places in SIS building.
     *
     * @param semanticPlace Semantic place to be queried.
     * @param date
     * @return JSONObject containing a TreeMap of semantic places sorted by user
     * count.
     */
    public static JSONObject getTopKNextPlaces(String semanticPlace, String date) {
        JSONObject jsonOutput = new JSONObject();

        ArrayList<String> usersInQueriedPlace = getBeforeUsers(semanticPlace, date);

        jsonOutput.put("total-users", usersInQueriedPlace.size());

        HashMap<String, ArrayList<String>> nextPlaceMap = retrieveNextPlacesVisitedByUsers(usersInQueriedPlace, date);
        
        System.out.println("nextPlace : " + nextPlaceMap );
        //otherwise, sort the places by the user count
        TreeMap<Integer, ArrayList<String>> placesSortedByCount = new TreeMap<>(Collections.reverseOrder());

        int totalNextUserCount = 0;

        //each semantic place in the map
        for (String semPlace : nextPlaceMap.keySet()) {
            //count the number of users in the semantic place
            int count = nextPlaceMap.get(semPlace).size();
            totalNextUserCount += count;

            //if the map contains a place with the same number of people,
            //add this place to the arraylist storing the semantic places
            if (placesSortedByCount.containsKey(count)) {
                ArrayList<String> semPlaces = placesSortedByCount.get(count);
                semPlaces.add(semPlace);
                
                System.out.println(semPlace);
            } //else the map doesn't already have a place with this number of users,
            //so instantiate a new arraylist and add the semantic place, 
            //then put into the map
            else {
                ArrayList<String> semPlaces = new ArrayList<>();
                semPlaces.add(semPlace);
                placesSortedByCount.put(count, semPlaces);
            }
        }
        jsonOutput.put("total-next-place-users", totalNextUserCount);
        jsonOutput.put("results", placesSortedByCount);
        return jsonOutput;
    }

    /**
     * This method returns the users that visited the queried semantic place in
     * the given time frame.
     *
     * @param semanticPlace Semantic place to be queried.
     * @param date
     * @return An ArrayList of mac addresses of the users that visited the
     * queried semantic place.
     */
    public static ArrayList<String> getBeforeUsers(String semanticPlace, String date) {
        ArrayList<String> users = new ArrayList<>();

        date = date.replace("T", " ");
        org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryWindowEnd = formatter.parseDateTime(date);
        DateTime queryWindowStart = queryWindowEnd.minusMinutes(15);
        queryWindowEnd = queryWindowEnd.minusSeconds(1);


        try {
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select mac from location l, location_lookup ll, "
                    + "(select mac_address as mac, max(timestamp) as time from location where timestamp "
                    + "between ? and ? group by mac_address) as A "
                    + "where l.location_id = ll.location_id and semantic_place = ? and "
                    + "A.time = timestamp and l.mac_address = mac");

            stmt.setString(1, queryWindowStart.toString(formatter));
            stmt.setString(2, queryWindowEnd.toString(formatter));
            stmt.setString(3, semanticPlace);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(rs.getString(1));
            }

            ConnectionManager.close(conn, stmt, rs);
        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return users;
    }

    /**
     * This method returns the places visited by the users that are passed in as
     * an argument.
     *
     * The return variable is a HashMap with the semantic place as the key, and
     * an ArrayList of mac addresses. This keeps track of each semantic place
     * along with the users located in each place. The map will be empty if the
     * queried users do not visit any places in this queried time frame (i.e.
     * they have left the building).
     *
     * @param previousUsers An ArrayList containing the users to be queried.
     * @param date
     * @return The map of semantic places and the users in the semantic place.
     */
    public static HashMap<String, ArrayList<String>> retrieveNextPlacesVisitedByUsers(ArrayList<String> previousUsers, String date) {
        HashMap<String, ArrayList<String>> semanticPlacesAndUsers = new HashMap<>();

        date = date.replace("T", " ");
        org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        DateTime queryWindowStart = formatter.parseDateTime(date);
        DateTime queryWindowEnd = queryWindowStart.plusMinutes(14).plusSeconds(59);


        try {
            Connection conn = ConnectionManager.getConnection();
            for (String macAddress : previousUsers) {
                //location traces for each user
                ArrayList<ArrayList<String>> locationTraces = new ArrayList<>();
                PreparedStatement stmt = conn.prepareStatement("select mac_address, semantic_place, timestamp"
                        + " from location l, location_lookup ll where l.location_id = ll.location_id and"
                        + " mac_address = ? and timestamp between ? and ? order by timestamp");

                stmt.setString(1, macAddress);
                stmt.setString(2, queryWindowStart.toString(formatter));
                stmt.setString(3, queryWindowEnd.toString(formatter));

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ArrayList<String> locationTrace = new ArrayList<>();
                    locationTrace.add(rs.getString(1)); //mac address [0]
                    locationTrace.add(rs.getString(2)); //semantic place [1]
                    locationTrace.add(rs.getString(3).substring(0, rs.getString(3).lastIndexOf("."))); //timestamp [2]
                    
                    locationTraces.add(locationTrace);
                }

                ConnectionManager.close(stmt, rs);

                //get last valid semantic place of the user
                String usersSemanticPlace = getLastValidSemanticPlace(locationTraces, queryWindowStart.toString(formatter), queryWindowEnd.toString(formatter));
                //if the user's semantic place is not null (there is a valid semantic place)
                //and the hashmap doesn't already have the semantic place logged,
                //create an arraylist to keep track of the users in the semantic place
                //and add it to the hashmap
                if (usersSemanticPlace != null && !usersSemanticPlace.equals("") && !semanticPlacesAndUsers.containsKey(usersSemanticPlace)) {
                    ArrayList<String> users = new ArrayList<>();
                    users.add(macAddress);
                    semanticPlacesAndUsers.put(usersSemanticPlace, users);
                } //if the user's semantic place is not null (there is a valid semantic place)
                //and the hashmap already has the semantic place logged,
                //get the arraylist that keeps track of the users in the semantic place
                //and add the user to the arraylist
                else if (usersSemanticPlace != null && !usersSemanticPlace.equals("") && semanticPlacesAndUsers.containsKey(usersSemanticPlace)) {
                    ArrayList<String> users = semanticPlacesAndUsers.get(usersSemanticPlace);
                    users.add(macAddress);
                }
            }
            
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return semanticPlacesAndUsers;
    }

    /**
     * This method retrieves the last valid semantic place from a list of
     * location traces, where a semantic place is considered valid if the
     * minimum and maximum timestamp logged at a semantic place is more than 5
     * minutes apart.
     *
     * @param locationTraces An ArrayList location traces (which are themselves
     * stored as ArrayList).
     * @return Returns the last valid semantic place, null if no valid semantic
     * place.
     */
    private static String getLastValidSemanticPlace(ArrayList<ArrayList<String>> locationTraces, String queryWindowStart, String queryWindowEnd) {
        //create formatter for LocalDateTime to convert table data to LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //keep track of semantic place and the min/max timestamp and the duration spent there
        //String prevSemPlace = "";
        LocalDateTime minTimestamp = LocalDateTime.parse(queryWindowStart, formatter);;
        LocalDateTime maxTimestamp = null;
        int durationInSeconds = 0;

        //list of valid semantic places
        ArrayList<String> semanticPlaces = new ArrayList<>();

        if (locationTraces.size() == 1) {
            ArrayList<String> locationTrace = locationTraces.get(0);
            String currSemPlace = locationTrace.get(1);
            String timestamp = locationTrace.get(2);
            minTimestamp = LocalDateTime.parse(timestamp, formatter);
            maxTimestamp = LocalDateTime.parse(queryWindowEnd, formatter).plusSeconds(1);

            int minTimestampInSeconds = (minTimestamp.getHour() * 60 * 60) + (minTimestamp.getMinute() * 60) + minTimestamp.getSecond();
            int maxTimestampInSeconds = (maxTimestamp.getHour() * 60 * 60) + (maxTimestamp.getMinute() * 60) + maxTimestamp.getSecond();

            durationInSeconds = maxTimestampInSeconds - minTimestampInSeconds;

            if (durationInSeconds >= (5 * 60)) {
                semanticPlaces.add(currSemPlace);
            }

        } else {
            String lastSemPlace = "";
            for (int i = 0; i < locationTraces.size(); i++) {
                if (i == 0) {
                    minTimestamp = LocalDateTime.parse(queryWindowStart, formatter);
                    continue;
                }
                ArrayList<String> al = locationTraces.get(i);

                //semantic place
                String currSemPlace = al.get(1);
                lastSemPlace = al.get(1);
                String timestamp = al.get(2);

                ArrayList<String> prevTrace = locationTraces.get(i - 1);
                String prevSemPlace = prevTrace.get(1);

                //if current semantic place doesn't match the previous semantic place,
                //calculate the duration spent in the previous semantic place using min and max timestamp
                if (!currSemPlace.equals(prevSemPlace)) {

                    maxTimestamp = LocalDateTime.parse(timestamp, formatter);

                    int minTimestampInSeconds = (minTimestamp.getHour() * 60 * 60) + (minTimestamp.getMinute() * 60) + minTimestamp.getSecond();
                    int maxTimestampInSeconds = (maxTimestamp.getHour() * 60 * 60) + (maxTimestamp.getMinute() * 60) + maxTimestamp.getSecond();

                    durationInSeconds = maxTimestampInSeconds - minTimestampInSeconds;

                    if (durationInSeconds >= (5 * 60)) {
                        semanticPlaces.add(prevSemPlace);
                    }

                    minTimestamp = LocalDateTime.parse(timestamp, formatter);
                }
            }

            //handle the last location
            maxTimestamp = LocalDateTime.parse(queryWindowEnd, formatter).plusSeconds(1);

            int minTimestampInSeconds = (minTimestamp.getHour() * 60 * 60) + (minTimestamp.getMinute() * 60) + minTimestamp.getSecond();
            int maxTimestampInSeconds = (maxTimestamp.getHour() * 60 * 60) + (maxTimestamp.getMinute() * 60) + maxTimestamp.getSecond();

            durationInSeconds = maxTimestampInSeconds - minTimestampInSeconds;

            if (durationInSeconds >= (5 * 60)) {
                semanticPlaces.add(lastSemPlace);
            }
        }

        //at this point there should be locations in the list of valid semantic places
        //if there are no valid semantic places, return null
        //else return the last semantic place in the arraylist
        if (semanticPlaces.isEmpty()) {
            return null;
        } else {
            return semanticPlaces.get(semanticPlaces.size() - 1);
        }
    }

    /**
     * This method retrieves the semantic place from location_lookup
     *
     * @return Returns a list of semantic places.
     */
    public static LinkedHashSet<String> retrieveSemanticPlacesFromDatabase() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        try {
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select distinct semantic_place as place from location_lookup");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("place"));
            }
            ConnectionManager.close(conn, stmt, rs);
        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * This method retrieves users based on the mac address from location table.
     *
     * @return Returns list of users' mac addresses.
     */
    public static ArrayList<String> retrieveUsersFromDatabase() {
        ArrayList<String> result = new ArrayList<>();
        try {
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select distinct(mac_address) as address from location");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("address"));
            }

            ConnectionManager.close(conn, stmt, rs);

        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
