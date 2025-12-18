package com.postech.payment.fastfood.application.gateways;


import com.postech.payment.fastfood.domain.Order;
import java.util.UUID;

public interface OrderRepositoryPort {
    Order save(Order order);

    Order findById(UUID orderId);
    
    Order findByIdentifier(String externalReference);
}
