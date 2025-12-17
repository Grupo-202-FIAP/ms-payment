package com.postech.payment.fastfood.infrastructure.http.mercadopago.dto;

import lombok.Builder;

@Builder
public record QrConfigDto(
        String external_pos_id,
        String mode
) {
}
