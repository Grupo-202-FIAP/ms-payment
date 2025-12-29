package com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response;


import com.postech.payment.fastfood.domain.enums.Category;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProductsResponse(
        Long id,
        String name,
        @Enumerated(EnumType.STRING)
        Category category,
        BigDecimal unitPrice,
        String urlImage,
        String description,
        UUID employeeCreatorId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
