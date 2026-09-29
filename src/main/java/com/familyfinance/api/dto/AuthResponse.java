package com.familyfinance.api.dto;

public record AuthResponse(
        String token,
        MemberResponse member
) {
}
