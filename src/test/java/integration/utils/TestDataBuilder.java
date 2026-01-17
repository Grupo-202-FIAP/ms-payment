package integration.utils;

import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TestDataBuilder {

    public Order buildOrder(UUID orderId, BigDecimal totalPrice) {
        return new Order.Builder()
                .id(orderId)
                .totalPrice(totalPrice)
                .identifier("ORDER-" + orderId.toString().substring(0, 8))
                .build();
    }

    public Order buildOrderWithCustomer(UUID orderId, BigDecimal totalPrice, UUID customerId) {
        return new Order.Builder()
                .id(orderId)
                .totalPrice(totalPrice)
                .identifier("ORDER-" + orderId.toString().substring(0, 8))
                .customerId(customerId)
                .build();
    }

    public Payment buildPayment(UUID orderId, UUID transactionId, BigDecimal amount, PaymentStatus status) {
        return new Payment.Builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .amount(amount)
                .status(status)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();
    }

    public EventOrder buildEventOrder(UUID orderId, UUID transactionId, String status, Order payload) {
        return EventOrder.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .transactionId(transactionId)
                .status(status)
                .payload(payload)
                .source("ORDER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public EventOrder buildEventOrderWithHistory(UUID orderId, UUID transactionId, String status, Order payload, List<History> history) {
        return EventOrder.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .transactionId(transactionId)
                .status(status)
                .payload(payload)
                .source("ORDER")
                .history(history)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public List<History> buildHistory(String source, String status, String message) {
        List<History> history = new ArrayList<>();
        history.add(History.builder()
                .source(source)
                .status(status)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build());
        return history;
    }

    public GeneratedQrCodeResponse buildQrCodeResponse(UUID orderId, BigDecimal amount) {
        return new GeneratedQrCodeResponse(
                orderId,
                "payment-" + orderId.toString().substring(0, 8),
                amount,
                "BRL",
                "00020126580014br.gov.bcb.pix0136" + UUID.randomUUID().toString(),
                OffsetDateTime.now().plusMinutes(30)
        );
    }
}

