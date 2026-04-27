/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api;

import com.mycompany.roombook.api.models.Reservation;
import com.mycompany.roombook.api.models.ReservationCreateRequest;
import com.mycompany.roombook.api.services.ReservationService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kcuar
 */
@Path("/reservations")
public class ReservationResource {
    private final ReservationService service = new ReservationService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Reservation> getAll() {
        return service.getAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object create(ReservationCreateRequest req) {
        try {
            Reservation created = service.create(req);
            return created;
        } catch (IllegalArgumentException ex) {
            return Map.of("error", ex.getMessage());
        }
    }
    
    @DELETE
@Path("/{id}")
@Produces(MediaType.APPLICATION_JSON)
public Object cancel(@PathParam("id") int id) {
    try {
        return Map.of("message", service.cancel(id));
    } catch (IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
}
