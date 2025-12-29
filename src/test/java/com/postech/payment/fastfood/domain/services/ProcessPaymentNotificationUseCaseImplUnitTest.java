package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.exception.NotificationValidationException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao.Data;
import com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao.WebhookEvent;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessPaymentNotificationUseCaseImplUnitTest {

    @Mock
    private MercadoPagoWebhookSignatureValidator validator;

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @Mock
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    @Mock
    private LoggerPort logger;

    @InjectMocks
    private ProcessPaymentNotificationUseCaseImpl useCase;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void whenSignatureMissing_thenThrow() {
        final WebhookEvent event = new WebhookEvent();
        final Data data = new Data();
        data.setExternalReference(UUID.randomUUID().toString());
        event.setData(data);
        event.setAction("order.processed");

        assertThrows(NotificationValidationException.class,
                () -> useCase.execute(event, null, "req", "data"));
        // logger.error is called inside method but not asserted here (behavior validated by exception)
    }

    @Test
    void whenNoPaymentFound_thenDoNothing() {
        final UUID orderId = UUID.randomUUID();
        final WebhookEvent event = new WebhookEvent();
        final Data data = new Data();
        data.setExternalReference(orderId.toString());
        event.setData(data);
        event.setAction("order.processed");

        when(paymentRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.empty());

        useCase.execute(event, "sig", "req", orderId.toString());

        verify(publishEventPaymentStatusPort, never()).publish(any());
        verify(paymentRepositoryPort).findByOrderId(orderId);
    }

    @Test
    void whenOrderProcessed_thenMarkProcessedAndPublish() {
        final UUID orderId = UUID.randomUUID();
        final WebhookEvent event = new WebhookEvent();
        final Data data = new Data();
        data.setExternalReference(orderId.toString());
        event.setData(data);
        event.setAction("order.processed");

        final Payment payment = new Payment.Builder().orderId(orderId).status(PaymentStatus.PENDING).build();
        when(paymentRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        useCase.execute(event, "sig", "req", orderId.toString());

        assertEquals(PaymentStatus.PROCESSED, payment.getStatus());
        verify(paymentRepositoryPort).save(payment);
        verify(publishEventPaymentStatusPort).publish(any());
    }

    @Test
    void whenOrderExpired_thenMarkExpiredAndPublish() {
        final UUID orderId = UUID.randomUUID();
        final WebhookEvent event = new WebhookEvent();
        final Data data = new Data();
        data.setExternalReference(orderId.toString());
        event.setData(data);
        event.setAction("order.expired");

        final Payment payment = new Payment.Builder().orderId(orderId).status(PaymentStatus.PENDING).build();
        when(paymentRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        useCase.execute(event, "sig", "req", orderId.toString());

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        verify(paymentRepositoryPort).save(payment);
        verify(publishEventPaymentStatusPort).publish(any());
    }

    @Test
    void whenUnknownAction_thenLogAndDoNothing() {
        final UUID orderId = UUID.randomUUID();
        final WebhookEvent event = new WebhookEvent();
        final Data data = new Data();
        data.setExternalReference(orderId.toString());
        event.setData(data);
        event.setAction("unknown.action");

        final Payment payment = new Payment.Builder().orderId(orderId).status(PaymentStatus.PENDING).build();
        when(paymentRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        useCase.execute(event, "sig", "req", orderId.toString());

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(paymentRepositoryPort, never()).save(any());
        verify(publishEventPaymentStatusPort, never()).publish(any());
        verify(logger).info(anyString(), eq("unknown.action"), eq(orderId.toString()));
    }

}

