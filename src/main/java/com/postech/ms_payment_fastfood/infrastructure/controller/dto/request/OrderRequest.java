package com.postech.ms_payment_fastfood.infrastructure.controller.dto.request;

import com.postech.ms_payment_fastfood.domain.OrderItem;
import com.postech.ms_payment_fastfood.domain.Payment;
import com.postech.ms_payment_fastfood.domain.enums.OrderStatus;

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
