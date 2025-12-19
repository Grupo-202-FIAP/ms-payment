package com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TypeResponse(
        @JsonProperty("qr_data")
        String qrData) {
}
