package com.posdb.sync.resource;

import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.dto.sync.*;
import com.posdb.sync.service.OrderMiscSyncService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/v1/pos/misc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class OrderMiscSyncResource {

    @Inject
    OrderMiscSyncService orderMiscSyncService;


    @POST
    @Path("/ordervoidlogs/sync")
    public Response syncOrderVoidLogs(OrderVoidLogSyncRequest request, @Context HttpHeaders headers) {
        log.info("orderMiscSyncResource:: Received Sync ORDER_VOID_LOGS request: {}", request.getOrderVoidLogs().size());
        SyncResponse syncResponse = orderMiscSyncService.syncOrderVoidLogs(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }

    @POST
    @Path("/onaccountcharges/sync")
    public Response syncOnAccountCharges(OnAccountChargeSyncRequest request, @Context HttpHeaders headers) {
        log.info("orderMiscSyncResource:: Received Sync ON_ACCOUNT_CHARGES request: {}", request.getOnAccountCharges().size());
        SyncResponse syncResponse = orderMiscSyncService.syncOnAccountCharges(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }

    @POST
    @Path("/customerfiles/sync")
    public Response syncCustomerFiles(CustomerFileSyncRequest request, @Context HttpHeaders headers) {
        log.info("orderMiscSyncResource:: Received Sync CUSTOMER_FILES request: {}", request.getCustomerFiles().size());
        SyncResponse syncResponse = orderMiscSyncService.syncCustomerFiles(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }
}

