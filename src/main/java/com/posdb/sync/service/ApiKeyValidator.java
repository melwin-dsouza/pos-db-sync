package com.posdb.sync.service;

import com.posdb.sync.entity.Restaurant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class ApiKeyValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyValidator.class);

    public UUID validateAndGetRestaurantId(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            LOGGER.warn("API key is missing");
            throw new IllegalArgumentException("API key is required");
        }

        try {
            Restaurant restaurant = Restaurant.<Restaurant>find("apiKey", apiKey).firstResult();

            if (restaurant == null) {
                LOGGER.warn("Invalid API key provided");
                throw new IllegalArgumentException("Invalid API key");
            }

            if (!"ACTIVE".equals(restaurant.status)) {
                LOGGER.warn("API key for inactive restaurant: {}", restaurant.id);
                throw new IllegalArgumentException("Restaurant is not active");
            }

            LOGGER.info("API key validated successfully for restaurantId: {}", restaurant.id);
            return restaurant.id;
        } catch (NoResultException e) {
            LOGGER.warn("API key validation failed - key not found");
            throw new IllegalArgumentException("Invalid API key", e);
        }
    }
}

