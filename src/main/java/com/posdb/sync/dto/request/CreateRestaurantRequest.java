package com.posdb.sync.dto.request;

import lombok.*;

import java.time.LocalTime;

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
    private LocalTime openingTime;
    private LocalTime closingTime;
}

