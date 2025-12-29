package com.postech.payment.fastfood.infrastructure.persistence.mapper;

import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapperEntity {

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
                .qrCode(QrCodeMapperEntity.toDomain(paymentEntity.getQrCode()))
                .updatedAt(paymentEntity.getUpdatedAt())
                .transactionId(paymentEntity.getTransactionId())
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
                .transactionId(payment.getTransactionId())
                .build();

        if (payment.getQrCode() != null) {
            final QrCodeEntity qrCodeEntity = QrCodeMapperEntity.toEntity(payment.getQrCode());
            qrCodeEntity.setPayment(entity);
            entity.setQrCode(qrCodeEntity);
        }

        return entity;
    }
}
