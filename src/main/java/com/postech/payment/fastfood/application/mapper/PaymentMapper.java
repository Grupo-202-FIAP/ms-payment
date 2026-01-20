package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.PaymentResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.QrCodeResponse;
import org.springframework.stereotype.Component;
 
@Component
public class PaymentMapper {

    public PaymentMapper() {

    }

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        QrCodeResponse qrCodeResponse = null;
        if (payment.getQrData() != null) {
            final QrCode qrCode = payment.getQrData();
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
