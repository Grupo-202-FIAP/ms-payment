package com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderItem(
        @JsonProperty("title")
        String title,
        @JsonProperty("unit_price")
        Double unitPrice,
        @JsonProperty("quantity")
        Integer quantity,
        @JsonProperty("external_code")
        String externalCode) {
}
