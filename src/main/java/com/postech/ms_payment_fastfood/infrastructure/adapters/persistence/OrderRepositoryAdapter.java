package com.postech.ms_payment_fastfood.infrastructure.adapters.persistence;


import com.postech.ms_payment_fastfood.application.gateways.LoggerPort;
import com.postech.ms_payment_fastfood.application.gateways.OrderRepositoryPort;
import com.postech.ms_payment_fastfood.application.mapper.OrderMapper;
import com.postech.ms_payment_fastfood.domain.Order;
import com.postech.ms_payment_fastfood.domain.exception.FastFoodException;
import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.OrderEntity;
import com.postech.ms_payment_fastfood.infrastructure.persistence.repository.order.IOrderEntityRepository;
import com.postech.ms_payment_fastfood.infrastructure.persistence.repository.order.IOrderItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final IOrderEntityRepository orderEntityRepository;
    private final IOrderItemRepository orderItemRepository;
    private final LoggerPort logger;

    public OrderRepositoryAdapter(IOrderEntityRepository orderEntityRepository, IOrderItemRepository orderItemRepository, LoggerPort logger) {
        this.orderEntityRepository = orderEntityRepository;
        this.orderItemRepository = orderItemRepository;
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
    public List<Order> findAll() {
        logger.info("[Repository][Order] Buscando todos os pedidos");

        final List<Order> orderList = this.orderEntityRepository.findAll().stream().map((order) -> OrderMapper.toDomain(order)).collect(Collectors.toList());

        if (orderList.isEmpty()) {
            logger.warn("[Repository][Order] Nenhum pedido encontrado");

            throw new FastFoodException("Nenhum pedido encontrado", "Não há pedidos registrados no sistema", HttpStatus.NOT_FOUND);
        }

        logger.info("[Repository][Order] {} pedidos encontrados", orderList.size());
        return orderList;
    }

    @Override
    public List<Order> findByStatus() {
        logger.info("[Repository][Order] Buscando pedidos ordenados por status");

        final List<Order> orderList = this.orderEntityRepository
                .findOrdersByStatus()
                .stream()
                .map((order) -> OrderMapper.toDomain(order))
                .collect(Collectors.toList());

        logger.info("[Repository][Order] {} pedidos encontrados ", orderList.size());
        return orderList;
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
