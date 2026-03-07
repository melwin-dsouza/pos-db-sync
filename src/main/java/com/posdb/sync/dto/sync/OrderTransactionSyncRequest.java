package com.posdb.sync.dto.sync;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderTransactionSyncRequest {
    private List<OrderTransactionData> orderTransactions;
}

