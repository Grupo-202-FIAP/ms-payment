package com.postech.payment.fastfood.infrastructure.adapters.http;

import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.mapper.OrderMapper;
import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
        logger.info("[Adapter][MercadoPago] Solicitando QR Code para o pedido: {}", payment.getOrderId());

        final OrderMPRequestDto requestBody = OrderMapper.toMPVOrderRequest(payment, items, externalPosId, "dynamic");

        try {
            final OrderResponse response = mercadoPagoClient.createOrder(
                    UUID.randomUUID().toString(),
                    authHeader,
                    requestBody
            );

            return mapToGeneratedQrCodeResponse(payment, response);

        } catch (feign.RetryableException e) {
            logger.error("[Adapter][MercadoPago] Erro de conectividade com a API para o pedido: {}", payment.getOrderId());
            throw new PaymentIntegrationException("Serviço de pagamento temporariamente indisponível", e);

        } catch (FeignException e) {
            handleFeignException(payment, e);
            return null;
        }
    }

    private GeneratedQrCodeResponse mapToGeneratedQrCodeResponse(Payment payment, OrderResponse response) {
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(1);

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

        logger.warn("[Adapter][MercadoPago] Falha na API externa. Status: {} | Detalhes: {} | Pedido: {}",
                status, errorDetails, payment.getOrderId());

        if (status != null && status.is4xxClientError()) {
            throw new PaymentIntegrationException(
                    "Dados de pagamento inválidos na integração para o pedido: " + payment.getOrderId(), e
            );
        }

        throw new PaymentIntegrationException(
                "Serviço de pagamento temporariamente indisponível para o pedido: " + payment.getOrderId(), e
        );
    }
}
