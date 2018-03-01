/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import util.Validator;

/**
 *
 * @author Kingston Poh
 */
public class BootstrapDAO {

    /**
     * This method takes in a JSONObject, locationLookupPath, demographicsPath
     * and locationPath to bootstrap new data.
     *
     * @param jsonOutput JSONObject that contains bootstrap records, according
     * to Wiki's requirements.
     * @param locationLookupPath The path of the location lookup file.
     * @param demographicsPath The path of the demographics file.
     * @param locationPath The path of the location file.
     */
    public static void bootstrap(JSONObject jsonOutput, String locationLookupPath, String demographicsPath, String locationPath) {
        try {
            dropTables();
            createTables();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        jsonOutput.put("num-record-loaded", new JSONArray());
        jsonOutput.put("error", new JSONArray());

        Validator.clearLocationIdList();
        updateDemographics(jsonOutput, demographicsPath);
        updateLocationLookup(jsonOutput, locationLookupPath);
        updateLocation(jsonOutput, locationPath);
    }

    /**
     * This method takes in a JSONObject and the filepath of the CSV file to be
     * read. This method handles update for both demographics and location
     * table.
     *
     * @param jsonOutput JSONObject that contains bootstrap records, according
     * to Wiki's requirements.
     * @param filepath FilePath of the CSV file to be read.
     */
    public static void update(JSONObject jsonOutput, String filepath) {
        if (!jsonOutput.containsKey("num-record-loaded")) {
            jsonOutput.put("num-record-loaded", new JSONArray());
        }
        if (!jsonOutput.containsKey("error")) {
            jsonOutput.put("error", new JSONArray());
        }

        if (filepath.contains("demographics")) {
            updateDemographics(jsonOutput, filepath);
        } else if (filepath.contains("location")) {
            //if(Validator.locationIdListIsEmpty()){
            Validator.initialiseLocations();
            //}
            updateLocation(jsonOutput, filepath);
        }
    }

    /**
     * This method handles the dropping of locationLookup, demographics and
     * location tables.
     *
     * @throws SQLException
     */
    public static void dropTables() throws SQLException {
        Connection conn = ConnectionManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE location_lookup")) {
            stmt.executeUpdate();

        } catch (SQLException ex) {
            //Logger.getLogger(CSVUnpacker.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE demographics")) {
            stmt.executeUpdate();

        } catch (SQLException ex) {
            //Logger.getLogger(CSVUnpacker.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE location")) {
            stmt.executeUpdate();

        } catch (SQLException ex) {
            //Logger.getLogger(CSVUnpacker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method creates LocationLookup, Demographics and Location tables.
     *
     * @throws SQLException
     */
    public static void createTables() throws SQLException {

        Connection conn = ConnectionManager.getConnection();

        PreparedStatement stmt = conn.prepareStatement("CREATE TABLE location_lookup(location_id char(10) NOT NULL,"
                + "semantic_place varchar(30) NOT NULL,PRIMARY KEY(location_id)) ENGINE=MYISAM");
        stmt.executeUpdate();
        stmt.close();

        stmt = conn.prepareStatement("CREATE TABLE demographics(mac_address char(40) NOT NULL,"
                + "name varchar(30) NOT NULL,password varchar(10) NOT NULL,"
                + "email varchar(60) NOT NULL,gender char(1) NOT NULL,PRIMARY KEY (mac_address)) ENGINE=MYISAM");
        stmt.executeUpdate();
        stmt.close();

        stmt = conn.prepareStatement("CREATE TABLE location (timestamp datetime NOT NULL,"
                + "mac_address char(40) NOT NULL,location_id char(10) NOT NULL,"
                + "PRIMARY KEY (timestamp,mac_address)) ENGINE=MYISAM;");
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * This method takes in a JSONObject and filePath of the CSV file to be
     * read. This method handles the update of the LocationLookUp table.
     *
     * @param jsonOutput JSONObject that contains bootstrap records, according
     * to Wiki's requirements.
     * @param filePath FilePath of the CSV file to be read.
     */
    private static void updateLocationLookup(JSONObject jsonOutput, String filePath) {
        Reader in;

        filePath = filePath.replace("\\", "/");
        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "-validated.csv";

        FileWriter fileWriter;
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withHeader("location-id", "semantic-place");
        CSVPrinter out;

        Connection conn = null;

        try {
            in = new FileReader(filePath);
            fileWriter = new FileWriter(newFilePath);
            out = new CSVPrinter(fileWriter, csvFileFormat);

            Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);

            int counter = 2;
            int validRowCount = 0;

            JSONArray errorArray = (JSONArray) jsonOutput.get("error");//array of errors from the jsonOutput object

            for (CSVRecord record : records) {
                String id = record.get("location-id").trim();
                String place = record.get("semantic-place").trim();

                JSONArray errorMsgs = Validator.validateLocationLookup(new String[]{id, place});
                if (errorMsgs.isEmpty()) {
                    validRowCount++;
                    out.printRecord(new Object[]{id, place});
                } else {
                    JSONObject error = new JSONObject();

                    error.put("file", "location-lookup.csv");
                    error.put("line", counter);
                    error.put("messages", errorMsgs);

                    errorArray.add(error);
                }
                counter++;
            }
            out.close();

            JSONArray recordLoadedArray = (JSONArray) jsonOutput.get("num-record-loaded");

            JSONObject locationLookupRecords = new JSONObject();
            locationLookupRecords.put("location-lookup.csv", validRowCount);
            recordLoadedArray.add(locationLookupRecords);

            conn = ConnectionManager.getConnection();

            String loadStatement = "LOAD DATA LOCAL INFILE ? "
                    + "INTO TABLE location_lookup "
                    + "FIELDS TERMINATED BY ',' "
                    + "LINES TERMINATED BY '\\r\\n' "
                    + "IGNORE 1 LINES";

            PreparedStatement stmt = conn.prepareStatement(loadStatement);
            stmt.setString(1, newFilePath);
            stmt.executeUpdate();
            in.close();
            ConnectionManager.close(conn, stmt);

        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SQLException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method takes in a JSONObject and filePath of the CSV file to be
     * read. This method updates the demographic table.
     *
     * @param jsonOutput JSONObject that contains bootstrap records, according
     * to Wiki's requirements.
     * @param filePath FilePath of the CSV file to be read.
     */
    private static void updateDemographics(JSONObject jsonOutput, String filePath) {
        Reader in;

        filePath = filePath.replace("\\", "/");
        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "-validated.csv";

        FileWriter fileWriter;
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withHeader("mac-address", "name", "password", "email", "gender");
        CSVPrinter out;

        Connection conn = null;

        try {
            in = new FileReader(filePath);
            fileWriter = new FileWriter(newFilePath);
            out = new CSVPrinter(fileWriter, csvFileFormat);

            Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);

            int counter = 2;
            int validRowCount = 0;

            JSONArray errorArray = (JSONArray) jsonOutput.get("error");//array of errors from the jsonOutput object
            for (CSVRecord record : records) {
                String mac = record.get("mac-address").trim();
                String name = record.get("name").trim();
                String pwd = record.get("password").trim();
                String email = record.get("email").trim();
                String gender = record.get("gender").trim();

                JSONArray errorMsgs = Validator.validateDemographics(new String[]{mac, name, pwd, email, gender});
                if (errorMsgs.isEmpty()) {
                    validRowCount++;
                    out.printRecord(new Object[]{mac, name, pwd, email, gender});
                } else {
                    JSONObject error = new JSONObject();

                    error.put("file", "demographics.csv");
                    error.put("line", counter);
                    error.put("messages", errorMsgs);

                    errorArray.add(error);
                }

                counter++;
            }
            out.close();

            JSONArray recordLoadedArray = (JSONArray) jsonOutput.get("num-record-loaded");

            JSONObject locationLookupRecords = new JSONObject();
            locationLookupRecords.put("demographics.csv", validRowCount);
            System.out.println(locationLookupRecords);
            recordLoadedArray.add(locationLookupRecords);

            conn = ConnectionManager.getConnection();

            String loadStatement = "LOAD DATA LOCAL INFILE ? "
                    + "INTO TABLE demographics "
                    + "FIELDS TERMINATED BY ',' "
                    + "LINES TERMINATED BY '\\r\\n' "
                    + "IGNORE 1 LINES";

            PreparedStatement stmt = conn.prepareStatement(loadStatement);
            stmt.setString(1, newFilePath);
            stmt.executeUpdate();
            in.close();
            ConnectionManager.close(conn, stmt);

        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SQLException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method takes in a JSONObject and filePath of the CSV file to be
     * read. This method updates the Location table and checks for duplicate rows.
     *
     * @param jsonOutput JSONObject that contains bootstrap records, according
     * to Wiki's requirements.
     * @param filePath FilePath of the CSV file to be read.
     */
    private static void updateLocation(JSONObject jsonOutput, String filePath) {
        Reader in;

        filePath = filePath.replace("\\", "/");
        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "-validated.csv";

        FileWriter fileWriter;
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withHeader("timestamp", "mac-address", "location-id");
        CSVPrinter out;

        Connection conn = null;

        HashSet<String> allLocations = new HashSet<>();

        try {
            conn = ConnectionManager.getConnection();

            PreparedStatement stmt = conn.prepareStatement("SELECT timestamp, mac_address FROM location");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String timestamp = rs.getString("timestamp");
                timestamp = timestamp.substring(0, timestamp.lastIndexOf("."));
                String macAddress = rs.getString("mac_address");

                allLocations.add(timestamp + "," + macAddress);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            in = new FileReader(filePath);
            fileWriter = new FileWriter(newFilePath);
            out = new CSVPrinter(fileWriter, csvFileFormat);

            Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);

            int counter = 2;

            JSONArray errorArray = (JSONArray) jsonOutput.get("error");//array of errors from the jsonOutput object
            TreeMap<Integer, JSONObject> errorMap = new TreeMap<>();
            TreeMap<Integer, ArrayList<String>> allDataRowsMap = new TreeMap<>(Collections.reverseOrder());

            for (CSVRecord record : records) {
                String time = record.get("timestamp").trim();
                String mac = record.get("mac-address").trim();
                String location_id = record.get("location-id").trim();

                JSONArray errorMsgs = Validator.validateLocation(new String[]{time, mac, location_id});
                if (errorMsgs.isEmpty()) {

                    ArrayList<String> rowData = new ArrayList<>();

                    rowData.add(time + "," + mac);
                    rowData.add(location_id);
                    allDataRowsMap.put(counter, rowData);

                } else {
                    JSONObject error = new JSONObject();

                    error.put("file", "location.csv");
                    error.put("line", counter);
                    error.put("messages", errorMsgs);

                    errorMap.put(counter, error);

                }
                counter++;
            }

            HashMap<String, String> filteredMap = new HashMap<>();

            for (int i : allDataRowsMap.keySet()) {
                ArrayList<String> al = allDataRowsMap.get(i);

                String timeStampMacAddress = al.get(0);
                String locationId = al.get(1);
                if (!filteredMap.containsKey(timeStampMacAddress) && !allLocations.contains(timeStampMacAddress)) { //if filteredMap does not contain timestamp-macaddress
                    filteredMap.put(timeStampMacAddress, locationId);
                } else {
                    JSONArray errorMsg = new JSONArray();
                    errorMsg.add("duplicate row");

                    JSONObject error = new JSONObject();

                    error.put("file", "location.csv");
                    error.put("line", i);
                    error.put("messages", errorMsg);

                    errorMap.put(i, error);
                }
            }

            for (int i : errorMap.keySet()) {
                errorArray.add(errorMap.get(i));
            }

            int validRowCount = 0;
            for (String key : filteredMap.keySet()) {
                validRowCount++;
                out.printRecord(new String[]{key.substring(0, key.indexOf(",")), key.substring(key.indexOf(",") + 1), filteredMap.get(key)});
            }
            out.close();

            JSONArray recordLoadedArray = (JSONArray) jsonOutput.get("num-record-loaded");

            JSONObject locationLookupRecords = new JSONObject();
            locationLookupRecords.put("location.csv", validRowCount);
            recordLoadedArray.add(locationLookupRecords);

            conn = ConnectionManager.getConnection();

            String loadStatement = "LOAD DATA LOCAL INFILE ? "
                    + "INTO TABLE location "
                    + "FIELDS TERMINATED BY ',' "
                    + "LINES TERMINATED BY '\\r\\n' "
                    + "IGNORE 1 LINES";

            PreparedStatement stmt = conn.prepareStatement(loadStatement);
            stmt.setString(1, newFilePath);
            stmt.executeUpdate();
            in.close();
            ConnectionManager.close(conn, stmt);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SQLException ex) {
            java.util.logging.Logger.getLogger(BootstrapDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
