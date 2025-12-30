package com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto;

import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventPaymentTest {

    @Test
    void eventExpired_whenPaymentProvided_thenReturnEventPaymentWithFailStatus() {
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .transactionId(transactionId)
                .status(PaymentStatus.EXPIRED)
                .paymentMethod(PaymentMethod.QR_CODE)
                .amount(BigDecimal.valueOf(100.50))
                .build();

        final EventPayment result = new EventPayment().eventExpired(payment);

        assertNotNull(result);
        assertEquals("FAIL", result.getStatus());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(orderId, result.getOrderId());
        assertEquals("PAYMENT", result.getSource());
        assertEquals(payment, result.getPayload());
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());
        final History history = result.getHistory().get(0);
        assertEquals("PAYMENT", history.getSource());
        assertEquals("EXPIRED", history.getStatus());
        assertTrue(history.getMessage().contains("EXPIRED"));
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void eventExpiring_whenPaymentAndTransactionIdProvided_thenReturnEventPaymentWithRollbackPendingStatus() {
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .status(PaymentStatus.EXPIRING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .amount(BigDecimal.valueOf(100.50))
                .build();

        final EventPayment result = new EventPayment().eventExpiring(payment, transactionId);

        assertNotNull(result);
        assertEquals("ROLLBACK_PENDING", result.getStatus());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(orderId, result.getOrderId());
        assertEquals("PAYMENT", result.getSource());
        assertEquals(payment, result.getPayload());
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());
        final History history = result.getHistory().get(0);
        assertEquals("PAYMENT", history.getSource());
        assertEquals("ROLLBACK_PENDING", history.getStatus());
        assertTrue(history.getMessage().contains("EXPIRING"));
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void eventSuccess_whenPaymentProvided_thenReturnEventPaymentWithSuccessStatus() {
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .transactionId(transactionId)
                .status(PaymentStatus.PROCESSED)
                .paymentMethod(PaymentMethod.QR_CODE)
                .amount(BigDecimal.valueOf(100.50))
                .build();

        final EventPayment result = new EventPayment().eventSuccess(payment);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(orderId, result.getOrderId());
        assertEquals("PAYMENT", result.getSource());
        assertEquals(payment, result.getPayload());
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());
        final History history = result.getHistory().get(0);
        assertEquals("PAYMENT", history.getSource());
        assertEquals("PROCESSED", history.getStatus());
        assertTrue(history.getMessage().contains("PROCESSED"));
        assertNotNull(result.getCreatedAt());
    }
}

