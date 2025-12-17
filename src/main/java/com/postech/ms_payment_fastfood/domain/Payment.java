package com.postech.ms_payment_fastfood.domain;



import com.postech.ms_payment_fastfood.domain.enums.PaymentMethod;
import com.postech.ms_payment_fastfood.domain.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
    private UUID id;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDateTime;

    public Payment(UUID id, PaymentStatus status, PaymentMethod paymentMethod, LocalDateTime paymentDateTime) {
        this.id = id;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentDateTime = paymentDateTime;
    }

    public Payment() {
    }

    public Payment(Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.paymentMethod = builder.paymentMethod;
        this.paymentDateTime = builder.paymentDateTime;
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


    public static class Builder {
        private UUID id;
        private PaymentStatus status;
        private PaymentMethod paymentMethod;
        private LocalDateTime paymentDateTime;

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

        public Payment build() {
            return new Payment(this);
        }
    }


}
