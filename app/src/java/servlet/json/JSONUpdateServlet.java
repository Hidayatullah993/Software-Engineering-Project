/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.BootstrapDAO;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import servlet.BootstrapServlet;
import util.UnzipUtility;
import util.Validator;

/**
 *
 * @author shenying
 */
@WebServlet("/json/update")
public class JSONUpdateServlet extends HttpServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        // create a factory
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // configure a repo
        ServletContext servletContext = this.getServletConfig().getServletContext();

        File repository = null;

        if (System.getProperty("os.name").equals("Linux")) {
            repository = new File("/tmp");
        } else {
            repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        }

        factory.setRepository(repository);

        // create file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // parse request
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException ex) {
            Logger.getLogger(BootstrapServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONObject jsonOutput = new JSONObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // process items
        Iterator<FileItem> iter = items.iterator();

        while (iter.hasNext()) {
            DiskFileItem item = (DiskFileItem) iter.next();
            if (item.isFormField()) {
                String inputToken = item.getString();
                Validator.validateToken(inputToken, jsonOutput);

                //if the output object has any error messages, print the json object and return
                if (jsonOutput.containsKey("messages")) {
                    jsonOutput.put("status", "error");
                    Collections.sort((List<String>) jsonOutput.get("messages"));

                    //response.setContentType("text/html");
                    try (PrintWriter out = response.getWriter()) {
                        String output = gson.toJson(jsonOutput);
                        out.println(output);
                    }
                    return;
                }
            } else {
                //check that file is a zip file
                String fileName = item.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

                // process file upload if file is a zip
                if (fileExtension.equals(".zip")) {
                    File uploadedFile = new File(repository + "/" + item.getName());
                    item.write(uploadedFile);

                    //unzip file
                    UnzipUtility unzipUtil = new UnzipUtility();

                    unzipUtil.unzip(uploadedFile.getAbsolutePath(), repository.getAbsolutePath());
                    uploadedFile.delete();

                }

                String demographicsPath = repository.getAbsolutePath() + "/demographics.csv";
                String locationPath = repository.getAbsolutePath() + "/location.csv";

                File demographics = new File(demographicsPath);
                File location = new File(locationPath);

                //update
                if (demographics.exists()) {
                    BootstrapDAO.update(jsonOutput, demographicsPath);
                    demographics.delete();
                    new File(repository.getAbsolutePath() + "/demographics-validated.csv").delete();
                }
                if (location.exists()) {
                    BootstrapDAO.update(jsonOutput, locationPath);
                    location.delete();
                    new File(repository.getAbsolutePath() + "/location-validated.csv").delete();
                }

                JSONArray errors = (JSONArray) jsonOutput.get("error");
                if (errors.isEmpty()) {
                    jsonOutput.put("status", "success");
                    jsonOutput.remove("error");
                } else {
                    jsonOutput.put("status", "error");
                }
                //response.setContentType("text/html");
                try (PrintWriter out = response.getWriter()) {
                    String output = gson.toJson(jsonOutput);
                    out.println(output);
                }
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(JSONUpdateServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(JSONUpdateServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
