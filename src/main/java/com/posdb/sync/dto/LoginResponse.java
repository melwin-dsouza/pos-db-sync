package com.posdb.sync.dto;

public class LoginResponse {
    public String token;
    public Boolean mustChangePassword;

    public LoginResponse() {
    }

    public LoginResponse(String token, Boolean mustChangePassword) {
        this.token = token;
        this.mustChangePassword = mustChangePassword;
    }
}

