package com.postech.payment.fastfood.infrastructure.adapters.output.persistence;

import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.mapper.PaymentMapperEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentRepositoryAdapterUnitTest {

    @Mock
    private IPaymentEntityRepository paymentEntityRepository;

    @Mock
    private LoggerPort logger;

    @InjectMocks
    private PaymentRepositoryAdapter adapter;

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
    void whenSaveSucceeds_thenDelegate() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder().orderId(orderId).build();
        adapter.save(payment);
        verify(paymentEntityRepository).saveAndFlush(any(PaymentEntity.class));
        verify(logger).info(anyString(), eq(orderId));
        verify(logger, never()).error(anyString());
    }

    @Test
    void whenSaveDataIntegrityViolation_thenThrowDatabaseException() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder().orderId(orderId).build();
        final DataIntegrityViolationException exception = mock(DataIntegrityViolationException.class);
        doThrow(exception).when(paymentEntityRepository).saveAndFlush(any(PaymentEntity.class));
        
        assertThrows(DatabaseException.class, () -> adapter.save(payment));
        
        verify(logger).info(anyString(), eq(orderId));
        verify(logger).error(anyString());
    }

    @Test
    void whenSaveDataAccessException_thenThrowDatabaseException() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder().orderId(orderId).build();
        final DataAccessException exception = mock(DataAccessException.class);
        doThrow(exception).when(paymentEntityRepository).saveAndFlush(any(PaymentEntity.class));
        
        assertThrows(DatabaseException.class, () -> adapter.save(payment));
        
        verify(logger).info(anyString(), eq(orderId));
        verify(logger).error(anyString());
    }

    @Test
    void whenFindByOrderId_thenReturnMapped() {
        final UUID id = UUID.randomUUID();
        final PaymentEntity entity = PaymentMapperEntity.toEntity(new Payment.Builder().orderId(id).build());
        when(paymentEntityRepository.findByOrderId(id)).thenReturn(Optional.of(entity));
        final Optional<Payment> result = adapter.findByOrderId(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getOrderId());
        verify(logger).debug(anyString(), eq(id));
    }

    @Test
    void whenFindByOrderIdNotFound_thenReturnEmpty() {
        final UUID id = UUID.randomUUID();
        when(paymentEntityRepository.findByOrderId(id)).thenReturn(Optional.empty());
        final Optional<Payment> result = adapter.findByOrderId(id);
        assertFalse(result.isPresent());
        verify(logger).debug(anyString(), eq(id));
    }

    @Test
    void whenSaveWithQrCode_thenBindRelations() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .orderId(orderId)
                .qrCode(new com.postech.payment.fastfood.domain.model.QrCode.Builder()
                        .orderId(orderId)
                        .build())
                .build();
        adapter.save(payment);
        verify(paymentEntityRepository).saveAndFlush(any(PaymentEntity.class));
    }

    @Test
    void whenDelete_thenDelegate() {
        final UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(paymentEntityRepository).deleteByOrderId(id);
        verifyNoInteractions(logger);
    }
}
