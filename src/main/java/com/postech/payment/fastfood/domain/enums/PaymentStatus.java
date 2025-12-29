package com.postech.payment.fastfood.domain.enums;

public enum PaymentStatus {
    PENDING("PENDING"),
    PROCESSED("PROCESSED"),
    EXPIRING("EXPIRING"),
    EXPIRED("EXPIRED");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
