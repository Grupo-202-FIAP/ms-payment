package com.postech.payment.fastfood.application.ports.output;

import com.postech.payment.fastfood.domain.model.QrCode;

public interface PaymentInformationQrCodeRepositoryPort {
    void save(QrCode paymentInformationQrCode);
}
