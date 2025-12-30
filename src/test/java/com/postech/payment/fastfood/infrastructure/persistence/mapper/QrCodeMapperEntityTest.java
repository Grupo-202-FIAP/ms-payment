package com.postech.payment.fastfood.infrastructure.persistence.mapper;

import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeMapperEntityTest {

    @Test
    void toDomain_whenEntityIsNull_thenReturnNull() {
        final QrCode result = QrCodeMapperEntity.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDomain_whenEntityWithoutPayment_thenReturnQrCodeWithoutPayment() {
        final UUID qrCodeId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        final QrCodeEntity entity = QrCodeEntity.builder()
                .id(qrCodeId)
                .orderId(orderId)
                .totalAmount(BigDecimal.valueOf(100.50))
                .currency("BRL")
                .qrCode("qrcode-data-123")
                .expiresAt(expiresAt)
                .build();

        final QrCode result = QrCodeMapperEntity.toDomain(entity);

        assertNotNull(result);
        assertEquals(qrCodeId, result.getId());
        assertEquals(orderId, result.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getTotalAmount());
        assertEquals("BRL", result.getCurrency());
        assertEquals("qrcode-data-123", result.getQrCode());
        assertEquals(expiresAt, result.getExpiresAt());
        assertNull(result.getPayment());
    }

    @Test
    void toDomain_whenEntityWithPayment_thenReturnQrCodeWithPayment() {
        final UUID qrCodeId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final UUID paymentId = UUID.randomUUID();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);
        final LocalDateTime now = LocalDateTime.now();

        final PaymentEntity paymentEntity = PaymentEntity.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .transactionId(UUID.randomUUID())
                .build();

        final QrCodeEntity entity = QrCodeEntity.builder()
                .id(qrCodeId)
                .orderId(orderId)
                .totalAmount(BigDecimal.valueOf(100.50))
                .currency("BRL")
                .qrCode("qrcode-data-123")
                .expiresAt(expiresAt)
                .payment(paymentEntity)
                .build();

        final QrCode result = QrCodeMapperEntity.toDomain(entity);

        assertNotNull(result);
        assertEquals(qrCodeId, result.getId());
        assertNotNull(result.getPayment());
        final Payment payment = result.getPayment();
        assertEquals(paymentId, payment.getId());
        assertEquals(orderId, payment.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), payment.getAmount());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentMethod.QR_CODE, payment.getPaymentMethod());
    }

    @Test
    void toEntity_whenDomainIsNull_thenReturnNull() {
        final QrCodeEntity result = QrCodeMapperEntity.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_whenDomainIsValid_thenReturnEntity() {
        final UUID qrCodeId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        final QrCode qrCode = new QrCode.Builder()
                .id(qrCodeId)
                .orderId(orderId)
                .totalAmount(BigDecimal.valueOf(100.50))
                .currency("BRL")
                .qrCode("qrcode-data-123")
                .expiresAt(expiresAt)
                .build();

        final QrCodeEntity result = QrCodeMapperEntity.toEntity(qrCode);

        assertNotNull(result);
        assertEquals(qrCodeId, result.getId());
        assertEquals(orderId, result.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getTotalAmount());
        assertEquals("BRL", result.getCurrency());
        assertEquals("qrcode-data-123", result.getQrCode());
        assertEquals(expiresAt, result.getExpiresAt());
    }
}

