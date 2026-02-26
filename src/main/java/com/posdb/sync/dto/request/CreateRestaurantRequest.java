package com.posdb.sync.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateRestaurantRequest {
    private String name;
    private String description;
    private String address;
    private String phone;
    private String keyword;
}

