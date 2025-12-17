package com.postech.payment.fastfood.application.gateways;


import com.postech.payment.fastfood.domain.Order;
import java.util.List;
import java.util.UUID;

public interface OrderRepositoryPort {
    Order save(Order order);

    Order findById(UUID orderId);

    List<Order> findAll();

    List<Order> findByStatus();

    Order findByIdentifier(String externalReference);
}
