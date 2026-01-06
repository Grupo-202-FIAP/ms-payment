package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;

import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventHandler Integration Tests")
class PaymentEventHandlerIntegrationTest {

    @Mock
    private LoggerPort loggerPort;

    @Mock
    private GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;

    @Mock
    private RollbackPaymentUseCase rollbackPaymentUseCase;

    @Mock
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    private PaymentEventHandler paymentEventHandler;

    @BeforeEach
    void setUp() {
        paymentEventHandler = new PaymentEventHandler(
                loggerPort,
                generateQrCodePaymentUseCase,
                rollbackPaymentUseCase,
                publishEventPaymentStatusPort
        );
    }

    @Nested
    @DisplayName("Handle SUCCESS Events")
    class HandleSuccessEvents {

        @Test
        @DisplayName("Should return true when SUCCESS event is processed successfully")
        void shouldReturnTrueWhenSuccessEventProcessedSuccessfully() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isTrue();
            verify(generateQrCodePaymentUseCase).execute(order, transactionId);
            verify(rollbackPaymentUseCase, never()).execute(any());
            verify(publishEventPaymentStatusPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should call generateQrCodePaymentUseCase with correct parameters")
        void shouldCallGenerateQrCodeWithCorrectParameters() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal totalPrice = BigDecimal.valueOf(250.50);
            final Order order = buildOrder(orderId, totalPrice);
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            ArgumentCaptor<UUID> transactionCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(generateQrCodePaymentUseCase).execute(orderCaptor.capture(), transactionCaptor.capture());

            assertThat(orderCaptor.getValue().getId()).isEqualTo(orderId);
            assertThat(orderCaptor.getValue().getTotalPrice()).isEqualByComparingTo(totalPrice);
            assertThat(transactionCaptor.getValue()).isEqualTo(transactionId);
        }

        @Test
        @DisplayName("Should log info messages during successful processing")
        void shouldLogInfoMessagesDuringSuccessfulProcessing() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            // When
            paymentEventHandler.handle(event);

