package com.posdb.sync.dto;

public class OwnerRequest {
    public String email;
    public String role;

    public OwnerRequest() {
    }

    public OwnerRequest(String email, String role) {
        this.email = email;
        this.role = role;
    }
}


