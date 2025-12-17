package com.postech.ms_payment_fastfood.infrastructure.controller.dto.request;

import java.math.BigDecimal;

public record OrderItemRequest(
        Long productId,
        Integer quantity,
        BigDecimal priceAtPurchase
) {
}
