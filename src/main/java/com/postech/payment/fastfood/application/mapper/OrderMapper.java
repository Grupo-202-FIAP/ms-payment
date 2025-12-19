package com.postech.payment.fastfood.application.mapper;


import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.OrderRequest;
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

    public static Order toDomain(OrderRequest orderRequest) {
        final Order order = new Order.Builder()
                .id(orderRequest.id())
                .identifier(orderRequest.identifier())
                .itens(orderRequest.itens())
                .totalPrice(orderRequest.totalPrice())
                .payment(orderRequest.payment())
                .build();
        return order;
    }

    public static OrderMPRequestDto toMPVOrderRequest(Order order, String posId, String mode) {
        final var items = order.getItens().stream().map(item ->
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
                .amount(order.getTotalPrice().toString())
                .build();
        final List<PaymentDto> paymentDtos = List.of(paymentDto);

        final TransactionsDto transactionsDto = TransactionsDto.builder()
                .payments(paymentDtos)
                .build();

        return OrderMPRequestDto.builder()
                .type("qr")
                .total_amount(order.getTotalPrice().toString())
                .description("Pedido FastFood - " + order.getIdentifier())
                .external_reference(order.getIdentifier())
                .expiration_time("PT2H") // 2 horas expiração
                .config(config)
                .transactions(transactionsDto)
                .items(items)
                .build();
    }

}
