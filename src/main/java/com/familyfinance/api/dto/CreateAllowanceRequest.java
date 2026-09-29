package com.familyfinance.api.dto;

import com.familyfinance.domain.enums.AllowancePeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAllowanceRequest(
        @NotNull Long childMemberId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull AllowancePeriod period
) {
}
