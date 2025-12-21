package com.postech.payment.fastfood.application.usecases.implementation.payment;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.mapper.QrCodeMapper;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.domain.QrCode;
import com.postech.payment.fastfood.domain.enums.PaymentMethod;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.exception.FastFoodException;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
import java.util.List;
import java.util.UUID;


public class GenerateQrCodePaymentUseCaseImpl implements GenerateQrCodePaymentUseCase {
    private final MercadoPagoPort mercadoPagoPort;
    private final LoggerPort logger;
    private final PaymentRepositoryPort paymentRepositoryPort;

    public GenerateQrCodePaymentUseCaseImpl(MercadoPagoPort mercadoPagoPort, LoggerPort logger, PaymentRepositoryPort paymentRepositoryPort) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.logger = logger;
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    public GeneratedQrCodeResponse execute(Payment payment, List<OrderItem> itens) {
        final QrCode existingQrCode = findExistingQrCode(payment);
        if (existingQrCode != null) {
            logger.info("[Service][Payment] QR Code já existe para o pedido: {}", payment.getOrderId());
            checkExpirationQrCode(existingQrCode);
            return QrCodeMapper.toResponse(existingQrCode);
        }
        logger.info("[Service][Payment] Criando Order no MercadoPago para o pedido: {}", payment.getOrderId());
        payment.setStatus(PaymentStatus.UNKNOWN);
        payment.setPaymentMethod(PaymentMethod.QR_CODE);
        final GeneratedQrCodeResponse qrCode = mercadoPagoPort.createQrCode(payment, itens);
        payment.setQrCode(QrCodeMapper.toDomain(qrCode));
        this.paymentRepositoryPort.save(payment);
        return qrCode;
    }

    private void checkExpirationQrCode(QrCode qrCode) {
        if (qrCode.getExpiresAt() != null && qrCode.getExpiresAt().isBefore(java.time.OffsetDateTime.now())) {
            logger.info("[Service][Payment] QR Code expirado para o pedido: {}", qrCode.getPayment().getOrderId());

            qrCode.getPayment().setStatus(PaymentStatus.EXPIRED);

            paymentRepositoryPort.save(qrCode.getPayment());

            throw new FastFoodException("QR Code expirado para o pedido: " + qrCode.getOrderId(),
                    "QR Code Expirado",
                    org.springframework.http.HttpStatus.GONE);
        }
        logger.info("[Service][Payment] QR Code Não Expirado: {}", qrCode.getOrderId());
    }

    private QrCode findExistingQrCode(Payment payment) {
        final UUID orderId = payment.getOrderId();
        logger.debug("[Service][Payment] Verificando se já existe um QR Code para o pedido id={}", orderId);
        return paymentRepositoryPort.findByOrderId(orderId)
                .map(Payment::getQrCode)
                .orElse(null);
    }
}
