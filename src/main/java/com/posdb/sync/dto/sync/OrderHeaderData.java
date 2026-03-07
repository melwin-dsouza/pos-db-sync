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
public class OrderHeaderData {
    private Integer orderId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime orderDateTime;

    private Integer employeeId;

    private Integer stationId;

    private String orderType;

    private Integer dineInTableId;

    private Integer driverEmployeeId;

    private Integer discountId;

    private BigDecimal discountAmount;

    private BigDecimal amountDue;

    private BigDecimal cashDiscountAmount;

    private Integer cashDiscountApprovalEmpId;

    private BigDecimal subTotal;

    private Integer guestNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime editTimestamp;

    private String rowGuid;
}

