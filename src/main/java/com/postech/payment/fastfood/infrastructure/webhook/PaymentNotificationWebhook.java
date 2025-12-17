package com.postech.payment.fastfood.infrastructure.webhook;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.infrastructure.webhook.dao.WebhookEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
