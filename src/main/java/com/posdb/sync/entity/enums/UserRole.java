package com.posdb.sync.entity.enums;

public enum UserRole {
    OWNER,
    MANAGER,
    STAFF;

    public static boolean isValidRole(String role) {
        try {
            UserRole.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
