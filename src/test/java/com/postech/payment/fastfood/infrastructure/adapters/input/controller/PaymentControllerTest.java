package com.postech.payment.fastfood.infrastructure.adapters.input.controller;

import com.postech.payment.fastfood.application.ports.input.FindPaymentByOrderIdUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private FindPaymentByOrderIdUseCase findPaymentByOrderIdUseCase;

    @Mock
    private LoggerPort loggerPort;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findPaymentByOrderId_whenPaymentExists_thenReturnOkWithPaymentResponse() {
        final UUID orderId = UUID.randomUUID();
        final UUID paymentId = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final Payment payment = new Payment.Builder()
                .id(paymentId)
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .paymentDateTime(now)
                .updatedAt(now)
                .build();

        when(findPaymentByOrderIdUseCase.findPaymentByOrderId(orderId)).thenReturn(payment);

        final ResponseEntity<PaymentResponse> result = paymentController.findPaymentByOrderId(orderId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        final PaymentResponse response = result.getBody();
        assertEquals(paymentId, response.id());
        assertEquals(orderId, response.orderId());
        assertEquals(BigDecimal.valueOf(100.50), response.amount());
        verify(findPaymentByOrderIdUseCase).findPaymentByOrderId(orderId);
    }
}

