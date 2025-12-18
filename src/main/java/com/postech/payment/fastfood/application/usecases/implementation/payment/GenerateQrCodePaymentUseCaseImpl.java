package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;


public class GenerateQrCodePaymentUseCaseImpl implements GenerateQrCodePaymentUseCase {
    private final MercadoPagoPort mercadoPagoPort;
    private final LoggerPort logger;

    public GenerateQrCodePaymentUseCaseImpl(MercadoPagoPort mercadoPagoPort, LoggerPort logger) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.logger = logger;
    }

    public GenerateQrCodeResult execute(Order order) {
        logger.info("[Service][Payment] Criando Order no MercadoPago para o pedido: {}", order.getId());
        return mercadoPagoPort.createQrCode(order);
    }
}
