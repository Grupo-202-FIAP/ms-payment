package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request;


import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import jakarta.persistence.Enumerated;

public record PaymentRequest(
        @Enumerated
        PaymentMethod paymentMethod
) {
}
