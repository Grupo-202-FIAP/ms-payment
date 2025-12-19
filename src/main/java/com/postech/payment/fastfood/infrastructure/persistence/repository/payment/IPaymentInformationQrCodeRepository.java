package com.postech.payment.fastfood.infrastructure.persistence.repository.payment;


import com.postech.payment.fastfood.infrastructure.persistence.documents.PaymentInformationQrCodeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IPaymentInformationQrCodeRepository extends MongoRepository<PaymentInformationQrCodeDocument, String> {
}
