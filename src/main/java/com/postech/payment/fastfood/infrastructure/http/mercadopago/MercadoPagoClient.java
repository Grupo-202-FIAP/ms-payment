package com.postech.payment.fastfood.infrastructure.http.mercadopago;


import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMercadoPagoRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "mercadoPagoClient",
        url = "https://api.mercadopago.com/v1/orders"
)
public interface MercadoPagoClient {
    @PostMapping(consumes = "application/json")
    OrderResponse createOrder(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader("Authorization") String authorization,
            @RequestBody OrderMercadoPagoRequestDto requestBody
    );
}

