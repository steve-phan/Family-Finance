package com.familyfinance.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String entity, Long id) {
        super(ErrorCode.NOT_FOUND, entity + " not found: " + id);
    }

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
