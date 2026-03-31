package com.posdb.sync.repository.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DailyChartDataDto {
    private LocalDate date;
    private Long numberOfOrders;
    private BigDecimal sumOfAmountPaid;
}

