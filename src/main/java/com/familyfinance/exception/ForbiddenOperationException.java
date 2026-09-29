package com.familyfinance.exception;

public class ForbiddenOperationException extends BusinessException {

    public ForbiddenOperationException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
