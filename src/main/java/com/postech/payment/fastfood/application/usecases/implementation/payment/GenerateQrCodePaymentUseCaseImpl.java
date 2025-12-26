package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.gateways.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.application.mapper.QrCodeMapper;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.QrCode;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.infrastructure.adapters.messaging.dto.EventPayment;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;

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
        logger.info("[Payment] Processando pagamento SQS para o pedido: {}", order.getId());

        findExistingQrCode(order.getId())
                .ifPresentOrElse(
                        this::handleExistingQrCode,
                        () -> createNewPayment(order)
                );
    }

    private void handleExistingQrCode(Payment payment) {
        if (isExpired(payment.getQrCode())) {
            logger.warn("[Payment] QR Code expirado para o pedido: {}", payment.getOrderId());
            updatePaymentStatus(payment, PaymentStatus.EXPIRED);
            EventPayment eventPayment = buildEvent(payment);
            publishEventPaymentStatusPort.publish(eventPayment);
        }
        logger.info("[Payment] QR Code válido já existe para o pedido: {}", payment.getOrderId());
    }

    private void createNewPayment(Order order) {
        try {
            Payment payment = buildInitialPayment(order);
            GeneratedQrCodeResponse response = mercadoPagoPort.createQrCode(payment, order.getItems());
            payment.setQrCode(QrCodeMapper.toDomain(response));
            savePayment(payment);
        } catch (Exception e) {
            logger.error("[Payment] Falha na integração para o pedido: {}", order.getId());
            throw new RuntimeException("Falha ao gerar QR Code externo", e);
        }
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
        try {
            paymentRepositoryPort.save(payment);
        } catch (Exception e) {
            logger.error("[Payment] Erro de banco para o pedido: {}", payment.getOrderId());
            throw new RuntimeException("Erro de persistência de dados", e);
        }
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