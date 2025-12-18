package com.postech.payment.fastfood.application.gateways;


import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;

public interface MercadoPagoPort {
    GenerateQrCodeResult createQrCode(Order order);
}
