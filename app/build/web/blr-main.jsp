<%-- 
    Document   : blr-main
    Created on : 15 Oct, 2017, 11:16:15 AM
    Author     : shenying
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Basic Location Report</title>
</head>

<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Breakdown</h1>
                <form method="post" action="blr">
                    Breakdown by: <br>

                    Datetime
                    <input name="date" type="datetime-local" step="1">

                    <br>
                    Breakdown by:<br>
                    First criterion<br>
                    <input type="radio" name="first" value="year" checked> Year
                    <input type="radio" name="first" value="school"> School
                    <input type="radio" name="first" value="gender"> Gender
                    <br>
                    Second criterion<br>
                    <input type="radio" name="second" value="year" checked> Year
                    <input type="radio" name="second" value="school"> School
                    <input type="radio" name="second" value="gender"> Gender
                    <input type="radio" name="second" value="none"> None
                    <br>
                    Third criterion<br>
                    <input type="radio" name="third" value="year" checked> Year
                    <input type="radio" name="third" value="school"> School
                    <input type="radio" name="third" value="gender"> Gender
                    <input type="radio" name="third" value="none"> None
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
