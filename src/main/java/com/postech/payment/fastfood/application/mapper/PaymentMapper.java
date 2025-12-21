package com.postech.payment.fastfood.application.mapper;


import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.PaymentQrCodeRequest;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.PaymentRequest;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public static Payment toDomain(PaymentEntity paymentEntity) {
        if (paymentEntity == null) {
            return null;
        }

        final Payment payment = new Payment.Builder()
                .id(paymentEntity.getId())
                .status(paymentEntity.getStatus())
                .paymentMethod(paymentEntity.getPaymentMethod())
                .paymentDateTime(paymentEntity.getPaymentDateTime())
                .orderId(paymentEntity.getOrderId())
                .amount(paymentEntity.getAmount())
                .qrCode(QrCodeMapper.toDomain(paymentEntity.getQrCode()))
                .build();
        return payment;
    }

    public static PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .orderId(payment.getOrderId())
                .paymentDateTime(payment.getPaymentDateTime())
                .amount(payment.getAmount())
                .qrCode(QrCodeMapper.toEntity(payment.getQrCode()))
                .build();
    }

    public static Payment toDomain(@Valid PaymentRequest paymentRequest) {
        return new Payment.Builder()
                .paymentMethod(paymentRequest.paymentMethod())
                .build();
    }


    public static Payment toDomain(PaymentQrCodeRequest paymentQrCodeRequest) {
        return new Payment.Builder()
                .orderId(paymentQrCodeRequest.orderId())
                .amount(paymentQrCodeRequest.totalPrice())
                .build();
    }
}
