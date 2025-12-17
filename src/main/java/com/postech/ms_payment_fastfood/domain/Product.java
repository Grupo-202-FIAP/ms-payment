package com.postech.ms_payment_fastfood.domain;


import com.postech.ms_payment_fastfood.domain.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Product {
    private Long id;
    private String name;
    private Category category;
    private BigDecimal unitPrice;
    private String urlImage;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product(
            Long id,
            String name,
            String urlImage,
            BigDecimal unitPrice,
            Category category,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.urlImage = urlImage;
        this.unitPrice = unitPrice;
        this.category = category;
        this.description = description;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.urlImage = builder.urlImage;
        this.unitPrice = builder.unitPrice;
        this.category = builder.category;
        this.description = builder.description;

        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Product() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String urlImage;
        private BigDecimal unitPrice;
        private Category category;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder urlImage(String urlImage) {
            this.urlImage = urlImage;
            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }


        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }


        public Product build() {
            return new Product(this);
        }
    }
}
