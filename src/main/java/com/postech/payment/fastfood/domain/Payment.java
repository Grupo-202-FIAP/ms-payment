package com.postech.payment.fastfood.domain;


import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private UUID id;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDateTime;
    private LocalDateTime updatedAt;
    private Order order;

    public Payment(UUID id, PaymentStatus status, PaymentMethod paymentMethod, LocalDateTime paymentDateTime, LocalDateTime updatedAt, Order order) {
        this.id = id;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentDateTime = paymentDateTime;
        this.updatedAt = updatedAt;
        this.order = order;
    }

    public Payment() {
    }

    public Payment(Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.paymentMethod = builder.paymentMethod;
        this.paymentDateTime = builder.paymentDateTime;
        this.updatedAt = builder.updatedAt;
        this.order = builder.order;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }

    public void setPaymentDateTime(LocalDateTime paymentDateTime) {
        this.paymentDateTime = paymentDateTime;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public static class Builder {
        private UUID id;
        private PaymentStatus status;
        private PaymentMethod paymentMethod;
        private LocalDateTime paymentDateTime;
        private LocalDateTime updatedAt;
        private Order order;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder paymentDateTime(LocalDateTime paymentDateTime) {
            this.paymentDateTime = paymentDateTime;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }


}
