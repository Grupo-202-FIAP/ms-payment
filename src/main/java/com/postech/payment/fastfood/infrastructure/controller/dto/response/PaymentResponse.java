package com.postech.payment.fastfood.infrastructure.controller.dto.response;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID id,
        UUID orderId,
        BigDecimal amount,
        LocalDateTime paymentDateTime,
        String paymentMethod,
        String status,
        LocalDateTime updatedAt,
        QrCodeResponse qrCodeDetails
) {
}
