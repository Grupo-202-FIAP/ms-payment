package com.postech.ms_payment_fastfood.application.usecases.interfaces.payment;

import com.postech.ms_payment_fastfood.infrastructure.controller.dto.request.OrderRequest;

public interface GenerateQrCodePaymentUseCase {
    String execute(OrderRequest order);
}
