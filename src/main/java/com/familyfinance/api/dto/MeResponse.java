package com.familyfinance.api.dto;

public record MeResponse(
        MemberResponse member,
        FamilyResponse family,
        AccountResponse account
) {
}
