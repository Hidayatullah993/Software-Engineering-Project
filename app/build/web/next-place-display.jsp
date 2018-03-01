<%-- 
    Document   : next-place-display
    Created on : 25 Oct, 2017, 2:26:38 PM
    Author     : shenying
--%>

<%@page import="net.minidev.json.JSONObject"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Next Places</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Next Places</h1>
            </div>

            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <%
                    try{
                    JSONObject jsonResult = (JSONObject) request.getAttribute("json-result");
                    TreeMap<Integer, ArrayList<String>> resultMap = (TreeMap<Integer, ArrayList<String>>)jsonResult.get("results");
                    
                    //System.out.println(resultMap);
                    if (resultMap == null) {
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
                                            <th>Rank</th>
                                            <th>Semantic Place</th>
                                            <th>Number of Users</th>
                                            <th>Percentage of Visitors</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            int topK = Integer.parseInt(request.getParameter("topK"));
                                            int rank = 0;

                                            for (int count : resultMap.keySet()) {
                                                ArrayList<String> places = resultMap.get(count);
                                                out.println("<tr><td>" + ++rank + "</td>");
                                                out.println("<td>");
                                                for (String place : places) {
                                                    out.println(place + "<br>");
                                                }
                                                out.println("</td>");
                                                out.println("<td>" + count + "</td>");
                                                double percentage = (double) count / (int) jsonResult.get("total-users") * 100;
                                                int rounded = (int) Math.round(percentage);
                                                out.println("<td>" + rounded + "%</td></tr>");
                                                if (rank == topK) {
                                                    break;
                                                }
                                            }
                                            
                                        %>
                                    </tbody>
                                </table>
                            </div>
                            <!— /.table-responsive —>
                            Number of people at the origin: <%=jsonResult.get("total-users")%> <br>
                            Number of people who visited other places: <%=jsonResult.get("total-next-place-users")%>
                            
                        </div>
                        <!— /.panel-body —>
                    </div>
                    <!— /.panel —>
                </div>
                <!— /.col-lg-4 —>
                <%
                    }
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