/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import dao.ReportDAO;
import dao.StudentDAO;
import entity.Student;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Java Servlet that controls the Basic Location Reports processing
 *
 * @author Poh Wei Kiat
 */
@WebServlet("/blr")
public class BLRServlet extends HttpServlet {

    /**
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //response.setContentType("text/html");
        RequestDispatcher view = request.getRequestDispatcher("breakdown-display.jsp");

        String date = request.getParameter("date");

        try {

            if (date.length() < 18) {
                date += ":00";
            }

            //placing the order of breakdown into a HashMap
            HashMap<Integer, String> orderMap = getOrder(request);
            //populating StudentDAO with all instances of Student objects in the database
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
            }
            request.setAttribute("total", allStudents.size());
            request.setAttribute("firstBreakdown", firstMap);
            request.setAttribute("secondBreakdown", secondMap);
            request.setAttribute("thirdBreakdown", thirdMap);
            request.setAttribute("order", orderMap);
            view.forward(request, response);
        } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
            response.sendRedirect("blr-main.jsp");
        }

    }

    /**
     * The method is to get the order for BLR
     *
     * @param request servlet request
     * @return HashMap of order number and the criteria (either by year, gender
     * or school)
     */
    private HashMap<Integer, String> getOrder(HttpServletRequest request) {
        String first = request.getParameter("first");
        String second = request.getParameter("second");
        String third = request.getParameter("third");
        HashMap<Integer, String> result = new HashMap<>();
        result.put(1, first);
        if (!second.equals("none")) {
            if (!result.containsValue(second)) {
                result.put(2, second);
            }
        }
        if (!third.equals("none")) {
            if (!result.containsValue(third)) {
                if (!result.containsKey(2)) {
                    result.put(2, third);
                } else {
                    result.put(3, third);
                }
            }
        }
        return result;
    }

}
