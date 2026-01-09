package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class RollbackPaymentUseCaseImplUnitTest {

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @InjectMocks
    private RollbackPaymentUseCaseImpl useCase;

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
    void execute_shouldCallDeleteOnRepository() {
        UUID orderId = UUID.randomUUID();
        useCase.execute(orderId);
        verify(paymentRepositoryPort, times(1)).delete(orderId);
    }

}

