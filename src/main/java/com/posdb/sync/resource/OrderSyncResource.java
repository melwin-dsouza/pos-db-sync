package com.posdb.sync.resource;

import com.posdb.sync.dto.request.OrderHeaderSyncRequest;
import com.posdb.sync.dto.table.OrderHeaderData;
import com.posdb.sync.dto.request.OrderPaymentSyncRequest;
import com.posdb.sync.dto.table.OrderPaymentData;
import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.entity.OrderPayment;
import com.posdb.sync.service.ApiKeyValidator;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Path("/api/v1/pos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class OrderSyncResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSyncResource.class);
    private static final int MAX_BATCH_SIZE = 500;

    private final ApiKeyValidator apiKeyValidator;

    public OrderSyncResource(ApiKeyValidator apiKeyValidator) {
        this.apiKeyValidator = apiKeyValidator;
    }

    @POST
    @Path("/orderheaders/sync")
    @Transactional
    public Response syncOrderHeaders(OrderHeaderSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString("X-API-KEY");
            UUID restaurantId = apiKeyValidator.validateAndGetRestaurantId(apiKey);

            LOGGER.info("Order header sync request received for restaurantId: {} with {} records",
                    restaurantId, request.orderHeaders != null ? request.orderHeaders.size() : 0);

            if (request.orderHeaders == null || request.orderHeaders.isEmpty()) {
                LOGGER.warn("Empty order headers list in sync request for restaurantId: {}", restaurantId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Order headers list is required\"}")
                        .build();
            }

            if (request.orderHeaders.size() > MAX_BATCH_SIZE) {
                LOGGER.warn("Order headers batch size {} exceeds maximum {} for restaurantId: {}",
                        request.orderHeaders.size(), MAX_BATCH_SIZE, restaurantId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"BATCH_SIZE_EXCEEDED\", \"message\": \"Maximum batch size is " + MAX_BATCH_SIZE + "\"}")
                        .build();
            }

            int successCount = 0;
            int failCount = 0;

            for (OrderHeaderData data : request.orderHeaders) {
                try {
                    OrderHeader header = new OrderHeader();
                    header.id = UUID.randomUUID();
                    header.restaurantId = restaurantId;
                    header.orderId = data.orderId;
                    header.orderDateTime = OffsetDateTime.parse(data.orderDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    header.employeeId = data.employeeId;
                    header.stationId = data.stationId;
                    header.orderType = data.orderType;
                    header.dineInTableId = data.dineInTableId;
                    header.driverEmployeeId = data.driverEmployeeId;
                    header.discountId = data.discountId;
                    header.discountAmount = data.discountAmount;
                    header.amountDue = data.amountDue;
                    header.cashDiscountAmount = data.cashDiscountAmount;
                    header.cashDiscountApprovalEmpId = data.cashDiscountApprovalEmpId;
                    header.subTotal = data.subTotal;
                    header.guestNumber = data.guestNumber;
                    header.editTimestamp = data.editTimestamp != null ?
                            OffsetDateTime.parse(data.editTimestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
                    header.rowGuid = data.rowGuid;

                    header.persist();
                    successCount++;
                    LOGGER.debug("Order header synced: orderId={}, restaurantId={}", data.orderId, restaurantId);
                } catch (Exception e) {
                    failCount++;
                    LOGGER.error("Failed to sync order header: orderId={}, restaurantId={}", data.orderId, restaurantId, e);
                }
            }

            LOGGER.info("Order header sync completed for restaurantId: {} - Success: {}, Failed: {}",
                    restaurantId, successCount, failCount);

            return Response.ok(new SyncResponse(
                    request.orderHeaders.size(),
                    successCount,
                    failCount,
                    "Order headers synced successfully")).build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("API key validation failed: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"code\": \"UNAUTHORIZED\", \"message\": \"Invalid or missing API key\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error syncing order headers", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to sync order headers\"}")
                    .build();
        }
    }

    @POST
    @Path("/orderpayments/sync")
    @Transactional
    public Response syncOrderPayments(OrderPaymentSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString("X-API-KEY");
            UUID restaurantId = apiKeyValidator.validateAndGetRestaurantId(apiKey);

            LOGGER.info("Order payment sync request received for restaurantId: {} with {} records",
                    restaurantId, request.orderPayments != null ? request.orderPayments.size() : 0);

            if (request.orderPayments == null || request.orderPayments.isEmpty()) {
                LOGGER.warn("Empty order payments list in sync request for restaurantId: {}", restaurantId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Order payments list is required\"}")
                        .build();
            }

            if (request.orderPayments.size() > MAX_BATCH_SIZE) {
                LOGGER.warn("Order payments batch size {} exceeds maximum {} for restaurantId: {}",
                        request.orderPayments.size(), MAX_BATCH_SIZE, restaurantId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"BATCH_SIZE_EXCEEDED\", \"message\": \"Maximum batch size is " + MAX_BATCH_SIZE + "\"}")
                        .build();
            }

            int successCount = 0;
            int failCount = 0;

            for (OrderPaymentData data : request.orderPayments) {
                try {
                    OrderPayment payment = new OrderPayment();
                    payment.id = UUID.randomUUID();
                    payment.restaurantId = restaurantId;
                    payment.orderPaymentId = data.orderPaymentId;
                    payment.paymentDateTime = OffsetDateTime.parse(data.paymentDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    payment.cashierId = data.cashierId;
                    payment.nonCashierEmployeeId = data.nonCashierEmployeeId;
                    payment.orderId = data.orderId;
                    payment.paymentMethod = data.paymentMethod;
                    payment.amountTendered = data.amountTendered;
                    payment.amountPaid = data.amountPaid;
                    payment.employeeComp = data.employeeComp;
                    payment.rowGuid = data.rowGuid;

                    payment.persist();
                    successCount++;
                    LOGGER.debug("Order payment synced: orderPaymentId={}, restaurantId={}", data.orderPaymentId, restaurantId);
                } catch (Exception e) {
                    failCount++;
                    LOGGER.error("Failed to sync order payment: orderPaymentId={}, restaurantId={}", data.orderPaymentId, restaurantId, e);
                }
            }

            LOGGER.info("Order payment sync completed for restaurantId: {} - Success: {}, Failed: {}",
                    restaurantId, successCount, failCount);

            return Response.ok(new SyncResponse(
                    request.orderPayments.size(),
                    successCount,
                    failCount,
                    "Order payments synced successfully")).build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("API key validation failed: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"code\": \"UNAUTHORIZED\", \"message\": \"Invalid or missing API key\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error syncing order payments", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to sync order payments\"}")
                    .build();
        }
    }
}

