package com.postech.ms_payment_fastfood.infrastructure.http.mercadopago.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderMercadoPagoRequestDto(
        String type,
        String total_amount,
        String description,
        String external_reference,
        String expiration_time,
        ConfigDto config,
        TransactionsDto transactions,
        List<ItemDto> items
) {


}
