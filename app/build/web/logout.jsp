<%-- 
    Document   : logout
    Created on : Nov 16, 2017, 4:32:29 PM
    Author     : Gerald
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Logout</title>
    </head>
    <%
        session.invalidate();
        response.sendRedirect(request.getContextPath());
    %>
</html>
