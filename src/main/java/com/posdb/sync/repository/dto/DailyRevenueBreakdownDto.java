package com.posdb.sync.repository.dto;

import com.posdb.sync.entity.enums.OrderTypeEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DailyRevenueBreakdownDto {
    private Long totalOrders;
    private Long totalGuests;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscounts;
    private OrderTypeEnum orderType;
    private Long ordertypeOrderCount;
    private BigDecimal ordertypeRevenue;
}
