<%-- 
    Document   : bootstrap-view
    Created on : 18 Oct, 2017, 4:27:10 PM
    Author     : User
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Bootstrap Page</title>
</head>
<body>
    <div id="page-wrapper">
    <div class="container">
        <div class="row">
            <div class="col-md-4 col-md-offset-2">
                <div class="login-panel panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Bootstrap/Update Data</h3>
                    </div>
                    <div class="panel-body">
                        <form action="bootstrap"  method="post" enctype="multipart/form-data">

                            File:<input type="file" name="bootstrap-file" /><br />
                            <input type="submit" class="btn btn-lg btn-success btn-block" value="Bootstrap/Update" />

                            <%
                                //request.getAttribute returns an Object. Hence, need to cast as ArrayList<String>
                                //ArrayList<String> errorMsgs = (ArrayList<String>)request.getAttribute("errorMsg");
                                String errorMsg = (String) request.getAttribute("errorMsg");

                                if (errorMsg != null) { // errorMsgs.size() > 0){
                                    out.println("<font color = 'red'>");
                                    //  for(String error : errorMsgs){
                                    out.println("<li>" + errorMsg + "</li>");
                                    // }
                                    out.println("</font>");
                                }
                                //}
%>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
