package com.postech.payment.fastfood.domain.enums;

public enum Category {
    SANDWICHES("SANDWICHES"),
    SIDES("SIDES"),
    DRINKS("DRINKS"),
    DESSERTS("DESSERTS");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
