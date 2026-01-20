package com.postech.payment.fastfood.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private UUID id;
    private Product product;
    private Integer quantity;
    private BigDecimal priceAtPurchase;

    public OrderItem() {
    }

    public OrderItem(UUID id, Product product, Integer quantity, BigDecimal priceAtPurchase) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    public OrderItem(Builder builder) {
        this.id = builder.id;
        this.product = builder.product;
        this.quantity = builder.quantity;
        this.priceAtPurchase = builder.priceAtPurchase;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public static class Builder {

        private UUID id;
        private Product product;
        private Integer quantity;
        private BigDecimal priceAtPurchase;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder priceAtPurchase(BigDecimal priceAtPurchase) {
            this.priceAtPurchase = priceAtPurchase;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }


    }
}

