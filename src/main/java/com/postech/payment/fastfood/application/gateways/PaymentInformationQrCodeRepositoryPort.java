package com.postech.payment.fastfood.application.gateways;

import com.postech.payment.fastfood.domain.QrCode;

public interface PaymentInformationQrCodeRepositoryPort {
    void save(QrCode paymentInformationQrCode);
}
