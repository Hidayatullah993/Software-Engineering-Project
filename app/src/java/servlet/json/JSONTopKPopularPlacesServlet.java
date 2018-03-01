/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.ReportDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * Java servlet that handles Top K Popular Places with JSON
 *
 * @author shenying
 */
@WebServlet("/json/top-k-popular-places")
public class JSONTopKPopularPlacesServlet extends HttpServlet {

    /**
     * Handle the HTTP<code>processRequest</code>method.
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

        Validator.validateField("date", date, jsonOutput);
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
        //date has been entered, so validate date
        date = date.replaceAll("T", " ");
        if (!Validator.validateDate(date)) {
            JSONArray messages = new JSONArray();

            if (jsonOutput.containsKey("messages")) {
                messages = (JSONArray) jsonOutput.get("messages");
            }
            messages.add("invalid date");
            jsonOutput.put("messages", messages);
        }
        String topK = request.getParameter("k");
        //validate k

        if (topK == null || topK.equals("")) {
            topK = "3";
        } else {
            try {
                int k = Integer.parseInt(topK);

                if (k < 1 || k > 10) {
                    JSONArray messages = new JSONArray();

                    if (jsonOutput.containsKey("messages")) {
                        messages = (JSONArray) jsonOutput.get("messages");
                    }
                    messages.add("invalid k");
                    jsonOutput.put("messages", messages);
                }

            } catch (NumberFormatException nfe) {
                JSONArray messages = new JSONArray();

                if (jsonOutput.containsKey("messages")) {
                    messages = (JSONArray) jsonOutput.get("messages");
                }
                messages.add("invalid k");
                jsonOutput.put("messages", messages);
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

        jsonOutput.put("status", "success"); //reaching this point means everything is valid

        JSONArray results = ReportDAO.getTopKPopularPlaces(date);

        Map<Integer, ArrayList<String>> orderedMap = new TreeMap<>(Collections.reverseOrder());
        for (Object row : results) {
            JSONObject jsonObject = (JSONObject) row;
            String semPlace = (String) jsonObject.get("semantic-place");
            int count = (int) jsonObject.get("count");
            if (orderedMap.containsKey(count)) {
                ArrayList<String> list = orderedMap.get(count);
                list.add(semPlace);
                orderedMap.put(count, list);
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(semPlace);
                orderedMap.put(count, list);
            }
        }

        JSONArray resultArray = new JSONArray();
        int rank = 0;
        int prevCount = 0;

        for (int count : orderedMap.keySet()) {
            for (String semanticPlace : orderedMap.get(count)) {
                if (count != prevCount) {
                    ++rank;
                }

                if (rank >= Integer.parseInt(topK) + 1) {
                    break;
                } else {
                    JSONObject row = new JSONObject();
                    row.put("rank", rank);
                    row.put("semantic-place", semanticPlace);
                    row.put("count", count);
                    resultArray.add(row);
                }
                prevCount = count;
            }
        }

        jsonOutput.put("results", resultArray);

        try (PrintWriter out = response.getWriter()) {
            String output = gson.toJson(jsonOutput);
            out.println(output);
        }
    }

    /**
     * Handle the HTTP<code>POST</code>method.
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
     * Handle the HTTP<code>GET</code>method.
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
