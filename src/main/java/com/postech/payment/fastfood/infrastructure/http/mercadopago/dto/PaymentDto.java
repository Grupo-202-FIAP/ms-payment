package com.postech.payment.fastfood.infrastructure.http.mercadopago.dto;

import lombok.Builder;

@Builder
public record PaymentDto(
        String amount
) {
}
