package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;


public class GenerateQrCodePaymentUseCaseImpl implements GenerateQrCodePaymentUseCase {
    private final MercadoPagoPort mercadoPagoPort;
    private final LoggerPort logger;
    private final PaymentRepositoryPort paymentRepositoryPort;


    public GenerateQrCodePaymentUseCaseImpl(MercadoPagoPort mercadoPagoPort, LoggerPort logger, PaymentRepositoryPort paymentRepositoryPort) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.logger = logger;
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    public GenerateQrCodeResult execute(Order order) {
        logger.info("[Service][Payment] Criando Order no MercadoPago para o pedido: {}", order.getId());
        order.getPayment().setAmount(order.getTotalPrice());
        this.paymentRepositoryPort.save(order.getPayment());
        return mercadoPagoPort.createQrCode(order);
    }
}
