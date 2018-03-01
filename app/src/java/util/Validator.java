/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import dao.ConnectionManager;
import is203.JWTException;
import is203.JWTUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author Gerald
 */
public class Validator {

    /**
     * HashSet of locationIdList, which represents the locationID.
     */
    private static HashSet<Integer> locationIdList = new HashSet<>();

    /**
     * Clears the existing LocationID list when bootstrapping.
     */
    public static void clearLocationIdList() {
        locationIdList = new HashSet<>();
    }

    /**
     * This method checks if LocationIDList is empty. If empty, returns false.
     *
     * @return Boolean result if LocationIDList is empty
     */
    public static boolean locationIdListIsEmpty() {
        return locationIdList.size() == 0;
    }

    /**
     * This method gets the LocationID from the locationLookUp table.
     */
    public static void initialiseLocations() {
        try {
            Connection conn = ConnectionManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select location_id from location_lookup");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                locationIdList.add(Integer.parseInt(rs.getString("location_id")));
            }

            ConnectionManager.close(conn, stmt, rs);
        } catch (SQLException ex) {
            Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method validates a string of values from Demographics fields.
     *
     * @param values The values from Demographics table fields.
     * @return JSONArray of the errors from the validation. Returns empty array
     * if no errors.
     */
    public static JSONArray validateDemographics(String[] values) {
        ArrayList<String> errors = new ArrayList<>();
        String mac = values[0];
        String name = values[1];
        String pwd = values[2];
        String email = values[3];
        String gender = values[4];

        if (mac.length() == 0) {
            errors.add("blank mac address");
        }
        if (name.length() == 0) {
            errors.add("blank name");
        }
        if (pwd.length() == 0) {
            errors.add("blank password");
        }
        if (email.length() == 0) {
            errors.add("blank email");
        }
        if (gender.length() == 0) {
            errors.add("blank gender");
        }

        if (!errors.isEmpty()) {
            JSONArray errorsJSON = new JSONArray();

            for (String s : errors) {
                errors.add(s);
            }
            return errorsJSON;
        }

        if (!isValidSHA1(mac)) {
            errors.add("invalid mac address");
        }
        if (pwd.length() < 8 || pwd.contains(" ")) {
            errors.add("invalid password");
        }

        if (!email.matches("[A-Za-z0-9.]*201[3-7][@](economics|sis|accountancy|business|law|socsc).smu.edu.sg")) {
            errors.add("invalid email");
        }

        if (!(gender.toUpperCase().equals("M") || gender.toUpperCase().equals("F"))) {
            errors.add("invalid gender");
        }

        if (!errors.isEmpty()) {
            Collections.sort(errors);

            JSONArray errorsJSON = new JSONArray();

            for (String s : errors) {
                errorsJSON.add(s);
            }
            return errorsJSON;
        }
        return new JSONArray();
    }

    /**
     * This method validates a string of values from Location LookUp fields. 
     *
     * @param values The values from the Location LookUp fields.
     * @return JSONArray of the errors from the validation. Returns empty array
     * if no errors.
     */
    public static JSONArray validateLocationLookup(String[] values) {
        ArrayList<String> errors = new ArrayList<>();
        String loc = values[0];
        String place = values[1];

        //check blank
        if (loc.length() == 0) {
            errors.add("blank location id");
        }
        if (place.length() == 0) {
            errors.add("blank semantic place");
        }

        if (!errors.isEmpty()) {
            JSONArray errorsJSON = new JSONArray();

            for (String s : errors) {
                errors.add(s);
            }
            return errorsJSON;
        }

        try {
            int locationId = Integer.parseInt(values[0]);
            if (locationId < 0) {
                errors.add("invalid location id");
            } else {
                locationIdList.add(locationId);
            }
        } catch (NumberFormatException nfe) {
            errors.add("invalid location id");
        }

        //validate semantic place. what is considered a valid place.
        //Should be of format "SMUSISL<level number><specific location>" or "SMUSISB<level number><specific location>"
        //use regex (SMUSIS[LB][1-5][A-Z0-9]*)
        if (!place.matches("SMUSIS[L][1-5].*") && !place.matches("SMUSIS[B][1].*")) {
            errors.add("invalid semantic place");
        }

        if (!errors.isEmpty()) {
            Collections.sort(errors);

            JSONArray errorsJSON = new JSONArray();

            for (String s : errors) {
                errorsJSON.add(s);
            }
            return errorsJSON;
        }
        return new JSONArray();
    }

    /**
     * This method validates a string of values from the Location fields.
     *
     * @param values The values of Location table fields.
     * @return JSONArray of the errors from the validation. Returns empty array
     * if there is no errors.
     */
    public static JSONArray validateLocation(String[] values) {
        ArrayList<String> errors = new ArrayList<>();
        String time = values[0];
        String mac = values[1];
        String loc = values[2];

        //check for blankness
        if (time.length() == 0) {
            errors.add("blank timestamp");
        }
        if (mac.length() == 0) {
            errors.add("blank mac address");
        }
        if (loc.length() == 0) {
            errors.add("blank location");
        }

        if (!errors.isEmpty()) {
            JSONArray errorsJSON = new JSONArray();

            for (String s : errors) {
                errors.add(s);
            }
            return errorsJSON;
        }

        //validate time
        if (!time.matches("20[0-1][0-9]-[0-9][0-9]-[0-9][0-9] [0-2][0-9]:[0-5][0-9]:[0-5][0-9]")) {
            errors.add("invalid timestamp");
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setLenient(false);
        try {
            df.parse(time);
        } catch (ParseException e) {
            errors.add("invalid timestamp");
        }

        //validate mac address
        if (!isValidSHA1(mac)) {
            errors.add("invalid mac address");
        }

        //validate location id
        int locationId = 0;
        try {
            locationId = Integer.parseInt(loc);
            if (!locationIdList.contains(locationId)) {
                errors.add("invalid location");
            }
        } catch (NumberFormatException nfe) {
            errors.add("invalid location");
        }

        if (!errors.isEmpty()) {
            Collections.sort(errors);

            JSONArray errorsJSON = new JSONArray();

            for (String s : errors) {
                errorsJSON.add(s);
            }
            return errorsJSON;
        }
        return new JSONArray();
    }

    /**
     * This method takes a string and checks if it is a valid SHA 1 string. If
     * the string is invalid, return false.
     *
     * @param s SHA 1 string
     * @return Boolean result of the valid SHA 1 string.
     */
    public static boolean isValidSHA1(String s) {
        return s.matches("[a-fA-F0-9]{40}");
    }

//    public static int checkDuplicateRow(TreeMap<Integer, String> data, String rowData) {
//
//        rowData = rowData.substring(0, rowData.lastIndexOf(","));
//
//        Set<Integer> keys = data.keySet();
//        for (int i : keys) {
//            String s = data.get(i);
//            if (s != null) {
//                s = s.substring(0, s.lastIndexOf(","));
//                s = s.substring(0, s.lastIndexOf(","));
//                if (s.equals(rowData)) {
//                    return i; //new row number
//                }
//            }
//
//        }
//        return -1;
//    }
    /**
     * This method takes in the fieldName, input and a JSONObject. This method
     * checks for missing or blank fields.
     *
     * @param fieldname The field to be validated.
     * @param input The actual input.
     * @param jsonOutput JSONOutput that contains messages, according to Wiki's
     * requirements.
     */
    public static void validateField(String fieldname, String input, JSONObject jsonOutput) {
        JSONArray messages = new JSONArray();
        if (jsonOutput.containsKey("messages")) {
            messages = (JSONArray) jsonOutput.get("messages");
        }
        if (input == null) { //checks whether the field is missing
            messages.add("missing " + fieldname);
        } else if (input.length() == 0) { //checks for a blank field
            messages.add("blank " + fieldname);
        }
        if (messages.size() > 0) {
            jsonOutput.put("messages", messages);
        }
    }

    /**
     * This method validates the input date. If date is invalid, return false.
     *
     * @param date Input date
     * @return Boolean result of the validated date.
     */
    public static boolean validateDate(String date) {
        boolean valid = date.matches("([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2})");
        if (valid) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setLenient(false);
            try {
                df.parse(date);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * This method takes in a token and JSONObject. Checks if the token is
     * valid, missing or blank.
     *
     * @param token Generated token
     * @param jsonOutput JSONOutput that contains error messages, according to
     * Wiki's requirements.
     */
    public static void validateToken(String token, JSONObject jsonOutput) {
        JSONArray messages = new JSONArray();
        String sharedSecret = "qwertyuiopasdfgh";
        if (token != null && token.length() != 0) {
            try {
                String verifiedUsername = JWTUtility.verify(token, sharedSecret);
                if (verifiedUsername == null) {
                    messages.add("invalid token");
                }
            } catch (JWTException e) {
                messages.add("invalid token");
            }
        } else {
            messages.add("invalid token");
        }
        if (messages.size() > 0) {
            jsonOutput.put("messages", messages);
        }
    }

    /**
     * This method validates semantic place. Semantic place is entered as a
     * parameter, and if semantic place is not valid, return false.
     *
     * @param semPlace Actual semantic place.
     * @return Boolean result of the validated semantic place.
     */
    public static boolean validateSemanticPlace(String semPlace) {
        return semPlace.matches("SMUSIS[L][1-5].*") || semPlace.matches("SMUSIS[B][1].*");
    }
}
