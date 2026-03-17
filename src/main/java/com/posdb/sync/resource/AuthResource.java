package com.posdb.sync.resource;

import com.posdb.sync.dto.request.LoginRequest;
import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.SessionResponse;
import com.posdb.sync.service.AuthService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AuthResource {

    private static final String AUTH_COOKIE_NAME = "auth_token";
    // Match JWT expiry (24 hours = 86400 seconds)
    private static final int COOKIE_MAX_AGE = 86400;

    @Inject
    AuthService authService;

    /**
     * POST /api/v1/auth/login
     * Single endpoint for both mobile and web clients.
     * - Mobile: reads token from response body (ignores cookie)
     * - Web: browser auto-sends HttpOnly cookie on every request
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        log.info("AuthResource:: Received Login request for email: {}", request.getEmail());

        String token = authService.login(request);

        // Set HttpOnly cookie for web clients
        NewCookie authCookie = new NewCookie.Builder(AUTH_COOKIE_NAME)
                .value(token)
                .httpOnly(true)            //prevents XSS, cross site scripting (Javascript cannot access it)
                .secure(false)            // Set to true in production (HTTPS)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite(NewCookie.SameSite.LAX) // Use NONE for cross-site SPA, LAX for same-site
                .comment("JWT auth token")
                .build();

        log.info("AuthResource:: Login successful, returning token + cookie");
        return Response.ok()
                .cookie(authCookie)
                .entity(new ApiResponse<>(200, token, null))
                .build();
    }

    /**
     * GET /api/v1/auth/session
     * Web PWA session validation on app restart.
     * Reads JWT from HttpOnly cookie (browser) or Bearer header (mobile).
     * Returns 200 with user claims if session is valid, 401 if expired/invalid.
     */
    @GET
    @Path("/session")
    @RolesAllowed({"OWNER", "MANAGER", "STAFF"})
    public Response validateSession() {
        log.info("AuthResource:: Session validation request received");
        SessionResponse session = authService.getSessionInfo();
        return Response.ok()
                .entity(new ApiResponse<>(200, session, null))
                .build();
    }

    /**
     * POST /api/v1/auth/logout
     * Clears the HttpOnly auth cookie for web clients.
     * Mobile clients should simply discard their token locally.
     */
    @POST
    @Path("/logout")
    public Response logout() {
        log.info("AuthResource:: Logout request received, clearing auth cookie");

        // Expire the cookie immediately (maxAge = 0)
        NewCookie expiredCookie = new NewCookie.Builder(AUTH_COOKIE_NAME)
                .value("")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite(NewCookie.SameSite.LAX)
                .build();

        return Response.ok()
                .cookie(expiredCookie)
                .entity(new ApiResponse<>(200, "Logged out successfully", null))
                .build();
    }
}
