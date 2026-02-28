package com.posdb.sync.dto.table;

import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderHeaderData {
    private Integer orderId;
    private String orderDateTime;
    private Integer employeeId;
    private Integer stationId;
    private String orderType;
    private Integer dineInTableId;
    private Integer driverEmployeeId;
    private Integer discountId;
    private BigDecimal discountAmount;
    private BigDecimal amountDue;
    private BigDecimal cashDiscountAmount;
    private Integer cashDiscountApprovalEmpId;
    private BigDecimal subTotal;
    private Integer guestNumber;
    private String editTimestamp;
    private String rowGuid;
}

