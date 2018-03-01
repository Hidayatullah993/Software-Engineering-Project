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
import java.util.LinkedHashSet;
import java.util.List;
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
 *
 * @author shenying
 */
@WebServlet("/json/top-k-next-places")
public class JSONTopKNextPlacesServlet extends HttpServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject jsonOutput = new JSONObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String inputToken = request.getParameter("token");
        String date = request.getParameter("date");
        String semanticPlace = request.getParameter("origin");

        Validator.validateField("token", inputToken, jsonOutput);
        Validator.validateField("date", date, jsonOutput);
        Validator.validateField("origin", semanticPlace, jsonOutput);

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

        if (!Validator.validateSemanticPlace(semanticPlace)) {
            JSONArray messages = new JSONArray();
            if (jsonOutput.containsKey("messages")) {
                messages = (JSONArray) jsonOutput.get("messages");
            }
            messages.add("invalid origin");
            jsonOutput.put("messages", messages);
        } else {
            LinkedHashSet<String> validSemanticList = ReportDAO.retrieveSemanticPlacesFromDatabase();
            if (!validSemanticList.contains(semanticPlace)) {
                JSONArray messages = new JSONArray();
                if (jsonOutput.containsKey("messages")) {
                    messages = (JSONArray) jsonOutput.get("messages");
                }
                messages.add("invalid origin");
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

        //at this point, date is valid and exists
        jsonOutput = ReportDAO.getTopKNextPlaces(semanticPlace, date);

        jsonOutput.put("status", "success");

        TreeMap<Integer, ArrayList<String>> placesSortedByCount = (TreeMap<Integer, ArrayList<String>>) jsonOutput.get("results");

        JSONArray resultArray = new JSONArray();

        if (placesSortedByCount == null) {


            try (PrintWriter out = response.getWriter()) {
                String output = gson.toJson(jsonOutput);
                out.println(output);
            }
            return;
        }

        int rank = 1;

        for (int count : placesSortedByCount.keySet()) {
            ArrayList<String> places = placesSortedByCount.get(count);

            //for (String place : places) {
            JSONObject rankObject = new JSONObject();

            rankObject.put("rank", rank);
            rankObject.put("semantic-place", places.toString().substring(1, places.toString().length() - 1));
            rankObject.put("count", count);

            resultArray.add(rankObject);
            //}

            if (rank == Integer.parseInt(topK)) {
                break;
            }
            rank++;
        }

        jsonOutput.put("results", resultArray);

        try (PrintWriter out = response.getWriter()) {
            String output = gson.toJson(jsonOutput);
            out.println(output);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
