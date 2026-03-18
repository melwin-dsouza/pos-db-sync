package com.posdb.sync.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DailyDetailedReportResponse {
    private Double totalRevenue;
    private Integer totalOrders;
    private List<OrderDetailDto> orderList;
}

