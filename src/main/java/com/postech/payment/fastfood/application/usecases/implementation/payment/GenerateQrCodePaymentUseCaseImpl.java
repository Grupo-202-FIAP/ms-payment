package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.mapper.QrCodeMapper;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.MercadoPagoPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.application.usecases.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.model.Order;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class GenerateQrCodePaymentUseCaseImpl implements GenerateQrCodePaymentUseCase {
    private final MercadoPagoPort mercadoPagoPort;
    private final LoggerPort logger;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final PublishEventPaymentStatusPort publishEventPaymentStatusPort;

    public GenerateQrCodePaymentUseCaseImpl(
            MercadoPagoPort mercadoPagoPort,
            LoggerPort logger,
            PaymentRepositoryPort paymentRepositoryPort,
            PublishEventPaymentStatusPort publishEventPaymentStatusPort
    ) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.logger = logger;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.publishEventPaymentStatusPort = publishEventPaymentStatusPort;
    }

    @Override
    public void execute(Order order) {
        logger.info("[PAYMENT][SQS] Processing SQS payment for order: {}", order.getId());
        findExistingQrCode(order.getId())
                .ifPresentOrElse(
                        this::handleExistingQrCode,
                        () -> createNewPayment(order)
                );
    }

    private void handleExistingQrCode(Payment payment) {
        if (isExpired(payment.getQrCode())) {
            logger.warn("[PAYMENT][SQS] QR Code expired for order: {}", payment.getOrderId());
            updatePaymentStatus(payment, PaymentStatus.EXPIRED);
            final EventPayment eventPayment = buildEvent(payment);
            publishEventPaymentStatusPort.publish(eventPayment);
        }
        logger.info("[PAYMENT][SQS] A valid QR Code already exists for order: {}", payment.getOrderId());
    }

    private void createNewPayment(Order order) {
        final Payment payment = buildInitialPayment(order);
        final GeneratedQrCodeResponse response = mercadoPagoPort.createQrCode(payment, order.getItems());
        payment.setQrCode(QrCodeMapper.toDomain(response));
        savePayment(payment);
    }

    private void updatePaymentStatus(Payment payment, PaymentStatus status) {
        payment.setStatus(status);
        paymentRepositoryPort.save(payment);
    }

    private boolean isExpired(QrCode qrCode) {
        return qrCode.getExpiresAt() != null && qrCode.getExpiresAt().isBefore(OffsetDateTime.now());
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
