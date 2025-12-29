package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request;


import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import jakarta.persistence.Enumerated;

//TODO
//remover quando estiver usando essa classe na controller
//@Schema(description = "Payment request DTO")
public record PaymentRequest(
        @Enumerated
        PaymentMethod paymentMethod
) {
}