            // Then
            verify(loggerPort).info("[PaymentEventHandler] Processando pagamento para pedido: {}", orderId);
            verify(loggerPort).info("[PaymentEventHandler] QR Code gerado com sucesso para pedido: {}", orderId);
        }
    }

    @Nested
    @DisplayName("Handle FAIL Events")
    class HandleFailEvents {

        @Test
        @DisplayName("Should return true when FAIL event triggers rollback successfully")
        void shouldReturnTrueWhenFailEventTriggersRollbackSuccessfully() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isTrue();
            verify(rollbackPaymentUseCase).execute(orderId);
            verify(generateQrCodePaymentUseCase, never()).execute(any(), any());
            verify(publishEventPaymentStatusPort, never()).publish(any());
        }

        @Test
        @DisplayName("Should call rollbackPaymentUseCase with correct orderId")
        void shouldCallRollbackWithCorrectOrderId() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<UUID> orderIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(rollbackPaymentUseCase).execute(orderIdCaptor.capture());

            assertThat(orderIdCaptor.getValue()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should log info messages during rollback processing")
        void shouldLogInfoMessagesDuringRollbackProcessing() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            // When
            paymentEventHandler.handle(event);

            // Then
            verify(loggerPort).info("[PaymentEventHandler] Executando rollback do pagamento. orderId={}", orderId);
            verify(loggerPort).info("[PaymentEventHandler] Rollback do pagamento concluído. orderId={}", orderId);
        }
    }

    @Nested
    @DisplayName("Handle Unsupported Events")
    class HandleUnsupportedEvents {

        @Test
        @DisplayName("Should return false and publish rollback for unknown status (IllegalArgumentException caught)")
        void shouldReturnFalseAndPublishRollbackForUnknownStatus() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "UNKNOWN", null);

            // When - UNKNOWN causes IllegalArgumentException from SagaStatus.valueOf()
            // which is caught and triggers rollback
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isFalse();
            verify(generateQrCodePaymentUseCase, never()).execute(any(), any());
            verify(rollbackPaymentUseCase, never()).execute(any());
            verify(publishEventPaymentStatusPort).publish(any(EventPayment.class));
        }

        @Test
        @DisplayName("Should throw PaymentEventNotSupportedException for ROLLBACK_PENDING status")
        void shouldThrowExceptionForRollbackPendingStatus() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "ROLLBACK_PENDING", null);

            // When & Then
            assertThatThrownBy(() -> paymentEventHandler.handle(event))
                    .isInstanceOf(PaymentEventNotSupportedException.class)
                    .hasMessageContaining("ROLLBACK_PENDING");
        }

        @Test
        @DisplayName("Should log warning when unsupported event is received")
        void shouldLogWarningWhenUnsupportedEventReceived() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "ROLLBACK_PENDING", null);

            // When & Then
            try {
                paymentEventHandler.handle(event);
            } catch (PaymentEventNotSupportedException ex) {
                // Expected exception
            }

            verify(loggerPort).warn("[PaymentEventHandler] Evento ignorado: {}", "Event status not supported: ROLLBACK_PENDING");
        }
    }

    @Nested
    @DisplayName("Exception Handling and Rollback Publishing")
    class ExceptionHandlingAndRollbackPublishing {

        @Test
        @DisplayName("Should return false and publish rollback when generateQrCodePaymentUseCase throws exception")
        void shouldReturnFalseAndPublishRollbackWhenGenerateQrCodeThrowsException() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            doThrow(new RuntimeException("MercadoPago API error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isFalse();
            verify(publishEventPaymentStatusPort).publish(any(EventPayment.class));
        }

        @Test
        @DisplayName("Should publish rollback event with correct transactionId and orderId")
        void shouldPublishRollbackEventWithCorrectIds() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(75.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            doThrow(new RuntimeException("Database error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            EventPayment publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.getTransactionId()).isEqualTo(transactionId);
            assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should publish rollback event with ROLLBACK_PENDING status")
        void shouldPublishRollbackEventWithRollbackPendingStatus() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            doThrow(new RuntimeException("Connection timeout"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getStatus()).isEqualTo("ROLLBACK_PENDING");
            assertThat(eventCaptor.getValue().getSource()).isEqualTo("PAYMENT");
        }

        @Test
        @DisplayName("Should include error reason in rollback history")
        void shouldIncludeErrorReasonInRollbackHistory() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);
            final String errorMessage = "Payment gateway unavailable";

            doThrow(new RuntimeException(errorMessage))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            EventPayment publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.getHistory()).isNotEmpty();

            History lastHistoryEntry = publishedEvent.getHistory().get(publishedEvent.getHistory().size() - 1);
            assertThat(lastHistoryEntry.getMessage()).contains("Rollback iniciado");
            assertThat(lastHistoryEntry.getMessage()).contains(errorMessage);
        }

        @Test
        @DisplayName("Should preserve existing history when publishing rollback")
        void shouldPreserveExistingHistoryWhenPublishingRollback() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final List<History> existingHistory = buildHistory("ORDER", "SUCCESS", "Order created");
            final EventOrder event = buildEventOrderWithHistory(orderId, transactionId, "SUCCESS", order, existingHistory);

            doThrow(new RuntimeException("Processing failed"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            EventPayment publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.getHistory()).hasSize(2);
            assertThat(publishedEvent.getHistory().get(0).getSource()).isEqualTo("ORDER");
            assertThat(publishedEvent.getHistory().get(1).getSource()).isEqualTo("PAYMENT");
        }

        @Test
        @DisplayName("Should return false and publish rollback when rollbackPaymentUseCase throws exception")
        void shouldReturnFalseAndPublishRollbackWhenRollbackThrowsException() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final EventOrder event = buildEventOrder(orderId, transactionId, "FAIL", null);

            doThrow(new RuntimeException("Payment not found for rollback"))
                    .when(rollbackPaymentUseCase).execute(any(UUID.class));

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isFalse();
            verify(publishEventPaymentStatusPort).publish(any(EventPayment.class));
        }

        @Test
        @DisplayName("Should rethrow exception when publishing rollback fails")
        void shouldRethrowExceptionWhenPublishingRollbackFails() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            doThrow(new RuntimeException("Processing error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));
            doThrow(new RuntimeException("SNS publish failed"))
                    .when(publishEventPaymentStatusPort).publish(any());

            // When & Then
            assertThatThrownBy(() -> paymentEventHandler.handle(event))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("SNS publish failed");
        }

        @Test
        @DisplayName("Should log error when exception occurs during processing")
        void shouldLogErrorWhenExceptionOccursDuringProcessing() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);
            final RuntimeException exception = new RuntimeException("Processing error");

            doThrow(exception).when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            verify(loggerPort).error(
                    eq("[PaymentEventHandler] Erro ao processar evento, iniciando rollback. transactionId={}"),
                    eq(transactionId),
                    eq(exception)
            );
        }
    }

    @Nested
    @DisplayName("Rollback Event Structure")
    class RollbackEventStructure {

        @Test
        @DisplayName("Should generate unique ID for rollback event")
        void shouldGenerateUniqueIdForRollbackEvent() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            doThrow(new RuntimeException("Error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getId()).isNotNull();
            assertThat(eventCaptor.getValue().getId()).isNotEqualTo(event.getId());
        }

        @Test
        @DisplayName("Should set createdAt timestamp for rollback event")
        void shouldSetCreatedAtTimestampForRollbackEvent() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);
            final LocalDateTime before = LocalDateTime.now();

            doThrow(new RuntimeException("Error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            final LocalDateTime after = LocalDateTime.now();
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getCreatedAt()).isNotNull();
            assertThat(eventCaptor.getValue().getCreatedAt()).isBetween(before, after.plusSeconds(1));
        }

        @Test
        @DisplayName("Should set PAYMENT as source for rollback event")
        void shouldSetPaymentAsSourceForRollbackEvent() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);

            doThrow(new RuntimeException("Error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getSource()).isEqualTo("PAYMENT");
        }

        @Test
        @DisplayName("Should create history with createdAt timestamp")
        void shouldCreateHistoryWithCreatedAtTimestamp() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));
            final EventOrder event = buildEventOrder(orderId, transactionId, "SUCCESS", order);
            final LocalDateTime before = LocalDateTime.now();

            doThrow(new RuntimeException("Error"))
                    .when(generateQrCodePaymentUseCase).execute(any(Order.class), any(UUID.class));

            // When
            paymentEventHandler.handle(event);

            // Then
            final LocalDateTime after = LocalDateTime.now();
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            History historyEntry = eventCaptor.getValue().getHistory().get(0);
            assertThat(historyEntry.getCreatedAt()).isNotNull();
            assertThat(historyEntry.getCreatedAt()).isBetween(before, after.plusSeconds(1));
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

