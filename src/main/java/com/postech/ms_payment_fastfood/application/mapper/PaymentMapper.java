package com.postech.ms_payment_fastfood.application.mapper;


import com.postech.ms_payment_fastfood.domain.Payment;
import com.postech.ms_payment_fastfood.infrastructure.controller.dto.request.PaymentRequest;
import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.PaymentEntity;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public static Payment toDomain(PaymentEntity paymentEntity) {
        if (paymentEntity == null) {
            return null;
        }
        return new Payment.Builder()
                .id(paymentEntity.getId())
                .status(paymentEntity.getStatus())
                .paymentMethod(paymentEntity.getPaymentMethod())
                .paymentDateTime(paymentEntity.getPaymentDateTime())
                .build();

    }

    public static PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDateTime(payment.getPaymentDateTime())
                .build();
    }

    public static Payment toDomain(@Valid PaymentRequest paymentRequest) {
        return new Payment.Builder()
                .paymentMethod(paymentRequest.paymentMethod())
                .build();
    }
}
