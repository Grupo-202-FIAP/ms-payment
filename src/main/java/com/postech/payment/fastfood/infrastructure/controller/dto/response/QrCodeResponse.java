package com.postech.payment.fastfood.infrastructure.controller.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record QrCodeResponse(
        UUID id,
        UUID orderId,
        String currency,
        OffsetDateTime expiresAt,
        String qrCode,
        BigDecimal totalAmount
) {
}
