package com.postech.payment.fastfood.application.mapper;


import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.infrastructure.persistence.entity.OrderItemEntity;
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


}
