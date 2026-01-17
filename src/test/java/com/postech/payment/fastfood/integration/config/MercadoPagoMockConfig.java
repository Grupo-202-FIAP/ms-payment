package com.postech.payment.fastfood.integration.config;

import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.QrConfig;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.QrProperty;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.TypeResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock configuration for MercadoPago client in integration tests.
 * Since Feign clients are disabled in test profile (@Profile("!test") in FeignClientConfig),
 * this mock bean will be the only MercadoPagoClient available in the test context.
 */
@TestConfiguration
public class MercadoPagoMockConfig {


    @Bean
    @Primary
    public MercadoPagoClient mercadoPagoClientMock() {
        MercadoPagoClient mock = mock(MercadoPagoClient.class);

        TypeResponse typeResponse = new TypeResponse("00020126580014br.gov.bcb.pix0136mock-qr-data");
        QrProperty qrProperty = new QrProperty("SUC001", "dynamic");
        QrConfig qrConfig = new QrConfig(qrProperty);

        OrderResponse defaultResponse = new OrderResponse(
                "mock-order-id", "created", "Pedido FastFood - Mock Test",
                100.0, "BRL", OffsetDateTime.now(), qrConfig,
                Collections.emptyList(), typeResponse
        );

        when(mock.createOrder(anyString(), anyString(), any(OrderMPRequestDto.class)))
                .thenReturn(defaultResponse);

        return mock;
    }
}