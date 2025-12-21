package com.postech.payment.fastfood.infrastructure.adapters.http;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MercadoPagoAdapterTest {
    @Mock
    private MercadoPagoClient mercadoPagoClient;

    @Mock
    private LoggerPort loggerPort;

    @InjectMocks
    private MercadoPagoAdapter mercadoPagoAdapter;

    @Nested
    class CreateQrCodeTests {
        // Test methods for createQrCode would go here

        @Test
        void shouldCreateQrCodeSuccessfullyWithRealDataStructure() {
//            // Arrange
//            Order order = OrderTestBuilder.validOrder();
//
//            TypeResponse typeResponseMock = new TypeResponse("00020101021243650016COM.MERCADOLIBRE...");
//            OrderResponse responseMock = new OrderResponse(
//                    "ORD01KCSN16162R3N8SZ7V8WPYW0R",
//                    "created",
//                    "Pedido FastFood - string",
//                    1.0,
//                    "BRL",
//                    OffsetDateTime.now(),
//                    null,
//                    null,
//                    typeResponseMock
//            );
//
//            when(mercadoPagoClient.createOrder(eq(order.getId().toString()), anyString(), any()))
//                    .thenReturn(responseMock);
//
//            // Act
//            GenerateQrCodeResult result = mercadoPagoAdapter.createQrCode(order);
//
//            // Assert
//            assertNotNull(result);
//            assertEquals(order.getId().toString(), result.orderId()); // Seu ID interno
//            assertEquals("ORD01KCSN16162R3N8SZ7V8WPYW0R", result.paymentId()); // ID do Mercado Pago
//
//            assertEquals(typeResponseMock.qrData(), result.qrCode());
//
//            verify(mercadoPagoClient).createOrder(eq(order.getId().toString()), contains("Bearer "), any());
        }

        @Test
        void shouldThrowFastFoodExceptionWhenMercadoPagoFails() {
            // Arrange
            /*
            Order order = OrderTestBuilder.validOrder();
            when(mercadoPagoClient.createOrder(any(), any(), any()))
                    .thenThrow(FeignException.class);

            // Act & Assert
            assertThrows(FastFoodException.class, () -> {
                mercadoPagoAdapter.createQrCode(order);
            });

             */
        }

        @Test
        void shouldThrowFastFoodExceptionWhenNullPointerExceptionOccurs() {
            /*
            // Arrange
            Order order = OrderTestBuilder.validOrder();
            order.setId(null);

            FastFoodException exception = assertThrows(FastFoodException.class, () -> {
                mercadoPagoAdapter.createQrCode(order);
            });

            assertEquals("Erro ao processar dados do pedido", exception.getMessage());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());

            verifyNoInteractions(mercadoPagoClient);

             */
        }

    }

}