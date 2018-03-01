<%-- 
    Document   : companion-display
    Created on : 31 Oct, 2017, 6:20:09 PM
    Author     : User
--%>

<%@page import="dao.CompanionDAO"%>
<%@page import="java.util.Set"%>
<%@page import="net.minidev.json.JSONObject"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="protect.jsp" %>
<%@include file="header.jsp" %>
<title>Companions</title>
</head>
<!-- Page Content -->
<div id="page-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <h1 class="page-header">Companions </h1>
            </div>

            <!-- /.col-lg-12 -->
        </div>
        <!-- /.row -->
        <div class="row">
            <div class="col-lg-12">
                <%
                    try{
                    JSONObject jsonResult = (JSONObject) request.getAttribute("json-result");
                    TreeMap<Integer, ArrayList<String>> resultMap = (TreeMap<Integer, ArrayList<String>>) jsonResult.get("results");

                    //System.out.println(resultMap);
                    if (resultMap == null) {
                %>
                <div class="col-lg-12">
                    <div class="panel panel-danger">
                        <div class="panel-heading">
                            Error Found
                        </div>
                        <div class="panel-body">
                            <%=jsonResult.get("error")%>
                        </div>
                        <div class="panel-footer">
                            Oh no... :(
                        </div>
                    </div>
                </div>
                <!— /.col-lg-4 —>
                <%
                } else {
                %>
                <div class="col-lg-12">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            Success
                        </div>
                        <!— /.panel-heading —>
                        <div class="panel-body">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>Rank</th>
                                            <th>Companion </th>
                                            <th>Mac Address</th>
                                            <th>Time-Together</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            int topK = Integer.parseInt(request.getParameter("topK"));
                                            int rank = 1;

                                            Set<Integer> set = resultMap.keySet();
                                            for (int count : set) {
                                                ArrayList<String> macAddressOfUsers = resultMap.get(count);
                                                ArrayList<String> email = new ArrayList<>();

                                                for (int k = 0; k < macAddressOfUsers.size(); k++) {
                                                    String address = macAddressOfUsers.get(k);
                                                    email.add(CompanionDAO.getEmail(address));
                                                }
                                                out.println("<tr>");
                                                out.println("<td>" + rank + "</td>");
                                                out.println("<td>" + email.toString().substring(1, email.toString().length() - 1) + "</td>");
                                                out.println("<td>" + macAddressOfUsers.toString().substring(1, macAddressOfUsers.toString().length() - 1) + "</td>");
                                                out.println("<td>" + count + "</td>");
                                                out.println("</tr>");
                                                if (rank == topK) {
                                                    break;
                                                }
                                                rank++;
                                            }
                                            out.println("</table>");

                                            //out.println("<a href='next-place-main.jsp'>Back</a>");
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
                <%                    }
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
