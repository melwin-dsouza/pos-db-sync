package com.posdb.sync.dto.sync;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentData {
    private Integer orderPaymentId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime paymentDateTime;

    private Integer cashierId;

    private Integer nonCashierEmployeeId;

    private Integer orderId;

    private String paymentMethod;

    private BigDecimal amountTendered;

    private BigDecimal amountPaid;

    private BigDecimal employeeComp;

    private String rowGuid;
}

