/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api;

import com.mycompany.roombook.api.models.Room;
import com.mycompany.roombook.api.services.RoomService;
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
import jakarta.ws.rs.QueryParam;

/**
 *
 * @author kcuar
 */

@Path("/rooms")
public class RoomResource {
 private final RoomService service = new RoomService();
 private final UserService userService = new UserService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getRooms() {
        return service.getAll();
    }
    
    @GET
@Path("/{id}/availability")
@Produces(MediaType.APPLICATION_JSON)
public Object checkAvailability(
        @PathParam("id") int id,
        @QueryParam("date") String date,
        @QueryParam("startTime") String startTime,
        @QueryParam("endTime") String endTime) {

    boolean available = service.isAvailable(id, date, startTime, endTime);

    if (available) {
        return Map.of(
                "roomId", id,
                "available", true,
                "message", "Room is available."
        );
    } else {
        return Map.of(
                "roomId", id,
                "available", false,
                "message", "Room is not available for this date and time."
        );
    }
}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam("X-User-Id") Integer userId, Room req) {
        // Only admins are allowed to create rooms.
        Response forbidden = requireAdmin(userId);
        if (forbidden != null) {
            return forbidden;
        }

        try {
            Room created = service.create(req);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @HeaderParam("X-User-Id") Integer userId,
            @PathParam("id") int id,
            Room req
    ) {
        // Only admins are allowed to update rooms.
        Response forbidden = requireAdmin(userId);
        if (forbidden != null) {
            return forbidden;
        }

        try {
            Room updated = service.update(id, req);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam("X-User-Id") Integer userId, @PathParam("id") int id) {
        // Only admins are allowed to delete rooms.
        Response forbidden = requireAdmin(userId);
        if (forbidden != null) {
            return forbidden;
        }

        try {
            return Response.ok(Map.of("message", service.delete(id))).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    // Checks the logged-in user's role before allowing room management actions.
    private Response requireAdmin(Integer userId) {
        if (userId == null || !userService.isAdmin(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Admin access is required to manage rooms."))
                    .build();
        }

        return null;
    }
}
