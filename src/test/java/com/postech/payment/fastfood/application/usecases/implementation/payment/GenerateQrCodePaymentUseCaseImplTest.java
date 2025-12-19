package com.postech.payment.fastfood.application.usecases.implementation.payment;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;
import com.postech.payment.fastfood.support.builders.OrderTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateQrCodePaymentUseCaseImplTest {

    @Mock
    private MercadoPagoPort mercadoPagoPort;

    @Mock
    private LoggerPort loggerPort;

    @InjectMocks
    private GenerateQrCodePaymentUseCaseImpl generateQrCodePaymentUseCaseImpl;

    @Nested
    class execute {

        @Test
        void shouldCallGenerateQrCodePaymentUseCaseImpl() {

            Order order1 = OrderTestBuilder.validOrder();
             
            GenerateQrCodeResult responseMock = mock(GenerateQrCodeResult.class);
            when(mercadoPagoPort.createQrCode(order1))
                    .thenReturn(responseMock);

            // ===== ACT =====
            GenerateQrCodeResult generatedResult = generateQrCodePaymentUseCaseImpl.execute(order1);

            // ===== ASSERT =====
            assertNotNull(generatedResult);
            verify(mercadoPagoPort).createQrCode(order1);

        }

    }
}