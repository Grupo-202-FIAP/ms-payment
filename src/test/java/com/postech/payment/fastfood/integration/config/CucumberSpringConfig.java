package com.postech.payment.fastfood.integration.config;

import com.postech.payment.fastfood.PaymentFastfoodApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber Spring configuration for integration tests.
 * This class bridges Cucumber with Spring Boot Test context.
 * Uses real Feign client pointing to WireMock server (configured in AbstractIntegrationTest).
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = {
                PaymentFastfoodApplication.class,
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Import({SqsTestConfig.class})
public class CucumberSpringConfig extends AbstractIntegrationTest {
}
