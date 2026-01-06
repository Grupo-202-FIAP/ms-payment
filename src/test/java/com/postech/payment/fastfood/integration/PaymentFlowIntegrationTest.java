package com.postech.payment.fastfood.integration;

import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.services.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.domain.services.RollbackPaymentUseCaseImpl;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.ConsumerPaymentQueue;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.PaymentEventHandler;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.config.SnsMessagingConfig;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import com.postech.payment.fastfood.utils.JsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.SnsClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * True Integration Tests that test the full flow from ConsumerPaymentQueue
 * through real use case implementations to real repository adapters with H2 database.
 * Only external dependencies (MercadoPago, SQS, SNS) are mocked.
 */
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "server.port=0",
        "mercadoPago.publicKey=test-key",
        "mercadoPago.accessToken=test-token",
        "mercadoPago.clientId=test-id",
        "mercadoPago.clientSecret=test-secret",
        "mercadoPago.externalPosID=test-pos",
        "mercadoPago.webhook.secretKey=test-webhook",
        "spring.cloud.aws.region.static=us-east-1",
        "spring.cloud.aws.credentials.access-key=test",
        "spring.cloud.aws.credentials.secret-key=test",
        "spring.cloud.aws.sqs.endpoint=http://localhost:4566",
        "spring.cloud.aws.sqs.queues.process-payment-queue=test-queue",
        "spring.datasource.url=jdbc:h2:mem:integrationtestdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Payment Flow Integration Tests (UseCase + Repository)")
class PaymentFlowIntegrationTest {

    // Mock external dependencies only
    @MockitoBean
    private SnsClient snsClient;

    @MockitoBean
    private SnsMessagingConfig snsMessagingConfig;

    @MockitoBean
    private MercadoPagoClient mercadoPagoClient;

    @MockitoBean
    private PaymentPort paymentPort; // Mock MercadoPago integration

    @MockitoBean
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    @MockitoBean
    private ConsumerPaymentQueue consumerPaymentQueue; // Mock SQS listener to prevent auto-start

    // Use real implementations
    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;

    @Autowired
    private IPaymentEntityRepository paymentEntityRepository;

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private com.postech.payment.fastfood.application.ports.output.LoggerPort loggerPort;

    private GenerateQrCodePaymentUseCaseImpl generateQrCodePaymentUseCase;
    private RollbackPaymentUseCaseImpl rollbackPaymentUseCase;
    private PaymentEventHandler paymentEventHandler;

    @BeforeEach
    void setUp() {

        // Create real use case implementations with real repository
        generateQrCodePaymentUseCase = new GenerateQrCodePaymentUseCaseImpl(
                paymentPort,
                loggerPort,
                paymentRepositoryPort,
                publishEventPaymentStatusPort
        );

        rollbackPaymentUseCase = new RollbackPaymentUseCaseImpl(paymentRepositoryPort);

        paymentEventHandler = new PaymentEventHandler(
                loggerPort,
                generateQrCodePaymentUseCase,
                rollbackPaymentUseCase,
                publishEventPaymentStatusPort
        );
    }

    @Nested
    @DisplayName("GenerateQrCodePaymentUseCase Integration Tests")
    class GenerateQrCodeUseCaseIntegrationTests {

        @Test
        @DisplayName("Should create new payment and save to database when order has no existing payment")
        void shouldCreateNewPaymentAndSaveToDatabase() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal amount = BigDecimal.valueOf(150.00);
            final Order order = buildOrder(orderId, amount);

            final GeneratedQrCodeResponse qrCodeResponse = new GeneratedQrCodeResponse(
                    orderId,
                    "payment-123",
                    amount,
                    "BRL",
                    "00020126580014br.gov.bcb.pix...",
                    OffsetDateTime.now().plusMinutes(30)
            );

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);

            // When
            generateQrCodePaymentUseCase.execute(order, transactionId);

            // Then - verify payment was saved to database
            Optional<Payment> savedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(savedPayment).isPresent();
            assertThat(savedPayment.get().getOrderId()).isEqualTo(orderId);
            assertThat(savedPayment.get().getTransactionId()).isEqualTo(transactionId);
            assertThat(savedPayment.get().getAmount()).isEqualByComparingTo(amount);
            assertThat(savedPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(savedPayment.get().getPaymentMethod()).isEqualTo(PaymentMethod.QR_CODE);
            assertThat(savedPayment.get().getQrCode()).isNotNull();
        }

        @Test
        @DisplayName("Should not create duplicate payment when order already has payment")
        void shouldNotCreateDuplicatePaymentWhenOrderAlreadyHasPayment() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal amount = BigDecimal.valueOf(100.00);
            final Order order = buildOrder(orderId, amount);

