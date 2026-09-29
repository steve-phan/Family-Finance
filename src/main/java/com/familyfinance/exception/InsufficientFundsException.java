package com.familyfinance.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(Long accountId, BigDecimal available, BigDecimal requested) {
        super(
                ErrorCode.INSUFFICIENT_FUNDS,
                "Account %d has %s available, but %s was requested".formatted(accountId, available, requested)
        );
    }
}
