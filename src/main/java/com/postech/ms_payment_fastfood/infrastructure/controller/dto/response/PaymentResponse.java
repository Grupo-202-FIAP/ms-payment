package com.postech.ms_payment_fastfood.infrastructure.controller.dto.response;


import com.postech.ms_payment_fastfood.domain.enums.PaymentMethod;
import com.postech.ms_payment_fastfood.domain.enums.PaymentStatus;
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
