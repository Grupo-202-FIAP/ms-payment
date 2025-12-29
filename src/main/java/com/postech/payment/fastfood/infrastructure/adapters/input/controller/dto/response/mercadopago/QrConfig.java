package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QrConfig(
        @JsonProperty("qr")
        QrProperty qrProperty) {
}
