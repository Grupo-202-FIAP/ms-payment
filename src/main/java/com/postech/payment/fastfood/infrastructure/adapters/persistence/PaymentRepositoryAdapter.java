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
    public Payment save(Payment payment) {
        logger.info("[Repository] Salvando pagamento para o pedido: {}", payment.getOrderId());
        try {
            final PaymentEntity entity = PaymentMapper.toEntity(payment);
            bindRelations(entity);
            final PaymentEntity saved = paymentEntityRepository.saveAndFlush(entity);
            return PaymentMapper.toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            logger.error("Este pagamento já foi processado ou viola regras de integridade.");
            throw new DatabaseException("Falha ao salvar pagamento", e);
        } catch (DataAccessException e) {
            logger.error("Erro genérico de banco de dados");
            throw new DatabaseException("Falha ao salvar pagamento", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(UUID orderId) {
        logger.debug("[Repository] Buscando pagamento por OrderId: {}", orderId);
        final Optional<PaymentEntity> byOrderId = paymentEntityRepository.findByOrderId(orderId);
        return byOrderId.map(PaymentMapper::toDomain);
    }

    private void bindRelations(PaymentEntity entity) {
        if (entity.getQrCode() != null) {
            entity.getQrCode().setPayment(entity);
        }
    }

}
