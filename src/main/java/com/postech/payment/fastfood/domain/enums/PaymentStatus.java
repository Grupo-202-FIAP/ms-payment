package com.postech.payment.fastfood.domain.enums;

public enum PaymentStatus {
    PROCESSED("PROCESSED"),
    PENDING("PENDING"),
    EXPIRED("EXPIRED");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
