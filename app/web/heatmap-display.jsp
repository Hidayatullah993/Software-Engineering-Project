<%-- 
    Document   : heatmap-display
    Created on : 29 Oct, 2017, 12:23:01 PM
    Author     : User
--%>

<%@page import="net.minidev.json.JSONObject"%>
<%@page import="net.minidev.json.JSONArray"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Heatmap</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Heatmap</h1>
            </div>
            <div class="col-lg-12">
                Showing results for <%=request.getAttribute("date")%> at floor <%=request.getParameter("floor")%>
                <br>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <div class="col-lg-12">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            Success
                        </div>
                        <!— /.panel-heading —>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>Semantic Place</th>
                                            <th>Number of Users</th>
                                            <th>Crowd Density</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            try {
                                                JSONArray heatmap = (JSONArray) request.getAttribute("heatmap");
                                                for (Object o : heatmap) {
                                                    JSONObject semPlace = (JSONObject) o;
                                                    out.println("<tr>");
                                                    out.println("<td>" + semPlace.get("semantic-place") + "</td>");
                                                    out.println("<td>" + semPlace.get("num-people") + "</td>");
                                                    out.println("<td>" + semPlace.get("crowd-density") + "</td>");
                                                    out.println("</tr>");
                                                }
                                                out.println("</table>");
                                            } catch (NullPointerException npe) {

                                            }
                                        %>
                                    </tbody>
                                </table>
                            </div>
                            <!— /.table-responsive —>
                        </div>
                        <!— /.panel-body —>
                    </div>
                    <!— /.panel —>
                </div>
                <!— /.col-lg-4 —>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /.container-fluid -->
</div>
<!-- /#page-wrapper -->
<%@include file="footer.jsp" %>
