package com.postech.payment.fastfood.domain;

import com.postech.payment.fastfood.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Order {

    private UUID id;
    private String identifier;
    private BigDecimal totalPrice;
    private Payment payment;
    private List<OrderItem> items;

    public Order(
            UUID id,
            String identifier, BigDecimal totalPrice, OrderStatus status, Payment payment,
            List<OrderItem> items) {
        this.id = id;
        this.identifier = identifier;
        this.totalPrice = totalPrice;
        this.payment = payment;
        this.items = items;
    }

    public Order() {
    }

    public Order(Builder builder) {
        this.id = builder.id;
        this.identifier = builder.identifier;
        this.totalPrice = builder.totalPrice;

        this.payment = builder.payment;
        this.items = builder.items;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public static class Builder {
        private UUID id;
        private BigDecimal totalPrice;
        private Payment payment;
        private List<OrderItem> items;
        private String identifier;

        public Builder id(UUID id) {
            if (id != null) {
                this.id = id;
            }
            return this;
        }

        public Builder totalPrice(BigDecimal totalPrice) {
            if (totalPrice != null) {
                this.totalPrice = totalPrice;
            }
            return this;
        }

        public Builder items(List<OrderItem> items) {
            if (items != null) {
                this.items = items;
            }
            return this;
        }

        public Builder payment(Payment payment) {
            if (payment != null) {
                this.payment = payment;
            }
            return this;
        }

        public Builder identifier(String identifier) {
            if (identifier != null) {
                this.identifier = identifier;
            }
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

}
