`<%-- 
    Document   : index
    Created on : 15 Sep, 2017, 7:36:11 PM
    Author     : Gan Shen Ying
--%>

<%@page import = "java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="header.jsp" %>
<title>Login Page</title>
</head>
<body background="images/login-background.jpg" style="background-size: 100%;">
    <div class="container">
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <div class="login-panel panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Sign in to SLOCA</h3>
                    </div>
                    <div class="panel-body">
                        <form method="post" action = "login">
                            <fieldset>
                                <!-- Form to request input from the user -->
                                <div class="form-group">
                                    <input class="form-control" placeholder="Username" name="username" type="text" autofocus>
                                </div>
                                <div class="form-group">
                                    <input class="form-control" placeholder="Password" name="password" type="password" value="">
                                </div>
                                
                                    <%
                                        //request.getAttribute returns an Object. Hence, need to cast as ArrayList<String>
                                        ArrayList<String> errorMsgs = (ArrayList<String>) request.getAttribute("errorList");

                                        if (errorMsgs != null && errorMsgs.size() > 0) {
                                            out.println("<font color = 'red'>");
                                            for (String error : errorMsgs) {
                                                out.println("<li>" + error + "</li>");
                                            }
                                            out.println("</font>");

                                        }
                                    %>
                                    <br>
                                <input class="btn btn-lg btn-success btn-block" type="submit" value ='Login'/>

                            </fieldset>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@include file="footer.jsp" %>