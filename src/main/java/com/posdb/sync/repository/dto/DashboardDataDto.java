package com.posdb.sync.repository.dto;

import com.posdb.sync.entity.enums.OrderTypeEnum;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DashboardDataDto {
    private Long orderCount;
    private String orderId;
    private Date orderDateTime;
    private OrderTypeEnum orderType;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private Integer guestNumber;
    private String orderPaymentId;
    private Date paymentDateTime;
    private String paymentMethod;
    private BigDecimal amountPaid;
}
