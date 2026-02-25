package com.posdb.sync.dto;

import java.math.BigDecimal;

public class OrderPaymentData {
    public Integer orderPaymentId;
    public String paymentDateTime;
    public Integer cashierId;
    public Integer nonCashierEmployeeId;
    public Integer orderId;
    public String paymentMethod;
    public BigDecimal amountTendered;
    public BigDecimal amountPaid;
    public BigDecimal employeeComp;
    public String rowGuid;
}

