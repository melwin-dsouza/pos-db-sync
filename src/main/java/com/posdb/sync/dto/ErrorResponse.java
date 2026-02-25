package com.posdb.sync.dto;

import java.time.OffsetDateTime;

public class ErrorResponse {
    public String code;
    public String message;
    public OffsetDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = OffsetDateTime.now();
    }

    public ErrorResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
}

