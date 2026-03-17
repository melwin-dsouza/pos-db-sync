package com.posdb.sync.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SessionResponse {
    private String email;
    private String fullName;
    private String role;
    private String restaurantName;
    private List<String> restaurants;
    private boolean valid;
}

