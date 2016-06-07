/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.Lee.myapplication.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.logging.Filter;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import javax.servlet.http.*;
//import javax.swing.text.html.parser.Entity;
import com.google.appengine.api.datastore.Entity;

public class MyServlet extends HttpServlet {
    private static final String BUCKET = "bucket";

    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        //resp.setContentType("text/plain");
        //resp.getWriter().println("Please use the form to POST to this url");
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /*GcsFileOptions instance = GcsFileOptions.getDefaultInstance();
        GcsFilename fileName = getFileName(req);
        System.out.println(fileName);
        GcsOutputChannel outputChannel;
        outputChannel = gcsService.createOrReplace(fileName, instance);
        copy(req.getInputStream(), Channels.newOutputStream(outputChannel));*/
        String op = req.getParameter("op");
        if(op.equals("create")){
            System.out.println("Equals create");
            try{
                addEntries(req,resp);
            }catch (Exception E){
            }
        }
        else if(op.equals("list")){
            System.out.println("Equals List");
            try {
                listEntries(req, resp);
            }catch (Exception E){
            }
        }
    }

    public void addEntries(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        char [] anArray;
        anArray = new char[10];
        Entity history = new Entity("Please work");
        history.setProperty("DateTime", "34");
        history.setProperty("DeviceID", "1234");
        history.setProperty("Latitude", "128.192");
        history.setProperty("Longitude", "1.0.1");
        datastore.put(history);
    }

    public void listEntries(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        Query q = new Query("history");
        PreparedQuery pq = datastore.prepare(q);
        for(com.google.appengine.api.datastore.Entity result : pq.asIterable()){
            String datetime = (String) result.getProperty("DateTime");
            String lat = (String) result.getProperty("Latitude");
            String log = (String) result.getProperty("Longitude");
            String dev = (String) result.getProperty("DeviceID");
            out.write("Date: " + datetime + "Device ID: " + dev + " Latitude: " + lat + " Longitude: " + log);
            out.write("\n");
        }

        //Entity history;
        String datetime = "0";
    }

    private GcsFilename getFileName(HttpServletRequest req) {
        String[] splits = req.getRequestURI().split("/", 4);
        System.out.println(req.getRequestURL());
        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
            throw new IllegalArgumentException("The URL is not formed as expected. " +
                    "Expecting /gcs/<bucket>/<object>");
        }
        return new GcsFilename(splits[2], splits[3]);
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}
