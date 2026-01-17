package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderItemResponse(
        UUID id,
        ProductsResponse product,
        int quantity,
        BigDecimal priceAtPurchase
) {
}

