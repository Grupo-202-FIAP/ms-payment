package com.postech.payment.fastfood.infrastructure.adapters.output.persistence;

import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentInformationQrCodeRepositoryPort;
import com.postech.payment.fastfood.domain.model.QrCode;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import com.postech.payment.fastfood.infrastructure.persistence.mapper.QrCodeMapperEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentInformationQrCodeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentInformationQrCodeRepositoryAdapter implements PaymentInformationQrCodeRepositoryPort {

    private final IPaymentInformationQrCodeRepository qrCodeRepository;
    private final LoggerPort logger; // Injetando a abstração de log do seu sistema

    public PaymentInformationQrCodeRepositoryAdapter(IPaymentInformationQrCodeRepository qrCodeRepository, LoggerPort logger) {
        this.qrCodeRepository = qrCodeRepository;
        this.logger = logger;
    }

    @Override
    @Transactional
    public void save(QrCode qrCode) {
        logger.info("[REPOSITORY] Persisting QR Code information for order: {}", qrCode.getOrderId());
        try {
            final QrCodeEntity entity = QrCodeMapperEntity.toEntity(qrCode);
            qrCodeRepository.save(entity);
        } catch (DataAccessException e) {
            logger.error("[REPOSITORY] Error saving QR Code information for order: {}", qrCode.getOrderId(), e);
            throw new DatabaseException("Error persisting QR Code information", e);
        }
    }
}
