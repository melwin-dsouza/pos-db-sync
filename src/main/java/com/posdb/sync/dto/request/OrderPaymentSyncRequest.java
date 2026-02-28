package com.posdb.sync.dto.request;

import com.posdb.sync.dto.table.OrderPaymentData;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentSyncRequest {
    private List<OrderPaymentData> orderPayments;

}


