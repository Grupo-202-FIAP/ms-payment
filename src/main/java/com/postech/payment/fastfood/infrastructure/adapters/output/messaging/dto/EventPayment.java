package com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto;

import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPayment {
    private UUID id;
    private UUID transactionId;
    private UUID orderId;
    private Payment payload;
    private String source;
    private String status;
    private List<History> history;
    private LocalDateTime createdAt;
    private static final String PAYMENT_SOURCE = "PAYMENT";
    private static final String STATUS_MSG_PREFIX = "Status updated to:";

    public EventPayment eventExpired(Payment payment) {

        final History historyEntry = History.builder()
                .source(PAYMENT_SOURCE)
                .status(payment.getStatus().name())
                .message(STATUS_MSG_PREFIX + payment.getStatus().name())
                .createdAt(LocalDateTime.now())
                .build();



        return EventPayment.builder()
                .id(UUID.randomUUID())
                .transactionId(payment.getTransactionId())
                .source(PAYMENT_SOURCE)
                .status("FAIL")
                .orderId(payment.getOrderId())
                .payload(payment)
                .history(List.of(historyEntry))
                .createdAt(LocalDateTime.now())
                .build();
    }


    public EventPayment eventExpiring(Payment payment, UUID transactionId) {
        final History historyEntry = History.builder()
                .source(PAYMENT_SOURCE)
                .status("ROLLBACK_PENDING")
                .message(STATUS_MSG_PREFIX + payment.getStatus().name())
                .createdAt(LocalDateTime.now())
                .build();


        return EventPayment.builder()
                .id(UUID.randomUUID())
                .transactionId(transactionId)
                .source(PAYMENT_SOURCE)
                .status("ROLLBACK_PENDING")
                .orderId(payment.getOrderId())
                .payload(payment)
                .history(List.of(historyEntry))
                .createdAt(LocalDateTime.now())
                .build();
    }


    public EventPayment eventSuccess(Payment payment) {

        final History historyEntry = History.builder()
                .source(PAYMENT_SOURCE)
                .status(payment.getStatus().name())
                .message(STATUS_MSG_PREFIX + payment.getStatus().name())
                .createdAt(LocalDateTime.now())
                .build();


        return EventPayment.builder()
                .id(UUID.randomUUID())
                .transactionId(payment.getTransactionId())
                .source(PAYMENT_SOURCE)
                .status("SUCCESS")
                .orderId(payment.getOrderId())
                .payload(payment)
                .history(List.of(historyEntry))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
