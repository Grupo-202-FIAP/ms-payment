package com.postech.payment.fastfood.application.usecases.interfaces.payment;


import com.postech.payment.fastfood.infrastructure.webhook.dao.WebhookEvent;

public interface ProcessPaymentNotificationUseCase {
    void execute(WebhookEvent webhookEvent, String signature, String requestId, String dataIdParam);
}
