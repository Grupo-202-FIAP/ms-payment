package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;


import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import com.postech.payment.fastfood.application.exception.ConversionException;
import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.utils.JsonConverter;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ConsumerPaymentQueue {

    private final LoggerPort logger;
    private final PaymentEventHandler paymentEventHandler;
    private final JsonConverter jsonConverter;

    @SqsListener("${spring.sqs.queues.payment-queue}")
    public void consumeMessage(String payload) {
        try {
            logger.info("[PaymentQueueListener] Mensagem recebida");
            final var event = jsonConverter.toEventOrder(payload);
            paymentEventHandler.handle(event);
        } catch (PaymentEventNotSupportedException ex) {
            logger.warn("[PaymentQueueListener] Evento ignorado por regra de negocio: {}", ex.getMessage());
        } catch (ConversionException ex) {
            logger.error("[PaymentQueueListener] Erro ao converter mensagem. Mensagem será removida da fila.", ex);
        } catch (DatabaseException ex) {
            logger.error("[PaymentQueueListener] Erro de banco de dados. Rollback publicado. Mensagem será removida da fila.", ex);
        } catch (PaymentIntegrationException ex) {
            logger.error("[PaymentQueueListener] Erro de integração com pagamento. Rollback publicado. Mensagem será removida da fila.", ex);
        } catch (MessagingException ex) {
            logger.error("[PaymentQueueListener] Erro de mensageria. Rollback publicado. Mensagem será removida da fila.", ex);
        }
    }

}
