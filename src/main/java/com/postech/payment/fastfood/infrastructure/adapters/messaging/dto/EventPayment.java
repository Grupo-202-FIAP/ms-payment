package com.postech.payment.fastfood.infrastructure.adapters.messaging.dto;

import com.postech.payment.fastfood.domain.Payment;
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
}
