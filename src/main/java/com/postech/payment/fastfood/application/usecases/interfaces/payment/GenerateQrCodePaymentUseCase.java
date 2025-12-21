package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
import java.util.List;

public interface GenerateQrCodePaymentUseCase {
    GeneratedQrCodeResponse execute(Payment payment, List<OrderItem> itens);
}
