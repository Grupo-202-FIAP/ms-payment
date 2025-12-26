package com.postech.payment.fastfood.application.usecases.ports.input;


import com.postech.payment.fastfood.infrastructure.adapters.input.webhook.dao.WebhookEvent;

public interface ProcessPaymentNotificationUseCase {
    void execute(WebhookEvent webhookEvent, String signature, String requestId, String dataIdParam);
}
