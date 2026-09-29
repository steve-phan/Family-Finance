package com.familyfinance.api.dto;

import com.familyfinance.domain.MoneyTransfer;
import com.familyfinance.domain.enums.TransferStatus;
import com.familyfinance.domain.enums.TransferType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        Long id,
        Long sourceMemberId,
        Long targetMemberId,
        BigDecimal amount,
        String currency,
        TransferType type,
        TransferStatus status,
        String description,
        Instant createdAt
) {
    public static TransferResponse from(MoneyTransfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getMember().getId(),
                transfer.getTargetAccount().getMember().getId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getType(),
                transfer.getStatus(),
                transfer.getDescription(),
                transfer.getCreatedAt()
        );
    }
}
