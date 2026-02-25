package com.posdb.sync.resource;

import com.posdb.sync.dto.ChangePasswordRequest;
import com.posdb.sync.dto.ChangePasswordResponse;
import com.posdb.sync.entity.User;
import com.posdb.sync.service.PasswordUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.UUID;

@Path("/api/v1/owner")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class OwnerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OwnerResource.class);

    @Inject
    SecurityIdentity securityIdentity;

    private final PasswordUtil passwordUtil;

    public OwnerResource(PasswordUtil passwordUtil) {
        this.passwordUtil = passwordUtil;
    }

    @POST
    @Path("/change-password")
    @RolesAllowed({"OWNER", "MANAGER"})
    @Transactional
    public Response changePassword(ChangePasswordRequest request) {
        try {
            String userId = securityIdentity.getPrincipal().getName();
            LOGGER.info("Password change requested for userId: {}", userId);

            if (request.currentPassword == null || request.currentPassword.trim().isEmpty() ||
                request.newPassword == null || request.newPassword.trim().isEmpty()) {
                LOGGER.warn("Password change failed - missing current or new password for userId: {}", userId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Current and new password are required\"}")
                        .build();
            }

            UUID userIdUuid = UUID.fromString(userId);
            User user = User.findById(userIdUuid);

            if (user == null) {
                LOGGER.warn("User not found for password change: {}", userId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"code\": \"NOT_FOUND\", \"message\": \"User not found\"}")
                        .build();
            }

            // Verify current password
            if (!passwordUtil.verifyPassword(request.currentPassword, user.passwordHash)) {
                LOGGER.warn("Password change failed - invalid current password for userId: {}", userId);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"code\": \"INVALID_PASSWORD\", \"message\": \"Current password is incorrect\"}")
                        .build();
            }

            // Update password
            user.passwordHash = passwordUtil.hashPassword(request.newPassword);
            user.mustChangePassword = false;
            user.updatedAt = OffsetDateTime.now();
            user.persist();

            LOGGER.info("Password changed successfully for userId: {}", userId);

            return Response.ok(new ChangePasswordResponse(true, "Password changed successfully")).build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid user ID format in security context");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Invalid user context\"}")
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error changing password", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Failed to change password\"}")
                    .build();
        }
    }
}

