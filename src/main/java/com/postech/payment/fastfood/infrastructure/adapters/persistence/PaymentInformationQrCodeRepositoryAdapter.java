package com.postech.payment.fastfood.infrastructure.adapters.persistence;

import com.postech.payment.fastfood.application.gateways.PaymentInformationQrCodeRepositoryPort;
import com.postech.payment.fastfood.infrastructure.persistence.documents.PaymentInformationQrCodeDocument;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentInformationQrCodeRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentInformationQrCodeRepositoryAdapter implements PaymentInformationQrCodeRepositoryPort {

    private final IPaymentInformationQrCodeRepository paymentInformationQrCodeRepositoryAdapter;

    public PaymentInformationQrCodeRepositoryAdapter(IPaymentInformationQrCodeRepository paymentInformationQrCodeRepositoryAdapter) {
        this.paymentInformationQrCodeRepositoryAdapter = paymentInformationQrCodeRepositoryAdapter;
    }

    @Override
    public void save(PaymentInformationQrCodeDocument paymentInformationQrCode) {
        paymentInformationQrCodeRepositoryAdapter.save(paymentInformationQrCode);
    }
}
