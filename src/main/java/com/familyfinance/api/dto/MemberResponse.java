package com.familyfinance.api.dto;

import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.Role;

import java.time.Instant;

public record MemberResponse(
        Long id,
        Long familyId,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean enabled,
        Instant createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getFamilyId(),
                member.getEmail(),
                member.getFirstName(),
                member.getLastName(),
                member.getRole(),
                member.isEnabled(),
                member.getCreatedAt()
        );
    }
}
