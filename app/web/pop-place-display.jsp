<%-- 
    Document   : pop-place-display
    Created on : 20 Oct, 2017, 8:17:17 PM
    Author     : hidayat
--%>

<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="net.minidev.json.JSONObject"%>
<%@page import="net.minidev.json.JSONArray"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Popular Places</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Popular Places</h1>
            </div>

            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <%
                    try {
                    String msg = (String) request.getAttribute("error");
                    if (msg != null) {
                %>
                <div class="col-lg-12">
                    <div class="panel panel-danger">
                        <div class="panel-heading">
                            Error Found
                        </div>
                        <div class="panel-body">
                            <%=msg%>
                        </div>
                        <div class="panel-footer">
                            Oh no... :(
                        </div>
                    </div>
                </div>
                <!— /.col-lg-4 —>
                <%
                } else {
                %>
                <div class="col-lg-12">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            Success
                        </div>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>Rank</th>
                                            <th>Semantic Place</th>
                                            <th>Number of Devices</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            Map<Integer, ArrayList<String>> map = (TreeMap) request.getAttribute("success");
                                            int topK = Integer.parseInt(request.getParameter("topK"));
                                            int rank = 1;

                                            Set<Integer> set = map.keySet();
                                            for (int count : set) {
                                                ArrayList<String> semPlaces = map.get(count);
                                                String semPlacesStr = semPlaces.toString();
                                                String toPrint = semPlacesStr.substring(1, semPlacesStr.length() - 1);
                                                out.println("<tr>");
                                                out.println("<td>" + rank + "</td>");
                                                out.println("<td>" + toPrint + "</td>");
                                                out.println("<td>" + count + "</td>");
                                                out.println("</tr>");
                                                if (rank == topK) {
                                                    break;
                                                }
                                                rank++;
                                            }
                                        %>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <!— /.col-lg-4 —>
                    <%
                        }
}catch(NullPointerException npe){

}
catch(NumberFormatException nfe) {

}
                    %>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
        </div>
        <!-- /.container-fluid -->
    </div>
    <!-- /#page-wrapper -->
    <%@include file="footer.jsp" %>