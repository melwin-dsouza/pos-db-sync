package com.posdb.sync.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderTypeDto {
    private String orderType;
    private Integer numberOfOrders;
    private BigDecimal sumOfAmountPaid;
    private Double percentageOfTotalRevenue;
}

