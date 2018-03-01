/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.CompanionDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
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
@WebServlet("/json/group_detect")
public class JSONAGDServlet extends HttpServlet {

    /**
     * Handles the HTTP <code> processRequest </code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet -specific error occurs
     * @throws IOException if an I /O error occurs
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

//            response.setContentType("text/html");
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

        JSONObject userGroups = CompanionDAO.detectGroups(date);
        //response.setContentType("text/html");
        try (PrintWriter out = response.getWriter()) {
            String output = gson.toJson(userGroups);
            out.println(output);
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
