package com.postech.ms_payment_fastfood.application.usecases.interfaces.payment;


import com.postech.ms_payment_fastfood.infrastructure.webhook.dao.WebhookEvent;

public interface ProcessPaymentNotificationUseCase {
    void execute(WebhookEvent webhookEvent, String signature, String requestId, String dataIdParam);
}
