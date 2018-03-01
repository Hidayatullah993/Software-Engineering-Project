/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import dao.ReportDAO;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

/**
 * Java Servlet that handles Top K Next Places 
 * @author Poh Wei Kiat
 */
@WebServlet("/next-places")
public class TopKNextPlacesServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs 
     * @throws IOException if an I/O error occurs 
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher view = request.getRequestDispatcher("next-place-display.jsp");

        String semanticPlace = request.getParameter("semanticplace");
        String date = request.getParameter("date");

        try {
            if (date.length() < 18) {
                date += ":00";
            }

            JSONObject jsonOutput = ReportDAO.getTopKNextPlaces(semanticPlace, date);

            request.setAttribute("json-result", jsonOutput);
            view.forward(request, response);
        } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
            response.sendRedirect("next-place-main.jsp");
            return;
        }

    }
}
