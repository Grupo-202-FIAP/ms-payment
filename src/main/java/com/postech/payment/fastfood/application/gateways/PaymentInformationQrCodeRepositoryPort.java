package com.postech.payment.fastfood.application.gateways;

import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;

public interface PaymentInformationQrCodeRepositoryPort {
    void save(QrCodeEntity paymentInformationQrCode);
}
