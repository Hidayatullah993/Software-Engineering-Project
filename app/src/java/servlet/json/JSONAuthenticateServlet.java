/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.UserDAO;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author User
 */
@WebServlet("/json/authenticate")
public class JSONAuthenticateServlet extends HttpServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        JSONObject jsonOutput = new JSONObject();

        if (username == null || password == null) {
            jsonOutput.put("status", "error");
            JSONArray messages = new JSONArray();
            messages.add("invalid username/password");
            jsonOutput.put("messages", messages);
        }

        if (username.equals("admin")) {
            if (password.equals("qwerty123")) {
                String sharedSecret = "qwertyuiopasdfgh";

                String token = JWTUtility.sign(sharedSecret, username);

                jsonOutput.put("status", "success");
                jsonOutput.put("token", token);
            } else {
                jsonOutput.put("status", "error");
                JSONArray messages = new JSONArray();
                messages.add("invalid username/password");
                jsonOutput.put("messages", messages);
            }

            try (PrintWriter out = response.getWriter()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String output = gson.toJson(jsonOutput);
                out.println(output);
            }
            return;
        }

        if (username != null && password != null) {
            boolean valid = UserDAO.validate(username, password);
            if (valid) {
                String sharedSecret = "qwertyuiopasdfgh";

                String token = JWTUtility.sign(sharedSecret, username);

                jsonOutput.put("status", "success");
                jsonOutput.put("token", token);
            } else {
                jsonOutput.put("status", "error");
                JSONArray messages = new JSONArray();
                messages.add("invalid username/password");
                jsonOutput.put("messages", messages);
            }
        }

        try (PrintWriter out = response.getWriter()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String output = gson.toJson(jsonOutput);
            out.println(output);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
