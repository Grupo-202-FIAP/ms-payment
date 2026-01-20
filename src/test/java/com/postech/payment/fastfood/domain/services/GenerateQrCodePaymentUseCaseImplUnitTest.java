package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.support.builders.OrderTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerateQrCodePaymentUseCaseImplUnitTest {

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private LoggerPort logger;

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @Mock
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    @InjectMocks
    private GenerateQrCodePaymentUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenExistingExpiredQrCode_thenMarkExpiringAndPublish() {
        final Order order = OrderTestBuilder.validOrder();
        final Payment existing = new Payment.Builder()
                .orderId(order.getId())
                .status(PaymentStatus.PENDING)
                .build();

        existing.setQrData(new com.postech.payment.fastfood.domain.model.QrCode.Builder()
                .orderId(order.getId())
                .expiresAt(OffsetDateTime.now().minusDays(1))
                .build());

        when(paymentRepositoryPort.findByOrderId(order.getId())).thenReturn(Optional.of(existing));

        final UUID transactionId = UUID.randomUUID();
        useCase.execute(order, transactionId);

        verify(paymentRepositoryPort).save(existing);
        assertEquals(PaymentStatus.EXPIRING, existing.getStatus());
        verify(publishEventPaymentStatusPort).publish(any());
    }

    @Test
    void whenExistingValidQrCode_thenDontChangeStatusAndDontPublish() {
        final Order order = OrderTestBuilder.validOrder();
        final Payment existing = new Payment.Builder()
                .orderId(order.getId())
                .status(PaymentStatus.PENDING)
                .build();

        existing.setQrData(new com.postech.payment.fastfood.domain.model.QrCode.Builder()
                .orderId(order.getId())
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .build());

        when(paymentRepositoryPort.findByOrderId(order.getId())).thenReturn(Optional.of(existing));

        useCase.execute(order, UUID.randomUUID());

        verify(paymentRepositoryPort, never()).save(any());
        verify(publishEventPaymentStatusPort, never()).publish(any());
        assertEquals(PaymentStatus.PENDING, existing.getStatus());
    }

    @Test
    void whenNoExistingAndCreateSucceeds_thenSaveWithQrCode() {
        final Order order = OrderTestBuilder.validOrder();
        when(paymentRepositoryPort.findByOrderId(order.getId())).thenReturn(Optional.empty());

        final UUID transactionId = UUID.randomUUID();
        final GeneratedQrCodeResponse response = new GeneratedQrCodeResponse(
                order.getId(),
                "payment-1",
                BigDecimal.ONE,
                "BRL",
                "qrcode-data",
                OffsetDateTime.now().plusHours(1)
        );

        when(paymentPort.createQrCode(any(), any())).thenReturn(response);

        useCase.execute(order, transactionId);

        verify(paymentRepositoryPort).save(paymentCaptor.capture());
        final Payment saved = paymentCaptor.getValue();
        assertNotNull(saved.getQrData());
        assertEquals("qrcode-data", saved.getQrData().getQrCode());
    }

    @Test
    void whenNoExistingAndCreateReturnsNull_thenDontSave() {
        final Order order = OrderTestBuilder.validOrder();
        when(paymentRepositoryPort.findByOrderId(order.getId())).thenReturn(Optional.empty());
        when(paymentPort.createQrCode(any(), any())).thenReturn(null);

        useCase.execute(order, UUID.randomUUID());

        verify(paymentRepositoryPort, never()).save(any());
    }

    @Test
    void whenExistingPaymentWithoutQrCode_thenCreateNewQrCode() {
        final Order order = OrderTestBuilder.validOrder();
        final Payment existing = new Payment.Builder()
                .orderId(order.getId())
                .status(PaymentStatus.PENDING)
                .build();
        // Payment exists but has no QR code

        when(paymentRepositoryPort.findByOrderId(order.getId())).thenReturn(Optional.of(existing));

        final UUID transactionId = UUID.randomUUID();
        final GeneratedQrCodeResponse response = new GeneratedQrCodeResponse(
                order.getId(),
                "payment-1",
                BigDecimal.ONE,
                "BRL",
                "qrcode-data",
                OffsetDateTime.now().plusHours(1)
        );

        when(paymentPort.createQrCode(any(), any())).thenReturn(response);

        useCase.execute(order, transactionId);

        verify(paymentRepositoryPort).save(paymentCaptor.capture());
        final Payment saved = paymentCaptor.getValue();
        assertEquals(existing, saved); // Should be the same payment instance
        assertNotNull(saved.getQrData());
        assertEquals("qrcode-data", saved.getQrData().getQrCode());
        verify(publishEventPaymentStatusPort, never()).publish(any());
    }
}

