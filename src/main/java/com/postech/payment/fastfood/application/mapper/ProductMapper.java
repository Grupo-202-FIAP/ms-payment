package com.postech.payment.fastfood.application.mapper;


import com.postech.payment.fastfood.domain.Product;
import com.postech.payment.fastfood.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public static Product toDomain(ProductEntity product) {
        return new Product.Builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .build();
    }

    public static ProductEntity toEntity(Product product) {
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .build();

    }

}
