package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response;


import com.postech.payment.fastfood.domain.enums.OrderStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID id,
        String identifier,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime orderDateTime,
        UUID customerId,
        UUID paymentId,
        List<OrderItemResponse> items
) {
}
