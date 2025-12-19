package com.postech.payment.fastfood.infrastructure.adapters.persistence;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.mapper.PaymentMapper;
import com.postech.payment.fastfood.domain.Payment;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.payment.IPaymentEntityRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final IPaymentEntityRepository paymentEntityRepository;
    private final LoggerPort logger;

    public PaymentRepositoryAdapter(IPaymentEntityRepository paymentEntityRepository, LoggerPort logger) {
        this.paymentEntityRepository = paymentEntityRepository;
        this.logger = logger;
    }

    public void save(Payment payment) {

        logger.info("[Repository][Payment] Processando pagamento para pedido id={}", payment.getOrderId());
        final PaymentEntity entity = PaymentMapper.toEntity(payment);
        this.paymentEntityRepository.save(entity);
    }


    private void validatePaymentStatus(PaymentEntity payment, UUID orderId) {
        /*
        logger.debug("[Repository][Payment] Validando status atual do pagamento para pedido id={}", orderId);

        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.APPROVED) {
            logger.warn("[Repository][Payment] Pagamento já processado anteriormente: status={}, orderId={}", payment.getStatus(), orderId);

            throw new FastFoodException("Payment already exists for Order ID: " + orderId, "Payment Already Exists", HttpStatus.CONFLICT);
        }
        
         */
    }
}
