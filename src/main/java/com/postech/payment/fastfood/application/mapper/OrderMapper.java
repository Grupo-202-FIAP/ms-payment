package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.CategoryIdDto;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.ConfigDto;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.ItemDto;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.PaymentDto;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.QrConfigDto;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.TransactionsDto;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public static OrderMPRequestDto toMPVOrderRequest(Payment payment, List<OrderItem> orderItems, String posId, String mode) {
        final var items = orderItems.stream().map(item ->
                ItemDto.builder()
                        .title(item.getProduct().getName())
                        .unit_price(item.getProduct().getUnitPrice().toString())
                        .quantity(item.getQuantity())
                        .unit_measure("UN")
                        .external_code(item.getProduct().getId().toString())
                        .external_categories(
                                List.of(CategoryIdDto.builder().id(item.getProduct().getCategory().getCategory()).build())
                        )
                        .build()
        ).collect(Collectors.toList());

        final ConfigDto config = ConfigDto.builder()
                .qr(
                        QrConfigDto.builder()
                                .external_pos_id(posId)
                                .mode(mode)
                                .build())
                .build();
        final PaymentDto paymentDto = PaymentDto.builder()
                .amount(payment.getAmount().toString())
                .build();
        final List<PaymentDto> paymentDtos = List.of(paymentDto);

        final TransactionsDto transactionsDto = TransactionsDto.builder()
                .payments(paymentDtos)
                .build();

        return OrderMPRequestDto.builder()
                .type("qr")
                .total_amount(payment.getAmount().toString())
                .description("Pedido FastFood - " + payment.getOrderId())
                .external_reference(payment.getOrderId().toString())
                .expiration_time("PT10M") // 2 horas expiração PT2H
                .config(config)
                .transactions(transactionsDto)
                .items(items)
                .build();
    }

}
