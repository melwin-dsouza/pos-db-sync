package com.posdb.sync.resource;

import com.posdb.sync.dto.request.CreateUserRequest;
import com.posdb.sync.dto.request.CreateRestaurantRequest;
import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.CreateRestaurantResponse;
import com.posdb.sync.service.AdminService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/v1/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AdminResource {

    @Inject
    AdminService adminService;

    // For creating new restaurant, For internal use only, not exposed to mobile app.
    // Used by admin panel to create new restaurant and its first user (owner)

    @POST
    @Path("/restaurant")
    public Response createRestaurant(CreateRestaurantRequest request) {
        log.info("AdminResource:: Creating new restaurant with name: {}", request);
        CreateRestaurantResponse restaurant = adminService.createRestaurant(request);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(201, restaurant, null))
                .build();
    }

    @POST
    @Path("/restaurants/{restaurantId}/user")
    public Response createUser(@PathParam("restaurantId") String restaurantId, CreateUserRequest request) {
        log.info("AdminResource:: Creating new user for restaurantId: {}, request:{}", restaurantId, request);
        adminService.createUser(restaurantId, request);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(201, "Success", null))
                .build();
    }

    //adding restaurant under existing owner not available right now, will be done through query.
}

