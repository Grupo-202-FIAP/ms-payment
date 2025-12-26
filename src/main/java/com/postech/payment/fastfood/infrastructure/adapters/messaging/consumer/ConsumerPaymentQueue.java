package com.postech.payment.fastfood.infrastructure.adapters.messaging.consumer;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.utils.JsonConverter;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.AllArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConsumerPaymentQueue {

    private final LoggerPort logger;
    private final JsonConverter jsonConverter;
    private final GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;

    @SqsListener("${spring.cloud.aws.sqs.queues.process-payment-queue}")
    //payment-queue
    public void consumeMessage(String payload) {
        try {
            logger.info("[consumeMessage] Consumindo mensagem da fila de payment-queue");
            
            final var event = jsonConverter.toEventOrder(payload);

            generateQrCodePaymentUseCase.execute(event.getPayload());

            logger.info("[consumeMessage] Mensagem processada: {}", event.getId());

        } catch (ConversionException e) {
            logger.error("[consumeMessage] Mensagem ignorada por erro de conversão: {}", e.getMessage());

        } catch (DatabaseException | PaymentIntegrationException | MessagingException e) {
            logger.error("[consumeMessage] Erro processável, tentando novamente: {}", e.getMessage());
            throw e;
        }
    }
}
