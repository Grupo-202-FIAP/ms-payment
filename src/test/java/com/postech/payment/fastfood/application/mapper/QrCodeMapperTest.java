package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeMapperTest {

    @Test
    void toDomain_whenResponseIsNull_thenReturnNull() {
        final QrCode result = QrCodeMapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDomain_whenResponseIsValid_thenReturnQrCode() {
        final UUID orderId = UUID.randomUUID();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);
        final GeneratedQrCodeResponse response = new GeneratedQrCodeResponse(
                orderId,
                "payment-123",
                BigDecimal.valueOf(100.50),
                "BRL",
                "qrcode-data-123",
                expiresAt
        );

        final QrCode result = QrCodeMapper.toDomain(response);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getTotalAmount());
        assertEquals("BRL", result.getCurrency());
        assertEquals("qrcode-data-123", result.getQrCode());
        assertEquals(expiresAt, result.getExpiresAt());
    }
}

