package com.postech.payment.fastfood.infrastructure.http.mercadopago;


import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "mercadoPagoClient",
        url = "${mercadoPago.api.url:https://api.mercadopago.com/v1/orders}"
)
@Component
public interface MercadoPagoClient {
    @PostMapping(consumes = "application/json")
    OrderResponse createOrder(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader("Authorization") String authorization,
            @RequestBody OrderMPRequestDto requestBody
    );
}

