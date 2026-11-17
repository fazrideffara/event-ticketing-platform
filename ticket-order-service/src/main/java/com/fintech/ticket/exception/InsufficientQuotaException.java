package com.fintech.ticket.exception;

public class InsufficientQuotaException extends RuntimeException {
    public InsufficientQuotaException(String message) {
        super(message);
    }
}
