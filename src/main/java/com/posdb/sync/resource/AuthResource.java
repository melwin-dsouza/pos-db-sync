package com.posdb.sync.resource;

import com.posdb.sync.dto.LoginRequest;
import com.posdb.sync.dto.LoginResponse;
import com.posdb.sync.entity.User;
import com.posdb.sync.service.JwtProvider;
import com.posdb.sync.service.PasswordUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

    private final PasswordUtil passwordUtil;
    private final JwtProvider jwtProvider;

    public AuthResource(PasswordUtil passwordUtil, JwtProvider jwtProvider) {
        this.passwordUtil = passwordUtil;
        this.jwtProvider = jwtProvider;
    }

    @POST
    @Path("/login")
    @Transactional
    public Response login(LoginRequest request) {
        try {
            LOGGER.info("Login attempt for email: {}", request.email);

            if (request.email == null || request.email.trim().isEmpty() ||
                request.password == null || request.password.trim().isEmpty()) {
                LOGGER.warn("Login failed - missing email or password");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"code\": \"INVALID_INPUT\", \"message\": \"Email and password are required\"}")
                        .build();
            }

            User user = User.<User>find("email", request.email).firstResultOptional().orElse(null);

            if (user == null || !passwordUtil.verifyPassword(request.password, user.passwordHash)) {
                LOGGER.warn("Login failed - invalid credentials for email: {}", request.email);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"code\": \"INVALID_CREDENTIALS\", \"message\": \"Invalid email or password\"}")
                        .build();
            }

            String token = jwtProvider.generateToken(user.id, user.restaurantId, user.role);

            LOGGER.info("Login successful for email: {} with userId: {}", request.email, user.id);

            return Response.ok(new LoginResponse(token, user.mustChangePassword)).build();
        } catch (Exception e) {
            LOGGER.error("Error during login", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"code\": \"INTERNAL_ERROR\", \"message\": \"Login failed\"}")
                    .build();
        }
    }
}

