package com.posdb.sync.dto.request;

import com.posdb.sync.dto.table.OrderTransactionData;
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

