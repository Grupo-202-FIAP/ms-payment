package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;

import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import com.postech.payment.fastfood.utils.JsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessagingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumerPaymentQueue Integration Tests")
class ConsumerPaymentQueueIntegrationTest {

    @Mock
    private LoggerPort loggerPort;

    @Mock
    private JsonConverter jsonConverter;

    @Mock
    private GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;

    @Mock
    private RollbackPaymentUseCase rollbackPaymentUseCase;

    @Mock
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    private ConsumerPaymentQueue consumerPaymentQueue;
    private PaymentEventHandler paymentEventHandler;

    @BeforeEach
    void setUp() {
        paymentEventHandler = new PaymentEventHandler(
                loggerPort,
                generateQrCodePaymentUseCase,
                rollbackPaymentUseCase,
                publishEventPaymentStatusPort
        );
        consumerPaymentQueue = new ConsumerPaymentQueue(
                loggerPort,
                paymentEventHandler,
                jsonConverter
        );
    }

    @Nested
    @DisplayName("Success Event Handling")
    class SuccessEventHandling {

        @Test
        @DisplayName("Should process SUCCESS event and generate QR code")
        void shouldProcessSuccessEventAndGenerateQrCode() {
            // Given
            final String payload = "{\"status\":\"SUCCESS\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.50));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            verify(generateQrCodePaymentUseCase).execute(order, transactionId);
            verify(rollbackPaymentUseCase, never()).execute(any());
            verify(publishEventPaymentStatusPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should process SUCCESS event with history and generate QR code")
        void shouldProcessSuccessEventWithHistoryAndGenerateQrCode() {
            // Given
            final String payload = "{\"status\":\"SUCCESS\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(250.00));
            final List<History> history = buildHistory("ORDER", "SUCCESS", "Order created");
            final EventOrder event = buildEventOrderWithHistory(orderId, transactionId, "SUCCESS", order, history);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            verify(generateQrCodePaymentUseCase).execute(order, transactionId);
        }
    }

    @Nested
    @DisplayName("Fail Event Handling (Rollback)")
    class FailEventHandling {

        @Test
        @DisplayName("Should process FAIL event and execute rollback")
        void shouldProcessFailEventAndExecuteRollback() {
            // Given
            final String payload = "{\"status\":\"FAIL\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            verify(rollbackPaymentUseCase).execute(orderId);
            verify(generateQrCodePaymentUseCase, never()).execute(any(), any());
            verify(publishEventPaymentStatusPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should process FAIL event with history and execute rollback")
        void shouldProcessFailEventWithHistoryAndExecuteRollback() {
            // Given
            final String payload = "{\"status\":\"FAIL\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final List<History> history = buildHistory("INVENTORY", "FAIL", "Insufficient stock");
            final EventOrder event = buildEventOrderWithHistory(orderId, transactionId, "FAIL", null, history);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            verify(rollbackPaymentUseCase).execute(orderId);
        }
    }

    @Nested
    @DisplayName("Unsupported Event Handling")
    class UnsupportedEventHandling {

        @Test
        @DisplayName("Should not throw exception for unsupported event status and log warning")
        void shouldNotThrowExceptionForUnsupportedEventStatus() {
            // Given
            final String payload = "{\"status\":\"ROLLBACK_PENDING\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "ROLLBACK_PENDING", null);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When & Then
            assertThatCode(() -> consumerPaymentQueue.consumeMessage(payload))
                    .doesNotThrowAnyException();

            verify(generateQrCodePaymentUseCase, never()).execute(any(), any());
            verify(rollbackPaymentUseCase, never()).execute(any());
            // Warn is called twice: once in PaymentEventHandler and once in ConsumerPaymentQueue
            verify(loggerPort, times(2)).warn(anyString(), anyString());
        }

        @Test
        @DisplayName("Should catch PaymentEventNotSupportedException and log warning")
        void shouldCatchPaymentEventNotSupportedExceptionAndLogWarning() {
            // Given
            final String payload = "{\"status\":\"ROLLBACK_PENDING\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "ROLLBACK_PENDING", null);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then - should not throw, just log warning
            verify(loggerPort).warn(eq("[PaymentQueueListener] Evento ignorado por regra de negocio: {}"), anyString());
        }
    }

    @Nested
    @DisplayName("Exception Handling with Rollback Publishing")
    class ExceptionHandlingWithRollbackPublishing {

        @Test
        @DisplayName("Should publish rollback when generateQrCodePaymentUseCase throws exception and remove message from queue")
        void shouldPublishRollbackWhenGenerateQrCodeThrowsExceptionAndRemoveMessageFromQueue() {
            // Given
            final String payload = "{\"status\":\"SUCCESS\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.50));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);
            doThrow(new PaymentIntegrationException("Integration error with MercadoPago"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When - Should not throw exception (message should be removed from queue)
            assertThatCode(() -> consumerPaymentQueue.consumeMessage(payload))
                    .doesNotThrowAnyException();

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            EventPayment publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.getTransactionId()).isEqualTo(transactionId);
            assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
            assertThat(publishedEvent.getStatus()).isEqualTo("ROLLBACK_PENDING");
            assertThat(publishedEvent.getSource()).isEqualTo("PAYMENT");
        }

        @Test
        @DisplayName("Should publish rollback with history when exception occurs during processing")
        void shouldPublishRollbackWithHistoryWhenExceptionOccurs() {
            // Given
            final String payload = "{\"status\":\"SUCCESS\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(50.00));
            final List<History> existingHistory = buildHistory("ORDER", "SUCCESS", "Order confirmed");
            final EventOrder event = buildEventOrderWithHistory(orderId, transactionId, "SUCCESS", order, existingHistory);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);
            doThrow(new DatabaseException("Database connection failed"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            EventPayment publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.getHistory()).isNotEmpty();
            assertThat(publishedEvent.getHistory()).hasSizeGreaterThanOrEqualTo(2);

            // Verify the rollback history entry was added
            History rollbackHistory = publishedEvent.getHistory().get(publishedEvent.getHistory().size() - 1);
            assertThat(rollbackHistory.getSource()).isEqualTo("PAYMENT");
            assertThat(rollbackHistory.getStatus()).isEqualTo("ROLLBACK_PENDING");
            assertThat(rollbackHistory.getMessage()).contains("Rollback iniciado");
        }

        @Test
        @DisplayName("Should publish rollback when rollbackPaymentUseCase throws exception")
        void shouldPublishRollbackWhenRollbackUseCaseThrowsException() {
            // Given
            final String payload = "{\"status\":\"FAIL\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);
            doThrow(new DatabaseException("Payment not found for rollback"))
                    .when(rollbackPaymentUseCase).execute(any(UUID.class));

            // When - Should not throw exception
            assertThatCode(() -> consumerPaymentQueue.consumeMessage(payload))
                    .doesNotThrowAnyException();

            // Then
            verify(publishEventPaymentStatusPort).publish(any(EventPayment.class));
        }

        @Test
        @DisplayName("Should handle exception gracefully when both rollback use case and publish fail")
        void shouldHandleExceptionWhenBothRollbackAndPublishFail() {
            // Given
            final String payload = "{\"status\":\"FAIL\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);
            doThrow(new DatabaseException("Payment not found"))
                    .when(rollbackPaymentUseCase).execute(any(UUID.class));
            doThrow(new MessagingException("SNS publish failed"))
                    .when(publishEventPaymentStatusPort).publish(any());

            // When & Then - MessagingException is caught, logged and message is removed from queue
            assertThatCode(() -> consumerPaymentQueue.consumeMessage(payload))
                    .doesNotThrowAnyException();

            // Verify error was logged
            verify(loggerPort).error(eq("[PaymentQueueListener] Erro de mensageria. Rollback publicado. Mensagem será removida da fila."), any(MessagingException.class));
        }
    }

    @Nested
    @DisplayName("Message Processing End-to-End")
    class MessageProcessingEndToEnd {

        @Test
        @DisplayName("Should complete full flow from JSON payload to use case execution")
        void shouldCompleteFullFlowFromJsonPayloadToUseCaseExecution() {
            // Given
            final String payload = "{\"status\":\"SUCCESS\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal totalPrice = BigDecimal.valueOf(199.99);
            final Order order = buildOrder(orderId, totalPrice);
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            ArgumentCaptor<UUID> transactionIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(generateQrCodePaymentUseCase).execute(orderCaptor.capture(), transactionIdCaptor.capture());

            assertThat(orderCaptor.getValue().getId()).isEqualTo(orderId);
            assertThat(orderCaptor.getValue().getTotalPrice()).isEqualByComparingTo(totalPrice);
            assertThat(transactionIdCaptor.getValue()).isEqualTo(transactionId);
        }

        @Test
        @DisplayName("Should handle multiple messages sequentially")
        void shouldHandleMultipleMessagesSequentially() {
            // Given
            final String payload1 = "{\"status\":\"SUCCESS\"}";
            final UUID orderId1 = UUID.randomUUID();
            final UUID transactionId1 = UUID.randomUUID();
            final Order order1 = buildOrder(orderId1, BigDecimal.valueOf(50.00));
            final EventOrder event1 = buildEventOrder(orderId1, transactionId1, "SUCCESS", order1);

            final String payload2 = "{\"status\":\"FAIL\"}";
            final UUID orderId2 = UUID.randomUUID();
            final UUID transactionId2 = UUID.randomUUID();
            final EventOrder event2 = buildEventOrder(orderId2, transactionId2, "FAIL", null);

            when(jsonConverter.toEventOrder(payload1)).thenReturn(event1);
            when(jsonConverter.toEventOrder(payload2)).thenReturn(event2);

            // When
            consumerPaymentQueue.consumeMessage(payload1);
            consumerPaymentQueue.consumeMessage(payload2);

            // Then
            verify(generateQrCodePaymentUseCase).execute(order1, transactionId1);
            verify(rollbackPaymentUseCase).execute(orderId2);
        }

        @Test
        @DisplayName("Should log info message when receiving payload")
        void shouldLogInfoMessageWhenReceivingPayload() {
            // Given
            final String payload = "{\"status\":\"SUCCESS\"}";
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            when(jsonConverter.toEventOrder(payload)).thenReturn(event);

            // When
            consumerPaymentQueue.consumeMessage(payload);

            // Then
            verify(loggerPort).info("[PaymentQueueListener] Mensagem recebida");
        }
    }

    // Helper methods
    private Order buildOrder(UUID orderId, BigDecimal totalPrice) {
        return new Order.Builder()
                .id(orderId)
                .totalPrice(totalPrice)
                .identifier("ORDER-" + orderId.toString().substring(0, 8))
                .build();
    }

    private EventOrder buildEventOrder(UUID orderId, UUID transactionId, String status, Order payload) {
        return EventOrder.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .transactionId(transactionId)
                .status(status)
                .payload(payload)
                .source("ORDER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private EventOrder buildEventOrderWithHistory(UUID orderId, UUID transactionId, String status, Order payload, List<History> history) {
        return EventOrder.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .transactionId(transactionId)
                .status(status)
                .payload(payload)
                .source("ORDER")
                .history(history)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<History> buildHistory(String source, String status, String message) {
        List<History> history = new ArrayList<>();
        history.add(History.builder()
                .source(source)
                .status(status)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build());
        return history;
    }
}

