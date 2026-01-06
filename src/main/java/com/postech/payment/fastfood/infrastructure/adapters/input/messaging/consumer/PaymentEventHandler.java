package com.postech.payment.fastfood.infrastructure.adapters.input.messaging.consumer;

import static com.postech.payment.fastfood.domain.enums.EventSource.PAYMENT;

import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.exception.PaymentEventNotSupportedException;
import com.postech.payment.fastfood.application.exception.PaymentIntegrationException;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.SagaStatus;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.EventOrder;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class PaymentEventHandler {

    private final LoggerPort logger;
    private final GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;
    private final RollbackPaymentUseCase rollbackPaymentUseCase;
    private final PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    public PaymentEventHandler(LoggerPort logger,
                               GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase,
                               RollbackPaymentUseCase rollbackPaymentUseCase,
                               PublishEventPaymentStatusPort publishEventPaymentStatusPort) {
        this.logger = logger;
        this.generateQrCodePaymentUseCase = generateQrCodePaymentUseCase;
        this.rollbackPaymentUseCase = rollbackPaymentUseCase;
        this.publishEventPaymentStatusPort = publishEventPaymentStatusPort;
    }

    public boolean handle(EventOrder event) {
        try {
            switch (SagaStatus.valueOf(event.getStatus())) {
                case SUCCESS -> handleSuccess(event);
                case FAIL -> handleRollback(event);
                default -> throw new PaymentEventNotSupportedException(event.getStatus());
            }
            return true;
        } catch (PaymentEventNotSupportedException ex) {
            logger.warn("[PaymentEventHandler] Evento ignorado: {}", ex.getMessage());
            throw ex;
        } catch (DatabaseException ex) {
            logger.error(
                    "[PaymentEventHandler] Erro de banco de dados ao processar evento, iniciando rollback. transactionId={}",
                    event.getTransactionId(), ex);
            publishRollback(event, ex.getMessage());
            return false;
        } catch (PaymentIntegrationException ex) {
            logger.error(
                    "[PaymentEventHandler] Erro de integração ao processar evento, iniciando rollback. transactionId={}",
                    event.getTransactionId(), ex);
            publishRollback(event, ex.getMessage());
            return false;
        } catch (MessagingException ex) {
            logger.error(
                    "[PaymentEventHandler] Erro de mensageria ao processar evento, iniciando rollback. transactionId={}",
                    event.getTransactionId(), ex);
            publishRollback(event, ex.getMessage());
            return false;
        } catch (IllegalArgumentException ex) {
            logger.error(
                    "[PaymentEventHandler] Status de evento inválido, iniciando rollback. transactionId={}",
                    event.getTransactionId(), ex);
            publishRollback(event, ex.getMessage());
            return false;
        }
    }



    private void handleSuccess(EventOrder event) {
        final var order = event.getPayload();
        logger.info("[PaymentEventHandler] Processando pagamento para pedido: {}", order.getId());
        generateQrCodePaymentUseCase.execute(order, event.getTransactionId());
        logger.info("[PaymentEventHandler] QR Code gerado com sucesso para pedido: {}", order.getId());
    }

    private void handleRollback(EventOrder event) {
        logger.info("[PaymentEventHandler] Executando rollback do pagamento. orderId={}", event.getOrderId());
        rollbackPaymentUseCase.execute(event.getOrderId());
        logger.info("[PaymentEventHandler] Rollback do pagamento concluído. orderId={}", event.getOrderId());
    }

    private void publishRollback(EventOrder event, String reason) {
        final var rollbackEvent = buildRollbackEvent(event, reason);
        publishEventPaymentStatusPort.publish(rollbackEvent);
        logger.info("[PaymentEventHandler] Rollback publicado com sucesso. transactionId={}", event.getTransactionId());
    }

    private EventPayment buildRollbackEvent(EventOrder event, String reason) {
        final var history = event.getHistory() != null ? new ArrayList<>(event.getHistory()) : new ArrayList<History>();

        history.add(History.builder()
                .source(PAYMENT.getSource())
                .status(SagaStatus.ROLLBACK_PENDING.name())
                .message("Rollback iniciado: " + reason)
                .createdAt(LocalDateTime.now())
                .build());

        return EventPayment.builder()
                .id(UUID.randomUUID())
                .transactionId(event.getTransactionId())
                .orderId(event.getOrderId())
                .source(PAYMENT.getSource())
                .status(SagaStatus.ROLLBACK_PENDING.name())
                .history(history)
                .createdAt(LocalDateTime.now())
                .build();
    }


}
