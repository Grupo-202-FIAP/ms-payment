package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import com.postech.payment.fastfood.infrastructure.controller.dto.request.OrderRequest;
import com.postech.payment.fastfood.infrastructure.controller.dto.response.mercadopago.OrderResponse;

public interface GenerateQrCodePaymentUseCase {
    OrderResponse execute(OrderRequest order);
}
