package com.postech.ms_payment_fastfood.application.usecases.interfaces.payment;

import java.util.UUID;

public interface ProccessPaymentUseCase {
    void execute(UUID order);
}
