package com.posdb.sync.service;

import com.posdb.sync.entity.Restaurant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;


@ApplicationScoped
@Slf4j
public class ApiKeyValidatorService {

    @Inject
    SubscriptionService subscriptionService;

    public Restaurant validateAndGetRestaurantId(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API key is missing");
            throw new IllegalArgumentException("API key is required");
        }

        try {
            Restaurant restaurant = Restaurant.<Restaurant>find("apiKey", apiKey).firstResult();

            if (restaurant == null) {
                log.warn("Invalid API key provided");
                throw new IllegalArgumentException("Invalid API key");
            }

            if (!"ACTIVE".equals(restaurant.getStatus())) {
                log.warn("API key for inactive restaurant: {}, {}", restaurant.getId(), restaurant.getName());
                throw new IllegalArgumentException("Restaurant is not active");
            }

            log.info("API key validated successfully for restaurantId: {}, {}", restaurant.getId(), restaurant.getName());
            return restaurant;
        } catch (NoResultException e) {
            log.warn("API key validation failed - key not found");
            throw new IllegalArgumentException("Invalid API key", e);
        }
    }

    /**
     * Validate API key and check subscription for user context
     */
    public Restaurant validateAndGetRestaurantWithSubscription(String apiKey) {
        // First validate the API key normally
        Restaurant restaurant = validateAndGetRestaurantId(apiKey);

        // Validate subscription for this user-restaurant combination
        subscriptionService.validateSubscriptionBeforeSync( restaurant.getId());

        return restaurant;
    }
}

