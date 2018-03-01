/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import net.minidev.json.JSONArray;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * HeatmapDAO consists of two methods, getHeatmap method which return JSONArray
 * object and getCrowdDensity method which return an integer between 0 and 6,
 * depending on the number of people in each semantic place
 *
 * @author User
 */
public class HeatmapDAO {

    /**
     * Generates heatmap for the requested floor and query window.
     *
     * @param floor floor of SIS building to be queried
     * @param date date to be queried
     * @return JSONArray of Heatmap with crowd density of a specified floor in
     * the SIS building given a particular date and time
     */
    public static JSONArray getHeatmap(String floor, String date) {
        JSONArray heatmap = new JSONArray();
        date = date.replace("T", " ");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime queryWindowEnd = formatter.parseDateTime(date);
        DateTime queryWindowStart = queryWindowEnd.minusMinutes(15);
        queryWindowEnd = queryWindowEnd.minusSeconds(1);

        try {
            Connection conn = ConnectionManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("select j.semantic_place as place, count(A.mac_address) "
                    + "as count_address from location_lookup j left outer join (select max(timestamp), mac_address, "
                    + "l.location_id, semantic_place from location l, location_lookup ll where l.location_id = ll.location_id "
                    + "and timestamp between ? and ? group by mac_address) as A on A.semantic_place = j.semantic_place "
                    + "and A.location_id = j.location_id group by j.semantic_place");

            stmt.setString(1, queryWindowStart.toString(formatter));
            stmt.setString(2, queryWindowEnd.toString(formatter));

            ResultSet rs = stmt.executeQuery();
            System.out.println(rs.getStatement());
            while (rs.next()) {
                String location = rs.getString("place");

                if (location.contains(floor)) {
                    JSONObject semPlace = new JSONObject();

                    int countMacAddress = Integer.parseInt(rs.getString("count_address"));

                    semPlace.put("semantic-place", location);
                    semPlace.put("num-people", countMacAddress);
                    semPlace.put("crowd-density", getCrowdDensity(countMacAddress));
                    heatmap.add(semPlace);
                }

            }

            ConnectionManager.close(conn, stmt, rs);

        } catch (SQLException ex) {
            Logger.getLogger(ReportDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return heatmap;
    }

    /**
     * The getCrowdDensity method calculates crowd density based on the number
     * of people that is passed in.
     *
     * @param count count of number of people in each semantic place
     * @return an integer between 0 and 6
     */
    public static int getCrowdDensity(int count) {
        if (count == 0) {
            return 0;
        } else if (count <= 2) {
            return 1;
        } else if (count <= 5) {
            return 2;
        } else if (count <= 10) {
            return 3;
        } else if (count <= 20) {
            return 4;
        } else if (count <= 30) {
            return 5;
        } else {
            return 6;
        }
    }
}
