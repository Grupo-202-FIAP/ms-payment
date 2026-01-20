package com.postech.payment.fastfood.application.mapper;

import com.postech.payment.fastfood.domain.enums.Category;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.OrderItem;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.Product;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.ItemDto;
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
        // --- Given (Setup) ---
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

        assertEquals(posId, result.config().qr().external_pos_id());
        assertEquals(mode, result.config().qr().mode());

        assertEquals(1, result.transactions().payments().size());
        assertEquals("150.75", result.transactions().payments().get(0).amount());

        assertEquals(2, result.items().size());

        assertItem(result.items().get(0), "Burger", "25.5", 2, product1.getId().toString(), Category.SANDWICHES);
        assertItem(result.items().get(1), "Coke", "5.25", 1, product2.getId().toString(), Category.DRINKS);
    }

    private void assertItem(ItemDto item, String title, String price, int quantity, String extCode, Category category) {
        assertEquals(title, item.title());
        assertEquals(price, item.unit_price());
        assertEquals(quantity, item.quantity());
        assertEquals("UN", item.unit_measure());
        assertEquals(extCode, item.external_code());
        // Implicit null check on categories list
        assertEquals(category.getValue(), item.external_categories().get(0).id());
    }
}

