package com.posdb.sync.resource;

import com.posdb.sync.dto.request.LoginRequest;
import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login") // No authentication required for login, For mobile user to login and get token
    public Response login(LoginRequest request) {
        log.info("AuthResource:: Received Login request: {}", request);
        String token = authService.login(request);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(201, token, null))
                .build();
    }
}

