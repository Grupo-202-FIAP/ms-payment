package com.postech.payment.fastfood.infrastructure.adapters.persistence;


import com.postech.payment.fastfood.application.exception.DatabaseException;
import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.mapper.PaymentMapper;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final IPaymentEntityRepository paymentEntityRepository;
    private final LoggerPort logger;

    public PaymentRepositoryAdapter(IPaymentEntityRepository paymentEntityRepository, LoggerPort logger) {
        this.paymentEntityRepository = paymentEntityRepository;
        this.logger = logger;
    }

    @Override
    @Transactional
    public void save(Payment payment) {
        logger.info("[REPOSITORY][PAYMENT] Saving payment for order: {}", payment.getOrderId());
        try {
            final PaymentEntity entity = PaymentMapper.toEntity(payment);
            bindRelations(entity);
            paymentEntityRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            logger.error("[REPOSITORY][PAYMENT] This payment has already been processed or violates integrity rules.");
            throw new DatabaseException("Failed to save payment", e);
        } catch (DataAccessException e) {
            logger.error("[REPOSITORY][PAYMENT] Generic database error");
            throw new DatabaseException("Failed to save payment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(UUID orderId) {
        logger.debug("[REPOSITORY][PAYMENT] Searching payment by OrderId: {}", orderId);
        final Optional<PaymentEntity> byOrderId = paymentEntityRepository.findByOrderId(orderId);
        return byOrderId.map(PaymentMapper::toDomain);
    }

    private void bindRelations(PaymentEntity entity) {
        if (entity.getQrCode() != null) {
            entity.getQrCode().setPayment(entity);
        }
    }

}
