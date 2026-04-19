package com.posdb.sync.service;

import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.dto.sync.*;
import com.posdb.sync.entity.CustomerFile;
import com.posdb.sync.entity.OnAccountCharge;
import com.posdb.sync.entity.OrderVoidLog;
import com.posdb.sync.entity.Restaurant;
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

import static com.posdb.sync.dto.constants.AppConstants.API_KEY;

@ApplicationScoped
@Slf4j
public class OrderMiscSyncService {

    @ConfigProperty(name = "pos.sync.batch.size", defaultValue = "500")
    Integer maxBatchSize;

    @Inject
    ApiKeyValidatorService apiKeyValidatorService;

    @Transactional
    public SyncResponse syncOrderVoidLogs(OrderVoidLogSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);

            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantWithSubscription(apiKey);
            log.info("OrderMiscSyncService::syncOrderVoidLogs Subscription validation passed for restaurant: {}", restaurant.getName());

            StringBuilder failureMessage = new StringBuilder("........RestaurantName: " + restaurant.getName() + ".......\n .........ORDER_VOID_LOGS........\n ");

            log.info("OrderMiscSyncService:: Order void logs sync request received for restaurantId: {} with {} records",
                    restaurant.getName(), request.getOrderVoidLogs() != null ? request.getOrderVoidLogs().size() : 0);

