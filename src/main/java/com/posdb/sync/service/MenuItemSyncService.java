package com.posdb.sync.service;

import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.dto.sync.MenuItemData;
import com.posdb.sync.dto.sync.MenuItemSyncRequest;
import com.posdb.sync.entity.MenuItem;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.repository.MenuItemRepository;
import com.posdb.sync.utils.TextUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.OffsetDateTime;

import static com.posdb.sync.dto.constants.AppConstants.API_KEY;

@ApplicationScoped
@Slf4j
public class MenuItemSyncService {

    @ConfigProperty(name = "pos.sync.batch.size", defaultValue = "500")
    Integer maxBatchSize;

    @Inject
    ApiKeyValidatorService apiKeyValidatorService;

    @Inject
    MenuItemRepository menuItemRepository;

    @Transactional
    public SyncResponse syncMenuItems(MenuItemSyncRequest request, HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);
            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantId(apiKey);
            StringBuilder failureMessage = new StringBuilder("\n........RestaurantName: " + restaurant.getName() + ".......\n.........MENU_ITEMS........\n ");

            log.info("MenuItemSyncService::syncMenuItems Validated API Key for restaurant: {} with records:{}",
                    restaurant.getName(),request.getMenuItems() != null ? request.getMenuItems().size() : 0);

