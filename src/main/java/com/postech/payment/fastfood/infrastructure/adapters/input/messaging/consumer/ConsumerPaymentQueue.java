package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.utils.JsonConverter;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.AllArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConsumerPaymentQueue {

    private final LoggerPort logger;
    private final PaymentEventHandler paymentEventHandler;
    private final JsonConverter jsonConverter;

    @SqsListener("${spring.cloud.aws.sqs.queues.process-payment-queue}")
    public void consumeMessage(String payload) {
        try {
            logger.info("[PaymentQueueListener] Mensagem recebida");
            final var event = jsonConverter.toEventOrder(payload);
            paymentEventHandler.handle(event);
        } catch (PaymentEventNotSupportedException ex) {
            logger.warn("[PaymentQueueListener] Evento ignorado por regra de negocio: {}", ex.getMessage());
        } catch (Exception ex) {
            logger.error("[PaymentQueueListener] Erro processado e rollback publicado. Mensagem será removida da fila.", ex);
        }
    }

}
