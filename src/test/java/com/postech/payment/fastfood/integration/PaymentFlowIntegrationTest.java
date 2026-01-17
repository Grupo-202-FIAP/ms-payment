package com.postech.payment.fastfood.integration;

import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.services.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.domain.services.RollbackPaymentUseCaseImpl;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.ConsumerPaymentQueue;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.PaymentEventHandler;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.config.SnsMessagingConfig;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import com.postech.payment.fastfood.integration.config.AbstractIntegrationTest;
import com.postech.payment.fastfood.integration.utils.SqsTestSupport; // Importe sua classe utilitária
import com.postech.payment.fastfood.utils.JsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest // Removemos properties hardcoded, o AbstractIntegrationTest cuida disso
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Payment Flow Integration Tests (UseCase + Repository)")
// Importamos a configuração interna para garantir que o SqsClient seja criado
@Import(PaymentFlowIntegrationTest.LocalSqsConfig.class)
class PaymentFlowIntegrationTest extends AbstractIntegrationTest {

    // --- CONFIGURAÇÃO INTERNA PARA CORRIGIR O ERRO DO SQS ---
    @TestConfiguration
    static class LocalSqsConfig {

        @Bean
        @Primary
        public SqsClient sqsClient(
                @Value("${spring.cloud.aws.sqs.endpoint}") String sqsEndpoint,
                @Value("${spring.cloud.aws.region.static}") String region,
                @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
                @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey) {

            return SqsClient.builder()
                    .endpointOverride(URI.create(sqsEndpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .build();
        }

        @Bean
        public SqsTestSupport sqsTestSupport(SqsAsyncClient sqsClient) {
            return new SqsTestSupport(sqsClient);
        }
    }

    @MockitoBean
    private SnsClient snsClient;

    @MockitoBean
    private SnsMessagingConfig snsMessagingConfig;

    @MockitoBean
    private MercadoPagoClient mercadoPagoClient;

    @MockitoBean
    private PaymentPort paymentPort;

    @MockitoBean
    private PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    @MockitoBean
    private ConsumerPaymentQueue consumerPaymentQueue;

    // Implementações reais
    @Autowired
    private PaymentRepositoryPort paymentRepositoryPort;

    @Autowired
    private IPaymentEntityRepository paymentEntityRepository;

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private com.postech.payment.fastfood.application.ports.output.LoggerPort loggerPort;

    // Use cases para teste
    private GenerateQrCodePaymentUseCaseImpl generateQrCodePaymentUseCase;
    private RollbackPaymentUseCaseImpl rollbackPaymentUseCase;
    private PaymentEventHandler paymentEventHandler;

    @BeforeEach
    void setUp() {
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
        @DisplayName("Should create new payment and save to database")
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

            // Then
            Optional<Payment> savedPayment = paymentRepositoryPort.findByOrderId(orderId);
            assertThat(savedPayment).isPresent();
            assertThat(savedPayment.get().getOrderId()).isEqualTo(orderId);
            assertThat(savedPayment.get().getTransactionId()).isEqualTo(transactionId);
            assertThat(savedPayment.get().getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("Should not create duplicate payment")
        void shouldNotCreateDuplicatePaymentWhenOrderAlreadyHasPayment() {
            // Given
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final BigDecimal amount = BigDecimal.valueOf(100.00);
            final Order order = buildOrder(orderId, amount);

            final GeneratedQrCodeResponse qrCodeResponse = new GeneratedQrCodeResponse(
                    orderId, "pay-456", amount, "BRL", "pix...", OffsetDateTime.now().plusMinutes(30)
            );

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);

            // First call
            generateQrCodePaymentUseCase.execute(order, transactionId);

            // Second call
            generateQrCodePaymentUseCase.execute(order, UUID.randomUUID());

            // Then
            assertThat(paymentEntityRepository.count()).isEqualTo(1);
            verify(paymentPort, times(1)).createQrCode(any(Payment.class), any());
        }

        @Test
        @DisplayName("Should not save payment when MercadoPago returns null")
        void shouldNotSavePaymentWhenMercadoPagoReturnsNull() {
            final UUID orderId = UUID.randomUUID();
            final UUID transactionId = UUID.randomUUID();
            final Order order = buildOrder(orderId, BigDecimal.valueOf(100.00));

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(null);

            generateQrCodePaymentUseCase.execute(order, transactionId);

            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("RollbackPaymentUseCase Integration Tests")
    class RollbackPaymentUseCaseIntegrationTests {

        @Test
        @DisplayName("Should delete payment on rollback")
        void shouldDeletePaymentFromDatabaseWhenRollbackExecuted() {
            final UUID orderId = UUID.randomUUID();
            final Payment payment = new Payment.Builder()
                    .orderId(orderId)
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.TEN)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();
            paymentRepositoryPort.save(payment);

            rollbackPaymentUseCase.execute(orderId);

            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Full Payment Event Flow")
    class FullPaymentEventFlowIntegrationTests {

        @Test
        @DisplayName("Should process SUCCESS event")
        void shouldProcessSuccessEventAndPersistPaymentToDatabase() {
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
                    orderId, "pay-success", amount, "BRL", "pix...", OffsetDateTime.now().plusMinutes(30)
            );

            when(paymentPort.createQrCode(any(Payment.class), any())).thenReturn(qrCodeResponse);

            boolean result = paymentEventHandler.handle(event);

            assertThat(result).isTrue();
            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isPresent();
        }

        @Test
        @DisplayName("Should process FAIL event")
        void shouldProcessFailEventAndDeletePaymentFromDatabase() {
            final UUID orderId = UUID.randomUUID();
            final Payment existingPayment = new Payment.Builder()
                    .orderId(orderId)
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.TEN)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.QR_CODE)
                    .build();
            paymentRepositoryPort.save(existingPayment);

            final com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder event =
                    com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder.builder()
                            .id(UUID.randomUUID())
                            .orderId(orderId)
                            .transactionId(existingPayment.getTransactionId())
                            .status("FAIL")
                            .source("INVENTORY")
                            .createdAt(LocalDateTime.now())
                            .build();

            boolean result = paymentEventHandler.handle(event);

            assertThat(result).isTrue();
            assertThat(paymentRepositoryPort.findByOrderId(orderId)).isEmpty();
        }

        @Test
        @DisplayName("Should publish rollback on API failure")
        void shouldPublishRollbackEventWhenMercadoPagoIntegrationFails() {
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
                    .thenThrow(new PaymentIntegrationException("API Error"));

            boolean result = paymentEventHandler.handle(event);

            assertThat(result).isFalse();

            ArgumentCaptor<EventPayment> eventCaptor = ArgumentCaptor.forClass(EventPayment.class);
            verify(publishEventPaymentStatusPort).publish(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getStatus()).isEqualTo("ROLLBACK_PENDING");
        }
    }

    private Order buildOrder(UUID orderId, BigDecimal totalPrice) {
        return new Order.Builder()
                .id(orderId)
                .totalPrice(totalPrice)
                .identifier("ORDER-" + orderId.toString().substring(0, 8))
                .build();
    }
}