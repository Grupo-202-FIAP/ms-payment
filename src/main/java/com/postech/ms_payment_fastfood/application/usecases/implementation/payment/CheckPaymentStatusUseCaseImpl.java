package com.postech.ms_payment_fastfood.application.usecases.implementation.payment;

import com.postech.ms_payment_fastfood.application.gateways.OrderRepositoryPort;
import com.postech.ms_payment_fastfood.application.usecases.interfaces.payment.CheckPaymentStatusUseCase;
import com.postech.ms_payment_fastfood.domain.enums.PaymentStatus;

import java.util.UUID;


public class CheckPaymentStatusUseCaseImpl implements CheckPaymentStatusUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public CheckPaymentStatusUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public String execute(UUID orderId) {
        final PaymentStatus status = orderRepositoryPort.findById(orderId).getPayment().getStatus();
        return status.toString();
    }
}
