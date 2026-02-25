package com.posdb.sync.resource;

import com.posdb.sync.dto.OwnerRequest;
import com.posdb.sync.dto.OwnerResponse;
import com.posdb.sync.dto.RestaurantRequest;
import com.posdb.sync.dto.RestaurantResponse;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.entity.User;
import com.posdb.sync.service.PasswordUtil;
import com.posdb.sync.service.RandomKeyGenerator;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Path("/api/v1/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminResource.class);

    private final RandomKeyGenerator keyGenerator;
    private final PasswordUtil passwordUtil;

    public AdminResource(RandomKeyGenerator keyGenerator, PasswordUtil passwordUtil) {
        this.keyGenerator = keyGenerator;
        this.passwordUtil = passwordUtil;
    }

    @POST
    @Path("/restaurants")
    @Transactional
    public Response createRestaurant(RestaurantRequest request) {
        try {
            LOGGER.info("Creating new restaurant: {}", request.name);

            if (request.name == null || request.name.trim().isEmpty()) {
                LOGGER.warn("Restaurant creation failed - name is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Restaurant name is required\"}")
                        .build();
            }

            String apiKey = keyGenerator.generateApiKey();

            Restaurant restaurant = new Restaurant();
            restaurant.id = UUID.randomUUID();
            restaurant.name = request.name;
            restaurant.apiKey = apiKey;
            restaurant.status = "ACTIVE";

            restaurant.persist();

            LOGGER.info("Restaurant created successfully: {} with ID: {}", request.name, restaurant.id);

            return Response.status(Response.Status.CREATED)
                    .entity(new RestaurantResponse(restaurant.id, restaurant.name, restaurant.apiKey))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error creating restaurant", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to create restaurant\"}")
                    .build();
        }
    }

    @POST
    @Path("/restaurants/{restaurantId}/owners")
    @Transactional
    public Response createOwner(@PathParam("restaurantId") String restaurantId, OwnerRequest request) {
        try {
            LOGGER.info("Creating new owner for restaurantId: {}", restaurantId);

            UUID restId = UUID.fromString(restaurantId);

            // Validate restaurant exists
            Restaurant restaurant = Restaurant.findById(restId);
            if (restaurant == null) {
                LOGGER.warn("Restaurant not found: {}", restaurantId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"code\": \"NOT_FOUND\", \"message\": \"Restaurant not found\"}")
                        .build();
            }

            if (request.email == null || request.email.trim().isEmpty()) {
                LOGGER.warn("Owner creation failed - email is required");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Email is required\"}")
                        .build();
            }

            // Check if email already exists for this restaurant
            User existingUser = User.<User>find("email = ?1 AND restaurant_id = ?2", request.email, restId).firstResultOptional().orElse(null);
            if (existingUser != null) {
                LOGGER.warn("Email already exists for restaurant: {}", request.email);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"DUPLICATE_EMAIL\", \"message\": \"Email already exists for this restaurant\"}")
                        .build();
            }

            String generatedPassword = keyGenerator.generatePassword();
            String passwordHash = passwordUtil.hashPassword(generatedPassword);

            User user = new User();
            user.id = UUID.randomUUID();
            user.restaurantId = restId;
            user.email = request.email;
            user.passwordHash = passwordHash;
            user.role = request.role != null ? request.role : "MANAGER";
            user.mustChangePassword = true;

            user.persist();

            LOGGER.info("Owner created successfully for restaurantId: {} with email: {}", restaurantId, request.email);

            return Response.status(Response.Status.CREATED)
                    .entity(new OwnerResponse(user.email, generatedPassword, user.role, user.mustChangePassword))
                    .build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid restaurantId format: {}", restaurantId);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Invalid restaurant ID format\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error creating owner", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to create owner\"}")
                    .build();
        }
    }
}

