package com.postech.payment.fastfood.infrastructure.adapters.http;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.domain.exception.FastFoodException;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.MercadoPagoClient;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.OrderMercadoPagoRequestDto;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private final MercadoPagoClient mercadoPagoClient;
    private final LoggerPort logger;

    public MercadoPagoAdapter(MercadoPagoClient mercadoPagoClient, LoggerPort logger) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.logger = logger;
    }

    @Override
    public String createOrder(String idempotencyKey, String accessToken, OrderMercadoPagoRequestDto requestBody, String orderId) {
        try {
            final String resposta = mercadoPagoClient.createOrder(idempotencyKey, "Bearer " + accessToken, requestBody);
            logger.info("[Service][Payment] Resposta MercadoPago: {}", resposta);
            return resposta;

        } catch (FeignException e) {
            logger.warn("[Service][Payment] Erro MercadoPago: {}", e.getMessage());
            throw new FastFoodException("Erro ao gerar QR Code de pagamento",
                    "Não foi possível gerar o QR Code de pagamento para o pedido: " + orderId,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("[Service][Payment] Erro de validação ou nulidade: {}", e.getMessage(), e);
            throw new FastFoodException("Erro ao processar dados do pedido",
                    "Houve um erro interno ao processar o pedido: " + orderId,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
