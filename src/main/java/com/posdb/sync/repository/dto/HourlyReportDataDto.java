package com.posdb.sync.repository.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HourlyReportDataDto {
    private Integer hour;
    private Integer orderCount;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscounts;
    private Integer totalGuests;
}

