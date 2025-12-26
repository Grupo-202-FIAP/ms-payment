package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.exception.NotificationValidationException;
import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.gateways.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import com.postech.payment.fastfood.infrastructure.webhook.dao.WebhookEvent;
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
            logger.error("[Webhook] Signature header is missing");
            throw new NotificationValidationException("Signature header is missing");
        }
        /*
        if (!mercadoPagoWebhookSignatureValidator.verifySignatureOfProvider(signature, requestId, dataIdParam)) {
            logger.error("[Webhook] Invalid signature for webhook event: {}", event);
            throw new NotificationValidationException("Invalid signature for webhook event");
        }
        */


        final String externalReference = event.getData().getExternalReference();
        final Optional<Payment> paymentOptional = paymentRepositoryPort.findByOrderId(UUID.fromString(externalReference));

        if (paymentOptional.isEmpty()) {
            logger.warn("[Webhook] No payment found for Order ID: {}. Ignoring event.", externalReference);
            return;
        }

        final Payment payment = paymentOptional.get();

        switch (event.getAction()) {
            case "order.processed":
                handlePaymentSuccess(payment);
                break;
            case "order.expired":
                handlePaymentExpired(payment);
                break;
            default:
                logger.info("[Webhook] Action '{}' ignored for order: {}", event.getAction(), externalReference);
        }
    }

    private void handlePaymentSuccess(Payment payment) {
        logger.info("[Webhook] Processing success for order: {}", payment.getOrderId());
        payment.setStatus(PaymentStatus.PROCESSED);

        paymentRepositoryPort.save(payment);

        publishEventPaymentStatusPort.publish(buildEvent(payment));
    }

    private void handlePaymentExpired(Payment payment) {
        logger.info("[Webhook] Processing expiration for order: {}", payment.getOrderId());
        payment.setStatus(PaymentStatus.EXPIRED);

        paymentRepositoryPort.save(payment);

        publishEventPaymentStatusPort.publish(buildEvent(payment));
    }

    private EventPayment buildEvent(Payment payment) {
        return EventPayment.builder()
                .id(UUID.randomUUID())
                .source("payment-service")
                .status(payment.getStatus().name())
                .orderId(payment.getOrderId())
                .payload(payment)
                .createdAt(LocalDateTime.now())
                .build();
    }
}


