package com.postech.payment.fastfood.application.gateways;

import com.postech.payment.fastfood.infrastructure.persistence.documents.PaymentInformationQrCodeDocument;

public interface PaymentInformationQrCodeRepositoryPort {
    void save(PaymentInformationQrCodeDocument paymentInformationQrCode);
}
