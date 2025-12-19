package com.postech.payment.fastfood.domain;

import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private UUID id;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDateTime;
    private LocalDateTime updatedAt;
    private BigDecimal amount;
    private UUID orderId;

    public Payment(UUID id, PaymentStatus status, PaymentMethod paymentMethod, LocalDateTime paymentDateTime, LocalDateTime updatedAt, BigDecimal amount, UUID orderId) {
        this.id = id;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentDateTime = paymentDateTime;
        this.updatedAt = updatedAt;
        this.amount = amount;
        this.orderId = orderId;
    }

    public Payment() {
    }

    public Payment(Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.paymentMethod = builder.paymentMethod;
        this.paymentDateTime = builder.paymentDateTime;
        this.updatedAt = builder.updatedAt;
        this.orderId = builder.orderId;
        this.amount = builder.amount;
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

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public static class Builder {
        private UUID id;
        private PaymentStatus status;
        private PaymentMethod paymentMethod;
        private LocalDateTime paymentDateTime;
        private LocalDateTime updatedAt;
        private BigDecimal amount;
        private UUID orderId;

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

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }


}
