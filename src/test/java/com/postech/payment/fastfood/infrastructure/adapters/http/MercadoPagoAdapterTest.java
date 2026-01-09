package com.postech.payment.fastfood.infrastructure.adapters.http;

import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.domain.enums.Category;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.OrderItem;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.Product;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.http.MercadoPagoAdapter;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.TypeResponse;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoAdapterTest {

    @Mock
    private MercadoPagoClient mercadoPagoClient;

    @Mock
    private LoggerPort loggerPort;

    @InjectMocks
    private MercadoPagoAdapter mercadoPagoAdapter;

    @BeforeEach
    void setUp() throws Exception {
        // Set private fields using reflection
        Field externalPosIdField = MercadoPagoAdapter.class.getDeclaredField("externalPosId");
        externalPosIdField.setAccessible(true);
        externalPosIdField.set(mercadoPagoAdapter, "pos-123");

        Field authHeaderField = MercadoPagoAdapter.class.getDeclaredField("authHeader");
        authHeaderField.setAccessible(true);
        authHeaderField.set(mercadoPagoAdapter, "Bearer token-123");
    }

    @Test
    void createQrCode_whenSuccess_thenReturnGeneratedQrCodeResponse() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final TypeResponse typeResponse = new TypeResponse("qrcode-data-123");
        final OrderResponse orderResponse = new OrderResponse(
                "payment-id-123",
                "created",
                "Pedido FastFood",
                100.50,
                "BRL",
                OffsetDateTime.now(),
                null,
                null,
                typeResponse
        );

        when(mercadoPagoClient.createOrder(eq(orderId.toString()), eq("Bearer token-123"), any()))
                .thenReturn(orderResponse);

        final GeneratedQrCodeResponse result = mercadoPagoAdapter.createQrCode(payment, List.of(item));

        assertNotNull(result);
        assertEquals(orderId, result.orderId());
        assertEquals("payment-id-123", result.paymentId());
        assertEquals(BigDecimal.valueOf(100.50), result.totalAmount());
        assertEquals("BRL", result.currency());
        assertEquals("qrcode-data-123", result.qrCode());
        assertNotNull(result.expiresAt());
        verify(loggerPort).info(anyString(), eq(orderId));
        verify(mercadoPagoClient).createOrder(eq(orderId.toString()), eq("Bearer token-123"), any());
    }

    @Test
    void createQrCode_whenRetryableException_thenThrowPaymentIntegrationException() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final RetryableException retryableException = mock(RetryableException.class);
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(retryableException);

        final PaymentIntegrationException exception = assertThrows(
                PaymentIntegrationException.class,
                () -> mercadoPagoAdapter.createQrCode(payment, List.of(item))
        );

        assertEquals("Payment service temporarily unavailable", exception.getMessage());
        verify(loggerPort).error(anyString(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignExceptionConflict_thenReturnNull() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(409);
        when(feignException.contentUTF8()).thenReturn("idempotency_key_already_used");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final GeneratedQrCodeResponse result = mercadoPagoAdapter.createQrCode(payment, List.of(item));

        assertNull(result);
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
        verify(loggerPort).error(anyString(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignException4xx_thenThrowPaymentIntegrationException() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(400);
        when(feignException.contentUTF8()).thenReturn("Invalid request");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final PaymentIntegrationException exception = assertThrows(
                PaymentIntegrationException.class,
                () -> mercadoPagoAdapter.createQrCode(payment, List.of(item))
        );

        assertTrue(exception.getMessage().contains("Invalid payment data"));
        assertTrue(exception.getMessage().contains(orderId.toString()));
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignException5xx_thenThrowPaymentIntegrationException() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(500);
        when(feignException.contentUTF8()).thenReturn("Internal server error");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final PaymentIntegrationException exception = assertThrows(
                PaymentIntegrationException.class,
                () -> mercadoPagoAdapter.createQrCode(payment, List.of(item))
        );

        assertTrue(exception.getMessage().contains("temporarily unavailable"));
        assertTrue(exception.getMessage().contains(orderId.toString()));
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignExceptionWithNullStatus_thenThrowPaymentIntegrationException() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(-1);
        when(feignException.contentUTF8()).thenReturn("Unknown error");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final PaymentIntegrationException exception = assertThrows(
                PaymentIntegrationException.class,
                () -> mercadoPagoAdapter.createQrCode(payment, List.of(item))
        );

        assertTrue(exception.getMessage().contains("temporarily unavailable"));
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignExceptionConflictStatus_thenReturnNull() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(409);
        when(feignException.contentUTF8()).thenReturn("Conflict error");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final GeneratedQrCodeResponse result = mercadoPagoAdapter.createQrCode(payment, List.of(item));

        assertNull(result);
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
        verify(loggerPort).error(anyString(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignExceptionWithIdempotencyInMessage_thenReturnNull() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(400);
        when(feignException.contentUTF8()).thenReturn("Error: idempotency_key_already_used");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final GeneratedQrCodeResponse result = mercadoPagoAdapter.createQrCode(payment, List.of(item));

        assertNull(result);
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
        verify(loggerPort).error(anyString(), eq(orderId));
    }

    @Test
    void createQrCode_whenFeignExceptionWithNullStatusButIdempotency_thenReturnNull() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(-1);
        when(feignException.contentUTF8()).thenReturn("idempotency_key_already_used");
        when(mercadoPagoClient.createOrder(anyString(), anyString(), any()))
                .thenThrow(feignException);

        final GeneratedQrCodeResponse result = mercadoPagoAdapter.createQrCode(payment, List.of(item));

        assertNull(result);
        verify(loggerPort).warn(anyString(), any(), any(), eq(orderId));
        verify(loggerPort).error(anyString(), eq(orderId));
    }

    @Test
    void createQrCode_whenExpiresAtIsCalculatedCorrectly_thenReturnResponseWithFutureDate() {
        final UUID orderId = UUID.randomUUID();
        final Payment payment = new Payment.Builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .amount(BigDecimal.valueOf(100.50))
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .build();

        final Product product = new Product(1L, "Burger", Category.SANDWICHES, BigDecimal.valueOf(25.50));
        final OrderItem item = new OrderItem.Builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .build();

        final TypeResponse typeResponse = new TypeResponse("qrcode-data-123");
        final OrderResponse orderResponse = new OrderResponse(
                "payment-id-123",
                "created",
                "Pedido FastFood",
                100.50,
                "BRL",
                OffsetDateTime.now(),
                null,
                null,
                typeResponse
        );

        when(mercadoPagoClient.createOrder(eq(orderId.toString()), eq("Bearer token-123"), any()))
                .thenReturn(orderResponse);

        final OffsetDateTime beforeCall = OffsetDateTime.now();
        final GeneratedQrCodeResponse result = mercadoPagoAdapter.createQrCode(payment, List.of(item));
        final OffsetDateTime afterCall = OffsetDateTime.now();

        assertNotNull(result);
        assertNotNull(result.expiresAt());
        assertTrue(result.expiresAt().isAfter(beforeCall.plusMinutes(9)));
        assertTrue(result.expiresAt().isBefore(afterCall.plusMinutes(11)));
    }
}
