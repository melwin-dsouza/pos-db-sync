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
public class DailyChartDataResponse {
    private String monthName;
    private LocalDate monthStartDate;
    private LocalDate monthEndDate;
    private List<DailyChartData> dailyData;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DailyChartData {
        private LocalDate date;
        private String weekday; // Monday, Tuesday, etc.
        private Integer totalOrders;
        private BigDecimal totalRevenue;
    }
}

