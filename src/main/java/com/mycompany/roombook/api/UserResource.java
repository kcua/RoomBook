/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api;

import com.mycompany.roombook.api.models.User;
import com.mycompany.roombook.api.models.UserLoginRequest;
import com.mycompany.roombook.api.models.UserRegisterRequest;
import com.mycompany.roombook.api.services.UserService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

/**
 *
 * @author kcuar
 */
    @Path("/users")
public class UserResource {

    private final UserService service = new UserService();

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object register(UserRegisterRequest req) {
        try {
            User created = service.register(req);
            return created;
        } catch (IllegalArgumentException ex) {
            return Map.of("error", ex.getMessage());
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object login(UserLoginRequest req) {
        try {
            User user = service.login(req);
            return user;
        } catch (IllegalArgumentException ex) {
            return Map.of("error", ex.getMessage());
        }
    }
}
