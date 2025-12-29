package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QrProperty(
        @JsonProperty("external_pos_id")
        String externalPosId,
        @JsonProperty("mode")
        String mode) {
}
