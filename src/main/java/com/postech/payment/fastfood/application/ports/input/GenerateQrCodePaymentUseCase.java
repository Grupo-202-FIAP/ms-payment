package com.postech.payment.fastfood.application.ports.input;

import com.postech.payment.fastfood.domain.model.Order;

public interface GenerateQrCodePaymentUseCase {
    void execute(Order order);
}
