<%-- 
    Document   : heatmap-main
    Created on : 29 Oct, 2017, 12:22:04 PM
    Author     : User
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Heatmap</title>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Heatmap</h1>
                <form method="post" action="heatmap">
                    Datetime
                    <input name="date" type="datetime-local" step="1"> <br>
                    
                    Floor
                    <select name="floor">
                        <option value="B1">B1</option>
                        <option value="L1">L1</option>
                        <option value="L2">L2</option>
                        <option value="L3">L3</option>
                        <option value="L4">L4</option>
                        <option value="L5">L5</option>
                    </select><br><br>
                    
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
