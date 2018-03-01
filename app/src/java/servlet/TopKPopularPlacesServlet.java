/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import dao.ReportDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import util.Validator;

/**
 * Java servlet that handles Top K Popular Places 
 * @author Poh Wei Kiat
 */
@WebServlet("/popularplaces")
public class TopKPopularPlacesServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs 
     * @throws IOException if an I/O error occurs 
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher view = request.getRequestDispatcher("pop-place-display.jsp");

        String date = request.getParameter("date");

        try {
           
            if (date.length() < 18) {
                date += ":00";
            }

            JSONArray queriedArray
                    = ReportDAO.getTopKPopularPlaces(date);

            Map<Integer, ArrayList<String>> resultMap = process(queriedArray);

            String msg = "";
            if (queriedArray == null) {
                msg = "Error with queried Array";
                request.setAttribute("error", msg);
            } else if (queriedArray.isEmpty()) {
                msg = "No Popular Places in query window";
                request.setAttribute("error", msg);
            } else {
                request.setAttribute("success", resultMap);
            }
            view.forward(request, response);

        } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
            response.sendRedirect("pop-place-main.jsp");
            return;
        }

    }

    /**
     * The process method is used to store query results for semantic places in time
     * window and the count of unique mac address in a TreeMap to automatically 
     * sort the results based on the count of users and reverse the order of the TreeMap
     * @param array JSONArray from the method ReportDAO getTopKPopularPlaces
     * @return results which to be used by Top K Popular places display page
     */
    private Map<Integer, ArrayList<String>> process(JSONArray array) {
        Map<Integer, ArrayList<String>> result = new TreeMap<>(Collections.reverseOrder());
        for (Object row : array) {
            JSONObject jsonObject = (JSONObject) row;
            String semPlace = (String) jsonObject.get("semantic-place");
            int count = (int) jsonObject.get("count");
            if (result.containsKey(count)) {
                ArrayList<String> list = result.get(count);
                list.add(semPlace);
                result.put(count, list);
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(semPlace);
                result.put(count, list);
            }
        }
        return result;
    }
}