            final GeneratedQrCodeResponse qrCodeResponse = new GeneratedQrCodeResponse(
                    orderId,
                    "payment-456",
                    amount,
                    "BRL",
                    "00020126580014br.gov.bcb.pix...",
                    OffsetDateTime.now().plusMinutes(30)
            );

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);

            // First call - creates payment
            generateQrCodePaymentUseCase.execute(order, transactionId);

            // Second call - should not duplicate
            final UUID newTransactionId = UUID.randomUUID();
            generateQrCodePaymentUseCase.execute(order, newTransactionId);

            // Then - verify only one payment exists
            long count = paymentEntityRepository.count();
            assertThat(count).isEqualTo(1);

            // And verify paymentPort was only called once (for the first payment)
            verify(paymentPort, times(1)).createQrCode(any(Payment.class), any());
        }

        @Test
        @DisplayName("Should not save payment when MercadoPago returns null")
        void shouldNotSavePaymentWhenMercadoPagoReturnsNull() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(null);

            // When
            generateQrCodePaymentUseCase.execute(order, transactionId);

            // Then - verify no payment was saved
            Optional<Payment> savedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(savedPayment).isEmpty();
        }
    }

    @Nested
    @DisplayName("RollbackPaymentUseCase Integration Tests")
    class RollbackPaymentUseCaseIntegrationTests {

        @Test
        @DisplayName("Should delete payment from database when rollback is executed")
        void shouldDeletePaymentFromDatabaseWhenRollbackExecuted() {
            // Given - create a payment first
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Payment payment = new Payment.Builder()
                    .orderId(orderId)
                    .transactionId(transactionId)
                    .amount(BigDecimal.valueOf(100.00))
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();
            paymentRepositoryPort.save(payment);

            // Verify payment exists
            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isPresent();

            // When
            rollbackPaymentUseCase.execute(orderId);

            // Then - payment should be deleted
            Optional<Payment> deletedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(deletedPayment).isEmpty();
        }

        @Test
        @DisplayName("Should not throw exception when rolling back non-existent payment")
        void shouldNotThrowExceptionWhenRollingBackNonExistentPayment() {
            // Given
            final UUID nonExistentOrderId = UUID.randomUUID();

            // When & Then - should not throw
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    rollbackPaymentUseCase.execute(nonExistentOrderId)
            );
        }
    }

    @Nested
    @DisplayName("Full Payment Event Flow Integration Tests")
    class FullPaymentEventFlowIntegrationTests {

        @Test
        @DisplayName("Should process SUCCESS event and persist payment to database")
        void shouldProcessSuccessEventAndPersistPaymentToDatabase() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal amount = BigDecimal.valueOf(250.00);
            final Order order = buildOrder(orderId, amount);

            final com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder event =
                    com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder.builder()
                            .id(UUID.randomUUID())
                            .orderId(orderId)
                            .transactionId(transactionId)
                            .status("SUCCESS")
                            .payload(order)
                            .source("ORDER")
                            .createdAt(LocalDateTime.now())
                            .build();

            final GeneratedQrCodeResponse qrCodeResponse = new GeneratedQrCodeResponse(
                    orderId,
                    "payment-" + orderId,
                    amount,
                    "BRL",
                    "00020126580014br.gov.bcb.pix...",
                    OffsetDateTime.now().plusMinutes(30)
            );

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isTrue();

            // Verify payment persisted to database
            Optional<Payment> savedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(savedPayment).isPresent();
            assertThat(savedPayment.get().getTransactionId()).isEqualTo(transactionId);
            assertThat(savedPayment.get().getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should process FAIL event and delete payment from database")
        void shouldProcessFailEventAndDeletePaymentFromDatabase() {
            // Given - create a payment first
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Payment existingPayment = new Payment.Builder()
                    .orderId(orderId)
                    .transactionId(transactionId)
                    .amount(BigDecimal.valueOf(100.00))
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();
            paymentRepositoryPort.save(existingPayment);

            // Verify payment exists
            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isPresent();

            final com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder event =
                    com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder.builder()
                            .id(UUID.randomUUID())
                            .orderId(orderId)
                            .transactionId(transactionId)
                            .status("FAIL")
                            .source("INVENTORY")
                            .createdAt(LocalDateTime.now())
                            .build();

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isTrue();

            // Verify payment deleted from database
            Optional<Payment> deletedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(deletedPayment).isEmpty();
        }

        @Test
        @DisplayName("Should publish rollback event when MercadoPago integration fails")
        void shouldPublishRollbackEventWhenMercadoPagoIntegrationFails() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));

            final com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder event =
                    com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder.builder()
                            .id(UUID.randomUUID())
                            .orderId(orderId)
                            .transactionId(transactionId)
                            .status("SUCCESS")
                            .payload(order)
                            .source("ORDER")
                            .createdAt(LocalDateTime.now())
                            .build();

            when(paymentPort.createQrCode(any(Payment.class), any()))
                    .thenThrow(new RuntimeException("MercadoPago API is unavailable"));

            // When
            boolean result = paymentEventHandler.handle(event);

            // Then
            assertThat(result).isFalse();

            // Verify rollback event was published
            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());

            EventPayment rollbackEvent = eventCaptor.getValue();
            assertThat(rollbackEvent.getTransactionId()).isEqualTo(transactionId);
            assertThat(rollbackEvent.getOrderId()).isEqualTo(orderId);
            assertThat(rollbackEvent.getStatus()).isEqualTo("ROLLBACK_PENDING");
        }

        @Test
        @DisplayName("Should not persist payment when rollback is published due to exception")
        void shouldNotPersistPaymentWhenRollbackPublishedDueToException() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));

            final com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder event =
                    com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder.builder()
                            .id(UUID.randomUUID())
                            .orderId(orderId)
                            .transactionId(transactionId)
                            .status("SUCCESS")
                            .payload(order)
                            .source("ORDER")
                            .createdAt(LocalDateTime.now())
                            .build();

            when(paymentPort.createQrCode(any(Payment.class), any()))
                    .thenThrow(new RuntimeException("API Error"));

            // When
            paymentEventHandler.handle(event);

            // Then - no payment should be saved
            Optional<Payment> savedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(savedPayment).isEmpty();
        }
    }

    @Nested
    @DisplayName("Repository Adapter Integration Tests")
    class RepositoryAdapterIntegrationTests {

        @Test
        @DisplayName("Should save and retrieve payment with all fields correctly")
        void shouldSaveAndRetrievePaymentWithAllFieldsCorrectly() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal amount = BigDecimal.valueOf(199.99);
            final Payment payment = new Payment.Builder()
                    .orderId(orderId)
                    .transactionId(transactionId)
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();

            // When
            paymentRepositoryPort.save(payment);

            // Then
            Optional<Payment> retrieved = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(retrieved).isPresent();
            Payment savedPayment = retrieved.get();
            assertThat(savedPayment.getId()).isNotNull();
            assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
            assertThat(savedPayment.getTransactionId()).isEqualTo(transactionId);
            assertThat(savedPayment.getAmount()).isEqualByComparingTo(amount);
            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.QR_CODE);
            assertThat(savedPayment.getPaymentDateTime()).isNotNull();
        }

        @Test
        @DisplayName("Should delete payment by orderId")
        void shouldDeletePaymentByOrderId() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final Payment payment = new Payment.Builder()
                    .orderId(orderId)
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(50.00))
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();
            paymentRepositoryPort.save(payment);

            // Verify exists
            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isPresent();

            // When
            paymentRepositoryPort.delete(orderId);

            // Then
            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when finding non-existent payment")
        void shouldReturnEmptyWhenFindingNonExistentPayment() {
            // Given
            final UUID nonExistentOrderId = UUID.randomUUID();

            // When
            Optional<Payment> result = paymentRepositoryPort.findByOrderId(nonExistentOrderId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple payments for different orders")
        void shouldHandleMultiplePaymentsForDifferentOrders() {
            // Given
            final UUID orderId1 = UUID.randomUUID();
            final UUID orderId2 = UUID.randomUUID();
            final UUID orderId3 = UUID.randomUUID();

            final Payment payment1 = new Payment.Builder()
                    .orderId(orderId1)
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(100.00))
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();

            final Payment payment2 = new Payment.Builder()
                    .orderId(orderId2)
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(200.00))
                    .status(PaymentStatus.PROCESSED)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();

            final Payment payment3 = new Payment.Builder()
                    .orderId(orderId3)
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(300.00))
                    .status(PaymentStatus.EXPIRED)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();

            // When
            paymentRepositoryPort.save(payment1);
            paymentRepositoryPort.save(payment2);
            paymentRepositoryPort.save(payment3);

            // Then
            assertThat(paymentEntityRepository.count()).isEqualTo(3);
            assertThat(paymentRepositoryPort.findByOrderId(orderId1).get().getAmount())
                    .isEqualByComparingTo(BigDecimal.valueOf(100.00));
            assertThat(paymentRepositoryPort.findByOrderId(orderId2).get().getStatus())
                    .isEqualTo(PaymentStatus.PROCESSED);
            assertThat(paymentRepositoryPort.findByOrderId(orderId3).get().getStatus())
                    .isEqualTo(PaymentStatus.EXPIRED);
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
}

