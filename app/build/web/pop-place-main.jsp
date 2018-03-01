<%-- 
    Document   : pop-place-main
    Created on : 15 Oct, 2017, 11:18:59 AM
    Author     : shenying
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Top-k Popular Places</title>
</head>

<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Top-k Popular Places</h1>
                <form method="post" action="popularplaces">
                    Datetime
                    <input name="date" type="datetime-local" step="1">

                    <br>
                    Top
                    <select name="topK">
                        <%
                            for (int i = 1; i <= 10; i++) {
                                if (i == 3) {
                                    out.println("<option selected='selected' value=" + i + ">" + i + "</option>");
                                } else {
                                    out.println("<option value=" + i + ">" + i + "</option>");
                                }
                            }
                        %>
                    </select>
                    <br>
                    <br>
                    <input type="submit" class="btn btn-lg btn-success" value="Submit">
                </form>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /.container-fluid -->
</div>
<!-- /#page-wrapper -->


<%@include file="footer.jsp" %>

