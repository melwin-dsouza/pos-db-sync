package com.posdb.sync.dto.response;

import com.posdb.sync.repository.dto.HourlyReportDataDto;
import lombok.*;

import java.math.BigDecimal;
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
    private List<HourlyReportDataDto> hourlyBreakdown;

    // Void order metrics
    private Integer voidOrderCount;
    private Double totalVoidAmount;
    private List<OrderDetailDto> voidOrderList;

    // Inhouse order metrics
    private Integer onlineOrderCount;
    private Double totalOnlineOrderAmount;
}
