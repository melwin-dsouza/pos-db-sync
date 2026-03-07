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
public class DashboardDataDto {
    private Integer orderId;
    private OffsetDateTime orderDateTime;
    private OrderTypeEnum orderType;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private Integer guestNumber;
    private Integer orderPaymentId;
    private OffsetDateTime paymentDateTime;
    private String paymentMethod;
    private BigDecimal amountPaid;
}
