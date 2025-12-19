package com.postech.payment.fastfood.application.mapper;


import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.PaymentRequest;
import com.postech.payment.fastfood.infrastructure.persistence.documents.PaymentInformationQrCodeDocument;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
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
                .orderId(paymentEntity.getOrderId())
                .build();
    }

    public static PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .orderId(payment.getOrderId())
                .paymentDateTime(payment.getPaymentDateTime())
                .amount(payment.getAmount())
                .build();
    }

    public static Payment toDomain(@Valid PaymentRequest paymentRequest) {
        return new Payment.Builder()
                .paymentMethod(paymentRequest.paymentMethod())
                .build();
    }

    public static PaymentInformationQrCodeDocument toPaymentDocument(GenerateQrCodeResult generateQrCodeResult) {
        return PaymentInformationQrCodeDocument
                .builder()
                .qrCode(generateQrCodeResult.qrCode())
                .paymentId(generateQrCodeResult.paymentId())
                .orderId(generateQrCodeResult.orderId())
                .totalAmount(generateQrCodeResult.totalAmount())
                .currency(generateQrCodeResult.currency())
                .expiresAt(generateQrCodeResult.expiresAt() != null
                        ? generateQrCodeResult.expiresAt().toInstant()
                        : null)
                .build();
    }
}
