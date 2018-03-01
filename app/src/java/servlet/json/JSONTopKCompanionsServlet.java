/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.CompanionDAO;
import static dao.CompanionDAO.getAllUserTimeLines;
import entity.Timeline;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
 * Java Servlet that controls the Heatmap processing with JSON
 *
 * @author Gerald
 */
@WebServlet("/json/top-k-companions")
public class JSONTopKCompanionsServlet extends HttpServlet {

    /**
     * Handles the HTTP <code> processRequest </code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet -specific error occurs
     * @throws IOException if an I /O error occurs
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JSONObject jsonOutput = new JSONObject();

        //this is the token that is entered with the request
        String inputToken = request.getParameter("token");
        Validator.validateField("token", inputToken, jsonOutput);

        //need to check that the input token is valid (for now, no need to authenticate)
        String date = request.getParameter("date");
        Validator.validateField("date", date, jsonOutput);

        String macAddress = request.getParameter("mac-address");
        Validator.validateField("mac-address", macAddress, jsonOutput);

        //print if there are any error messages
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

        if (!Validator.isValidSHA1(macAddress)) {
            JSONArray messages = new JSONArray();
            if (jsonOutput.containsKey("messages")) {
                messages = (JSONArray) jsonOutput.get("messages");
            }
            messages.add("invalid mac-address");
            jsonOutput.put("messages", messages);
        }

        //print if there are any error messages
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
        try {
            HashMap<String, Timeline> filteredMap = getAllUserTimeLines(date);

            jsonOutput = CompanionDAO.getTopKCompanion(filteredMap, macAddress, date);

            jsonOutput.put("status", "success"); //reaching this point means everything is valid

            TreeMap<Integer, ArrayList<String>> resultMap = (TreeMap<Integer, ArrayList<String>>) jsonOutput.get("results");

            JSONArray resultArray = new JSONArray();

            int rank = 1;
            for (int count : resultMap.keySet()) {
                ArrayList<String> macAddressOfUsers = resultMap.get(count);
                ArrayList<String> email = new ArrayList<>();

                for (int k = 0; k < macAddressOfUsers.size(); k++) {
                    String address = macAddressOfUsers.get(k);
                    email.add(CompanionDAO.getEmail(address));
                }
                JSONObject rankObject = new JSONObject();
                rankObject.put("rank", rank);
                rankObject.put("companion", email.toString().substring(1, email.toString().length() - 1));
                rankObject.put("mac-address", macAddressOfUsers.toString().substring(1, macAddressOfUsers.toString().length() - 1));
                rankObject.put("time-together", count);

                resultArray.add(rankObject);
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
        } catch (NullPointerException npe) {
            JSONArray messages = new JSONArray();
            if (jsonOutput.containsKey("messages")) {
                messages = (JSONArray) jsonOutput.get("messages");
            }
            messages.add("invalid mac-address");
            jsonOutput.put("messages", messages);
        } catch (StringIndexOutOfBoundsException e) {

        }
        if (jsonOutput.containsKey("messages")) {
            jsonOutput.put("status", "error");
            Collections.sort((List<String>) jsonOutput.get("messages"));

            try (PrintWriter out = response.getWriter()) {
                String output = gson.toJson(jsonOutput);
                out.println(output);
            }
            return;
        }
    }

    /**
     * Handles the HTTP<code>POST</code> method.
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
     * Handles the HTTP<code>GET</code> method.
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
