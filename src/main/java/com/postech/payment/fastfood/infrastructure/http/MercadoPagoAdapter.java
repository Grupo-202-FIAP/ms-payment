package com.postech.payment.fastfood.infrastructure.http;

import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.mapper.OrderMapper;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.MercadoPagoPort;
import com.postech.payment.fastfood.domain.model.OrderItem;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private final MercadoPagoClient mercadoPagoClient;
    private final LoggerPort logger;
    private final String externalPosId;
    private final String authHeader;

    public MercadoPagoAdapter(
            MercadoPagoClient mercadoPagoClient,
            LoggerPort logger,
            @Value("${mercadoPago.externalPosID}") String externalPosId,
            @Value("${mercadoPago.accessToken}") String accessToken) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.logger = logger;
        this.externalPosId = externalPosId;
        this.authHeader = "Bearer " + accessToken;
    }

    @Override
    public GeneratedQrCodeResponse createQrCode(Payment payment, List<OrderItem> items) {
        logger.info("[ADAPTER][MERCADOPAGO] Requesting QR Code for order: {}", payment.getOrderId());

        final OrderMPRequestDto requestBody = OrderMapper.toMPVOrderRequest(payment, items, externalPosId, "dynamic");

        try {
            final OrderResponse response = mercadoPagoClient.createOrder(
                    payment.getOrderId().toString(),
                    authHeader,
                    requestBody
            );

            return mapToGeneratedQrCodeResponse(payment, response);

        } catch (feign.RetryableException e) {
            logger.error("[ADAPTER][MERCADOPAGO] Connectivity error with API for order: {}", payment.getOrderId());
            throw new PaymentIntegrationException("Payment service temporarily unavailable", e);

        } catch (FeignException e) {
            handleFeignException(payment, e);
            return null;
        }
    }

    private GeneratedQrCodeResponse mapToGeneratedQrCodeResponse(Payment payment, OrderResponse response) {
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);

        return new GeneratedQrCodeResponse(
                payment.getOrderId(),
                response.id(),
                BigDecimal.valueOf(response.totalAmount()),
                response.currency(),
                response.typeResponse().qrData(),
                expiresAt
        );
    }

    private void handleFeignException(Payment payment, FeignException e) {
        final HttpStatus status = HttpStatus.resolve(e.status());
        final String errorDetails = e.contentUTF8();

        logger.warn("[ADAPTER][MERCADOPAGO] External API failure. Status: {} | Details: {} | Order: {}",
                status, errorDetails, payment.getOrderId());

        if (status == HttpStatus.CONFLICT || errorDetails.contains("idempotency_key_already_used")) {
            logger.error("[ADAPTER][MERCADOPAGO] Idempotency conflict for order {}. Message will be discarded to avoid loop.", payment.getOrderId());
            return;
        }

        if (status != null && status.is4xxClientError()) {
            throw new PaymentIntegrationException(
                    "Invalid payment data in integration for order: " + payment.getOrderId(), e
            );
        }

        throw new PaymentIntegrationException(
                "Payment service temporarily unavailable for order: " + payment.getOrderId(), e
        );
    }
}
