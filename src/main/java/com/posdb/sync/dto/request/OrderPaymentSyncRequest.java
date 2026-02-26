package com.posdb.sync.dto.request;

import com.posdb.sync.dto.table.OrderPaymentData;

import java.util.List;

public class OrderPaymentSyncRequest {
    public List<OrderPaymentData> orderPayments;

    public OrderPaymentSyncRequest() {
    }

    public OrderPaymentSyncRequest(List<OrderPaymentData> orderPayments) {
        this.orderPayments = orderPayments;
    }
}


