package com.postech.payment.fastfood;

import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sns.SnsClient;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.flyway.enabled=false"
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
