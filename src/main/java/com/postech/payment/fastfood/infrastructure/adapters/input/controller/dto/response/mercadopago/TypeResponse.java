package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TypeResponse(
        @JsonProperty("qr_data")
        String qrData) {
}
