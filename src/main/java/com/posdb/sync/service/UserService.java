package com.posdb.sync.service;

import com.posdb.sync.dto.request.ChangePasswordRequest;
import com.posdb.sync.dto.response.RestaurantInfo;
import com.posdb.sync.dto.response.UserInfoResponse;
import com.posdb.sync.entity.Restaurant;
import com.posdb.sync.entity.User;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.utils.PasswordUtil;
import com.posdb.sync.utils.TextUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@ApplicationScoped
@Slf4j
public class UserService {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    PasswordUtil passwordUtil;

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        try {
            String userEmail = securityIdentity.getPrincipal().getName();
            log.info("Password change requested for user: {}", userEmail);

            if (TextUtil.isEmpty(request.getCurrentPassword()) || TextUtil.isEmpty(request.getNewPassword())) {
                log.warn("UserService:: Password change failed - missing current or new password for user: {}", userEmail);
                throw new AppException("Current password and new password are required", Response.Status.BAD_REQUEST);
            }
            User user = User.<User>find("email = ?1", userEmail)
                    .firstResultOptional().orElse(null);
            if (user == null) {
                log.warn("User not found for password change: {}", userEmail);
                throw new AppException("User not found", Response.Status.BAD_REQUEST);
            }
            // Verify current password
            if (passwordUtil.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
                log.warn("Password change failed - invalid current password for userId: {}", userEmail);
                throw new AppException("Current password is incorrect", Response.Status.BAD_REQUEST);
            }
            user.setPasswordHash(passwordUtil.hashPassword(request.getNewPassword()));
            user.setMustChangePassword(false);
            user.persist();

            log.info("Password changed successfully for userId: {}", userEmail);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID format in security context");
            throw new AppException("Invalid user ID format", Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error changing password", e);
            throw new AppException("Error changing password", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public UserInfoResponse getUserInfo() {
        String userEmail = securityIdentity.getPrincipal().getName();
        User user = User.<User>find("email = ?1", userEmail)
                .firstResultOptional().orElse(null);
        if (user == null ) {
            log.warn("User not found for Get User Info request : {}", userEmail);
            throw new AppException("User not found", Response.Status.BAD_REQUEST);
        }
        RestaurantInfo primaryRestaurantInfo = null;
        if (user.getPrimaryRestaurant() != null) {
            Restaurant restaurant = user.getPrimaryRestaurant();
            primaryRestaurantInfo = new RestaurantInfo(restaurant.getId().toString(), restaurant.getName(), restaurant.getAddress());
        }
        List<RestaurantInfo> restaurantInfos = user.getRestaurants().stream()
                .map(r -> new RestaurantInfo(r.getId().toString(), r.getName(), r.getAddress()))
                .toList();
        UserInfoResponse userInfoResponse = new UserInfoResponse(
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                primaryRestaurantInfo,
                restaurantInfos
        );
        log.info("Fetched User info successfully for userId: {}", userEmail);
        return userInfoResponse;

    }
}
