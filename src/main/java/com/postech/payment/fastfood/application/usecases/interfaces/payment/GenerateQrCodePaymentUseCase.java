package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import com.postech.payment.fastfood.domain.Order;

public interface GenerateQrCodePaymentUseCase {
    void execute(Order order);
}
