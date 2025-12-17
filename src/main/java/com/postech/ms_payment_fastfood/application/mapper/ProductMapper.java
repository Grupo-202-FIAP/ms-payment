package com.postech.ms_payment_fastfood.application.mapper;


import com.postech.ms_payment_fastfood.domain.Product;
import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public static Product toDomain(ProductEntity product) {
        return new Product.Builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .unitPrice(product.getUnitPrice())
                .urlImage(product.getUrlImage())
                .updatedAt(product.getUpdatedAt())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public static ProductEntity toEntity(Product product) {
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .unitPrice(product.getUnitPrice())
                .urlImage(product.getUrlImage())
                .updatedAt(product.getUpdatedAt())
                .createdAt(product.getCreatedAt())
                .build();

    }
//
//    public static Product toDomain(ProductRequest product) {
//
//        final Employee employee = new Employee.Builder()
//                .id(product.employee_id())
//                .build();
//
//        return new Product.Builder()
//                .name(product.name())
//                .description(product.description())
//                .unitPrice(product.unitPrice())
//                .urlImage(product.urlImage())
//                .createdByEmployee(employee)
//                .build();
//    }
//
//    public static ProductsResponse toResponse(Product product) {
//        return ProductsResponse.builder()
//                .id(product.getId())
//                .name(product.getName())
//                .category(product.getCategory())
//                .unitPrice(product.getUnitPrice())
//                .urlImage(product.getUrlImage())
//                .description(product.getDescription())
//                .employeeCreatorId(product.getCreatedByEmployee().getId())
//                .createdAt(product.getCreatedAt())
//                .updatedAt(product.getUpdatedAt())
//                .build();
//    }
}
