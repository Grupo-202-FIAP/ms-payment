package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;

public interface GenerateQrCodePaymentUseCase {
    GenerateQrCodeResult execute(Order order);
}
