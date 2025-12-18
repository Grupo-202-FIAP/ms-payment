package com.postech.payment.fastfood.infrastructure.controller.dto.request;

import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotNull
        UUID id,
        @NotEmpty
        String identifier,
        @Positive
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime orderDateTime,
        Payment payment,
        @NotEmpty
        List<OrderItem> itens,
        LocalDateTime updatedAt
) {
}
