package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request;

import java.math.BigDecimal;

public record OrderItemRequest(
        Long productId,
        Integer quantity,
        BigDecimal priceAtPurchase
) {
}
