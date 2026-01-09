package com.postech.payment.fastfood.domain.model;


import com.postech.payment.fastfood.domain.enums.Category;
import java.math.BigDecimal;


public class Product {
    private Long id;
    private String name;
    private Category category;
    private BigDecimal unitPrice;

    public Product() {
    }

    public Product(Long id, String name, Category category, BigDecimal unitPrice) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
    }

    public Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unitPrice = builder.unitPrice;
        this.category = builder.category;
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

    public static class Builder {
        private Long id;
        private String name;
        private BigDecimal unitPrice;
        private Category category;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
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

        public Product build() {
            return new Product(this);
        }
    }
}
