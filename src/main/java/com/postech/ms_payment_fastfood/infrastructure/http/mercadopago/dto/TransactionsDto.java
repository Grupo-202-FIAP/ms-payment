package com.postech.ms_payment_fastfood.infrastructure.http.mercadopago.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TransactionsDto(
        List<PaymentDto> payments
) {
}




