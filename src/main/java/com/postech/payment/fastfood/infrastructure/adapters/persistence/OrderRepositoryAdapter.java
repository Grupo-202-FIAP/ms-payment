package com.postech.payment.fastfood.infrastructure.adapters.persistence;


import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.OrderRepositoryPort;
import com.postech.payment.fastfood.application.mapper.OrderMapper;
import com.postech.payment.fastfood.domain.Order;
import com.postech.payment.fastfood.domain.exception.FastFoodException;
import com.postech.payment.fastfood.infrastructure.persistence.entity.OrderEntity;
import com.postech.payment.fastfood.infrastructure.persistence.repository.order.IOrderEntityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final IOrderEntityRepository orderEntityRepository;
    private final LoggerPort logger;

    public OrderRepositoryAdapter(IOrderEntityRepository orderEntityRepository, LoggerPort logger) {
        this.orderEntityRepository = orderEntityRepository;
        this.logger = logger;
    }

    @Override
    public Order findById(UUID orderId) {
        logger.info("[Repository][Order] Buscando pedido por id={}", orderId);

        Order order = null;
        final OrderEntity orderEntity = this.orderEntityRepository.findById(orderId).orElseThrow(() -> {
            logger.warn("[Repository][Order] Pedido não encontrado: id={}", orderId);

            return new FastFoodException("Order not found with id:" + orderId, "Order Not Found", HttpStatus.NOT_FOUND);
        });
        order = OrderMapper.toDomain(orderEntity);
        return order;
    }
    
    @Override
    public Order findByIdentifier(String externalReference) {
        logger.info("[Repository][Order] Buscando pedido por identificador={}", externalReference);

        final OrderEntity orderEntity = this.orderEntityRepository.findByIdentifier(externalReference).orElseThrow(() -> {
            logger.warn("[Repository][Order] Pedido não encontrado: identificador={}", externalReference);

            return new FastFoodException("Order not found with identifier:" + externalReference, "Order Not Found", HttpStatus.NOT_FOUND);
        });

        return OrderMapper.toDomain(orderEntity);
    }

    @Override
    public Order save(Order order) {
        logger.info("[Repository][Order] Salvando novo pedido");
        final OrderEntity entity = OrderMapper.toEntity(order);
        final OrderEntity save = orderEntityRepository.save(entity);
        logger.info("[Repository][Order] Pedido salvo com sucesso: id={}", save.getId());
        return OrderMapper.toDomain(save);
    }
}
