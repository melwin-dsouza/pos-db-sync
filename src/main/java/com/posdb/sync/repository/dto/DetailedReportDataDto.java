package com.posdb.sync.repository.dto;

import com.posdb.sync.entity.enums.OrderTypeEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DetailedReportDataDto {
    private Integer orderId;
    private OffsetDateTime orderDateTime;
    private OrderTypeEnum orderType;
    private Integer guestNumber;
    private Integer orderPaymentId;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private Integer orderTransactionId;
    private Integer menuItemId;
    private BigDecimal quantity;
    private BigDecimal extendedPrice;
    private BigDecimal discountAmount;
    private String menuItemText;
}
