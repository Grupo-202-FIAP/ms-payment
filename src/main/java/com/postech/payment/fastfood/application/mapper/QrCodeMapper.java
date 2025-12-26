package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.QrCode;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import org.springframework.stereotype.Component;

@Component
public class QrCodeMapper {

    public static QrCode toDomain(GeneratedQrCodeResponse response) {
        return new QrCode.Builder()
                .orderId(response.orderId())
                .totalAmount(response.totalAmount())
                .currency(response.currency())
                .qrCode(response.qrCode())
                .expiresAt(response.expiresAt())
                .build();

    }

    public static QrCode toDomain(QrCodeEntity qrCodeEntity) {
        if (qrCodeEntity == null) {
            return null;
        }

        final QrCode.Builder builder = new QrCode.Builder()
                .id(qrCodeEntity.getId())
                .orderId(qrCodeEntity.getOrderId())
                .totalAmount(qrCodeEntity.getTotalAmount())
                .currency(qrCodeEntity.getCurrency())
                .qrCode(qrCodeEntity.getQrCode())
                .expiresAt(qrCodeEntity.getExpiresAt());

        if (qrCodeEntity.getPayment() != null) {
            final PaymentEntity paymentEntity = qrCodeEntity.getPayment();
            builder.payment(
                    new Payment.Builder()
                            .id(paymentEntity.getId())
                            .orderId(paymentEntity.getOrderId())
                            .amount(paymentEntity.getAmount())
                            .status(paymentEntity.getStatus())
                            .paymentMethod(paymentEntity.getPaymentMethod())
                            .paymentDateTime(paymentEntity.getPaymentDateTime())
                            .updatedAt(paymentEntity.getUpdatedAt())
                            .build()
            );
        }

        return builder.build();
    }

    public static QrCodeEntity toEntity(QrCode qrCode) {
        if (qrCode == null) {
            return null;
        }
        return QrCodeEntity.builder()
                .id(qrCode.getId())
                .orderId(qrCode.getOrderId())
                .totalAmount(qrCode.getTotalAmount())
                .currency(qrCode.getCurrency())
                .qrCode(qrCode.getQrCode())
                .expiresAt(qrCode.getExpiresAt())
                .build();
    }

}
