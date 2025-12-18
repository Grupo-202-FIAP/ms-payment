package com.postech.payment.fastfood.infrastructure.controller.dto.request;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record GenerateQrCodeResult(
        String orderId,
        String paymentId,
        BigDecimal totalAmount,
        String currency,
        String qrCode,
        OffsetDateTime expiresAt
) {
}
