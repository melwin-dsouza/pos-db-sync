package com.posdb.sync.dto.sync;

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


