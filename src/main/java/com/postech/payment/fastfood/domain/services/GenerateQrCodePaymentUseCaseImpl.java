package com.postech.payment.fastfood.domain.services;


import com.postech.payment.fastfood.application.mapper.QrCodeMapper;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.input.messaging.dto.History;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GenerateQrCodePaymentUseCaseImpl implements GenerateQrCodePaymentUseCase {
    private final PaymentPort paymentPort;
    private final LoggerPort logger;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    public GenerateQrCodePaymentUseCaseImpl(
            PaymentPort paymentPort,
            LoggerPort logger,
            PaymentRepositoryPort paymentRepositoryPort,
            PublishEventPaymentStatusPort publishEventPaymentStatusPort
    ) {
        this.paymentPort = paymentPort;
        this.logger = logger;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.publishEventPaymentStatusPort = publishEventPaymentStatusPort;
    }

    @Override
    public void execute(Order order) {
        logger.info("[Payment][Messaging] Processing payment for order: {}", order.getId());
        findExistingQrCode(order.getId())
                .ifPresentOrElse(
                        this::handleExistingQrCode,
                        () -> createNewPayment(order)
                );
    }

    private void handleExistingQrCode(Payment payment) {
        if (payment.getQrCode().isExpired()) {
            logger.warn("[Payment][Messaging] QR Code expired for order: {}", payment.getOrderId());
            updatePaymentStatus(payment, PaymentStatus.CANCELLATION_REQUESTED);
            final EventPayment eventPayment = buildEvent(payment);
            publishEventPaymentStatusPort.publish(eventPayment);
        }
        logger.info("[Payment][Messaging] A valid QR Code already exists for order: {}", payment.getOrderId());
    }

    private void createNewPayment(Order order) {
        final Payment payment = buildInitialPayment(order);
        final GeneratedQrCodeResponse response = paymentPort.createQrCode(payment, order.getItems());

        if (response != null) {
            payment.setQrCode(QrCodeMapper.toDomain(response));
            savePayment(payment);
        } else {
            logger.warn("[PAYMENT][USECASE] Payment creation skipped for order {} due to integration conflict.", order.getId());
        }
    }

    private void updatePaymentStatus(Payment payment, PaymentStatus status) {
        payment.setStatus(status);
        paymentRepositoryPort.save(payment);
    }


    private Payment buildInitialPayment(Order order) {
        return new Payment.Builder()
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .amount(order.getTotalPrice())
                .orderId(order.getId())
                .build();
    }

    private Optional<Payment> findExistingQrCode(UUID orderId) {
        return paymentRepositoryPort.findByOrderId(orderId);
    }

    private void savePayment(Payment payment) {
        paymentRepositoryPort.save(payment);
    }

    private EventPayment buildEvent(Payment payment) {

        final History historyEntry = History.builder()
                .source("PAYMENT")
                .status(payment.getStatus().name())
                .message("Status atualizado para " + payment.getStatus().name())
                .createdAt(LocalDateTime.now())
                .build();

        return EventPayment.builder()
                .id(UUID.randomUUID())
                .source("PAYMENT")
                .status("ROLLBACK_PENDING")
                .orderId(payment.getOrderId())
                .payload(payment)
                .history(List.of(historyEntry))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
