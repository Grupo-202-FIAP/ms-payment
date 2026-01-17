package com.postech.payment.fastfood.application.exception;

import org.springframework.http.HttpStatus;

public class PaymentEventNotSupportedException extends FastFoodException {

    public PaymentEventNotSupportedException(String eventStatus) {
        super("Event status not supported: " + eventStatus, "Payment Event Not Supported", HttpStatus.BAD_REQUEST);
    }
}

