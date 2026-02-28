package com.posdb.sync.resource;

import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.DashboardResponse;
import com.posdb.sync.service.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class DashboardResource {

    @Inject
    DashboardService dashboardService;

    @GET
    @Path("/")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDashboardData(@QueryParam("restaurant") String restaurantId) {
        log.info("DashboardResource:: Dashboard data requested ");
        DashboardResponse dashboardResponse=  dashboardService.getDashboardData(restaurantId);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(200, dashboardResponse, null))
                .build();
    }

//    @GET
//    @Path("/orders")
//    @RolesAllowed({"OWNER", "MANAGER"})
//    public Response getOrders(
//            @QueryParam("from") String fromDate,
//            @QueryParam("to") String toDate,
//            @QueryParam("limit") @DefaultValue("100") Integer limit,
//            @QueryParam("offset") @DefaultValue("0") Integer offset) {
//        try {
//            String restaurantId = securityIdentity.getPrincipal().getName();
//            log.info("Orders list requested for restaurantId: {}, from: {}, to: {}, limit: {}, offset: {}",
//                    restaurantId, fromDate, toDate, limit, offset);
//        }
//    }

}

