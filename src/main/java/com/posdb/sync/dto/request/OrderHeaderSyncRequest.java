package com.posdb.sync.dto.request;

import com.posdb.sync.dto.table.OrderHeaderData;

import java.util.List;

public class OrderHeaderSyncRequest {
    public List<OrderHeaderData> orderHeaders;

    public OrderHeaderSyncRequest() {
    }

    public OrderHeaderSyncRequest(List<OrderHeaderData> orderHeaders) {
        this.orderHeaders = orderHeaders;
    }
}


