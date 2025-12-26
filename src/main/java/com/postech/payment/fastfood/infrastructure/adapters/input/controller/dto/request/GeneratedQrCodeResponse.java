package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GeneratedQrCodeResponse(
        UUID orderId,
        String paymentId,
        BigDecimal totalAmount,
        String currency,
        String qrCode,
        OffsetDateTime expiresAt
) {
}
