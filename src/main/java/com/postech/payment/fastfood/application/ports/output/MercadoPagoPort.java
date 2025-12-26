package com.postech.payment.fastfood.application.ports.output;


import com.postech.payment.fastfood.domain.model.OrderItem;
import com.postech.payment.fastfood.domain.model.Payment;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.request.GeneratedQrCodeResponse;
import java.util.List;

public interface MercadoPagoPort {
    GeneratedQrCodeResponse createQrCode(Payment payment, List<OrderItem> items);
}
