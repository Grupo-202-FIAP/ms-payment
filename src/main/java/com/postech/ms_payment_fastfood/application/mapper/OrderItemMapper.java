package com.postech.ms_payment_fastfood.application.mapper;


import com.postech.ms_payment_fastfood.domain.OrderItem;
import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public static OrderItemEntity toEntity(OrderItem orderItem) {
        return OrderItemEntity.builder()
                .id(orderItem.getId())
                .product(ProductMapper.toEntity(orderItem.getProduct()))
                .priceAtPurchase(orderItem.getPriceAtPurchase())
                .quantity(orderItem.getQuantity())
                .build();
    }

    public static OrderItem toDomain(OrderItemEntity orderItem) {
        return new OrderItem.Builder()
                .id(orderItem.getId())
                .product(ProductMapper.toDomain(orderItem.getProduct()))
                .priceAtPurchase(orderItem.getPriceAtPurchase())
                .quantity(orderItem.getQuantity())
                .build();
    }
//
//    public static OrderItem toDomain(OrderItemRequest orderItemRequest) {
//        return new OrderItem.Builder()
//                .product(new Product.Builder()
//                        .id(orderItemRequest.productId())
//                        .build())
//                .quantity(orderItemRequest.quantity())
//                .priceAtPurchase(orderItemRequest.priceAtPurchase())
//                .build();
//    }
//
//    public static OrderItemResponse toResponse(OrderItem orderItem) {
//        return OrderItemResponse.builder()
//                .id(orderItem.getId())
//                .product(ProductMapper.toResponse(orderItem.getProduct()))
//                .quantity(orderItem.getQuantity())
//                .priceAtPurchase(orderItem.getPriceAtPurchase())
//                .build();
//    }

}
