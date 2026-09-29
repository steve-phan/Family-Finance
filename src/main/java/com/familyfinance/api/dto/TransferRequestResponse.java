package com.familyfinance.api.dto;

import com.familyfinance.domain.TransferRequest;
import com.familyfinance.domain.enums.TransferRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferRequestResponse(
        Long id,
        Long requesterId,
        Long payerId,
        BigDecimal amount,
        TransferRequestStatus status,
        String message,
        Instant createdAt,
        Instant resolvedAt
) {
    public static TransferRequestResponse from(TransferRequest request) {
        return new TransferRequestResponse(
                request.getId(),
                request.getRequester().getId(),
                request.getPayer().getId(),
                request.getAmount(),
                request.getStatus(),
                request.getMessage(),
                request.getCreatedAt(),
                request.getResolvedAt()
        );
    }
}
