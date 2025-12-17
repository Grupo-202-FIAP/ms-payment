package com.postech.payment.fastfood.infrastructure.controller.dto.request;

import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
        UUID id,
        String identifier,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime orderDateTime,
        Payment payment,
        List<OrderItem> itens,
        LocalDateTime updatedAt
) {
}
