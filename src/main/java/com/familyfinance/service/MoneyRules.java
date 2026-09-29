package com.familyfinance.service;

import com.familyfinance.config.TransferProperties;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MoneyRules {

    private final TransferProperties transferProperties;

    public MoneyRules(TransferProperties transferProperties) {
        this.transferProperties = transferProperties;
    }

    public BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "Amount cannot have more than two decimal places");
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
        if (normalized.compareTo(transferProperties.getMaxAmount()) > 0) {
            throw new BusinessException(
                    ErrorCode.AMOUNT_TOO_LARGE,
                    "Amount exceeds maximum of " + transferProperties.getMaxAmount()
            );
        }
        return normalized;
    }
}
