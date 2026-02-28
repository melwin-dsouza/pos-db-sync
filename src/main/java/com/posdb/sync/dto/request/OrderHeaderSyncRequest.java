package com.posdb.sync.dto.request;

import com.posdb.sync.dto.table.OrderHeaderData;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderHeaderSyncRequest {
    private List<OrderHeaderData> orderHeaders;
}


