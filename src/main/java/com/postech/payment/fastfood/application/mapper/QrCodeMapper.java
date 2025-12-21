package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.QrCode;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
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
        return new QrCode.Builder()
                .id(qrCodeEntity.getId())
                .orderId(qrCodeEntity.getOrderId())
                .totalAmount(qrCodeEntity.getTotalAmount())
                .currency(qrCodeEntity.getCurrency())
                .payment(
                        new Payment.Builder()
                                .id(qrCodeEntity.getPayment().getId())
                                .orderId(qrCodeEntity.getPayment().getOrderId())
                                .amount(
                                        qrCodeEntity.getPayment().getAmount()
                                )
                                .status(qrCodeEntity.getPayment().getStatus())
                                .paymentMethod(qrCodeEntity.getPayment().getPaymentMethod())
                                .paymentDateTime(qrCodeEntity.getPayment().getPaymentDateTime())
                                .updatedAt(qrCodeEntity.getPayment().getUpdatedAt())
                                .build()
                )
                .qrCode(qrCodeEntity.getQrCode())
                .expiresAt(qrCodeEntity.getExpiresAt())
                .build();
    }

    public static QrCodeEntity toEntity(QrCode qrCode) {
        if (qrCode == null) {
            return null;
        }
        return QrCodeEntity.builder()
                .orderId(qrCode.getOrderId())
                .totalAmount(qrCode.getTotalAmount())
                .currency(qrCode.getCurrency())
                .qrCode(qrCode.getQrCode())
                .expiresAt(qrCode.getExpiresAt())
                .build();
    }

    public static GeneratedQrCodeResponse toResponse(QrCode qrCode1) {
        return new GeneratedQrCodeResponse(
                qrCode1.getOrderId(),
                qrCode1.getId().toString(),
                qrCode1.getTotalAmount(),
                qrCode1.getCurrency(),
                qrCode1.getQrCode(),
                qrCode1.getExpiresAt()
        );
    }
}
