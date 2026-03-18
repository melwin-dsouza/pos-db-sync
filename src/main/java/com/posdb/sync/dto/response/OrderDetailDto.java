package com.posdb.sync.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDetailDto {
    private Integer orderNumber;
    private OffsetDateTime orderTime;
    private Double totalAmount;
    private String paymentMode;
    private Integer guests;
    private String orderType;
    private List<OrderItemDetailDto> orderItems;
}

