package com.posdb.sync.service;

import com.posdb.sync.entity.UserRestaurant;
import com.posdb.sync.exception.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class SubscriptionService {

    @PersistenceContext
    EntityManager entityManager;

    public boolean isSubscriptionValid(UUID userId, UUID restaurantId) {
        try {
            log.debug("Checking subscription validity for user: {} and restaurant: {}", userId, restaurantId);

            UserRestaurant userRestaurant = entityManager
                    .createQuery("SELECT ur FROM UserRestaurant ur WHERE ur.user.id = :userId AND ur.restaurant.id = :restaurantId", UserRestaurant.class)
                    .setParameter("userId", userId)
                    .setParameter("restaurantId", restaurantId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (userRestaurant == null) {
                log.warn("No user-restaurant relationship found for user: {} and restaurant: {}", userId, restaurantId);
                return false;
            }

            boolean isValid = userRestaurant.isSubscriptionCurrentlyValid();
            if(!isValid) {
                log.warn("Subscription is not valid for user: {} and restaurant: {}. Expiry Date: {}", userId, restaurantId, userRestaurant.getSubscriptionExpiryDate());
            } else {
                log.debug("Subscription is valid for user: {} and restaurant: {}", userId, restaurantId);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error checking subscription validity for user: {} and restaurant: {}", userId, restaurantId, e);
            return false;
        }
    }

    /**
     * Get all restaurants where user has active subscription
     */
    public List<UserRestaurant> getUserSubscribedRestaurants(UUID userId) {
        try {
            log.debug("Getting subscribed restaurants for user: {}", userId);

            List<UserRestaurant> userRestaurants = entityManager
                    .createQuery("SELECT ur FROM UserRestaurant ur WHERE ur.user.id = :userId", UserRestaurant.class)
                    .setParameter("userId", userId)
                    .getResultList();

            List<UserRestaurant> activeSubscriptions = userRestaurants.stream()
                    .filter(UserRestaurant::isSubscriptionCurrentlyValid)
                    .collect(Collectors.toList());

            log.debug("Found {} active subscriptions for user: {}", activeSubscriptions.size(), userId);
            return activeSubscriptions;
        } catch (Exception e) {
            log.error("Error getting subscribed restaurants for user: {}", userId, e);
            return List.of();
        }
    }

    /**
     * Validate subscription before sync operations - throws exception if invalid
     */
    public void validateSubscriptionBeforeSync(UUID restaurantId) {
        UserRestaurant userRestaurant = entityManager
                .createQuery("SELECT ur FROM UserRestaurant ur WHERE ur.restaurant.id = :restaurantId", UserRestaurant.class)
                .setParameter("restaurantId", restaurantId)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (userRestaurant == null) {
            log.warn("No user-restaurant relationship found for restaurant: {}", restaurantId);
            throw new AppException("No user-restaurant relationship found for restaurant. Please renew your subscription to continue sync operations.", Response.Status.FORBIDDEN);
        }

        boolean isValid = userRestaurant.isSubscriptionCurrentlyValid();
        if (!isValid) {
            log.warn("Subscription validation failed for restaurant: {} - sync operation denied. Expiry Date: {} ", restaurantId, userRestaurant.getSubscriptionExpiryDate());
            throw new AppException("Subscription expired or inactive for this restaurant. Please renew your subscription to continue sync operations.", Response.Status.PAYMENT_REQUIRED);
        }
        log.debug("Subscription validation passed for restaurant: {}", restaurantId);
    }

    /**
     * Validate subscription for dashboard access - throws exception if invalid
     */
    public void validateSubscriptionForDashboard(UUID userId, UUID restaurantId) {
        if (!isSubscriptionValid(userId, restaurantId)) {
            log.warn("Subscription validation failed for user: {} and restaurant: {} - dashboard access denied", userId, restaurantId);
            throw new AppException("Subscription expired or inactive for this restaurant. Please renew your subscription to access dashboard data.", Response.Status.PAYMENT_REQUIRED);
        }
        log.debug("Dashboard subscription validation passed for user: {} and restaurant: {}", userId, restaurantId);
    }

    /**
     * Get UserRestaurant entity for user and restaurant
     */
    public UserRestaurant getUserRestaurant(UUID userId, UUID restaurantId) {
        try {
            return entityManager
                    .createQuery("SELECT ur FROM UserRestaurant ur WHERE ur.user.id = :userId AND ur.restaurant.id = :restaurantId", UserRestaurant.class)
                    .setParameter("userId", userId)
                    .setParameter("restaurantId", restaurantId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error getting UserRestaurant for user: {} and restaurant: {}", userId, restaurantId, e);
            return null;
        }
    }
}
