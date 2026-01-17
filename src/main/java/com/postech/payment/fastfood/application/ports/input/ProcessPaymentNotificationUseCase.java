package com.postech.payment.fastfood.application.ports.input;


import com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao.WebhookEvent;

public interface ProcessPaymentNotificationUseCase {
    void execute(WebhookEvent webhookEvent, String signature, String requestId, String dataIdParam);
}
