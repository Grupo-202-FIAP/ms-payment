package com.postech.payment.fastfood;

import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.ConsumerPaymentQueue;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.producer.ProducerEventPaymentStatusAdapter;
import com.postech.payment.fastfood.infrastructure.config.SnsMessagingConfig;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.integration.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sns.SnsClient;

@ActiveProfiles("test")
@SpringBootTest
class MsPaymentFastfoodApplicationTests extends AbstractIntegrationTest {
    @MockitoBean
    private SnsClient snsClient;
    @MockitoBean
    private SnsMessagingConfig snsMessagingConfig;
    @MockitoBean
    private MercadoPagoClient mercadoPagoClient;

    @MockitoBean
    private ConsumerPaymentQueue consumerPaymentQueue;

    @MockitoBean
    private GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;

    @MockitoBean
    private ProducerEventPaymentStatusAdapter producerEventPaymentStatusAdapter;

    @Test
    void contextLoads() {
        // Agora o Docker vai subir automaticamente, configurar as portas e o contexto vai carregar com sucesso.
    }
}
