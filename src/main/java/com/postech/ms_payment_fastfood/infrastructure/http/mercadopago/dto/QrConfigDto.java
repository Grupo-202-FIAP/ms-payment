package com.postech.ms_payment_fastfood.infrastructure.http.mercadopago.dto;

import lombok.Builder;

@Builder
public record QrConfigDto(
        String external_pos_id,
        String mode
) {
}
