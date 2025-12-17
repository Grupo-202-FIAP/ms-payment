package com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        @JsonProperty("id")
        String id,
        @JsonProperty("status")
        String status,
        @JsonProperty("description")
        String description,
        @JsonProperty("total_amount")
        Double totalAmount,
        @JsonProperty("currency")
        String currency,
        @JsonProperty("created_date")
        OffsetDateTime createdDate,
        @JsonProperty("config")
        QrConfig config,
        List<OrderItem> items,
        @JsonProperty("type_response")
        TypeResponse typeResponse) {
}
