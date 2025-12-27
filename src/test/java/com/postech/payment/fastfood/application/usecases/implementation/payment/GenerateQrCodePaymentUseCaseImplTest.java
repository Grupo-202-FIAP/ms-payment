package com.postech.payment.fastfood.application.usecases.implementation.payment;

import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.domain.services.GenerateQrCodePaymentUseCaseImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateQrCodePaymentUseCaseImplTest {

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private LoggerPort loggerPort;

    @InjectMocks
    private GenerateQrCodePaymentUseCaseImpl generateQrCodePaymentUseCaseImpl;

    @Nested
    class execute {

        @Test
        void shouldCallGenerateQrCodePaymentUseCaseImpl() {

//            Order order1 = OrderTestBuilder.validOrder();
//
//            GenerateQrCodeResult responseMock = mock(GenerateQrCodeResult.class);
//            when(mercadoPagoPort.createQrCode(order1.getPayment(), order1.getItems()))
//                    .thenReturn(responseMock);
//
//            // ===== ACT =====
//            GenerateQrCodeResult generatedResult = generateQrCodePaymentUseCaseImpl.execute(order1);
//
//            // ===== ASSERT =====
//            assertNotNull(generatedResult);
//            verify(mercadoPagoPort).createQrCode(order1);

        }

    }
}