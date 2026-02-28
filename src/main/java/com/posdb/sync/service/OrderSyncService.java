package com.posdb.sync.service;

import com.posdb.sync.dto.request.OrderHeaderSyncRequest;
import com.posdb.sync.dto.request.OrderPaymentSyncRequest;
import com.posdb.sync.dto.request.OrderTransactionSyncRequest;
import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.dto.table.OrderHeaderData;
import com.posdb.sync.dto.table.OrderPaymentData;
import com.posdb.sync.dto.table.OrderTransactionData;
import com.posdb.sync.entity.OrderHeader;
import com.posdb.sync.entity.OrderPayment;
import com.posdb.sync.entity.OrderTransaction;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.entity.enums.OrderTypeEnum;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.utils.TextUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.ParseException;
import java.util.Date;

import static com.posdb.sync.dto.constants.AppConstants.API_KEY;

@ApplicationScoped
@Slf4j
public class OrderSyncService {

    @ConfigProperty(name = "pos.sync.batch.size", defaultValue = "500")
    Integer maxBatchSize;


    @Inject
    ApiKeyValidatorService apiKeyValidatorService;

    @Transactional
    public SyncResponse syncOrderHeaders(OrderHeaderSyncRequest request, HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);
            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantId(apiKey);
            StringBuilder failureMessage = new StringBuilder("........RestaurantName: " + restaurant.getName() + ".......\n .........ORDER_HEADERS........\n ");

            log.info("OrderSyncService:: Order header sync request received for restaurantId: {} with {} records",
                    restaurant.getName(), request.getOrderHeaders() != null ? request.getOrderHeaders().size() : 0);

