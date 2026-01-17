package com.postech.payment.fastfood.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class QrCode {
    private UUID id;
    private UUID orderId;
    private Payment payment;
    private BigDecimal totalAmount;
    private String currency;
    private String qrCode;
    private OffsetDateTime expiresAt;

    public QrCode() {
    }

    public QrCode(UUID id, UUID orderId, Payment payment, BigDecimal totalAmount, String currency, String qrCode, OffsetDateTime expiresAt) {
        this.id = id;
        this.orderId = orderId;
        this.payment = payment;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.qrCode = qrCode;
        this.expiresAt = expiresAt;
    }

    public QrCode(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.payment = builder.payment;
        this.totalAmount = builder.totalAmount;
        this.currency = builder.currency;
        this.qrCode = builder.qrCode;
        this.expiresAt = builder.expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(OffsetDateTime.now());
    }

    public static class Builder {
        private UUID id;
        private UUID orderId;
        private Payment payment;
        private BigDecimal totalAmount;
        private String currency;
        private String qrCode;
        private OffsetDateTime expiresAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder orderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder payment(Payment payment) {
            this.payment = payment;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder qrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        public Builder expiresAt(OffsetDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public QrCode build() {
            return new QrCode(this);
        }
    }

}
