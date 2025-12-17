package com.postech.ms_payment_fastfood.application.mapper;


import com.postech.ms_payment_fastfood.domain.Order;
import com.postech.ms_payment_fastfood.infrastructure.controller.dto.request.OrderRequest;
import com.postech.ms_payment_fastfood.infrastructure.http.mercadopago.dto.*;
import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.OrderEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public static Order toDomain(OrderEntity orderEntity) {
        final Order order = new Order.Builder()
                .id(orderEntity.getId())
                .identifier(orderEntity.getIdentifier())
                .itens(orderEntity.getItens()
                        .stream()
                        .map(OrderItemMapper::toDomain).toList())
                .totalPrice(orderEntity.getTotalPrice())
                .payment(PaymentMapper.toDomain(orderEntity.getPayment()))
                .updatedAt(orderEntity.getUpdatedAt())
                .build();
        if (order.getItens() != null) {
            order.getItens().forEach(item -> item.setOrder(order));
        }
        return order;
    }

    public static Order toDomain(OrderRequest orderRequest) {
        final Order order = new Order.Builder()
                .id(orderRequest.id())
                .identifier(orderRequest.identifier())
                .itens(orderRequest.itens())
                .totalPrice(orderRequest.totalPrice())
                .payment(orderRequest.payment())
                .updatedAt(orderRequest.updatedAt())
                .build();
        if (order.getItens() != null) {
            order.getItens().forEach(item -> item.setOrder(order));
        }
        return order;
    }


    public static OrderEntity toEntity(Order order) {
        final OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId())
                .identifier(order.getIdentifier())
                .payment(PaymentMapper.toEntity(order.getPayment()))
                .itens(order.getItens()
                        .stream()
                        .map(OrderItemMapper::toEntity)
                        .toList())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus())
                .orderDateTime(order.getOrderDateTime())
                .updatedAt(order.getUpdatedAt())
                .build();


        if (orderEntity.getItens() != null) {
            orderEntity.getItens().forEach(item -> item.setOrder(orderEntity));
        }

        return orderEntity;
    }

    //
//    public static OrderEntity toEntity(Order order, PaymentEntity paymentEntity) {
//        return OrderEntity.builder()
//                .id(order.getId())
//                .identifier(order.getIdentifier())
//                .itens(order.getItens()
//                        .stream()
//                        .map(OrderItemMapper::toEntity).toList())
//                .totalPrice(order.getTotalPrice())
//                .payment(paymentEntity)
//                .orderStatus(order.getStatus())
//                .orderDateTime(order.getOrderDateTime())
//                .updatedAt(order.getUpdatedAt())
//                .build();
//    }
//
//
    public static OrderMercadoPagoRequestDto toMercadoPagoV1OrderRequest(Order order, String posId, String mode) {

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

        return OrderMercadoPagoRequestDto.builder()
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
