package com.posdb.sync.dto.response;

import com.posdb.sync.entity.enums.SubscriptionTypeEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantSubscriptionInfo {
    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;

    // Subscription details
    private SubscriptionTypeEnum subscriptionType;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionExpiryDate;
    private Boolean isSubscriptionActive;
}
