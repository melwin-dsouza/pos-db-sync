package com.posdb.sync.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserInfoResponse {
    private String fullName;
    private String email;
    private String phoneNumber;

    private RestaurantInfo primaryRestaurant;
    private List<RestaurantInfo> associatedRestaurants;


}

