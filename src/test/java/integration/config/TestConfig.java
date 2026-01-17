package integration.config;

import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer.ConsumerPaymentQueue;
import com.postech.payment.fastfood.infrastructure.config.SnsMessagingConfig;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sns.SnsClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public SnsClient snsClientMock() {
        return mock(SnsClient.class);
    }

    @Bean
    @Primary
    public SnsMessagingConfig snsMessagingConfigMock() {
        return mock(SnsMessagingConfig.class);
    }

    @Bean
    @Primary
    public MercadoPagoClient mercadoPagoClientMock() {
        return mock(MercadoPagoClient.class);
    }

    @Bean
    @Primary
    public PaymentPort paymentPortMock() {
        return mock(PaymentPort.class);
    }

    @Bean
    @Primary
    public PublishEventPaymentStatusPort publishEventPaymentStatusPortMock() {
        return mock(PublishEventPaymentStatusPort.class);
    }

    @Bean
    @Primary
    public ConsumerPaymentQueue consumerPaymentQueueMock() {
        return mock(ConsumerPaymentQueue.class);
    }
}

