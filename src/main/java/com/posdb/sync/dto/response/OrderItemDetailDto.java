package com.posdb.sync.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderItemDetailDto {
    private String orderItemName;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal discountGiven;
}

