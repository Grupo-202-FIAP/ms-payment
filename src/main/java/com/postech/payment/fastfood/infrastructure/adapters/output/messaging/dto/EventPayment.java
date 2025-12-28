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


    public EventPayment eventExpired(Payment payment) {
        final History historyEntry = History.builder()
                .source("PAYMENT")
                .status(payment.getStatus().name())
                .message("Status updated to:" + payment.getStatus().name())
                .createdAt(LocalDateTime.now())
                .build();


        return EventPayment.builder()
                //TODO: definir transactionId
                .id(UUID.randomUUID())
                .source("PAYMENT")
                .status("ROLLBACK_PENDING")
                .orderId(payment.getOrderId())
                .payload(payment)
                .history(List.of(historyEntry))
                .createdAt(LocalDateTime.now())
                .build();
    }


}
