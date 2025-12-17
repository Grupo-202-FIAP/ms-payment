package com.postech.ms_payment_fastfood.infrastructure.webhook;


import com.postech.ms_payment_fastfood.application.gateways.LoggerPort;
import com.postech.ms_payment_fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;

import com.postech.ms_payment_fastfood.infrastructure.webhook.dao.WebhookEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/webhook")
@Slf4j
public class PaymentNotificationWebhook {

    private final ProcessPaymentNotificationUseCase processPaymentNotification;
    private final LoggerPort logger;

    public PaymentNotificationWebhook(ProcessPaymentNotificationUseCase processPaymentNotification, LoggerPort logger) {
        this.processPaymentNotification = processPaymentNotification;
        this.logger = logger;
    }

    @PostMapping
    public ResponseEntity<String> handlePaymentNotification(
            @RequestBody WebhookEvent webhookEvent,
            @RequestHeader("x-signature") String signature,
            @RequestHeader("x-request-id") String requestId,
            @RequestParam("data.id") String dataIdParam) {
        logger.info("[Webhook][Payment] Received payment notification: event={}, signature={}, requestId={}, dataId={}",
                webhookEvent, signature, requestId, dataIdParam);
        processPaymentNotification.execute(webhookEvent, signature, requestId, dataIdParam);
        return ResponseEntity.ok("Payment notification received successfully");
    }

}
