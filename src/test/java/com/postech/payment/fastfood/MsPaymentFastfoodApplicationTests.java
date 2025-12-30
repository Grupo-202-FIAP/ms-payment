package com.postech.payment.fastfood;

import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sns.SnsClient;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.profiles.active=${SPRING_PROFILES_ACTIVE:test}",
        "server.port=${SERVER_PORT:8084}",
        "mercadoPago.publicKey=${PUBLIC_KEY:none}",
        "mercadoPago.accessToken=${ACCESS_TOKEN:none}",
        "mercadoPago.clientId=${CLIENT_ID:none}",
        "mercadoPago.clientSecret=${CLIENT_SECRET:none}",
        "mercadoPago.externalPosID=${EXTERNAL_POS_ID:none}",
        "mercadoPago.webhook.secretKey=${WEBHOOK_SECRET:none}",
        "cloud.aws.region.static=${AWS_REGION:us-east-1}",
        "cloud.aws.credentials.access-key=${AWS_ACCESS_KEY:test}",
        "cloud.aws.credentials.secret-key=${AWS_SECRET_KEY:test}",
        "spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:h2:mem:testdb}",
        "spring.datasource.username=${SPRING_DATASOURCE_USERNAME:sa}",
        "spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}",
        "cloud.aws.sqs.queues.process-payment-queue=${PAYMENT_QUEUE_NAME:test-queue}"
        "spring.cloud.aws.sqs.endpoint=${SPRING_CLOUD_AWS_SQS_ENDPOINT:http://localhost:4566}"
})
class MsPaymentFastfoodApplicationTests {
    @MockitoBean
    private SnsClient snsClient;

    @MockitoBean
    private MercadoPagoClient mercadoPagoClient;

    @Test
    void contextLoads() {
    }
}
