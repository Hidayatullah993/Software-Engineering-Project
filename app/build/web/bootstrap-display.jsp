<%-- 
    Document   : bootstrap-display
    Created on : 14 Nov, 2017, 4:33:23 PM
    Author     : shenying
--%>

<%@page import="net.minidev.json.JSONArray"%>
<%@page import="net.minidev.json.JSONObject"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Bootstrap Results</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Bootstrap</h1>
            </div>

            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <div class="col-lg-12">
                    <div class="panel panel-info">
                        <div class="panel-heading">
                            Results
                        </div>
                        
                        <%
                            try {JSONObject jsonOutput = (JSONObject) request.getAttribute("result");%>
                        <!— /.panel-heading —>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>File</th>
                                            <th>Number of Rows Loaded</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            JSONArray files = (JSONArray) jsonOutput.get("num-record-loaded");
                                            for (Object o : files) {
                                                JSONObject data = (JSONObject) o;
                                        %>
                                        <tr>
                                            <td><%=data.keySet().iterator().next()%></td>
                                            <td><%=data.get(data.keySet().iterator().next())%></td>
                                        </tr>
                                        <%

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
            <div class="col-lg-12">
                <div class="col-lg-12">

                    <%
                        
                        JSONArray errors = (JSONArray) jsonOutput.get("error");
                        if (errors != null) {
                    %>
                    <div class="panel panel-red">
                        <div class="panel-heading">
                            Errors
                        </div>
                        <!— /.panel-heading —>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>File</th>
                                            <th>Line</th>
                                            <th>Errors</th>
                                        </tr>
                                    </thead>
                                    <tbody>


                                        <%
                                            for (Object ew : errors) {
                                                JSONObject error = (JSONObject) ew;

                                        %>
                                        <tr>
                                            <td><%=error.get("file")%></td>
                                            <td><%=error.get("line")%></td>
                                            <td>
                                                <%
                                                    JSONArray messages = (JSONArray) error.get("messages");
                                                    if (messages != null) {
                                                        for (Object rr : messages) {
                                                %>
                                                <%=rr%><br>
                                                <%
                                                                }
                                                            }
                                                        }
                                                    }
                                                %>
                                            </td></tr>
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
                <%
                    
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
