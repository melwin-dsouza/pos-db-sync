package com.posdb.sync.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoidOrderMetricsDto {
    private Long voidOrderCount;
    private BigDecimal totalVoidAmount;
}

