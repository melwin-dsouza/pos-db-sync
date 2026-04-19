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
public class CustomerFileData {
    private Integer customerId;

    private String customerName;

    private String customerNotes;

    private String deliveryAddress;

    private String deliveryRemarks;

    private BigDecimal deliveryCharge;

    private BigDecimal deliveryComp;

    private String phoneNumber;

    private String companyName;

    private String emailAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime customerSinceDate;

    private BigDecimal totalSpent;

    private Integer totalCount;
}

