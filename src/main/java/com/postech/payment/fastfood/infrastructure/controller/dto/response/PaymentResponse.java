package com.postech.payment.fastfood.infrastructure.controller.dto.response;


import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID id,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        LocalDateTime paymentDateTime
) {
}
