package com.postech.payment.fastfood.utils;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventPayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class JsonConverter {

    private static final String ERROR_CONVERT_JSON = "[JsonConverter][ERROR] Failed to convert JSON to Event: {}";
    private static final String ERROR_CONVERT_OBJ = "[JsonConverter][ERROR] Failed to convert object to JSON: {}";

    private final ObjectMapper objectMapper;
    private final LoggerPort logger;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            logger.error(ERROR_CONVERT_OBJ, e.getMessage());
            throw new ConversionException("Failed to serialize object", e);
        }
    }

    public EventPayment toEventPayment(String json) {
        try {
            return objectMapper.readValue(json, EventPayment.class);
        } catch (JacksonException e) {
            logger.error(ERROR_CONVERT_JSON, e.getMessage());
            throw new ConversionException("Failed to deserialize EventPayment", e);
        }
    }

    public EventOrder toEventOrder(String json) {
        try {
            return objectMapper.readValue(json, EventOrder.class);
        } catch (JacksonException e) {
            logger.error(ERROR_CONVERT_JSON, e.getMessage());
            throw new ConversionException("Failed to deserialize EventOrder", e);
        }
    }
}
