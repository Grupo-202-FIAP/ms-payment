package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.PaymentResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.QrCodeResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    @Test
    void toResponse_whenPaymentIsNull_thenReturnNull() {
        final PaymentResponse result = PaymentMapper.toResponse(null);
        assertNull(result);
    }

    @Test
    void toResponse_whenPaymentWithoutQrCode_thenReturnPaymentResponseWithoutQrCode() {
        final UUID paymentId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();
        final Payment payment = new Payment.Builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .build();

        final PaymentResponse result = PaymentMapper.toResponse(payment);

        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(orderId, result.orderId());
        assertEquals(BigDecimal.valueOf(100.50), result.amount());
        assertEquals(PaymentStatus.PENDING.getStatus(), result.status());
        assertEquals(PaymentMethod.QR_CODE.getMethod(), result.paymentMethod());
        assertEquals(now, result.paymentDateTime());
        assertEquals(now, result.updatedAt());
        assertNull(result.qrCodeDetails());
    }

    @Test
    void toResponse_whenPaymentWithQrCode_thenReturnPaymentResponseWithQrCode() {
        final UUID paymentId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final UUID qrCodeId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        final QrCode qrCode = new QrCode.Builder()
                .id(qrCodeId)
                .orderId(orderId)
                .totalAmount(BigDecimal.valueOf(100.50))
                .currency("BRL")
                .qrCode("qrcode-data-123")
                .expiresAt(expiresAt)
                .build();

        final Payment payment = new Payment.Builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .qrCode(qrCode)
                .build();

        final PaymentResponse result = PaymentMapper.toResponse(payment);

        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(orderId, result.orderId());
        assertNotNull(result.qrCodeDetails());
        final QrCodeResponse qrCodeResponse = result.qrCodeDetails();
        assertEquals(qrCodeId, qrCodeResponse.id());
        assertEquals(orderId, qrCodeResponse.orderId());
        assertEquals(BigDecimal.valueOf(100.50), qrCodeResponse.totalAmount());
        assertEquals("BRL", qrCodeResponse.currency());
        assertEquals("qrcode-data-123", qrCodeResponse.qrCode());
        assertEquals(expiresAt, qrCodeResponse.expiresAt());
    }
}

