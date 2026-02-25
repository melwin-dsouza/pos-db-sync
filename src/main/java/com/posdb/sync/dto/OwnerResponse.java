package com.posdb.sync.dto;

public class OwnerResponse {
    public String email;
    public String password;
    public String role;
    public Boolean mustChangePassword;

    public OwnerResponse() {
    }

    public OwnerResponse(String email, String password, String role, Boolean mustChangePassword) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }
}

