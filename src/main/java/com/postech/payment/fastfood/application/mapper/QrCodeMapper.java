package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import org.springframework.stereotype.Component;

@Component
public class QrCodeMapper {

    public static QrCode toDomain(GeneratedQrCodeResponse response) {
        if (response == null) {
            return null;
        }

        return new QrCode.Builder()
                .orderId(response.orderId())
                .totalAmount(response.totalAmount())
                .currency(response.currency())
                .qrCode(response.qrCode())
                .expiresAt(response.expiresAt())
                .build();
    }
}
