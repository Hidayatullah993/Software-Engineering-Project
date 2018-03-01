/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.ReportDAO;
import dao.StudentDAO;
import entity.Student;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import util.Validator;

/**
 * Java Servlet that controls the Basic Location Reports processing with JSON
 *
 * @author User
 */
@WebServlet("/json/basic-loc-report")
public class JSONBLRServlet extends HttpServlet {

    /**
     * Handles the HTTP<code>processRequest</code>method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonOutput = new JSONObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String inputToken = request.getParameter("token");
        String date = request.getParameter("date");
        String order = request.getParameter("order");

        Validator.validateField("date", date, jsonOutput);
        Validator.validateField("order", order, jsonOutput);
        Validator.validateField("token", inputToken, jsonOutput);

        //if the output object has any error messages, print the json object and return
        if (jsonOutput.containsKey("messages")) {
            jsonOutput.put("status", "error");
            Collections.sort((List<String>) jsonOutput.get("messages"));


            try (PrintWriter out = response.getWriter()) {
                String output = gson.toJson(jsonOutput);
                out.println(output);
            }

            return;
        }
        Validator.validateToken(inputToken, jsonOutput);
        //validate date
        date = date.replaceAll("T", " ");
        if (!Validator.validateDate(date)) {
            JSONArray messages = new JSONArray();

            if (jsonOutput.containsKey("messages")) {
                messages = (JSONArray) jsonOutput.get("messages");
            }
            messages.add("invalid date");
            jsonOutput.put("messages", messages);
        }

        //validate order
        HashMap<Integer, String> orderMap = new HashMap<>();
        int orderNumber = 1;
        try (Scanner orderSc = new Scanner(order)) {
            orderSc.useDelimiter(",");
            while (orderSc.hasNext()) {
                String criteria = orderSc.next();
                if (criteria == null || criteria.length() == 0 || (!criteria.equals("year") && !criteria.equals("gender") && !criteria.equals("school"))) {
                    JSONArray messages = new JSONArray();

                    if (jsonOutput.containsKey("messages")) {
                        messages = (JSONArray) jsonOutput.get("messages");
                    }
                    messages.add("invalid order");
                    jsonOutput.put("messages", messages);
                } else {
                    if (orderNumber > 1) {
                        if (!orderMap.containsValue(criteria)) {
                            orderMap.put(orderNumber, criteria);
                        }
                    } else {
                        orderMap.put(orderNumber, criteria);
                    }
                    orderNumber++;

                }
            }
        }

        //if the output object has any error messages, print the json object and return
        if (jsonOutput.containsKey("messages")) {
            jsonOutput.put("status", "error");
            Collections.sort((List<String>) jsonOutput.get("messages"));


            try (PrintWriter out = response.getWriter()) {
                String output = gson.toJson(jsonOutput);
                out.println(output);
            }

            return;
        }

        jsonOutput.put("status", "success");

        StudentDAO studentDao = ReportDAO.retrieveAllStudentsInSISInTimeframe(date);
        //ArrayList of all students
        ArrayList<Student> allStudents = studentDao.getAllStudents();
        //Assume there is always a first breakdown
        String firstBreakdown = orderMap.get(1);

        Map<String, ArrayList<Student>> firstMap = new TreeMap<>();
        if (firstBreakdown.equals("year")) {
            firstMap = studentDao.firstBreakdownByYear();
        } else if (firstBreakdown.equals("gender")) {
            firstMap = studentDao.firstBreakdownByGender();
        } else {
            firstMap = studentDao.firstBreakdownBySchool();
        }

        JSONArray firstArray = processToJSONArray(firstMap, firstBreakdown);
        jsonOutput.put("breakdown", firstArray);

        String secondBreakdown = null;
        if (orderMap.containsKey(2)) {
            secondBreakdown = orderMap.get(2);
        }
        Map<String, Map<String, ArrayList<Student>>> secondMap = new TreeMap<>();
        if (secondBreakdown != null) {
            if (secondBreakdown.equals("year")) {
                secondMap = studentDao.secondBreakdownByYear(firstMap);
            } else if (secondBreakdown.equals("gender")) {
                secondMap = studentDao.secondBreakdownByGender(firstMap);
            } else {
                secondMap = studentDao.secondBreakdownBySchool(firstMap);
            }
            jsonOutput = processSecondBreakdownToJSON(jsonOutput, secondMap, firstBreakdown, secondBreakdown);
        }

        String thirdBreakdown = null;
        if (orderMap.containsKey(3)) {
            thirdBreakdown = orderMap.get(3);
        }

        Map<String, Map<String, Map<String, ArrayList<Student>>>> thirdMap = new TreeMap<>();
        if (thirdBreakdown != null) {
            if (thirdBreakdown.equals("year")) {
                thirdMap = studentDao.thirdBreakdownByYear(secondMap);
            } else if (thirdBreakdown.equals("gender")) {
                thirdMap = studentDao.thirdBreakdownByGender(secondMap);
            } else {
                thirdMap = studentDao.thirdBreakdownBySchool(secondMap);
            }
            jsonOutput = processThirdBreakdownToJSON(jsonOutput, thirdMap, firstBreakdown, secondBreakdown, thirdBreakdown);
        }

   
        try (PrintWriter out = response.getWriter()) {
            String output = gson.toJson(jsonOutput);
            out.println(output);
        }
    }

    /**
     * This method takes in a single breakdown Map and processes it to JSON
     *
     * @param map map where key is breakdown by the criteria and the value is
     * the List of student objects in that breakdown
     * @param criteria criteria by year, gender or school
     * @return JSONArray object
     */
    private JSONArray processToJSONArray(Map<String, ArrayList<Student>> map, String criteria) {
        JSONArray result = new JSONArray();
        if (criteria.equals("year")) {
            Set<String> set = map.keySet();
            for (String key : set) {
                int k = Integer.parseInt(key);
                JSONObject row = new JSONObject();
                row.put(criteria, k);
                row.put("count", map.get(key).size());
                result.add(row);
            }
        } else {
            Set<String> set = map.keySet();
            for (String key : set) {
                JSONObject row = new JSONObject();
                row.put(criteria, key);
                row.put("count", map.get(key).size());
                result.add(row);
            }
        }

        return result;
    }

