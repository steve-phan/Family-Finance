package com.familyfinance.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTransferRequestDto(
        @NotNull Long payerMemberId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 255) String message
) {
}
