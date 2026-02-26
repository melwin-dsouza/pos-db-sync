package com.posdb.sync.dto.response;

import java.util.Map;

public class DailyOrderResponse {
    public String date;
    public Integer totalOrders;
    public Map<String, Integer> ordersByType;

    public DailyOrderResponse() {
    }

    public DailyOrderResponse(String date, Integer totalOrders, Map<String, Integer> ordersByType) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.ordersByType = ordersByType;
    }
}

