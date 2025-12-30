package com.postech.payment.fastfood.utils;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JsonConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LoggerPort loggerPort;

    private JsonConverter jsonConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jsonConverter = new JsonConverter(objectMapper, loggerPort);
    }

    @Test
    void toJson_whenValidObject_thenReturnJsonString() throws Exception {
        final String expectedJson = "{\"id\":\"123\"}";
        final Object object = new Object();
        when(objectMapper.writeValueAsString(object)).thenReturn(expectedJson);

        final String result = jsonConverter.toJson(object);

        assertEquals(expectedJson, result);
        verify(objectMapper).writeValueAsString(object);
        verifyNoInteractions(loggerPort);
    }

    @Test
    void toJson_whenException_thenThrowConversionException() throws Exception {
        final Object object = new Object();
        final JacksonException exception = mock(JacksonException.class);
        when(exception.getMessage()).thenReturn("Serialization error");
        when(objectMapper.writeValueAsString(object)).thenThrow(exception);

        final ConversionException result = assertThrows(ConversionException.class, () -> jsonConverter.toJson(object));

        assertEquals("Failed to serialize object", result.getMessage());
        assertEquals(exception, result.getCause());
        verify(loggerPort).error(anyString(), eq("Serialization error"));
    }

    @Test
    void toEventPayment_whenValidJson_thenReturnEventPayment() throws Exception {
        final String json = "{\"id\":\"123\"}";
        final EventPayment expectedEvent = new EventPayment();
        when(objectMapper.readValue(json, EventPayment.class)).thenReturn(expectedEvent);

        final EventPayment result = jsonConverter.toEventPayment(json);

        assertEquals(expectedEvent, result);
        verify(objectMapper).readValue(json, EventPayment.class);
        verifyNoInteractions(loggerPort);
    }

    @Test
    void toEventPayment_whenException_thenThrowConversionException() throws Exception {
        final String json = "invalid json";
        final JacksonException exception = mock(JacksonException.class);
        when(exception.getMessage()).thenReturn("Deserialization error");
        when(objectMapper.readValue(json, EventPayment.class)).thenThrow(exception);

        final ConversionException result = assertThrows(ConversionException.class, () -> jsonConverter.toEventPayment(json));

        assertEquals("Failed to deserialize EventPayment", result.getMessage());
        assertEquals(exception, result.getCause());
        verify(loggerPort).error(anyString(), eq("Deserialization error"));
    }

    @Test
    void toEventOrder_whenValidJson_thenReturnEventOrder() throws Exception {
        final String json = "{\"orderId\":\"" + UUID.randomUUID() + "\"}";
        final EventOrder expectedEvent = new EventOrder();
        when(objectMapper.readValue(json, EventOrder.class)).thenReturn(expectedEvent);

        final EventOrder result = jsonConverter.toEventOrder(json);

        assertEquals(expectedEvent, result);
        verify(objectMapper).readValue(json, EventOrder.class);
        verifyNoInteractions(loggerPort);
    }

    @Test
    void toEventOrder_whenException_thenThrowConversionException() throws Exception {
        final String json = "invalid json";
        final JacksonException exception = mock(JacksonException.class);
        when(exception.getMessage()).thenReturn("Deserialization error");
        when(objectMapper.readValue(json, EventOrder.class)).thenThrow(exception);

        final ConversionException result = assertThrows(ConversionException.class, () -> jsonConverter.toEventOrder(json));

        assertEquals("Failed to deserialize EventOrder", result.getMessage());
        assertEquals(exception, result.getCause());
        verify(loggerPort).error(anyString(), eq("Deserialization error"));
    }
}

