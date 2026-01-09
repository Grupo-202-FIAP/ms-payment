package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.enums.Category;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.OrderItem;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.Product;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    @Test
    void toMPVOrderRequest_whenValidInput_thenReturnOrderMPRequestDto() {
        final UUID orderId = UUID.randomUUID();
        final UUID paymentId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final Payment payment = new Payment.Builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(150.75))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .build();

        final Product product1 = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final Product product2 = new Product(2L, "Coke", Category.DRINKS, BigDecimal.valueOf(5.25));

        final OrderItem item1 = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product1)
                .quantity(2)
                .build();
        final OrderItem item2 = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product2)
                .quantity(1)
                .build();

        final List<OrderItem> orderItems = List.of(item1, item2);
        final String posId = "pos-123";
        final String mode = "dynamic";

        final OrderMPRequestDto result = OrderMapper.toMPVOrderRequest(payment, orderItems, posId, mode);

        assertNotNull(result);
        assertEquals("qr", result.type());
        assertEquals("150.75", result.total_amount());
        assertEquals("Pedido FastFood - " + orderId, result.description());
        assertEquals(orderId.toString(), result.external_reference());
        assertEquals("PT10M", result.expiration_time());
        assertNotNull(result.config());
        assertNotNull(result.config().qr());
        assertEquals(posId, result.config().qr().external_pos_id());
        assertEquals(mode, result.config().qr().mode());
        assertNotNull(result.transactions());
        assertNotNull(result.transactions().payments());
        assertEquals(1, result.transactions().payments().size());
        assertEquals("150.75", result.transactions().payments().get(0).amount());
        assertNotNull(result.items());
        assertEquals(2, result.items().size());

        final var firstItem = result.items().get(0);
        assertEquals("Burger", firstItem.title());
        assertEquals("25.5", firstItem.unit_price());
        assertEquals(2, firstItem.quantity());
        assertEquals("UN", firstItem.unit_measure());
        assertEquals(product1.getId().toString(), firstItem.external_code());
        assertNotNull(firstItem.external_categories());
        assertEquals(1, firstItem.external_categories().size());
        assertEquals(Category.SANDWICHES.getCategory(), firstItem.external_categories().get(0).id());

        final var secondItem = result.items().get(1);
        assertEquals("Coke", secondItem.title());
        assertEquals("5.25", secondItem.unit_price());
        assertEquals(1, secondItem.quantity());
        assertEquals(product2.getId().toString(), secondItem.external_code());
        assertEquals(Category.DRINKS.getCategory(), secondItem.external_categories().get(0).id());
    }
}