            if (request.getOrderVoidLogs() == null || request.getOrderVoidLogs().isEmpty()) {
                log.warn("OrderMiscSyncService:: Empty order void logs list in sync request for restaurantId: {}", restaurant.getName());
                throw new AppException("Order void logs list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getOrderVoidLogs().size() > maxBatchSize) {
                log.warn("OrderMiscSyncService:: Order void logs batch size {} exceeds maximum {} for restaurantId: {}",
                        request.getOrderVoidLogs().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded.", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0, 0);
            for (OrderVoidLogData data : request.getOrderVoidLogs()) {
                result = processOrderVoidLogData(data, restaurant, failureMessage, result);
            }

            if (result.failCount() > 0) {
                log.warn("OrderMiscSyncService:: Order void logs sync failed with some failures for restaurantId: {} - Success: {}, Failed: {}. Failure details: {}",
                        restaurant.getName(), result.successCount(), result.failCount(), failureMessage);
                throw new AppException("Order void logs sync completed with some failures. Success: " + result.successCount() + ", Failed: " + result.failCount()
                        + "..\n FailureMessages: " + failureMessage, Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("OrderMiscSyncService:: Order void logs sync completed successfully for restaurantId: {} - Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
                restaurant.setLastSyncTime(java.time.OffsetDateTime.now());
                restaurant.persist();
            }
            return new SyncResponse(request.getOrderVoidLogs().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("OrderMiscSyncService::syncOrderVoidLogs API key validation failed: {}", e.getMessage());
            throw new AppException("Invalid or missing API key.", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("OrderMiscSyncService::syncOrderVoidLogs Error syncing order void logs", e);
            throw new AppException("Failed to sync order void logs.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processOrderVoidLogData(OrderVoidLogData data, Restaurant restaurant, StringBuilder failureMessage, Result result) {
        try {
            int recordIndex = result.recordIndex() + 1;
            OrderVoidLog voidLog = new OrderVoidLog();
            voidLog.setRestaurant(restaurant);
            if (TextUtil.isEmpty(String.valueOf(data.getOrderId()))) {
                log.warn("OrderMiscSyncService:: Missing orderId in order void log data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("orderId is required for order void log. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            voidLog.setOrderId(data.getOrderId());
            voidLog.setOrderTransactionId(data.getOrderTransactionId());
            voidLog.setEmployeeId(data.getEmployeeId());
            voidLog.setVoidReason(data.getVoidReason());
            voidLog.setVoidDateTime(data.getVoidDateTime());
            voidLog.setVoidForItemReduction(data.getVoidForItemReduction());
            voidLog.setVoidAmount(data.getVoidAmount());
            voidLog.setAutoId(data.getAutoId());
            voidLog.persist();
            log.debug("OrderMiscSyncService:: Order void log synced: orderId={}, restaurantId={}", data.getOrderId(), restaurant.getName());
            return new Result(result.successCount() + 1, result.failCount(), recordIndex);
        } catch (Exception e) {
            int recordIndex = result.recordIndex() + 1;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex).append(" OrderId: ").append(data.getOrderId()).append(". Error: ").append(e.getMessage()).append("\n");
            log.error("OrderMiscSyncService:: Failed to sync order void log. recordIndex: {} : orderId={}, restaurant={}", recordIndex, data.getOrderId(), restaurant.getName(), e);
            return new Result(result.successCount(), result.failCount() + 1, recordIndex);
        }
    }

    @Transactional
    public SyncResponse syncOnAccountCharges(OnAccountChargeSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);

            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantWithSubscription(apiKey);
            log.info("OrderMiscSyncService::syncOnAccountCharges Subscription validation passed for restaurant: {}", restaurant.getName());

            StringBuilder failureMessage = new StringBuilder("........RestaurantName: " + restaurant.getName() + ".......\n .........ON_ACCOUNT_CHARGES........\n ");

            log.info("OrderMiscSyncService:: On account charges sync request received for restaurantId: {} with {} records",
                    restaurant.getName(), request.getOnAccountCharges() != null ? request.getOnAccountCharges().size() : 0);

            if (request.getOnAccountCharges() == null || request.getOnAccountCharges().isEmpty()) {
                log.warn("OrderMiscSyncService:: Empty on account charges list in sync request for restaurantId: {}", restaurant.getName());
                throw new AppException("On account charges list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getOnAccountCharges().size() > maxBatchSize) {
                log.warn("OrderMiscSyncService:: On account charges batch size {} exceeds maximum {} for restaurantId: {}",
                        request.getOnAccountCharges().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded.", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0, 0);
            for (OnAccountChargeData data : request.getOnAccountCharges()) {
                result = processOnAccountChargeData(data, restaurant, failureMessage, result);
            }

            if (result.failCount() > 0) {
                log.warn("OrderMiscSyncService:: On account charges sync failed with some failures for restaurantId: {} - Success: {}, Failed: {}. Failure details: {}",
                        restaurant.getName(), result.successCount(), result.failCount(), failureMessage);
                throw new AppException("On account charges sync completed with some failures. Success: " + result.successCount() + ", Failed: " + result.failCount()
                        + "..\n FailureMessages: " + failureMessage, Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("OrderMiscSyncService:: On account charges sync completed successfully for restaurantId: {} - Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
                restaurant.setLastSyncTime(java.time.OffsetDateTime.now());
                restaurant.persist();
            }
            return new SyncResponse(request.getOnAccountCharges().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("OrderMiscSyncService::syncOnAccountCharges API key validation failed: {}", e.getMessage());
            throw new AppException("Invalid or missing API key.", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("OrderMiscSyncService::syncOnAccountCharges Error syncing on account charges", e);
            throw new AppException("Failed to sync on account charges.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processOnAccountChargeData(OnAccountChargeData data, Restaurant restaurant, StringBuilder failureMessage, Result result) {
        try {
            int recordIndex = result.recordIndex() + 1;
            OnAccountCharge charge = new OnAccountCharge();
            charge.setRestaurant(restaurant);
            if (TextUtil.isEmpty(String.valueOf(data.getOrderId()))) {
                log.warn("OrderMiscSyncService:: Missing orderId in on account charge data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("orderId is required for on account charge. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            charge.setOrderChargeId(data.getOrderChargeId());
            charge.setChargeDateTime(data.getChargeDateTime());
            charge.setCashierId(data.getCashierId());
            charge.setNonCashierEmployeeId(data.getNonCashierEmployeeId());
            charge.setCustomerId(data.getCustomerId());
            charge.setOrderId(data.getOrderId());
            charge.setAmountCharged(data.getAmountCharged());
            charge.setOrderChargePaymentId(data.getOrderChargePaymentId());
            charge.setEmployeeComp(data.getEmployeeComp());
            charge.setChargeDueDate(data.getChargeDueDate());
            charge.persist();
            log.debug("OrderMiscSyncService:: On account charge synced: orderChargeId={}, restaurantId={}", data.getOrderChargeId(), restaurant.getName());
            return new Result(result.successCount() + 1, result.failCount(), recordIndex);
        } catch (Exception e) {
            int recordIndex = result.recordIndex() + 1;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex).append(" OrderChargeId: ").append(data.getOrderChargeId()).append(". Error: ").append(e.getMessage()).append("\n");
            log.error("OrderMiscSyncService:: Failed to sync on account charge. recordIndex: {} : orderChargeId={}, restaurant={}", recordIndex, data.getOrderChargeId(), restaurant.getName(), e);
            return new Result(result.successCount(), result.failCount() + 1, recordIndex);
        }
    }

    @Transactional
    public SyncResponse syncCustomerFiles(CustomerFileSyncRequest request, @Context HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);

            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantWithSubscription(apiKey);
            log.info("OrderMiscSyncService::syncCustomerFiles Subscription validation passed for restaurant: {}", restaurant.getName());

            StringBuilder failureMessage = new StringBuilder("........RestaurantName: " + restaurant.getName() + ".......\n .........CUSTOMER_FILES........\n ");

            log.info("OrderMiscSyncService:: Customer files sync request received for restaurantId: {} with {} records",
                    restaurant.getName(), request.getCustomerFiles() != null ? request.getCustomerFiles().size() : 0);

            if (request.getCustomerFiles() == null || request.getCustomerFiles().isEmpty()) {
                log.warn("OrderMiscSyncService:: Empty customer files list in sync request for restaurantId: {}", restaurant.getName());
                throw new AppException("Customer files list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getCustomerFiles().size() > maxBatchSize) {
                log.warn("OrderMiscSyncService:: Customer files batch size {} exceeds maximum {} for restaurantId: {}",
                        request.getCustomerFiles().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded.", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0, 0);
            for (CustomerFileData data : request.getCustomerFiles()) {
                result = processCustomerFileData(data, restaurant, failureMessage, result);
            }

            if (result.failCount() > 0) {
                log.warn("OrderMiscSyncService:: Customer files sync failed with some failures for restaurantId: {} - Success: {}, Failed: {}. Failure details: {}",
                        restaurant.getName(), result.successCount(), result.failCount(), failureMessage);
                throw new AppException("Customer files sync completed with some failures. Success: " + result.successCount() + ", Failed: " + result.failCount()
                        + "..\n FailureMessages: " + failureMessage, Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("OrderMiscSyncService:: Customer files sync completed successfully for restaurantId: {} - Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
                restaurant.setLastSyncTime(java.time.OffsetDateTime.now());
                restaurant.persist();
            }
            return new SyncResponse(request.getCustomerFiles().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("OrderMiscSyncService::syncCustomerFiles API key validation failed: {}", e.getMessage());
            throw new AppException("Invalid or missing API key.", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("OrderMiscSyncService::syncCustomerFiles Error syncing customer files", e);
            throw new AppException("Failed to sync customer files.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processCustomerFileData(CustomerFileData data, Restaurant restaurant, StringBuilder failureMessage, Result result) {
        try {
            int recordIndex = result.recordIndex() + 1;
            CustomerFile customerFile = new CustomerFile();
            customerFile.setRestaurant(restaurant);
            if (TextUtil.isEmpty(String.valueOf(data.getCustomerId()))) {
                log.warn("OrderMiscSyncService:: Missing customerId in customer file data for restaurantId: {}, Index: {}", restaurant.getName(), recordIndex);
                throw new AppException("customerId are required for customer file. Index: " + recordIndex, Response.Status.BAD_REQUEST);
            }
            customerFile.setCustomerId(data.getCustomerId());
            customerFile.setCustomerName(data.getCustomerName());
            customerFile.setCustomerNotes(data.getCustomerNotes());
            customerFile.setDeliveryAddress(data.getDeliveryAddress());
            customerFile.setDeliveryRemarks(data.getDeliveryRemarks());
            customerFile.setDeliveryCharge(data.getDeliveryCharge());
            customerFile.setDeliveryComp(data.getDeliveryComp());
            customerFile.setPhoneNumber(data.getPhoneNumber());
            customerFile.setCompanyName(data.getCompanyName());
            customerFile.setEmailAddress(data.getEmailAddress());
            customerFile.setCustomerSinceDate(data.getCustomerSinceDate());
            customerFile.setTotalSpent(data.getTotalSpent());
            customerFile.setTotalCount(data.getTotalCount());
            customerFile.persist();
            log.debug("OrderMiscSyncService:: Customer file synced: customerId={}, restaurantId={}", data.getCustomerId(), restaurant.getName());
            return new Result(result.successCount() + 1, result.failCount(), recordIndex);
        } catch (Exception e) {
            int recordIndex = result.recordIndex() + 1;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex).append(" CustomerId: ").append(data.getCustomerId()).append(". Error: ").append(e.getMessage()).append("\n");
            log.error("OrderMiscSyncService:: Failed to sync customer file. recordIndex: {} : customerId={}, restaurant={}", recordIndex, data.getCustomerId(), restaurant.getName(), e);
            return new Result(result.successCount(), result.failCount() + 1, recordIndex);
        }
    }

    private record Result(int successCount, int failCount, int recordIndex) {
    }
}
