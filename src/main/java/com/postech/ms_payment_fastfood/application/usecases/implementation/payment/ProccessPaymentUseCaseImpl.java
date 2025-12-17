package com.postech.ms_payment_fastfood.application.usecases.implementation.payment;


import com.postech.ms_payment_fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.ms_payment_fastfood.application.usecases.interfaces.payment.ProccessPaymentUseCase;
import com.postech.ms_payment_fastfood.domain.exception.FastFoodException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProccessPaymentUseCaseImpl implements ProccessPaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public ProccessPaymentUseCaseImpl(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public void execute(UUID orderId) {
        try {
            this.paymentRepositoryPort.save(orderId);
        } catch (FastFoodException e) {
            throw new FastFoodException(e.getMessage(), "Error realizing payment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
