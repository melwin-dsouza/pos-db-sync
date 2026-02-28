package com.posdb.sync.dto.response;

import com.posdb.sync.entity.enums.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderTypeInfo {
    private OrderTypeEnum orderType;
    private Integer numberOfOrders;
    private Double totalRevenue;
}