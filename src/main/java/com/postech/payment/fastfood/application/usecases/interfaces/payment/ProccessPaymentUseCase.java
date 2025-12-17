package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import java.util.UUID;

public interface ProccessPaymentUseCase {
    void execute(UUID order);
}
