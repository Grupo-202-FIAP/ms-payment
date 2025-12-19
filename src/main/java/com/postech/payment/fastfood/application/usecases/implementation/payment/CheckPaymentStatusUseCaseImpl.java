package com.postech.payment.fastfood.application.usecases.implementation.payment;

import com.postech.payment.fastfood.application.usecases.interfaces.payment.CheckPaymentStatusUseCase;
import java.util.UUID;


public class CheckPaymentStatusUseCaseImpl implements CheckPaymentStatusUseCase {
    /*
    private final OrderRepositoryPort orderRepositoryPort;

    public CheckPaymentStatusUseCaseImpl(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }
    */


    @Override
    public String execute(UUID orderId) {
        /*
        final PaymentStatus status = orderRepositoryPort.findById(orderId).getPayment().getStatus();
        return status.toString();
         */

        return "NOT_IMPLEMENTED";
    }
}

