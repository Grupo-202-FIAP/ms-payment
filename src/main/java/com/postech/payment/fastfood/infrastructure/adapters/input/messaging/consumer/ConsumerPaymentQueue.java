package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;

import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.exception.DatabaseException;
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
    private final JsonConverter jsonConverter;
    private final GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;
    private final RollbackPaymentUseCase rollbackPayment;

    @SqsListener("${spring.cloud.aws.sqs.queues.process-payment-queue}")
    public void consumeMessage(String payload) {
        try {
            final var event = jsonConverter.toEventOrder(payload);

            switch (event.getStatus()) {
                case "SUCCESS":
                    generateQrCodePaymentUseCase.execute(event.getPayload(),event.getTransactionId());
                    break;
                case "ROLLBACK_PENDING":
                    rollbackPayment.execute(event.getOrderId());
                    break;
                default:
                    logger.warn(
                            "[CONSUMER][SQS] Unhandled payment status: {} for Order ID: {}. Ignoring message.",
                            event.getStatus(),
                            event.getOrderId());
                    break;
            }

        } catch (ConversionException e) {
            logger.error("[CONSUMER][SQS] Message ignored due to conversion error: {}", e.getMessage());
        } catch (PaymentIntegrationException e) {
            if (e.getMessage().contains("409") || e.getMessage().contains("idempotency")) {
                logger.warn(
                        "[CONSUMER][SQS] Conflito de idempotência detectado para a ordem. Removendo mensagem da fila para evitar loop infinito.");
            } else {
                logger.error("[CONSUMER][SQS] Erro de integração (Retry habilitado): {}", e.getMessage());
                throw e;
            }
        } catch (DatabaseException | MessagingException e) {
            logger.error("[CONSUMER][SQS] Erro de infraestrutura, tentará novamente: {}", e.getMessage());
            throw e;
        }
    }
}
