package com.posdb.sync.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateRestaurantResponse {
    private String restaurantId;
    private String restaurantName;
    private String apiKey;

}

