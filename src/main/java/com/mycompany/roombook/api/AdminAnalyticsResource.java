package com.mycompany.roombook.api;

import com.mycompany.roombook.api.services.AnalyticsService;
import com.mycompany.roombook.api.services.UserService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/admin/analytics")
public class AdminAnalyticsResource {
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final UserService userService = new UserService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboard(@HeaderParam("X-User-Id") Integer userId) {
        if (userId == null || !userService.isAdmin(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Admin access is required to view analytics."))
                    .build();
        }

        return Response.ok(analyticsService.getDashboard()).build();
    }
}
