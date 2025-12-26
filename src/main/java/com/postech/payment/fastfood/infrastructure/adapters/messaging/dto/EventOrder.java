package com.postech.payment.fastfood.infrastructure.adapters.messaging.dto;

import com.postech.payment.fastfood.domain.Order;
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
public class EventOrder {
    private UUID id;
    private UUID transactionId;
    private UUID orderId;
    private Order payload;
    private String source;
    private String status;
    private List<History> history;
    private LocalDateTime createdAt;
}
