package com.posdb.sync.exception;

import com.posdb.sync.dto.response.ApiResponse;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class GlobalExceptionHandler {

    @ServerExceptionMapper
    public Response handleAppException(AppException ex) {
        ApiResponse<String> response = new ApiResponse<>(ex.getStatus().getStatusCode(), ex.getMessage());
        return Response.status(ex.getStatus()).entity(response).build();
    }

    @ServerExceptionMapper
    public Response handleException(Exception ex) {
        ApiResponse<String> response = new ApiResponse<>(500, "Internal Error occurred");
        return Response.status(500).entity(response).build();
    }
}