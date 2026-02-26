package com.posdb.sync.dto.response;

import java.util.UUID;

public class RestaurantResponse {
    public UUID id;
    public String name;
    public String apiKey;

    public RestaurantResponse() {
    }

    public RestaurantResponse(UUID id, String name, String apiKey) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
    }
}

