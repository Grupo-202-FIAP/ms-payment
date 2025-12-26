package com.postech.payment.fastfood.infrastructure.adapters.messaging.consumer;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.utils.JsonConverter;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.AllArgsConstructor;
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
            logger.info("[consumeMessage] Consumindo mensagem da fila de payment-queue: ");
            var event = jsonConverter.toEventOrder(payload);
            generateQrCodePaymentUseCase.execute(event.getPayload());
            logger.info("[consumeMessage] Mensagem consumida da fila de payment-queue: {}", event.getId());
        } catch (Exception e) {
            logger.error("[consumeMessage] Erro ao consumir mensagem da fila de payment-queue com a mensagem: {}", e.getMessage());

        }
    }

}
