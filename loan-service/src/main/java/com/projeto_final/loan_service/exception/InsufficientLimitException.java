package com.projeto_final.loan_service.exception;

public class InsufficientLimitException extends RuntimeException {
    public InsufficientLimitException(String message) {
        super(message);
    }
}
