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
    private OrderStatus status;
    private LocalDateTime orderDateTime;
    private Payment payment;
    private List<OrderItem> itens;
    private LocalDateTime updatedAt;

    public Order(
            UUID id,
            String identifier, BigDecimal totalPrice, OrderStatus status, LocalDateTime orderDateTime, Payment payment,
            List<OrderItem> itens, LocalDateTime updatedAt) {
        this.id = id;
        this.identifier = identifier;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDateTime = orderDateTime;

        this.payment = payment;
        this.itens = itens;
        this.updatedAt = updatedAt;
    }

    public Order() {
    }

    public Order(Builder builder) {
        this.id = builder.id;
        this.identifier = builder.identifier;
        this.totalPrice = builder.totalPrice;
        this.status = builder.status;
        this.orderDateTime = builder.orderDateTime;
        this.payment = builder.payment;
        this.itens = builder.itens;
        this.updatedAt = builder.updatedAt;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
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

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<OrderItem> getItens() {
        return itens;
    }

    public void setItens(List<OrderItem> itens) {
        this.itens = itens;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal calculateTotalPrice() {
        if (itens == null || itens.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return itens.stream()
                .map(item -> item.getProduct().getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateTotalPrice() {
        this.totalPrice = calculateTotalPrice();
    }

    public static class Builder {
        private UUID id;
        private BigDecimal totalPrice;
        private OrderStatus status;
        private LocalDateTime orderDateTime;
        private Payment payment;
        private List<OrderItem> itens;
        private String identifier;
        private LocalDateTime updatedAt;

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

        public Builder orderDateTime(LocalDateTime orderDateTime) {
            if (orderDateTime != null) {
                this.orderDateTime = orderDateTime;
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

        public Builder updatedAt(LocalDateTime updatedAt) {
            if (updatedAt != null) {
                this.updatedAt = updatedAt;
            }
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }

}
