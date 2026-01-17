package com.postech.payment.fastfood.domain.enums;

public enum OrderStatus {
    RECEIVED("RECEIVED"),
    PREPARING("PREPARING"),
    READY("READY"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
