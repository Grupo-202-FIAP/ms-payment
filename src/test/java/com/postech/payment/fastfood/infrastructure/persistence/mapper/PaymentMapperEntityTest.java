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

class PaymentMapperEntityTest {

    @Test
    void toDomain_whenEntityIsNull_thenReturnNull() {
        final Payment result = PaymentMapperEntity.toDomain(null);
        assertNull(result);
    }

    @Test
    void toDomain_whenEntityWithoutQrCode_thenReturnPaymentWithoutQrCode() {
        final UUID paymentId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final UUID transactionId = UUID.randomUUID();
        final PaymentEntity entity = PaymentEntity.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .transactionId(transactionId)
                .build();

        final Payment result = PaymentMapperEntity.toDomain(entity);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertEquals(orderId, result.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getAmount());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(PaymentMethod.QR_CODE, result.getPaymentMethod());
        assertEquals(now, result.getPaymentDateTime());
        assertEquals(now, result.getUpdatedAt());
        assertNull(result.getQrData());
    }

    @Test
    void toDomain_whenEntityWithQrCode_thenReturnPaymentWithQrCode() {
        final UUID paymentId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final UUID qrCodeId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        final QrCodeEntity qrCodeEntity = QrCodeEntity.builder()
                .id(qrCodeId)
                .orderId(orderId)
                .totalAmount(BigDecimal.valueOf(100.50))
                .currency("BRL")
                .qrCode("qrcode-data-123")
                .expiresAt(expiresAt)
                .build();

        final UUID transactionId = UUID.randomUUID();
        final PaymentEntity entity = PaymentEntity.builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .transactionId(transactionId)
                .qrCode(qrCodeEntity)
                .build();

        qrCodeEntity.setPayment(entity);

        final Payment result = PaymentMapperEntity.toDomain(entity);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertNotNull(result.getQrData());
        final QrCode qrCode = result.getQrData();
        assertEquals(qrCodeId, qrCode.getId());
        assertEquals(orderId, qrCode.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), qrCode.getTotalAmount());
        assertEquals("BRL", qrCode.getCurrency());
        assertEquals("qrcode-data-123", qrCode.getQrCode());
        assertEquals(expiresAt, qrCode.getExpiresAt());
    }

    @Test
    void toEntity_whenPaymentIsNull_thenReturnNull() {
        final PaymentEntity result = PaymentMapperEntity.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_whenPaymentWithoutQrCode_thenReturnEntityWithoutQrCode() {
        final UUID paymentId = UUID.randomUUID();
        final UUID orderId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final UUID transactionId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .transactionId(transactionId)
                .build();

        final PaymentEntity result = PaymentMapperEntity.toEntity(payment);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertEquals(orderId, result.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getAmount());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(PaymentMethod.QR_CODE, result.getPaymentMethod());
        assertEquals(now, result.getPaymentDateTime());
        // updatedAt is managed by Hibernate @UpdateTimestamp annotation, 
        // so it's not mapped in toEntity() and will be null until persisted
        assertNull(result.getUpdatedAt());
        assertNull(result.getQrCode());
    }

    @Test
    void toEntity_whenPaymentWithQrCode_thenReturnEntityWithQrCode() {
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

        final UUID transactionId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .transactionId(transactionId)
                .qrCode(qrCode)
                .build();

        final PaymentEntity result = PaymentMapperEntity.toEntity(payment);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertNotNull(result.getQrCode());
        final QrCodeEntity qrCodeEntity = result.getQrCode();
        assertEquals(qrCodeId, qrCodeEntity.getId());
        assertEquals(orderId, qrCodeEntity.getOrderId());
        assertEquals(BigDecimal.valueOf(100.50), qrCodeEntity.getTotalAmount());
        assertEquals("BRL", qrCodeEntity.getCurrency());
        assertEquals("qrcode-data-123", qrCodeEntity.getQrCode());
        assertEquals(expiresAt, qrCodeEntity.getExpiresAt());
        assertEquals(result, qrCodeEntity.getPayment());
    }
}

