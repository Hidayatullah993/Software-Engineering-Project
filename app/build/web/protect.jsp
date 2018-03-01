<%@page import="is203.JWTException"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="is203.JWTUtility"%>
<%
    String token = (String) session.getAttribute("token");
    String sharedSecret = "qwertyuiopasdfgh";
    String uri = request.getRequestURI();

    try {
        String username = JWTUtility.verify(token, sharedSecret);
        if (token == null || username == null) {
            response.sendRedirect("index.jsp");
        } else {
            if (uri.contains("bootstrap") && !username.equals("admin")) {
                response.sendRedirect("home.jsp");
            }
        }

        if (username.equals("admin")) {
        %>
        <%@include file="navbar-sidebar-admin.jsp" %>
    <%} else {
    %>
        <%@include file="navbar-sidebar.jsp" %>
    <%}
    } catch (NullPointerException npe) {
        response.sendRedirect("index.jsp");
    } catch (JWTException jwte) {
        response.sendRedirect("index.jsp");
    }
%>
