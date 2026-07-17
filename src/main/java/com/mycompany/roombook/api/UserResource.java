/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api;

import com.mycompany.roombook.api.models.User;
import com.mycompany.roombook.api.models.UserChangePasswordRequest;
import com.mycompany.roombook.api.models.UserLoginRequest;
import com.mycompany.roombook.api.models.UserRegisterRequest;
import com.mycompany.roombook.api.models.UserRoleUpdateRequest;
import com.mycompany.roombook.api.services.UserService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
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

    @POST
    @Path("/change-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object changePassword(UserChangePasswordRequest req) {
        try {
            return Map.of("message", service.changePassword(req));
        } catch (IllegalArgumentException ex) {
            return Map.of("error", ex.getMessage());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("X-User-Id") Integer adminUserId) {
        Response forbidden = requireAdmin(adminUserId);
        if (forbidden != null) {
            return forbidden;
        }

        List<User> users = service.getAllUsers();
        return Response.ok(users).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@HeaderParam("X-User-Id") Integer adminUserId, UserRegisterRequest req) {
        Response forbidden = requireAdmin(adminUserId);
        if (forbidden != null) {
            return forbidden;
        }

        try {
            User created = service.createUserByAdmin(req);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRole(
            @HeaderParam("X-User-Id") Integer adminUserId,
            @PathParam("id") int userId,
            UserRoleUpdateRequest req
    ) {
        Response forbidden = requireAdmin(adminUserId);
        if (forbidden != null) {
            return forbidden;
        }

        try {
            if (req == null) {
                throw new IllegalArgumentException("Role is required.");
            }

            User updated = service.updateUserRole(userId, req.getRole());
            return Response.ok(updated).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("X-User-Id") Integer adminUserId, @PathParam("id") int userId) {
        Response forbidden = requireAdmin(adminUserId);
        if (forbidden != null) {
            return forbidden;
        }

        if (adminUserId == userId) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "You cannot delete your own admin account."))
                    .build();
        }

        try {
            return Response.ok(Map.of("message", service.deleteUser(userId))).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    private Response requireAdmin(Integer userId) {
        if (userId == null || !service.isAdmin(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Admin access is required to manage users."))
                    .build();
        }

        return null;
    }
}
