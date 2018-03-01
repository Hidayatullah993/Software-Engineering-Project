package servlet;

import dao.BootstrapDAO;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.util.*;
import java.util.logging.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import util.*;

@WebServlet("/bootstrap")
public class BootstrapServlet extends HttpServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NullPointerException {
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
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();

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


                    RequestDispatcher view = request.getRequestDispatcher("bootstrap-display.jsp");
                    request.setAttribute("result", jsonOutput);
                    view.forward(request, response);
                    return;
                }
            } else {
                //check that file is a zip file
                String fileName = item.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

                // process file upload if file is a zip
                if (fileExtension.equals(".zip")) {
                    File uploadedFile = new File(repository + "/" + item.getName());
                    try {
                        item.write(uploadedFile);
                    } catch (Exception ex) {
                        Logger.getLogger(BootstrapServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //unzip file
                    UnzipUtility unzipUtil = new UnzipUtility();

                    unzipUtil.unzip(uploadedFile.getAbsolutePath(), repository.getAbsolutePath());
                    uploadedFile.delete();

                }

                String locationLookupPath = repository.getAbsolutePath() + "/location-lookup.csv";
                String demographicsPath = repository.getAbsolutePath() + "/demographics.csv";
                String locationPath = repository.getAbsolutePath() + "/location.csv";

                File locationLookup = new File(locationLookupPath);
                File demographics = new File(demographicsPath);
                File location = new File(locationPath);

                if (locationLookup.exists()) {
                    BootstrapDAO.bootstrap(jsonOutput, locationLookupPath, demographicsPath, locationPath);
                    locationLookup.delete();
                    new File(repository.getAbsolutePath() + "/location-lookup-validated.csv").delete();
                    demographics.delete();
                    new File(repository.getAbsolutePath() + "/demographics-validated.csv").delete();
                    location.delete();
                    new File(repository.getAbsolutePath() + "/location-validated.csv").delete();
                } else {
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
                }
                JSONArray errors = (JSONArray) jsonOutput.get("error");
                if (errors != null && errors.isEmpty()) {
                    jsonOutput.put("status", "success");
                    jsonOutput.remove("error");
                } else {
                    jsonOutput.put("status", "error");
                }
//                response.setContentType("text/html");
//                try (PrintWriter out = response.getWriter()) {
//                    String output = gson.toJson(jsonOutput);
//                    out.println(output);
//                }

            }
        }
        RequestDispatcher view = request.getRequestDispatcher("bootstrap-display.jsp");
        request.setAttribute("result", jsonOutput);
      
        System.out.println(jsonOutput);
        view.forward(request, response);
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
