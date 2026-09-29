package com.familyfinance.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    EMAIL_TAKEN(HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_FUNDS(HttpStatus.UNPROCESSABLE_ENTITY),
    SAME_ACCOUNT(HttpStatus.BAD_REQUEST),
    DIFFERENT_FAMILY(HttpStatus.FORBIDDEN),
    MEMBER_DISABLED(HttpStatus.FORBIDDEN),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    AMOUNT_TOO_LARGE(HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST),
    REQUEST_NOT_PENDING(HttpStatus.CONFLICT),
    PARENT_REQUIRED(HttpStatus.FORBIDDEN),
    DUPLICATE_IDEMPOTENCY_KEY(HttpStatus.CONFLICT),
    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
