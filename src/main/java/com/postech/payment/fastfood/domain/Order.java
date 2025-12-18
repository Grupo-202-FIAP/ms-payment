package com.postech.payment.fastfood.domain;

import com.postech.payment.fastfood.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private String identifier;
    private BigDecimal totalPrice;
    private Payment payment;
    private List<OrderItem> itens;

    public Order(
            UUID id,
            String identifier, BigDecimal totalPrice, OrderStatus status, LocalDateTime orderDateTime, Payment payment,
            List<OrderItem> itens, LocalDateTime updatedAt) {
        this.id = id;
        this.identifier = identifier;
        this.totalPrice = totalPrice;
        this.payment = payment;
        this.itens = itens;
    }

    public Order() {
    }

    public Order(Builder builder) {
        this.id = builder.id;
        this.identifier = builder.identifier;
        this.totalPrice = builder.totalPrice;

        this.payment = builder.payment;
        this.itens = builder.itens;
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

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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

    public List<OrderItem> getItens() {
        return itens;
    }

    public static class Builder {
        private UUID id;
        private BigDecimal totalPrice;
        private OrderStatus status;
        private Payment payment;
        private List<OrderItem> itens;
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

        public Builder status(OrderStatus status) {
            if (status != null) {
                this.status = status;
            }
            return this;
        }


        public Builder itens(List<OrderItem> itens) {
            if (itens != null) {
                this.itens = itens;
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
