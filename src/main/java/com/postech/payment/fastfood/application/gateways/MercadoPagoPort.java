package com.postech.payment.fastfood.application.gateways;


import com.postech.payment.fastfood.domain.OrderItem;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
import java.util.List;

public interface MercadoPagoPort {
    GeneratedQrCodeResponse createQrCode(Payment payment, List<OrderItem> items);
}
