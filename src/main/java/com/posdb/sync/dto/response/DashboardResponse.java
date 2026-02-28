package com.posdb.sync.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DashboardResponse {
    private String dayTitle;//Today, Yesterday, or Date (last available data)

    private RestaurantInfo restaurantInfo;
    private List<RestaurantInfo> associatedRestaurants;

    private String date;
    private Double totalRevenue;

    private Integer totalOrders;
    private String topSellingItem;
    private Integer numberOfGuests;
    private Double averageRevenuePerGuest;
    private Double averageRevenuePerOrder;
    private Double averageItemsPerOrder;
    private Double averageRevenuePerItem;
    private Double averageOrderValue;

    private List<OrderTypeInfo> orderTypeInfoList;


}

