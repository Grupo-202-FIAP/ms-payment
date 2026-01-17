package com.postech.payment.fastfood.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client configuration for all environments.
 * In tests, the Feign client will point to WireMock server configured in AbstractIntegrationTest.
 * In production, it will point to the real Mercado Pago API.
 */
@Configuration
@EnableFeignClients(basePackages = "com.postech.payment.fastfood.infrastructure.http.mercadopago")
public class FeignClientConfig {
}
