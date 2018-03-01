<%-- 
    Document   : breakdown-display
    Created on : Oct 26, 2017, 12:40:01 PM
    Author     : Gerald
--%>

<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@page import="entity.Student"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="net.minidev.json.JSONObject"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Breakdown</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Breakdown</h1>
            </div>
            <div class="col-lg-12">
                <div class="panel panel-success">
                    <div class="panel-heading">
                        Success
                    </div>
                    <div class="panel-body">
                        <%@include file="breakdown-tables-display.jsp" %>
                    </div>
                </div>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /.container-fluid -->
</div>
<!-- /#page-wrapper -->
<%@include file="footer.jsp" %>