package com.posdb.sync.dto;

import java.math.BigDecimal;

public class OrderHeaderData {
    public Integer orderId;
    public String orderDateTime;
    public Integer employeeId;
    public Integer stationId;
    public String orderType;
    public Integer dineInTableId;
    public Integer driverEmployeeId;
    public Integer discountId;
    public BigDecimal discountAmount;
    public BigDecimal amountDue;
    public BigDecimal cashDiscountAmount;
    public Integer cashDiscountApprovalEmpId;
    public BigDecimal subTotal;
    public Integer guestNumber;
    public String editTimestamp;
    public String rowGuid;
}

