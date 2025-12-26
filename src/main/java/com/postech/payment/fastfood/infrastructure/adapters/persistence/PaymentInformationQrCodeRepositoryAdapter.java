package com.postech.payment.fastfood.infrastructure.adapters.persistence;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PaymentInformationQrCodeRepositoryPort;
import com.postech.payment.fastfood.application.mapper.QrCodeMapper;
import com.postech.payment.fastfood.domain.QrCode;
import com.postech.payment.fastfood.infrastructure.persistence.entity.QrCodeEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentInformationQrCodeRepository;
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
        logger.info("[Repository] Persistindo informações do QR Code para o pedido: {}", qrCode.getOrderId());

        try {
            QrCodeEntity entity = QrCodeMapper.toEntity(qrCode);

            qrCodeRepository.save(entity);
        } catch (Exception e) {
            logger.error("[Repository] Erro ao salvar informações do QR Code para o pedido: {}", qrCode.getOrderId(), e);
            throw e;
        }
    }
}
