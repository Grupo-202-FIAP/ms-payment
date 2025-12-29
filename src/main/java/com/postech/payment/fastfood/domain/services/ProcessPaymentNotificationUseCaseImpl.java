package com.postech.payment.fastfood.domain.services;


import com.postech.payment.fastfood.application.exception.NotificationValidationException;
import com.postech.payment.fastfood.application.ports.input.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.webhook.mercadopago.dao.WebhookEvent;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
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
            logger.error("[WEBHOOK] Signature header is missing");
            throw new NotificationValidationException("Signature header is missing");
        }
        /*
        if (!mercadoPagoWebhookSignatureValidator.verifySignatureOfProvider(signature, requestId, dataIdParam)) {
            logger.error("[WEBHOOK] Invalid signature for webhook event: {}", event);
            throw new NotificationValidationException("Invalid signature for webhook event");
        }
        */


        final String externalReference = event.getData().getExternalReference();
        final Optional<Payment> paymentOptional = paymentRepositoryPort.findByOrderId(UUID.fromString(externalReference));

        if (paymentOptional.isEmpty()) {
            logger.warn("[WEBHOOK] No payment found for Order ID: {}. Ignoring event.", externalReference);
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
                logger.info("[WEBHOOK] Action '{}' ignored for order: {}", event.getAction(), externalReference);
        }
    }

    private void handlePaymentSuccess(Payment payment) {
        logger.info("[WEBHOOK] Processing success for order: {}", payment.getOrderId());
        payment.setStatus(PaymentStatus.PROCESSED);
        paymentRepositoryPort.save(payment);
        final EventPayment eventPayment = new EventPayment().eventSuccess(payment);
        publishEventPaymentStatusPort.publish(eventPayment);
    }

    private void handlePaymentExpired(Payment payment) {
        logger.info("[WEBHOOK] Processing expiration for order: {}", payment.getOrderId());
        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepositoryPort.save(payment);
        final EventPayment eventPayment = new EventPayment().eventExpired(payment);
        publishEventPaymentStatusPort.publish(eventPayment);
    }


}


