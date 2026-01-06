package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
@Builder
public record GeneratedQrCodeResponse(
        UUID orderId,
        String paymentId,
        BigDecimal totalAmount,
        String currency,
        String qrCode,
        OffsetDateTime expiresAt
) {
}
