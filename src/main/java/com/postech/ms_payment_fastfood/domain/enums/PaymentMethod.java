package com.postech.ms_payment_fastfood.domain.enums;

public enum PaymentMethod {
    QR_CODE("QR Code");

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
