package com.familyfinance.api.dto;

import com.familyfinance.domain.Family;

import java.time.Instant;

public record FamilyResponse(
        Long id,
        String name,
        String currency,
        Instant createdAt
) {
    public static FamilyResponse from(Family family) {
        return new FamilyResponse(family.getId(), family.getName(), family.getCurrency(), family.getCreatedAt());
    }
}
