package com.posdb.sync.dto;

public class ChangePasswordResponse {
    public Boolean success;
    public String message;

    public ChangePasswordResponse() {
    }

    public ChangePasswordResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

