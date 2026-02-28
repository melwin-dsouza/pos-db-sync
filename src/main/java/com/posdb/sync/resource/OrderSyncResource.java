package com.posdb.sync.resource;

import com.posdb.sync.dto.request.OrderHeaderSyncRequest;
import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.request.OrderPaymentSyncRequest;
import com.posdb.sync.dto.request.OrderTransactionSyncRequest;
import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.service.OrderSyncService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import static com.posdb.sync.dto.constants.AppConstants.API_KEY;

@Path("/api/v1/pos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class OrderSyncResource {

    @Inject
    OrderSyncService orderSyncService;

    @POST
    @Path("/orderheaders/sync")
    public Response syncOrderHeaders(OrderHeaderSyncRequest request, @Context HttpHeaders headers) {
        log.info("OrderSyncResource:: Received Sync ORDER_HEADERS request: {}", request);
        String apiKey = headers.getHeaderString(API_KEY);
        if(apiKey == null || apiKey.isEmpty()) {
            log.warn("OrderSyncResource:: Missing API Key in headers");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse<>(400, null, "Missing API Key in headers"))
                    .build();
        }
        SyncResponse syncResponse = orderSyncService.syncOrderHeaders(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }

    @POST
    @Path("/orderpayments/sync")
    public Response syncOrderPayments(OrderPaymentSyncRequest request, @Context HttpHeaders headers) {
        log.info("OrderSyncResource:: Received Sync ORDER_PAYMENTS request: {}", request);
        SyncResponse syncResponse = orderSyncService.syncOrderPayments(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }

    @POST
    @Path("/ordertransactions/sync")
    public Response syncOrderTransactions(OrderTransactionSyncRequest request, @Context HttpHeaders headers) {
        log.info("OrderSyncResource:: Received Sync ORDER_TRANSACTIONS request: {}", request);
        SyncResponse syncResponse = orderSyncService.syncOrderTransactions(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }
}

