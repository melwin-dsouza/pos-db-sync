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
public class OnAccountChargeData {
    private Integer orderChargeId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime chargeDateTime;

    private Integer cashierId;

    private Integer nonCashierEmployeeId;

    private Integer customerId;

    private Integer orderId;

    private BigDecimal amountCharged;

    private Integer orderChargePaymentId;

    private BigDecimal employeeComp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime chargeDueDate;
}

