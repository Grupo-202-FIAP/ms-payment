package com.postech.payment.fastfood.infrastructure.adapters.output.persistence;

import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentInformationQrCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentInformationQrCodeRepositoryAdapterUnitTest {

    @Mock
    private IPaymentInformationQrCodeRepository qrCodeRepository;

    @Mock
    private LoggerPort logger;

    @InjectMocks
    private PaymentInformationQrCodeRepositoryAdapter adapter;

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
    void whenSaveSucceeds_thenDelegateToRepository() {
        final UUID orderId = UUID.randomUUID();
        final QrCode qrCode = new QrCode.Builder()
                .orderId(orderId)
                .totalAmount(BigDecimal.ONE)
                .currency("BRL")
                .qrCode("qrcode")
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build();

        adapter.save(qrCode);

        verify(qrCodeRepository).save(any(QrCodeEntity.class));
        verify(logger).info(anyString(), eq(orderId));
        verify(logger, never()).error(anyString(), any(), any());
    }

    @Test
    void whenSaveThrowsDataAccessException_thenWrapInDatabaseException() {
        final UUID orderId = UUID.randomUUID();
        final QrCode qrCode = new QrCode.Builder()
                .orderId(orderId)
                .totalAmount(BigDecimal.ONE)
                .currency("BRL")
                .qrCode("qrcode")
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build();

        final DataAccessException dataAccessException = mock(DataAccessException.class);
        doThrow(dataAccessException).when(qrCodeRepository).save(any(QrCodeEntity.class));

        assertThrows(DatabaseException.class, () -> adapter.save(qrCode));
        verify(logger).info(anyString(), eq(orderId));
        verify(logger).error(anyString(), eq(orderId), eq(dataAccessException));
    }

}
