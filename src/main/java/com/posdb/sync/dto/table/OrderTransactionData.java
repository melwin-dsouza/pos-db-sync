package com.posdb.sync.dto.table;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderTransactionData {
    private Integer orderTransactionId;
    private Integer orderId;
    private Integer menuItemId;
    private BigDecimal menuItemUnitPrice;
    private BigDecimal quantity;
    private BigDecimal extendedPrice;
    private Integer discountId;
    private BigDecimal discountAmount;
    private String discountBasis;
    private BigDecimal discountAmountUsed;
    private String rowGuid;
}

