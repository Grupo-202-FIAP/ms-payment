package com.postech.payment.fastfood.domain.services;

import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;

import java.util.UUID;

public class RollbackPaymentUseCaseImpl implements RollbackPaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;

    public RollbackPaymentUseCaseImpl(PaymentRepositoryPort paymentRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
    }


    @Override
    public void execute(UUID orderID) {
        paymentRepositoryPort.delete(orderID);
    }
}
