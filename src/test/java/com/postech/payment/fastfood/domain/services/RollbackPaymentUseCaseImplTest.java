package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RollbackPaymentUseCaseImplTest {

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @InjectMocks
    private RollbackPaymentUseCaseImpl useCase;

    @Captor
    ArgumentCaptor<UUID> uuidCaptor;

    @Test
    void whenExecuteWithValidId_thenDeletesFromRepository() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        useCase.execute(id);

        // Then
        verify(paymentRepositoryPort).delete(uuidCaptor.capture());
        assert id.equals(uuidCaptor.getValue());
    }
}

