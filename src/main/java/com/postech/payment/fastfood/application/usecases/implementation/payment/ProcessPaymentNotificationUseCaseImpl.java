package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.gateways.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.exception.FastFoodException;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import com.postech.payment.fastfood.infrastructure.webhook.dao.WebhookEvent;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class ProcessPaymentNotificationUseCaseImpl implements ProcessPaymentNotificationUseCase {


    private final MercadoPagoWebhookSignatureValidator mercadoPagoWebhookSignatureValidator;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PublishEventPaymentStatusPort publishEventPaymentStatusPort;
    private final LoggerPort logger;

    public ProcessPaymentNotificationUseCaseImpl(
            MercadoPagoWebhookSignatureValidator mercadoPagoWebhookSignatureValidator,
            PaymentRepositoryPort paymentRepositoryPort,
            PublishEventPaymentStatusPort publishEventPaymentStatusPort,
            LoggerPort logger) {
        this.mercadoPagoWebhookSignatureValidator = mercadoPagoWebhookSignatureValidator;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.publishEventPaymentStatusPort = publishEventPaymentStatusPort;
        this.logger = logger;
    }

    @Override
    public void execute(WebhookEvent event, String signature, String requestId, String dataIdParam) {

        if (signature == null) {
            logger.error("[Webhook][Payment] Signature header is missing in the webhook event: {}", event);
            throw new FastFoodException("Signature header is missing in the webhook event", "Signature Header Missing", HttpStatus.BAD_REQUEST);
        }

//        if (!mercadoPagoWebhookSignatureValidator.verifySignatureOfProvider(signature, requestId, dataIdParam)) {
//            logger.error("[Webhook][Payment] Invalid signature for webhook event: {}", event);
//            throw new FastFoodException("Invalid signature for webhook event", "Invalid Signature", HttpStatus.UNAUTHORIZED);
//        }

        final Optional<Payment> payment = paymentRepositoryPort.findByOrderId(UUID.fromString(event.getData().getExternalReference()));

        if (payment.isEmpty()) {
            logger.warn("[Webhook][Payment] No order found for webhook event: {}", event);
            throw new FastFoodException("No order found for webhook event", "Order Not Found", HttpStatus.NOT_FOUND);
        }
        switch (event.getAction()) {
            case "order.processed":
                processOrderSucess(payment.get());
                break;
            case "order.expired":
                processOrderExpired(payment.get());
                break;
            default:
                logger.warn("[Webhook][Payment] Unknown action '{}' for webhook event: {}", event.getAction(), event);
        }
    }

    private void processOrderExpired(Payment expiredPayment) {
        logger.info("[Webhook][Payment] Payment expired for webhook order: {}", expiredPayment);
        expiredPayment.setStatus(PaymentStatus.EXPIRED);
        paymentRepositoryPort.save(expiredPayment);
        final EventPayment event = buildEvent(expiredPayment);
        publishEventPaymentStatusPort.publish(event);
        logger.info("[Webhook][Payment] Payment updated to cancelled status:: {}", expiredPayment.getId());
    }

    private void processOrderSucess(Payment payment) {
        payment.setStatus(PaymentStatus.PROCESSED);
        paymentRepositoryPort.save(payment);
        final EventPayment event = buildEvent(payment);
        publishEventPaymentStatusPort.publish(event);
        logger.info("[Webhook][Payment] Payment updated to authorized status: {}", payment);
    }

    private EventPayment buildEvent(Payment payment) {
        return EventPayment.builder()
                .id(UUID.randomUUID())
                .source("payment-service")
                .status("status")
                .orderId(payment.getOrderId())
                .payload(payment)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
