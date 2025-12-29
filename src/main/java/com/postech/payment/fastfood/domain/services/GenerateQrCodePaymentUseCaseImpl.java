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
import com.postech.payment.fastfood.infrastructure.adapters.output.messaging.dto.EventPayment;
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
    public void execute(Order order, UUID transactionId) {
        logger.info("[Payment][Messaging] Processing payment for order: {}", order.getId());
        Optional<Payment> existingQrCode = findExistingQrCode(order.getId());
        if (existingQrCode.isPresent()) {
            handleExistingQrCode(existingQrCode.get(),transactionId);
        } else {
            createNewPayment(order,transactionId);
        }
    }

    private void handleExistingQrCode(Payment payment, UUID transactionId) {
        if (payment.getQrCode().isExpired()) {
            logger.warn("[Payment][Messaging] QR Code expired for order: {}", payment.getOrderId());
            updatePaymentStatus(payment);
            final EventPayment eventPayment = new EventPayment().eventExpiring(payment, transactionId);
            publishEventPaymentStatusPort.publish(eventPayment);
        }
        logger.info("[Payment][Messaging] A valid QR Code already exists for order: {}", payment.getOrderId());
    }

    private void createNewPayment(Order order, UUID transactionId) {
        final Payment payment = buildInitialPayment(order,transactionId);
        final GeneratedQrCodeResponse response = paymentPort.createQrCode(payment, order.getItems());

        if (response != null) {
            payment.setQrCode(QrCodeMapper.toDomain(response));
            savePayment(payment);
        } else {
            logger.warn("[PAYMENT][USECASE] Payment creation skipped for order {} due to integration conflict.", order.getId());
        }
    }

    private void updatePaymentStatus(Payment payment) {
        payment.setStatus(PaymentStatus.EXPIRING);
        paymentRepositoryPort.save(payment);
    }


    private Payment buildInitialPayment(Order order,UUID transactionId) {
        return new Payment.Builder()
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.QR_CODE)
                .amount(order.getTotalPrice())
                .orderId(order.getId())
                .transactionId(transactionId)
                .build();
    }

    private Optional<Payment> findExistingQrCode(UUID orderId) {
        return paymentRepositoryPort.findByOrderId(orderId);
    }

    private void savePayment(Payment payment) {
        paymentRepositoryPort.save(payment);
    }


}
