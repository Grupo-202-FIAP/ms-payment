package com.postech.payment.fastfood.application.gateways;


import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMercadoPagoRequestDto;

public interface MercadoPagoPort {
    OrderResponse createOrder(String idempotencyKey, String accessToken, OrderMercadoPagoRequestDto requestBody, String orderId);
}
