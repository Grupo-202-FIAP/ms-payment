package com.postech.payment.fastfood.infrastructure.controller.dto.request;


import com.postech.payment.fastfood.domain.enums.PaymentMethod;
//import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Enumerated;

//TODO
//remover quando estiver usando essa classe na controller
//@Schema(description = "Payment request DTO")
public record PaymentRequest(
        @Enumerated
        PaymentMethod paymentMethod
) {
}
