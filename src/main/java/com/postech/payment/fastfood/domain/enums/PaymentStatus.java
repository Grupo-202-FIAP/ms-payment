package com.postech.payment.fastfood.domain.enums;

public enum PaymentStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    AUTHORIZED("AUTHORIZED"),
    IN_PROCESS("IN_PROCESS"),
    IN_MEDIATION("IN_MEDIATION"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED"),
    REFUNDED("REFUNDED"),
    CHARGED_BACK("CHARGED_BACK"),
    UNKNOWN("UNKNOWN"),
    EXPIRED("EXPIRED");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
