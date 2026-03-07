package com.posdb.sync.exception;

import com.posdb.sync.dto.response.ApiResponse;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Slf4j
public class GlobalExceptionHandler {

    @ServerExceptionMapper
    public Response handleAppException(AppException ex) {
        ApiResponse<String> response = new ApiResponse<>(ex.getStatus().getStatusCode(), ex.getMessage());
        log.error("GlobalExceptionHandler:: AppException occurred: ", ex);
        return Response.status(ex.getStatus()).entity(response).build();
    }

    @ServerExceptionMapper
    public Response handleException(Exception ex) {
        ApiResponse<String> response = new ApiResponse<>(500, "Internal Error occurred");
        log.error("GlobalExceptionHandler:: Unhandled exception: ", ex);
        return Response.status(500).entity(response).build();
    }
}