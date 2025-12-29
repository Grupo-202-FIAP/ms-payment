package com.postech.payment.fastfood;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.flyway.enabled=false"
})
class MsPaymentFastfoodApplicationTests {

    @Test
    void contextLoads() {
    }

}
