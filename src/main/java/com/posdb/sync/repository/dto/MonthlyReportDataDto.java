package com.posdb.sync.repository.dto;

import com.posdb.sync.entity.enums.OrderTypeEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MonthlyReportDataDto {
    private OrderTypeEnum orderType;
    private Long numberOfOrders;
    private BigDecimal sumOfAmountPaid;
}