            if (request.getMenuItems() == null || request.getMenuItems().isEmpty()) {
                log.warn("MenuItemSyncService::syncMenuItems Menu items list is empty or null for restaurant: {}", restaurant.getName());
                throw new AppException("Menu items list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getMenuItems().size() > maxBatchSize) {
                log.warn("MenuItemSyncService::syncMenuItems Batch size {} exceeds maximum limit of {} for restaurant: {}",
                        request.getMenuItems().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0, 0);

            for (MenuItemData data : request.getMenuItems()) {
                result = processMenuItemData(data, restaurant, failureMessage, result);
            }

            if (result.failCount() > 0) {
                log.warn("MenuItemSyncService::syncMenuItems Completed with failures for restaurant: {}. Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
                throw new AppException("Menu item sync completed with some failures. Success: " + result.successCount()
                        + ", Failed: " + result.failCount() + "..\n FailureMessages: " + failureMessage,
                        Response.Status.INTERNAL_SERVER_ERROR);
            }else {
                log.info("MenuItemSyncService::syncMenuItems Successfully synced all menu items for restaurant: {}. Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
            }

            return new SyncResponse(request.getMenuItems().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid or missing API key", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("MenuItemSyncService::syncMenuItems Error syncing menu items", e);
            throw new AppException("Failed to sync menu items.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processMenuItemData(MenuItemData data, Restaurant restaurant, StringBuilder failureMessage, Result result) {
        try {
            int recordIndex = result.recordIndex() + 1;
            MenuItem menuItem = new MenuItem();
            if (TextUtil.isEmpty(String.valueOf(data.getMenuItemId())) || TextUtil.isEmpty(data.getMenuItemText())) {
                log.warn("MenuItemSyncService::processMenuItemData Missing required fields for menu item at index: {}. MenuItemId: {}, MenuItemText: {}",
                        recordIndex, data.getMenuItemId(), data.getMenuItemText());
                throw new AppException("menuItemId and menuItemText are required for menu item. Index: " + recordIndex,
                        Response.Status.BAD_REQUEST);
            }
            menuItem.setRestaurant(restaurant);
            menuItem.setMenuItemId(data.getMenuItemId());
            menuItem.setMenuItemText(data.getMenuItemText());
            menuItem.setMenuCategoryId(data.getMenuCategoryId());
            menuItem.setMenuGroupId(data.getMenuGroupId());
            menuItem.setDisplayIndex(data.getDisplayIndex());
            menuItem.setDefaultUnitPrice(data.getDefaultUnitPrice());
            menuItem.setMenuItemDescription(data.getMenuItemDescription());
            menuItem.setMenuItemNotification(data.getMenuItemNotification());
            menuItem.setMenuItemInActive(data.getMenuItemInActive());
            menuItem.setMenuItemInStock(data.getMenuItemInStock());
            menuItem.setMenuItemDiscountable(data.getMenuItemDiscountable());
            menuItem.setMenuItemType(data.getMenuItemType());
            menuItem.persist();
            log.debug("MenuItemSyncService::processMenuItemData Successfully synced menu item at index: {}. MenuItemId: {}, restaurant: {}",
                    recordIndex, data.getMenuItemId(), restaurant.getName());
            return new Result(result.successCount() + 1, result.failCount(), recordIndex);
        } catch (Exception e) {
            int recordIndex = result.recordIndex() + 1;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex)
                    .append(" MenuItemId: ").append(data.getMenuItemId())
                    .append(". Error: ").append(e.getMessage()).append("\n");
            log.error("MenuItemSyncService::processMenuItemData. Failed to sync menu items. at index: {}. MenuItemId: {}, restaurant: {}",
                    recordIndex, data.getMenuItemId(), restaurant.getName(), e);
            return new Result(result.successCount(), result.failCount() + 1, recordIndex);
        }
    }

    @Transactional
    public SyncResponse fullSyncMenuItems(MenuItemSyncRequest request, HttpHeaders headers) {
        try {
            String apiKey = headers.getHeaderString(API_KEY);
            Restaurant restaurant = apiKeyValidatorService.validateAndGetRestaurantId(apiKey);
            StringBuilder failureMessage = new StringBuilder("\n........RestaurantName: " + restaurant.getName() + ".......\n.........MENU_ITEMS_FULL_SYNC........\n ");

            log.info("MenuItemSyncService::fullSyncMenuItems Full sync request received for restaurant: {} with records:{}",
                    restaurant.getName(), request.getMenuItems() != null ? request.getMenuItems().size() : 0);

            if (request.getMenuItems() == null || request.getMenuItems().isEmpty()) {
                log.warn("MenuItemSyncService::fullSyncMenuItems Menu items list is empty or null for restaurant: {}", restaurant.getName());
                throw new AppException("Menu items list is required", Response.Status.BAD_REQUEST);
            }

            if (request.getMenuItems().size() > maxBatchSize) {
                log.warn("MenuItemSyncService::fullSyncMenuItems Batch size {} exceeds maximum limit of {} for restaurant: {}",
                        request.getMenuItems().size(), maxBatchSize, restaurant.getName());
                throw new AppException("Maximum batch size exceeded", Response.Status.BAD_REQUEST);
            }

            Result result = new Result(0, 0, 0);

            for (MenuItemData data : request.getMenuItems()) {
                result = processMenuItemDataUpsert(data, restaurant, failureMessage, result);
            }

            if (result.failCount() > 0) {
                log.warn("MenuItemSyncService::fullSyncMenuItems Completed with failures for restaurant: {}. Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
                throw new AppException("Menu item full sync completed with some failures. Success: " + result.successCount()
                        + ", Failed: " + result.failCount() + "..\n FailureMessages: " + failureMessage,
                        Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                log.info("MenuItemSyncService::fullSyncMenuItems Successfully synced all menu items for restaurant: {}. Success: {}, Failed: {}",
                        restaurant.getName(), result.successCount(), result.failCount());
                restaurant.setLastSyncTime(OffsetDateTime.now());
                restaurant.persist();
            }

            return new SyncResponse(request.getMenuItems().size(), result.successCount(), result.failCount(), failureMessage.toString());
        } catch (AppException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid or missing API key", Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("MenuItemSyncService::fullSyncMenuItems Error syncing menu items", e);
            throw new AppException("Failed to sync menu items.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Result processMenuItemDataUpsert(MenuItemData data, Restaurant restaurant, StringBuilder failureMessage, Result result) {
        try {
            int recordIndex = result.recordIndex() + 1;
            if (TextUtil.isEmpty(String.valueOf(data.getMenuItemId())) || TextUtil.isEmpty(data.getMenuItemText())) {
                log.warn("MenuItemSyncService::processMenuItemDataUpsert Missing required fields for menu item at index: {}. MenuItemId: {}, MenuItemText: {}",
                        recordIndex, data.getMenuItemId(), data.getMenuItemText());
                throw new AppException("menuItemId and menuItemText are required for menu item. Index: " + recordIndex,
                        Response.Status.BAD_REQUEST);
            }

            MenuItem menuItem = menuItemRepository.findByRestaurantAndMenuItemId(restaurant, data.getMenuItemId())
                    .orElseGet(() -> {
                        log.info("MenuItemSyncService::processMenuItemDataUpsert Creating new menu item. MenuItemId: {}, restaurant: {}",
                                data.getMenuItemId(), restaurant.getName());
                        MenuItem newItem = new MenuItem();
                        newItem.setRestaurant(restaurant);
                        newItem.setMenuItemId(data.getMenuItemId());
                        return newItem;
                    });

            menuItem.setMenuItemText(data.getMenuItemText());
            menuItem.setMenuCategoryId(data.getMenuCategoryId());
            menuItem.setMenuGroupId(data.getMenuGroupId());
            menuItem.setDisplayIndex(data.getDisplayIndex());
            menuItem.setDefaultUnitPrice(data.getDefaultUnitPrice());
            menuItem.setMenuItemDescription(data.getMenuItemDescription());
            menuItem.setMenuItemNotification(data.getMenuItemNotification());
            menuItem.setMenuItemInActive(data.getMenuItemInActive());
            menuItem.setMenuItemInStock(data.getMenuItemInStock());
            menuItem.setMenuItemDiscountable(data.getMenuItemDiscountable());
            menuItem.setMenuItemType(data.getMenuItemType());

            menuItem.persist();
            log.debug("MenuItemSyncService::processMenuItemDataUpsert Upserted menu item at index: {}. MenuItemId: {}, restaurant: {}",
                    recordIndex, data.getMenuItemId(), restaurant.getName());
            return new Result(result.successCount() + 1, result.failCount(), recordIndex);
        } catch (Exception e) {
            int recordIndex = result.recordIndex() + 1;
            failureMessage.append("Failure at recordIndex: ").append(recordIndex)
                    .append(" MenuItemId: ").append(data.getMenuItemId())
                    .append(". Error: ").append(e.getMessage()).append("\n");
            log.error("MenuItemSyncService::processMenuItemDataUpsert. Failed to upsert menu items. at index: {}. MenuItemId: {}, restaurant: {}",
                    recordIndex, data.getMenuItemId(), restaurant.getName(), e);
            return new Result(result.successCount(), result.failCount() + 1, recordIndex);
        }
    }

    private record Result(int successCount, int failCount, int recordIndex) {
    }
}

