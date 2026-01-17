package com.postech.payment.fastfood.integration.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

/**
 * Abstract base class for integration tests that require LocalStack (SQS/SNS) and WireMock.
 * Provides shared container instances and dynamic property configuration.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final DockerImageName LOCALSTACK_IMAGE =
            DockerImageName.parse("localstack/localstack:3.4.0");

    protected static LocalStackContainer localStackContainer;
    protected static WireMockServer wireMockServer;

    protected static final String PAYMENT_QUEUE = "payment-queue";
    protected static final String ORDER_CALLBACK_QUEUE = "order-callback-queue";
    protected static final String PAYMENT_CALLBACK_TOPIC = "payment-callback";

    static {
        // 1. Inicia LocalStack
        localStackContainer = new LocalStackContainer(LOCALSTACK_IMAGE)
                .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.SNS)
                .withReuse(true);
        localStackContainer.start();

        // 2. Cria recursos AWS
        try {
            localStackContainer.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", PAYMENT_QUEUE);
            localStackContainer.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", ORDER_CALLBACK_QUEUE);
            localStackContainer.execInContainer("awslocal", "sns", "create-topic", "--name", PAYMENT_CALLBACK_TOPIC);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Falha ao criar filas no LocalStack", e);
        }

        // 3. Inicia WireMock
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Garante que o WireMock está vivo antes de ler a porta
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
            wireMockServer.start();
        }

        String sqsEndpoint = localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString();
        String snsEndpoint = localStackContainer.getEndpointOverride(LocalStackContainer.Service.SNS).toString();

        registry.add("spring.cloud.aws.sqs.endpoint", () -> sqsEndpoint);
        registry.add("spring.cloud.aws.sns.endpoint", () -> snsEndpoint);
        registry.add("spring.cloud.aws.region.static", localStackContainer::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
        registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");

        registry.add("spring.cloud.aws.sqs.queues.process-payment-queue",
                () -> sqsEndpoint + "/000000000000/" + PAYMENT_QUEUE);
        registry.add("spring.cloud.aws.sns.paymentCallbackTopic",
                () -> "arn:aws:sns:" + localStackContainer.getRegion() + ":000000000000:" + PAYMENT_CALLBACK_TOPIC);

        registry.add("mercadoPago.baseUrl", () -> "http://localhost:" + wireMockServer.port());
        registry.add("mercadoPago.publicKey", () -> "TEST-public-key");
        registry.add("mercadoPago.accessToken", () -> "TEST-access-token");
        registry.add("mercadoPago.clientId", () -> "TEST-client-id");
        registry.add("mercadoPago.clientSecret", () -> "TEST-client-secret");
        registry.add("mercadoPago.externalPosID", () -> "TEST-pos-id");
        registry.add("mercadoPago.webhook.secretKey", () -> "TEST-webhook-secret");

        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeAll
    static void beforeAll() {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
    }

    @AfterAll
    static void afterAll() {
        // --- CORREÇÃO IMPORTANTE ---
        // Usar resetAll() em vez de stop() para permitir múltiplos testes
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }

    public static WireMockServer getWireMockServer() {
        return wireMockServer;
    }
}