package com.familyfinance.api.dto;

import com.familyfinance.domain.Account;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long memberId,
        BigDecimal balance,
        String currency
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getMember().getId(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
