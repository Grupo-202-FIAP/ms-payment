package com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request;

import lombok.Builder;
import java.util.List;

@Builder
public record OrderMPRequestDto(
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
