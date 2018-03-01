package servlet;

import dao.UserDAO;
import is203.JWTUtility;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.util.*;

/**
 * Java servlet that validates and process the login process
 * 
 */

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs 
     * @throws IOException if an I/O error occurs 
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //setup output
        /* response.setContentType("text/html"); 
        PrintWriter out = response.getWriter(); */

        ArrayList<String> errorMsgs = new ArrayList<String>();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        //prompts user to re-enter email id
        if (username == null || username.length() == 0) {
            errorMsgs.add("username is required");
        }
        //prompts user to re-enter password
        if (password == null || password.length() == 0) {
            errorMsgs.add("password is required");
        }

        if (errorMsgs != null && errorMsgs.size() > 0) {
            request.setAttribute("errorList", errorMsgs);
            RequestDispatcher view = request.getRequestDispatcher("index.jsp");
            view.forward(request, response);
            return;
        }
        //Check is login user is an admin 
        if (username.equals("admin")) {
            if (password.equals("qwerty123")) {
                HttpSession session = request.getSession();
                String sharedSecret = "qwertyuiopasdfgh";

                String token = JWTUtility.sign(sharedSecret, username);
                session.setAttribute("token",token);
                response.sendRedirect("home.jsp");
                return;
            } else {
                errorMsgs.add("admin login failed");
                request.setAttribute("errorList", errorMsgs);
                RequestDispatcher view = request.getRequestDispatcher("index.jsp");
                view.forward(request, response);
                return;
            }
        }

        boolean valid = UserDAO.validate(username, password);

        if (valid) {
            HttpSession session = request.getSession();
            String sharedSecret = "qwertyuiopasdfgh";

            String token = JWTUtility.sign(sharedSecret, username);
            session.setAttribute("token", token);
            response.sendRedirect("home.jsp"); //Main Page

        } else {
            //error message 
            errorMsgs.add("email/password is wrong");
            request.setAttribute("errorList", errorMsgs);
            RequestDispatcher view = request.getRequestDispatcher("index.jsp");
            view.forward(request, response);
            return;
        }

   
    }
}
