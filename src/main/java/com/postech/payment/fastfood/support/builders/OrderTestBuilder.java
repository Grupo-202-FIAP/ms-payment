package com.postech.payment.fastfood.support.builders;

import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.Product;
import com.postech.payment.fastfood.domain.enums.Category;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderItemMP;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.QrConfig;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.QrProperty;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.TypeResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OrderTestBuilder {
    public static Order validOrder() {
        final Product product = new Product.Builder()
                .id(1L)
                .name("X-Burger")
                .category(Category.SANDWICHES)
                .unitPrice(BigDecimal.ONE)
                .build();

        final OrderItem item = new OrderItem.Builder()
                .product(product)
                .quantity(1)
                .priceAtPurchase(BigDecimal.ONE)
                .build();

        final Payment payment = new Payment.Builder()
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        return new Order.Builder()
                .id(UUID.randomUUID())
                .identifier("ORDER-123")
                .payment(payment)
                .itens(List.of(item))
                .totalPrice(BigDecimal.ONE)
                .build();
    }

    public static OrderResponse validOrderResponse() {
        return new OrderResponse(
                "ORD01KCSN16162R3N8SZ7V8WPYW0R",
                "created",
                "Pedido FastFood - string",
                1.0,
                "BRL",
                OffsetDateTime.parse("2025-12-18T21:14:39.087Z"),
                new QrConfig(
                        new QrProperty(
                                "SUC001",
                                "dynamic"
                        )
                ),
                List.of(
                        new OrderItemMP(
                                "string",
                                1.0,
                                1,
                                "0"
                        )
                ),
                new TypeResponse(
                        ""
                )
        );
    }
}
