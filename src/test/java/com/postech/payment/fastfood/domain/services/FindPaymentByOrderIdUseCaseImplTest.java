package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.exception.FastFoodException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.domain.model.Payment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPaymentByOrderIdUseCaseImplTest {

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @Mock
    private LoggerPort loggerPort;

    @InjectMocks
    private FindPaymentByOrderIdUseCaseImpl useCase;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void whenPaymentExists_thenReturnPayment() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder().orderId(orderId).build();
        when(paymentRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        final Payment result = useCase.findPaymentByOrderId(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        verify(paymentRepositoryPort).findByOrderId(orderId);
        verify(loggerPort).debug(anyString(), eq(orderId));
    }

    @Test
    void whenPaymentNotFound_thenThrowFastFoodException() {
        final UUID orderId = UUID.randomUUID();
        when(paymentRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.empty());

        final FastFoodException ex = assertThrows(FastFoodException.class,
                () -> useCase.findPaymentByOrderId(orderId));

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains(orderId.toString()));
        verify(paymentRepositoryPort).findByOrderId(orderId);
        verify(loggerPort).debug(anyString(), eq(orderId));
    }
}

