package com.posdb.sync.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MonthlyReportResponse {
    private String monthName;
    private BigDecimal totalMonthlyRevenue;
    private LocalDate monthStartDate;
    private LocalDate monthEndDate;
    private List<OrderTypeDto> byOrderTypeList;
}

