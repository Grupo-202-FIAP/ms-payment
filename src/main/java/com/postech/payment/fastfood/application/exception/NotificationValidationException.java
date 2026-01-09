package com.postech.payment.fastfood.application.exception;

public class NotificationValidationException extends RuntimeException {
    public NotificationValidationException(String message) {
        super(message);
    }

    public NotificationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