    /**
     * This method takes in the 2-layered breakdown of students Map and
     * processes it to JSON
     *
     * @param jsonOutput JSONObject to put results in and return
     * @param map 2-layered breakdown map
     * @param firstCriteria criteria by year, gender or school which user inputs
     * as first choice
     * @param secondCriteria criteria by year, gender or school which user
     * inputs as second choice
     * @return JSONObject
     */
    private JSONObject processSecondBreakdownToJSON(JSONObject jsonOutput, Map<String, Map<String, ArrayList<Student>>> map, String firstCriteria, String secondCriteria) {
        JSONArray firstBreakdownArray = (JSONArray) jsonOutput.get("breakdown");
        for (Object o : firstBreakdownArray) {
            JSONObject row = (JSONObject) o;
            //for each breakdown object, need to put a "breakdown" and assign a JSONArray to it

            //retirieving the key for the first layer map in map input
            Object firstBreakdown = row.get(firstCriteria);
            String firstBreakdownStr = null;
            if (firstBreakdown instanceof String) {
                firstBreakdownStr = (String) firstBreakdown;
            } else {
                firstBreakdownStr = "" + firstBreakdown;
            }

            Map<String, ArrayList<Student>> innerMap = map.get(firstBreakdownStr);
            JSONArray toPut = processToJSONArray(innerMap, secondCriteria);
            row.put("breakdown", toPut);
        }
        return jsonOutput;
    }

    /**
     * This method takes in the 3-layered breakdown of students Map and
     * processes it to JSON
     *
     * @param jsonOutput JSONObject to put results in and return
     * @param map 2-layered breakdown map
     * @param firstCriteria criteria by year, gender or school which user inputs
     * as first choice
     * @param secondCriteria criteria by year, gender or school which user
     * inputs as second choice
     * @param thirdCriteria criteria by year, gender or school which user inputs
     * as third choice
     * @return JSONObject
     */
    private JSONObject processThirdBreakdownToJSON(JSONObject jsonOutput, Map<String, Map<String, Map<String, ArrayList<Student>>>> map,
            String firstCriteria, String secondCriteria, String thirdCriteria) {

        JSONArray firstBreakdownArray = (JSONArray) jsonOutput.get("breakdown");
        for (Object firstObj : firstBreakdownArray) {
            JSONObject firstRow = (JSONObject) firstObj;
            //String firstBreakdownStr = (String) firstRow.get(firstCriteria);
            Object firstBreakdown = firstRow.get(firstCriteria);
            String firstBreakdownStr;
            if (firstBreakdown instanceof String) {
                firstBreakdownStr = (String) firstBreakdown;
            } else {
                firstBreakdownStr = "" + firstBreakdown;
            }
            //for each first breakdown row, get the json array that is valued to "breakdown"
            JSONArray secondBreakdownArray = (JSONArray) firstRow.get("breakdown");
            for (Object secondObj : secondBreakdownArray) {
                JSONObject secondRow = (JSONObject) secondObj;
                //String secondBreakdownStr = (String) secondRow.get(secondCriteria);
                Object secondBreakdown = secondRow.get(secondCriteria);
                String secondBreakdownStr;
                if (secondBreakdown instanceof String) {
                    secondBreakdownStr = (String) secondBreakdown;
                } else {
                    secondBreakdownStr = "" + secondBreakdown;
                }
                Map<String, Map<String, ArrayList<Student>>> innerMap = map.get(firstBreakdownStr);
                Map<String, ArrayList<Student>> lastMap = innerMap.get(secondBreakdownStr);
                JSONArray toPut = processToJSONArray(lastMap, thirdCriteria);
                secondRow.put("breakdown", toPut);
            }
        }
        return jsonOutput;
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
