package com.postech.payment.fastfood.domain.enums;

public enum EventSource {
    PAYMENT("PAYMENT");

    private final String source;

    EventSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}

