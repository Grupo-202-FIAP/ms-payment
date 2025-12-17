package com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request;

import lombok.Builder;
import java.util.List;

@Builder
public record TransactionsDto(
        List<PaymentDto> payments
) {
}




