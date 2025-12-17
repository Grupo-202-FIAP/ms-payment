package com.postech.ms_payment_fastfood.application.gateways;


import com.postech.ms_payment_fastfood.infrastructure.http.mercadopago.dto.OrderMercadoPagoRequestDto;

public interface MercadoPagoPort {
    String createOrder(String idempotencyKey, String accessToken, OrderMercadoPagoRequestDto requestBody, String orderId);
}
