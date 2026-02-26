package com.posdb.sync.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateUserRequest {
    private String email;
    private String fullName;
    private String mobileNumber;
    private String role;
}


