package com.postech.payment.fastfood.utils;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventPayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class JsonConverter {
    private final ObjectMapper objectMapper;
    private final LoggerPort logger;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("[toJson] Falha para converter objeto para JSON: {}", e);
            throw new RuntimeException("[toJson] Falha para converter objeto para JSON: {}", e);
        }
    }

    public EventPayment toEventPayment(String json) {
        try {
            return objectMapper.readValue(json, EventPayment.class);
        } catch (Exception e) {
            logger.error("[toEvent] Falha para converter JSON para Event: {}", e);
            throw new RuntimeException("[toEvent] Falha para converter JSON para Event: {}", e);
        }
    }

    public EventOrder toEventOrder(String json) {
        try {
            return objectMapper.readValue(json, EventOrder.class);
        } catch (Exception e) {
            logger.error("[toEvent] Falha para converter JSON para Event: {}", e);
            throw new RuntimeException("[toEvent] Falha para converter JSON para Event: {}", e);
        }
    }

}
