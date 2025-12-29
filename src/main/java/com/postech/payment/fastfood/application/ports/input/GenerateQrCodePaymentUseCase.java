package com.postech.payment.fastfood.application.ports.input;

import com.postech.payment.fastfood.domain.model.Order;

import java.util.UUID;

public interface GenerateQrCodePaymentUseCase {
    void execute(Order order, UUID transactionId);
}
