package com.postech.payment.fastfood.infrastructure.adapters.http;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.mapper.OrderMapper;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.domain.exception.FastFoodException;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderResponse;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.request.OrderMPRequestDto;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private final MercadoPagoClient mercadoPagoClient;
    private final LoggerPort logger;
    @Value("${mercadoPago.externalPosID}")
    private String externalPosId;
    @Value("${mercadoPago.accessToken}")
    private String accessToken;

    public MercadoPagoAdapter(MercadoPagoClient mercadoPagoClient, LoggerPort logger) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.logger = logger;

    }

    @Override
    public GenerateQrCodeResult createQrCode(Order order) {

        final OrderMPRequestDto requestBody = OrderMapper.toMPVOrderRequest(order, externalPosId, "dynamic");
        try {
            final OrderResponse response = mercadoPagoClient
                    .createOrder(UUID.randomUUID().toString(), "Bearer " + accessToken, requestBody);
            logger.info("[Service][Payment] Resposta MercadoPago: {}", response);
            final OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(2);
            return new GenerateQrCodeResult(
                    order.getId().toString(),
                    response.id(),
                    BigDecimal.valueOf(response.totalAmount()),
                    response.currency(),
                    response.typeResponse().qrData(),
                    expiresAt
            );
        } catch (FeignException e) {
            logger.warn("[Service][Payment] Erro MercadoPago: {}", e.getMessage());
            throw new FastFoodException("Erro ao gerar QR Code de pagamento",
                    "Não foi possível gerar o QR Code de pagamento para o pedido: " + order.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("[Service][Payment] Erro de validação ou nulidade: {}", e.getMessage(), e);
            throw new FastFoodException("Erro ao processar dados do pedido",
                    "Houve um erro interno ao processar o pedido: " + order.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
