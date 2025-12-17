package com.postech.ms_payment_fastfood.application.usecases.interfaces.payment;

import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public interface CheckPaymentStatusUseCase {
    String execute(@NotEmpty UUID orderId);
}

