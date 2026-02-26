package com.posdb.sync.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final Response.Status status;

    public AppException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public AppException(String message, Throwable cause, Response.Status status) {
        super(message, cause);
        this.status = status;
    }



}