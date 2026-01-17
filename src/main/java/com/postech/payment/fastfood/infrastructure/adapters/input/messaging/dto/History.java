package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class History {
    private String source;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}
