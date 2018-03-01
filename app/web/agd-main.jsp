<%-- 
    Document   : agd-main
    Created on : 31 Oct, 2017, 5:10:00 PM
    Author     : shenying
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="header.jsp" %>
<%@include file="protect.jsp" %>
<title>Automatic Group Detection</title>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Automatic Group Detection</h1>
                <form method="post" action="agd">
                    Datetime
                    <input name="date" type="datetime-local" step="1"> <br><br>
                    
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
