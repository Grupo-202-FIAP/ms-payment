package com.postech.payment.fastfood.infrastructure.controller.dto.request;

import com.postech.payment.fastfood.domain.enums.Category;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Image URL is required")
        @Size(max = 255, message = "Image URL must not exceed 255 characters")
        @Pattern(regexp = "^(http|https)://.*$", message = "Image URL must be valid")
        String urlImage,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than zero")
        BigDecimal unitPrice,

        @NotNull(message = "Category is required")
        @Enumerated
        Category category,

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "employee id is required")
        UUID employee_id
) {
}
