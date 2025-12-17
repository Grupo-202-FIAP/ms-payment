package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import com.postech.payment.fastfood.infrastructure.webhook.dao.WebhookEvent;

public class ProcessPaymentNotificationUseCaseImpl implements ProcessPaymentNotificationUseCase {
    
    private final MercadoPagoWebhookSignatureValidator mercadoPagoWebhookSignatureValidator;
    private final LoggerPort logger;

    public ProcessPaymentNotificationUseCaseImpl(
            MercadoPagoWebhookSignatureValidator mercadoPagoWebhookSignatureValidator,
            LoggerPort logger) {

        this.mercadoPagoWebhookSignatureValidator = mercadoPagoWebhookSignatureValidator;
        this.logger = logger;
    }

    @Override
    public void execute(WebhookEvent event, String signature, String requestId, String dataIdParam) {

    }

    private void processOrderExpired(Order expiredOrder) {

    }

}
