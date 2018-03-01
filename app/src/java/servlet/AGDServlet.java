/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import dao.CompanionDAO;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

/**
 *
 * @author Poh Wei Kiat
 */
@WebServlet("/agd")
public class AGDServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher view = request.getRequestDispatcher("agd-display.jsp");

        try {
            String date = request.getParameter("date");
            if (date.length() < 18) {
                date += ":00";
            }

            //CompanionDAO.getAllUserTimeLines(day, month, year, hours, minutes, seconds);
            JSONObject userGroups = CompanionDAO.detectGroups(date);

            request.setAttribute("json-result", userGroups);
            request.setAttribute("date", date);
            view.forward(request, response);
        } catch (StringIndexOutOfBoundsException e) {
            response.sendRedirect("agd-main.jsp");
            return;
        }
    }
}
