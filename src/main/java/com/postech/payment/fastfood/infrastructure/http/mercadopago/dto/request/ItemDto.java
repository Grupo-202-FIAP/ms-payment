package com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request;

import lombok.Builder;
import java.util.List;

@Builder
public record ItemDto(
        String title,
        String unit_price,
        int quantity,
        String unit_measure,
        String external_code,
        List<CategoryIdDto> external_categories
) {

}
