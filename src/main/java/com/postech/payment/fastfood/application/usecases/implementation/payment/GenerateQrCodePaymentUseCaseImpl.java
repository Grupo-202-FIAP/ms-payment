package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.mapper.OrderMapper;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.OrderRequest;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.dto.OrderMercadoPagoRequestDto;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

public class GenerateQrCodePaymentUseCaseImpl implements GenerateQrCodePaymentUseCase {

    private final String externalPosId = "TOTEMFASTFOOD";
    private final String qrCodeModeType = "dynamic";
    private final MercadoPagoPort mercadoPagoPort;
    private final LoggerPort logger;
    @Value("${mercadoPago.accessToken}")
    private String accessToken;

    public GenerateQrCodePaymentUseCaseImpl(MercadoPagoPort mercadoPagoPort, LoggerPort logger) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.logger = logger;
    }


    public String execute(OrderRequest orderRequest) {
        logger.info("[Service][Payment] Criando Order no MercadoPago para o pedido: {}", orderRequest.id());
        final Order order = OrderMapper.toDomain(orderRequest);
        final OrderMercadoPagoRequestDto requestBody = OrderMapper.toMercadoPagoV1OrderRequest(order, externalPosId, qrCodeModeType);
        final String idempotencyKey = UUID.randomUUID().toString();
        return mercadoPagoPort.createOrder(idempotencyKey, accessToken, requestBody, order.getId().toString());
    }
}
