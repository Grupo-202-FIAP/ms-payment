package com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request;

import lombok.Builder;

@Builder
public record ConfigDto(
        QrConfigDto qr
) {
}



