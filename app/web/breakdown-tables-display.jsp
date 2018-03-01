<%-- 
   Document   : breakdown-tables-display
   Created on : Oct 26, 2017, 12:41:58 PM
   Author     : Gerald
--%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="entity.Student"%>
<%@page import="entity.Student"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<%
    try {

        Map<String, ArrayList<Student>> firstMap = (Map<String, ArrayList<Student>>) request.getAttribute("firstBreakdown");

        Map<String, Map<String, ArrayList<Student>>> secondMap
                = (Map<String, Map<String, ArrayList<Student>>>) request.getAttribute("secondBreakdown");

        Map<String, Map<String, Map<String, ArrayList<Student>>>> thirdMap
                = (Map<String, Map<String, Map<String, ArrayList<Student>>>>) request.getAttribute("thirdBreakdown");

        HashMap<Integer, String> orderMap = (HashMap<Integer, String>) request.getAttribute("order");

        int totalNumberOfStudents = (Integer) request.getAttribute("total");

        Set<String> firstSet = firstMap.keySet();

        for (String firstKey : firstSet) {
            out.println("<div class='table-responsive'><table class='table table-hover'>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th>Breakdown Criteria</th>");
            out.println("<th>Count</th>");
            out.println("<th>Percentage</th>");
            out.println("</tr>");
            out.println("</thead>");

            out.println("<tbody>");
            out.println("<tr>");
            out.println("<td>" + firstKey + "</td>");
            int firstSize = firstMap.get(firstKey).size();
            double percentage = Math.round((double) firstSize / totalNumberOfStudents * 100);
            //out.println(firstKey + ": " + firstSize + "(" + percentage + "%)<br>");
            out.println("<td>" + firstSize + "</td>");
            out.println("<td>" + percentage + "%</td>");
            out.println("</tr>");

            if (!secondMap.isEmpty()) {
                Map<String, ArrayList<Student>> innerMap = secondMap.get(firstKey);
                Set<String> secondSet = innerMap.keySet();
                for (String secondKey : secondSet) {
                    out.println("<tr>");
                    out.println("<td>&nbsp;&nbsp;" + secondKey + "</td>");

                    int secondSize = innerMap.get(secondKey).size();
                    percentage = Math.round((double) secondSize / totalNumberOfStudents * 100);
                    //out.println(secondKey + ": " + secondSize + "(" + percentage + "%)<br>");
                    out.println("<td>" + secondSize + "</td>");
                    out.println("<td>" + percentage + "%</td>");
                    out.println("</tr>");
                    if (!thirdMap.isEmpty()) {
                        Map<String, ArrayList<Student>> lastMap = thirdMap.get(firstKey).get(secondKey);
                        Set<String> thirdSet = lastMap.keySet();
                        for (String thirdKey : thirdSet) {
                            out.println("<tr>");
                            out.println("<td>&nbsp;&nbsp;&nbsp;&nbsp;" + thirdKey + "</td>");
                            int thirdSize = lastMap.get(thirdKey).size();
                            percentage = Math.round((double) thirdSize / totalNumberOfStudents * 100);
                            //out.println(thirdKey + ": " + thirdSize + "(" + percentage + "%)<br>");
                            out.println("<td>" + thirdSize + "</td>");
                            out.println("<td>" + percentage + "%</td>");
                            out.println("</tr>");
                        }
                    }
                }
            }
            out.println("</tbody></table><br>");
        }
    } catch (NullPointerException npe) {
        if(session.getAttribute("token")==null) {
            response.sendRedirect("index.jsp");
        }
        else {
            response.sendRedirect("home.jsp");
        }
    }
%>