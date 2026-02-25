package com.posdb.sync.dto;

public class ChangePasswordRequest {
    public String currentPassword;
    public String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}


