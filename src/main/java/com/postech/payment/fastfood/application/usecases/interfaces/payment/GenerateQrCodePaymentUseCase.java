package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import com.postech.payment.fastfood.infrastructure.controller.dto.request.OrderRequest;

public interface GenerateQrCodePaymentUseCase {
    String execute(OrderRequest order);
}
