package com.postech.payment.fastfood.infrastructure.persistence.mapper;

import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;

public class QrCodeMapperEntity {
    public static QrCode toDomain(QrCodeEntity entity) {
        if (entity == null) {
            return null;
        }

        final QrCode.Builder builder = new QrCode.Builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .totalAmount(entity.getTotalAmount())
                .currency(entity.getCurrency())
                .qrCode(entity.getQrCode())
                .expiresAt(entity.getExpiresAt());

        if (entity.getPayment() != null) {
            final PaymentEntity p = entity.getPayment();
            builder.payment(new Payment.Builder()
                    .id(p.getId())
                    .orderId(p.getOrderId())
                    .amount(p.getAmount())
                    .status(p.getStatus())
                    .paymentMethod(p.getPaymentMethod())
                    .paymentDateTime(p.getPaymentDateTime())
                    .updatedAt(p.getUpdatedAt())
                    .build());
        }

        return builder.build();
    }

    public static QrCodeEntity toEntity(QrCode domain) {
        if (domain == null) {
            return null;
        }

        return QrCodeEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .totalAmount(domain.getTotalAmount())
                .currency(domain.getCurrency())
                .qrCode(domain.getQrCode())
                .expiresAt(domain.getExpiresAt())
                .build();
    }
}
