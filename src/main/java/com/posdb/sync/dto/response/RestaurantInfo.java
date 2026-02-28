package com.posdb.sync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantInfo {
    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;
}