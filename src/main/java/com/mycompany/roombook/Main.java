/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook;



import com.mycompany.roombook.api.database.DB;
import com.mycompany.roombook.api.database.DBInit;
import jakarta.ws.rs.Path;
import java.net.URI;
import java.sql.Connection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 *
 * @author kcuar
 */
@Path("/hello")
public class Main {
    public static final String BASE_URI = "http://localhost:8080/api/";

  public static HttpServer startServer() {
    final ResourceConfig rc = new ResourceConfig()
            .packages("com.mycompany.roombook.api")
            .register(JacksonFeature.class);

    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
}

    public static void main(String[] args) {
        System.out.println("Starting server...");

        // ✅ TEST DATABASE CONNECTION
        try (Connection conn = DB.getConnection()) {
            if (conn != null) {
                System.out.println("✔ Database connected successfully!");
            } else {
                System.out.println("❌ Database NOT connected!");
            }
        } catch (Exception e) {
            System.out.println("DB Connection Error: " + e.getMessage());
        }

        // OPTIONAL: Create tables
        DBInit.initialize();

        // Start server
        HttpServer server = startServer();
        System.out.println("API available at " + BASE_URI + "hello");
    }
}