            if (request.getOrderHeaders() == null || request.getOrderHeaders().isEmpty()) {
                log.warn("OrderSyncService:: Empty order headers list in sync request for restaurantId: {}", restaurant.getName());
                throw new AppException("Order headers list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getOrderHeaders().size() > maxBatchSize) {
                log.warn("OrderSyncService:: Order headers batch size {} exceeds maximum {} for restaurantId: {}",
                        request.getOrderHeaders().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0);

            for (OrderHeaderData data : request.getOrderHeaders()) {
                result = processOrderHeaderData(data, restaurant, failureMessage);

            }

            if (result.failCount > 0) {
                log.warn("OrderSyncService:: Order header sync failed with some failures for restaurantId: {} - Success: {}, Failed: {}. Failure details: {}",
                        restaurant.getName(), result.successCount, result.failCount, failureMessage);
                throw new AppException("Order header sync completed with some failures. Success: " + result.successCount + ", Failed: " + result.failCount
                        + "..\n FailureMessages: " + failureMessage, Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("OrderSyncService:: Order header sync completed successfully for restaurantId: {} - Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount, result.failCount);
            }
            return new SyncResponse(request.getOrderHeaders().size(), result.successCount, result.failCount, failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("OrderSyncService::syncOrderHeaders API key validation failed: {}", e.getMessage());
            throw new AppException("Invalid or missing API key", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("OrderSyncService::syncOrderHeaders Error syncing order headers", e);
            throw new AppException("Failed to sync order headers. ", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processOrderHeaderData(OrderHeaderData data, Restaurant restaurant, StringBuilder failureMessage) {
        int successCount = 0;
        int failCount = 0;
        int recordIndex = 0;
        try {
            recordIndex++;
            OrderHeader headerEntity = new OrderHeader();
            headerEntity.setRestaurant(restaurant);
            if (TextUtil.isEmpty(String.valueOf(data.getOrderId()))) {
                log.warn("OrderSyncService:: Missing orderId in order header data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("orderId is required for order header. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            headerEntity.setOrderId(data.getOrderId());
            try {
                Date parsedDate = TextUtil.parseDate(data.getOrderDateTime());
                headerEntity.setOrderDateTime(parsedDate);
            } catch (ParseException ex) {
                log.warn("OrderSyncService:: Parse Error orderDateTime in order header data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("orderDateTime is incorrect for  order header. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            headerEntity.setEmployeeId(data.getEmployeeId());
            headerEntity.setStationId(data.getStationId());
            headerEntity.setOrderTypeId(data.getOrderType());
            try {
                headerEntity.setOrderType(OrderTypeEnum.valueOf(OrderTypeEnum.getOrderTypeByValue(Integer.parseInt(data.getOrderType()))));
            } catch (Exception ex) {
                log.warn("OrderSyncService:: Invalid orderType in order header data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
            }
            headerEntity.setDineInTableId(data.getDineInTableId());
            headerEntity.setDriverEmployeeId(data.getDriverEmployeeId());
            headerEntity.setDiscountId(data.getDiscountId());
            headerEntity.setDiscountAmount(data.getDiscountAmount());
            headerEntity.setAmountDue(data.getAmountDue());
            headerEntity.setCashDiscountAmount(data.getCashDiscountAmount());
            headerEntity.setCashDiscountApprovalEmpId(data.getCashDiscountApprovalEmpId());
            headerEntity.setSubTotal(data.getSubTotal());
            headerEntity.setGuestNumber(data.getGuestNumber());
            try {
                Date parsedDate = TextUtil.parseDate(data.getEditTimestamp());
                headerEntity.setEditTimestamp(parsedDate);
            } catch (ParseException ex) {
                log.warn("OrderSyncService:: Parse Error editTimestamp in order header data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("editTimestamp is incorrect for  order header. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            headerEntity.setRowGuid(data.getRowGuid());
            headerEntity.persist();
            successCount++;
            log.debug("OrderSyncService:: Order header synced: orderId={}, restaurantId={}", data.getOrderId(), restaurant.getName());
        } catch (Exception e) {
            failCount++;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex).append(" OrderId: ").append(data.getOrderId()).append(". Error: ").append(e.getMessage()).append("\n");
            log.error("OrderSyncService:: Failed to sync order header. recordIndex: {} : orderId={}, restaurant={}", recordIndex, data.getOrderId(), restaurant.getName(), e);
        }
        return new Result(successCount, failCount);
    }

    @Transactional
    public SyncResponse syncOrderPayments(OrderPaymentSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);
            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantId(apiKey);
            StringBuilder failureMessage = new StringBuilder("........RestaurantName: " + restaurant.getName() + ".......\n .........ORDER_PAYMENTS........\n ");

            log.info("OrderSyncService:: Order payment sync request received for restaurantId: {} with {} records",
                    restaurant.getName(), request.getOrderPayments() != null ? request.getOrderPayments().size() : 0);

            if (request.getOrderPayments() == null || request.getOrderPayments().isEmpty()) {
                log.warn("OrderSyncService:: Empty order payments list in sync request for restaurantId: {}", restaurant.getName());
                throw new AppException("Order payments list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getOrderPayments().size() > maxBatchSize) {
                log.warn("OrderSyncService:: Order payments batch size {} exceeds maximum {} for restaurantId: {}",
                        request.getOrderPayments().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0);
            for (OrderPaymentData data : request.getOrderPayments()) {
                result = processOrderPaymentData(data, restaurant, failureMessage);
            }

            if (result.failCount() > 0) {
                log.warn("OrderSyncService:: Order payment sync failed with some failures for restaurantId: {} - Success: {}, Failed: {}. Failure details: {}",
                        restaurant.getName(), result.successCount(), result.failCount(), failureMessage);
                throw new AppException("Order payment sync completed with some failures. Success: " + result.successCount() + ", Failed: " + result.failCount()
                        + "..\n FailureMessages: " + failureMessage, Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("OrderSyncService:: Order payment sync completed successfully for restaurantId: {} - Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
            }
            return new SyncResponse(request.getOrderPayments().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("OrderSyncService::syncOrderPayments API key validation failed: {}", e.getMessage());
            throw new AppException("Invalid or missing API key", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("OrderSyncService::syncOrderPayments Error syncing order headers", e);
            throw new AppException("Failed to sync order payments. ", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processOrderPaymentData(OrderPaymentData data, Restaurant restaurant, StringBuilder failureMessage) {
        int successCount = 0;
        int failCount = 0;
        int recordIndex = 0;

        try {
            recordIndex++;
            OrderPayment payment = new OrderPayment();
            payment.setRestaurant(restaurant);
            if (TextUtil.isEmpty(String.valueOf(data.getOrderId())) || TextUtil.isEmpty(String.valueOf(data.getOrderPaymentId()))) {
                log.warn("OrderSyncService:: Missing orderId or PaymentId in order payment data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("orderId or PaymentId is required for order payment. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            payment.setOrderPaymentId(data.getOrderPaymentId());
            payment.setOrderId(data.getOrderId());
            try {
                Date parsedDate = TextUtil.parseDate(data.getPaymentDateTime());
                payment.setPaymentDateTime(parsedDate);
            } catch (ParseException ex) {
                log.warn("OrderSyncService:: Parse Error paymentDateTime in order payment data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("paymentDateTime is incorrect for  order payment. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            payment.setCashierId(data.getCashierId());
            payment.setNonCashierEmployeeId(data.getNonCashierEmployeeId());
            payment.setPaymentMethod(data.getPaymentMethod());
            payment.setAmountTendered(data.getAmountTendered());
            payment.setAmountPaid(data.getAmountPaid());
            payment.setEmployeeComp(data.getEmployeeComp());
            payment.setRowGuid(data.getRowGuid());

            payment.persist();
            successCount++;
            log.debug("Order payment synced: orderPaymentId={}, restaurantId={}", data.getOrderPaymentId(), restaurant.getName());
        } catch (Exception e) {
            failCount++;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex).append(" OrderId: ").append(data.getOrderId()).append(". Error: ").append(e.getMessage()).append("\n");
            log.error("OrderSyncService:: Failed to sync order payment. recordIndex: {} : orderId={}, restaurant={}", recordIndex, data.getOrderId(), restaurant.getName(), e);
        }

        return new Result(successCount, failCount);
    }

    @Transactional
    public SyncResponse syncOrderTransactions(OrderTransactionSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);
            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantId(apiKey);
            StringBuilder failureMessage = new StringBuilder("........RestaurantName: " + restaurant.getName() + ".......\n .........ORDER_TRANSACTIONS........\n ");

            log.info("OrderSyncService:: Order transaction sync request received for restaurantId: {} with {} records",
                    restaurant.getName(), request.getOrderTransactions() != null ? request.getOrderTransactions().size() : 0);

            if (request.getOrderTransactions() == null || request.getOrderTransactions().isEmpty()) {
                log.warn("OrderSyncService:: Empty order transactions list in sync request for restaurantId: {}", restaurant.getName());
                throw new AppException("Order transactions list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getOrderTransactions().size() > maxBatchSize) {
                log.warn("OrderSyncService:: Order transactions batch size {} exceeds maximum {} for restaurantId: {}",
                        request.getOrderTransactions().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0);
            for (OrderTransactionData data : request.getOrderTransactions()) {
                result = processOrderTransactionData(data, restaurant, failureMessage);
            }

            if (result.failCount() > 0) {
                log.warn("OrderSyncService:: Order transaction sync failed with some failures for restaurantId: {} - Success: {}, Failed: {}. Failure details: {}",
                        restaurant.getName(), result.successCount(), result.failCount(), failureMessage);
                throw new AppException("Order transaction sync completed with some failures. Success: " + result.successCount() + ", Failed: " + result.failCount()
                        + "..\n FailureMessages: " + failureMessage, Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("OrderSyncService:: Order transaction sync completed successfully for restaurantId: {} - Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
            }
            return new SyncResponse(request.getOrderTransactions().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("OrderSyncService::syncOrderTransactions API key validation failed: {}", e.getMessage());
            throw new AppException("Invalid or missing API key", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("OrderSyncService::syncOrderTransactions Error syncing order transactions", e);
            throw new AppException("Failed to sync order transactions. ", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processOrderTransactionData(OrderTransactionData data, Restaurant restaurant, StringBuilder failureMessage) {
        int successCount = 0;
        int failCount = 0;
        int recordIndex = 0;
        try {
            recordIndex++;
            OrderTransaction transaction = new OrderTransaction();
            transaction.setRestaurant(restaurant);
            if (TextUtil.isEmpty(String.valueOf(data.getOrderTransactionId())) || TextUtil.isEmpty(String.valueOf(data.getOrderId()))) {
                log.warn("OrderSyncService:: Missing orderTransactionId or orderId in order transaction data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("orderTransactionId and orderId are required for order transaction. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            transaction.setOrderTransactionId(data.getOrderTransactionId());
            transaction.setOrderId(data.getOrderId());
            transaction.setMenuItemId(data.getMenuItemId());
            transaction.setMenuItemUnitPrice(data.getMenuItemUnitPrice());
            transaction.setQuantity(data.getQuantity());
            transaction.setExtendedPrice(data.getExtendedPrice());
            transaction.setDiscountId(data.getDiscountId());
            transaction.setDiscountAmount(data.getDiscountAmount());
            transaction.setDiscountBasis(data.getDiscountBasis());
            transaction.setDiscountAmountUsed(data.getDiscountAmountUsed());
            transaction.setRowGuid(data.getRowGuid());
            transaction.persist();
            successCount++;
            log.debug("OrderSyncService:: Order transaction synced: orderTransactionId={}, restaurantId={}", data.getOrderTransactionId(), restaurant.getName());
        } catch (Exception e) {
            failCount++;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex).append(" OrderTransactionId: ").append(data.getOrderTransactionId()).append(". Error: ").append(e.getMessage()).append("\n");
            log.error("OrderSyncService:: Failed to sync order transaction. recordIndex: {} : orderTransactionId={}, restaurant={}", recordIndex, data.getOrderTransactionId(), restaurant.getName(), e);
        }
        return new Result(successCount, failCount);
    }

    private record Result(int successCount, int failCount) {
    }
}
