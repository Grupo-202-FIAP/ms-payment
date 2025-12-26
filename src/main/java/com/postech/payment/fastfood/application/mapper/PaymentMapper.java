package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.QrCode;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.PaymentResponse;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.QrCodeResponse;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
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
                .orderId(paymentEntity.getOrderId())
                .amount(paymentEntity.getAmount())
                .qrCode(QrCodeMapper.toDomain(paymentEntity.getQrCode()))
                .updatedAt(paymentEntity.getUpdatedAt())
                .build();
    }

    public static PaymentEntity toEntity(Payment payment) {
        if (payment == null) {
            return null;
        }

        final PaymentEntity entity = PaymentEntity.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .orderId(payment.getOrderId())
                .paymentDateTime(payment.getPaymentDateTime())
                .amount(payment.getAmount())
                .build();

        if (payment.getQrCode() != null) {
            final QrCodeEntity qrCodeEntity = QrCodeMapper.toEntity(payment.getQrCode());
            qrCodeEntity.setPayment(entity);
            entity.setQrCode(qrCodeEntity);
        }

        return entity;
    }

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        QrCodeResponse qrCodeResponse = null;
        if (payment.getQrCode() != null) {
            final QrCode qrCode = payment.getQrCode();
            qrCodeResponse = new QrCodeResponse(
                    qrCode.getId(),
                    qrCode.getOrderId(),
                    qrCode.getCurrency(),
                    qrCode.getExpiresAt(),
                    qrCode.getQrCode(),
                    qrCode.getTotalAmount()
            );
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentDateTime(),
                payment.getPaymentMethod().getMethod(),
                payment.getStatus().getStatus(),
                payment.getUpdatedAt(),
                qrCodeResponse

        );
    }
}
