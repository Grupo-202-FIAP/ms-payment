package com.postech.payment.fastfood.infrastructure.webhook;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.infrastructure.webhook.dao.WebhookEvent;
import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestParam(value = "id", required = false) String idParam,
            HttpServletRequest request) {


        logger.info("[Webhook][Payment][DEBUG] === ALL QUERY PARAMS ===");
        request.getParameterMap().forEach((key, values) ->
                logger.info("[Webhook][Payment][DEBUG] Param: '{}' = '{}'", key, String.join(",", values))
        );

        // DEBUG: Log dos headers
        logger.info("[Webhook][Payment][DEBUG] x-signature: '{}'", signature);
        logger.info("[Webhook][Payment][DEBUG] x-request-id: '{}'", requestId);

        final String finalDataId = dataIdParam != null ? dataIdParam : idParam;

        if (finalDataId == null) {
            logger.error("[Webhook][Payment] ERROR: No 'data.id' or 'id' parameter found!");
            logger.error("[Webhook][Payment] Available params: {}", request.getParameterMap().keySet());
            return ResponseEntity.badRequest().body("Missing required parameter: data.id or id");
        }

        logger.info("[Webhook][Payment] Using dataId: '{}'", finalDataId);
        logger.info("[Webhook][Payment] Received payment notification: event={}, signature={}, requestId={}, dataId={}",
                webhookEvent, signature, requestId, finalDataId);

        processPaymentNotification.execute(webhookEvent, signature, requestId, finalDataId);
        return ResponseEntity.ok("Payment notification received successfully");
    }

}
