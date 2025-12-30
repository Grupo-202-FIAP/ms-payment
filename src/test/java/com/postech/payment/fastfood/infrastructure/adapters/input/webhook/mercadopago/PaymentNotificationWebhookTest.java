package com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago;

import com.postech.payment.fastfood.application.ports.input.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao.WebhookEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentNotificationWebhookTest {

    @Mock
    private ProcessPaymentNotificationUseCase processPaymentNotificationUseCase;

    @Mock
    private LoggerPort loggerPort;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PaymentNotificationWebhook paymentNotificationWebhook;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handlePaymentNotification_whenDataIdParamProvided_thenProcessAndReturnOk() {
        final WebhookEvent webhookEvent = new WebhookEvent();
        final String signature = "signature-123";
        final String requestId = "request-123";
        final String dataId = "data-id-123";

        final Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("data.id", new String[]{dataId});
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        final ResponseEntity<String> result = paymentNotificationWebhook.handlePaymentNotification(
                webhookEvent, signature, requestId, dataId, null, httpServletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Payment notification received successfully", result.getBody());
        verify(processPaymentNotificationUseCase).execute(webhookEvent, signature, requestId, dataId);
        verify(loggerPort, atLeastOnce()).info(anyString(), any());
    }

    @Test
    void handlePaymentNotification_whenIdParamProvided_thenProcessAndReturnOk() {
        final WebhookEvent webhookEvent = new WebhookEvent();
        final String signature = "signature-123";
        final String requestId = "request-123";
        final String id = "id-123";

        final Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("id", new String[]{id});
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        final ResponseEntity<String> result = paymentNotificationWebhook.handlePaymentNotification(
                webhookEvent, signature, requestId, null, id, httpServletRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Payment notification received successfully", result.getBody());
        verify(processPaymentNotificationUseCase).execute(webhookEvent, signature, requestId, id);
    }

    @Test
    void handlePaymentNotification_whenNoDataIdOrId_thenReturnBadRequest() {
        final WebhookEvent webhookEvent = new WebhookEvent();
        final String signature = "signature-123";
        final String requestId = "request-123";

        final Map<String, String[]> parameterMap = new HashMap<>();
        when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

        final ResponseEntity<String> result = paymentNotificationWebhook.handlePaymentNotification(
                webhookEvent, signature, requestId, null, null, httpServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Missing required parameter: data.id or id", result.getBody());
        verify(processPaymentNotificationUseCase, never()).execute(any(), anyString(), anyString(), anyString());
        verify(loggerPort, atLeastOnce()).error(anyString());
    }
}

