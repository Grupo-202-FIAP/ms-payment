package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.exception.FastFoodException;
import com.postech.payment.fastfood.application.ports.input.FindPaymentByOrderIdUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.domain.model.Payment;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.UUID;

public class FindPaymentByOrderIdUseCaseImpl implements FindPaymentByOrderIdUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final LoggerPort loggerPort;

    public FindPaymentByOrderIdUseCaseImpl(PaymentRepositoryPort paymentRepositoryPort, LoggerPort loggerPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.loggerPort = loggerPort;
    }

    @Override
    public Payment findPaymentByOrderId(UUID orderId) {
        loggerPort.debug("[UseCase][FindPaymentByOrderId] Finding payment for orderId={}", orderId);
        final Optional<Payment> byOrderId = paymentRepositoryPort.findByOrderId(orderId);
        return byOrderId.orElseThrow(() -> new FastFoodException(
                "Payment not found for orderId: " + orderId,
                "Payment Not Found",
                HttpStatus.NOT_FOUND
        ));
    }
}
