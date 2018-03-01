<%-- 
    Document   : agd-display
    Created on : Nov 1, 2017, 3:10:20 PM
    Author     : Gerald
--%>

<%@page import="java.util.Iterator"%>
<%@page import="net.minidev.json.JSONArray"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="net.minidev.json.JSONObject"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Automatic Group Detection</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Automatic Group Detection</h1>
            </div>
            <div class="col-lg-12">
                <%
                    try{
                    JSONObject jsonResult = (JSONObject) request.getAttribute("json-result");%>
                Showing results for <b><%=request.getAttribute("date")%></b>
                <br>
                Total number of users found: <%=jsonResult.get("total-users")%>
                <br>
                Total number of groups found: <%=jsonResult.get("total-groups")%>
                <br>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <%

                    JSONArray groups = (JSONArray) jsonResult.get("groups");
                    //System.out.println(groups);
                    //System.out.println(resultMap);
                    if (groups == null) {
                %>
                <div class="col-lg-12">
                    <div class="panel panel-danger">
                        <div class="panel-heading">
                            Error Found
                        </div>
                        <div class="panel-body">
                            <%=jsonResult.get("error")%>
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
                        <!— /.panel-heading —>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>Group Size</th>
                                            <th>Users</th>
                                            <th>Locations (Time Together)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            Iterator<Object> iter = groups.iterator();

                                            while (iter.hasNext()) {
                                                JSONObject group = (JSONObject) iter.next();
                                                int groupSize = (int) group.get("size");

                                                JSONArray members = (JSONArray) group.get("members");
                                                JSONArray locations = (JSONArray) group.get("locations");

                                                out.println("<tr>");
                                                out.println("<td rowspan='" + groupSize + "'>" + groupSize + "</td>");
                                                out.println("<td>" + ((JSONObject) members.get(0)).get("mac-address") + "<br>"
                                                        + ((JSONObject) members.get(0)).get("email") + "</td>");

                                                out.println("<td>");
                                                for (Object location : locations) {
                                                    out.println(((JSONObject) location).get("location"));
                                                    out.println(" (" + ((JSONObject) location).get("time-spent") + ")");
                                                    out.println("<br>");
                                                }
                                                out.println("</td>");
                                                
                                                out.println("<td>" + group.get("total-time-spent") + "</td>");
                                                
                                                out.println("</tr>");

                                                for (int i = 1; i < members.size(); i++) {
                                                    out.println("<tr>");
                                                    out.println("<td>" + ((JSONObject) members.get(i)).get("mac-address") + "<br>"
                                                            + ((JSONObject) members.get(i)).get("email") + "</td>");
                                                    out.println("</tr>");
                                                }
                                            }
                                            out.println("</table>");

                                            //out.println("<a href='next-place-main.jsp'>Back</a>");
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
                <%                    }
} catch(NullPointerException npe) {

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

