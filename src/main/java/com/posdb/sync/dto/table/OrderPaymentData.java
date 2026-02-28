package com.posdb.sync.dto.table;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentData {
    private Integer orderPaymentId;
    private String paymentDateTime;
    private Integer cashierId;
    private Integer nonCashierEmployeeId;
    private Integer orderId;
    private String paymentMethod;
    private BigDecimal amountTendered;
    private BigDecimal amountPaid;
    private BigDecimal employeeComp;
    private String rowGuid;
}

