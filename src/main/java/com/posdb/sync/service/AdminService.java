package com.posdb.sync.service;

import com.posdb.sync.dto.request.CreateUserRequest;
import com.posdb.sync.dto.request.CreateRestaurantRequest;
import com.posdb.sync.dto.response.OwnerResponse;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.entity.User;
import com.posdb.sync.entity.enums.UserRole;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.utils.PasswordUtil;
import com.posdb.sync.utils.RandomKeyGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j
public class AdminService {

    @Inject
    RandomKeyGenerator keyGenerator;
    @Inject
    PasswordUtil passwordUtil;

    @Transactional
    public void createRestaurant(CreateRestaurantRequest request) {
        try {
            log.info("AdminService:: Creating new restaurant: {}", request.getName());
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                log.warn("AdminService:: Restaurant creation failed - name is required");
                throw new AppException("Restaurant name is required", Response.Status.BAD_REQUEST);
            }
            String apiKey = keyGenerator.generateApiKey();

            Restaurant restaurant = new Restaurant();
            restaurant.setName(request.getName());
            restaurant.setApiKey(apiKey);
            restaurant.setStatus("ACTIVE");
            restaurant.setKeyword(request.getKeyword());
            restaurant.setDescription(request.getDescription());
            restaurant.setAddress(request.getAddress());
            restaurant.setPhoneNumber(request.getPhone());
            restaurant.persist();

            log.info("AdminService::Restaurant created successfully: {} with ID: {}", request.getName(), restaurant.getId());
        } catch (Exception e) {
            log.error("AdminService:: Error creating restaurant. ", e);
            throw new AppException("Failed to create restaurant", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public void createUser(String restaurantId, CreateUserRequest request) {
        try {
            log.info("AdminService::Creating new user for restaurantId: {}", restaurantId);
            UUID restId = UUID.fromString(restaurantId);
            Restaurant restaurant = Restaurant.findById(restId);
            if (restaurant == null) {
                log.warn("AdminService:: Restaurant not found: {}", restaurantId);
                throw new AppException("Restaurant not found.", Response.Status.BAD_REQUEST);
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                log.warn("AdminService::Owner creation failed - email is required");
                throw new AppException("Email is required.", Response.Status.BAD_REQUEST);
            }

            if(request.getRole() != null && !UserRole.isValidRole(request.getRole()))  {
                log.warn("AdminService:: Invalid role specified: {}", request.getRole());
                throw new AppException("Invalid role specified.", Response.Status.BAD_REQUEST);
            }

            // Check if email already exists for this restaurant
            User existingUser = User.<User>find("email = ?1 AND restaurant_id = ?2", request.getEmail(), restId)
                    .firstResultOptional().orElse(null);
            if (existingUser != null) {
                log.warn("AdminService:: Email already exists for restaurant: {}", request.getEmail());
                throw new AppException("Email already exists for this restaurant.", Response.Status.BAD_REQUEST);
            }

            String generatedPassword = keyGenerator.generatePassword();
            String passwordHash = passwordUtil.hashPassword(generatedPassword);

            User user = new User();
            user.setRestaurant(restaurant);
            user.setEmail(request.getEmail());
            user.setMobileNumber(request.getMobileNumber());
            user.setFullName(request.getFullName());
            user.setPasswordHash(passwordHash);
            user.setRole(request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.OWNER);
            user.setMustChangePassword(true);
            user.persist();

            log.info("AdminService:: Owner created successfully for restaurantId: {} with email: {}", restaurantId, request.getEmail());
        } catch (IllegalArgumentException e) {
            log.warn("AdminService:: Invalid restaurantId format: {}", restaurantId);
            throw new AppException("Invalid restaurant ID format.", Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            log.error("AdminService:: Error creating owner", e);
            throw new AppException("Failed to create owner", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
