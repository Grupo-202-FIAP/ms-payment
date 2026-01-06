package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.utils.JsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.MessagingException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConsumerPaymentQueueTest {

    @Mock
    private LoggerPort loggerPort;

    @Mock
    private JsonConverter jsonConverter;

    @Mock
    private PaymentEventHandler paymentEventHandler;

    @Mock
    private GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;

    @Mock
    private RollbackPaymentUseCase rollbackPaymentUseCase;

    @InjectMocks
    private ConsumerPaymentQueue consumerPaymentQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consumeMessage_whenStatusIsSuccess_thenCallPaymentEventHandler() {
        final String payload = "{\"status\":\"SUCCESS\"}";
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Order order = new Order.Builder().id(orderId).build();
        final EventOrder event = EventOrder.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .status("SUCCESS")
                .payload(order)
                .build();

        when(jsonConverter.toEventOrder(payload)).thenReturn(event);

        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler).handle(event);
    }

    @Test
    void consumeMessage_whenStatusIsRollbackPending_thenCallPaymentEventHandler() {
        final String payload = "{\"status\":\"ROLLBACK_PENDING\"}";
        final UUID orderId = UUID.randomUUID();
        final EventOrder event = EventOrder.builder()
                .orderId(orderId)
                .status("ROLLBACK_PENDING")
                .build();

        when(jsonConverter.toEventOrder(payload)).thenReturn(event);

        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler).handle(event);
    }

    @Test
    void consumeMessage_whenStatusIsUnhandled_thenPaymentEventNotSupportedExceptionIsCaughtAndLogged() {
        final String payload = "{\"status\":\"UNKNOWN\"}";
        final UUID orderId = UUID.randomUUID();
        final EventOrder event = EventOrder.builder()
                .orderId(orderId)
                .status("UNKNOWN")
                .build();

        when(jsonConverter.toEventOrder(payload)).thenReturn(event);
        doThrow(new PaymentEventNotSupportedException("UNKNOWN")).when(paymentEventHandler).handle(event);

        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler).handle(event);
        verify(loggerPort).warn(anyString(), anyString());
    }

    @Test
    void consumeMessage_whenConversionException_thenLogErrorAndDoNotThrow() {
        final String payload = "invalid json";
        final ConversionException exception = new ConversionException("Invalid JSON", new RuntimeException());

        when(jsonConverter.toEventOrder(payload)).thenThrow(exception);

        // Should not throw - exception is caught and logged
        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler, never()).handle(any());
        verify(loggerPort).error(anyString(), any(Exception.class));
    }

    @Test
    void consumeMessage_whenPaymentIntegrationException_thenLogErrorAndDoNotThrow() {
        final String payload = "{\"status\":\"SUCCESS\"}";
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Order order = new Order.Builder().id(orderId).build();
        final EventOrder event = EventOrder.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .status("SUCCESS")
                .payload(order)
                .build();
        final PaymentIntegrationException exception = new PaymentIntegrationException("Integration error");

        when(jsonConverter.toEventOrder(payload)).thenReturn(event);
        doThrow(exception).when(paymentEventHandler).handle(event);

        // Should not throw - exception is caught and logged
        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler).handle(event);
        verify(loggerPort).error(anyString(), any(Exception.class));
    }

    @Test
    void consumeMessage_whenDatabaseException_thenLogErrorAndDoNotThrow() {
        final String payload = "{\"status\":\"SUCCESS\"}";
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Order order = new Order.Builder().id(orderId).build();
        final EventOrder event = EventOrder.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .status("SUCCESS")
                .payload(order)
                .build();
        final DatabaseException exception = new DatabaseException("Database error");

        when(jsonConverter.toEventOrder(payload)).thenReturn(event);
        doThrow(exception).when(paymentEventHandler).handle(event);

        // Should not throw - exception is caught and logged
        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler).handle(event);
        verify(loggerPort).error(anyString(), any(Exception.class));
    }

    @Test
    void consumeMessage_whenMessagingException_thenLogErrorAndDoNotThrow() {
        final String payload = "{\"status\":\"SUCCESS\"}";
        final UUID orderId = UUID.randomUUID();
        final UUID transactionId = UUID.randomUUID();
        final Order order = new Order.Builder().id(orderId).build();
        final EventOrder event = EventOrder.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .status("SUCCESS")
                .payload(order)
                .build();
        final MessagingException exception = new MessagingException("Messaging error");

        when(jsonConverter.toEventOrder(payload)).thenReturn(event);
        doThrow(exception).when(paymentEventHandler).handle(event);

        // Should not throw - exception is caught and logged
        consumerPaymentQueue.consumeMessage(payload);

        verify(jsonConverter).toEventOrder(payload);
        verify(paymentEventHandler).handle(event);
        verify(loggerPort).error(anyString(), any(Exception.class));
    }
}
