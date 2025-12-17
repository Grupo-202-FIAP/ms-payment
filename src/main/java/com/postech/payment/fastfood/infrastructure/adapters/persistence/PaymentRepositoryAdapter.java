package com.postech.payment.fastfood.infrastructure.adapters.persistence;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.domain.enums.OrderStatus;
import com.postech.payment.fastfood.domain.enums.PaymentStatus;
import com.postech.payment.fastfood.domain.exception.FastFoodException;
import com.postech.payment.fastfood.infrastructure.persistence.entity.OrderEntity;
import com.postech.payment.fastfood.infrastructure.persistence.entity.PaymentEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.order.IOrderEntityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final IOrderEntityRepository orderEntityRepository;
    private final LoggerPort logger;

    public PaymentRepositoryAdapter(IOrderEntityRepository orderEntityRepository,
                                    LoggerPort logger) {
        this.orderEntityRepository = orderEntityRepository;
        this.logger = logger;
    }


    @Override
    public void save(UUID orderId) {
        logger.info("[Repository][Payment] Processando pagamento para pedido id={}", orderId);

        final OrderEntity orderEntity = getOrderById(orderId);

        validatePaymentStatus(orderEntity.getPayment(), orderId);
        orderEntity.setOrderStatus(OrderStatus.RECEIVED);
        orderEntity.getPayment().setStatus(PaymentStatus.AUTHORIZED);

        this.orderEntityRepository.save(orderEntity);

        logger.info("[Repository][Payment] Pagamento autorizado e status do pedido atualizado: id={}, status={}", orderId,
                orderEntity.getOrderStatus());
    }


    private OrderEntity getOrderById(UUID orderId) {
        logger.debug("[Repository][Payment] Buscando pedido por id={}", orderId);

        return orderEntityRepository.findById(orderId).orElseThrow(() -> {
            logger.warn("[Repository][Payment] Pedido não encontrado: id={}", orderId);

            return new FastFoodException("Order not found for ID: " + orderId, "Order Not Found", HttpStatus.NOT_FOUND);
        });
    }

    private void validatePaymentStatus(PaymentEntity payment, UUID orderId) {
        logger.debug("[Repository][Payment] Validando status atual do pagamento para pedido id={}", orderId);

        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.APPROVED) {
            logger.warn("[Repository][Payment] Pagamento já processado anteriormente: status={}, orderId={}", payment.getStatus(), orderId);

            throw new FastFoodException("Payment already exists for Order ID: " + orderId, "Payment Already Exists", HttpStatus.CONFLICT);
        }
    }
}
