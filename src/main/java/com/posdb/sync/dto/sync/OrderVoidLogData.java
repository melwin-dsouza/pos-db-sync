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
public class OrderVoidLogData {
    private Integer orderId;

    private Integer orderTransactionId;

    private Integer employeeId;

    private String voidReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime voidDateTime;

    private Boolean voidForItemReduction;

    private BigDecimal voidAmount;

    private Integer autoId;
}

