package com.posdb.sync.resource;

import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.DailyOrderResponse;
import com.posdb.sync.service.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class DashboardResource {

    @Inject
    DashboardService dashboardService;

    @GET
    @Path("/orders/daily")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDailyOrders(
            @QueryParam("restaurantId") String restaurantId,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) {
        log.info("DashboardResource:: Daily orders report requested for restaurantId: {}, from: {}, to: {}",
                restaurantId, fromDate, toDate);

        dashboardService.getDailyOrders(restaurantId, fromDate, toDate);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(201, "Success"))
                .build();
    }

    @GET
    @Path("/orders")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getOrders(
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @QueryParam("limit") @DefaultValue("100") Integer limit,
            @QueryParam("offset") @DefaultValue("0") Integer offset) {
        try {
            String restaurantId = securityIdentity.getPrincipal().getName();
            log.info("Orders list requested for restaurantId: {}, from: {}, to: {}, limit: {}, offset: {}",
                    restaurantId, fromDate, toDate, limit, offset);

}

