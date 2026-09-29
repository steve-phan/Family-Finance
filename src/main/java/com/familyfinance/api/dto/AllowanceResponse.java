package com.familyfinance.api.dto;

import com.familyfinance.domain.AllowanceRule;
import com.familyfinance.domain.enums.AllowancePeriod;

import java.math.BigDecimal;
import java.time.Instant;

public record AllowanceResponse(
        Long id,
        Long parentId,
        Long childId,
        BigDecimal amount,
        AllowancePeriod period,
        boolean active,
        Instant nextRunAt
) {
    public static AllowanceResponse from(AllowanceRule rule) {
        return new AllowanceResponse(
                rule.getId(),
                rule.getParent().getId(),
                rule.getChild().getId(),
                rule.getAmount(),
                rule.getPeriod(),
                rule.isActive(),
                rule.getNextRunAt()
        );
    }
}
