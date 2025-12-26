package com.postech.payment.fastfood.infrastructure.controller.dto.request;

import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentQrCodeRequest(
        @NotNull
        UUID orderId,
        @NotEmpty
        String orderIdentifier,
        PaymentMethod paymentMethod,
        @Positive
        BigDecimal totalPrice,
        @NotEmpty
        List<OrderItem> items

) {
}
