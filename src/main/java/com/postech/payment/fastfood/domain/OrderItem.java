package com.postech.payment.fastfood.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private UUID id;
    private Order order;
    private Product product;
    private Integer quantity;
    private BigDecimal priceAtPurchase;

    public OrderItem(UUID id, Order order, Product product, Integer quantity, BigDecimal priceAtPurchase) {
        this.id = id;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    public OrderItem(Builder builder) {
        this.id = builder.id;
        this.order = builder.order;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public static class Builder {

        private UUID id;
        private Order order;
        private Product product;
        private Integer quantity;
        private BigDecimal priceAtPurchase;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder order(Order order) {
            this.order = order;
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

