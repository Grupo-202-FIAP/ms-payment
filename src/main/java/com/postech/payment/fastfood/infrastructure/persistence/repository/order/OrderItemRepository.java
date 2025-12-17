package com.postech.payment.fastfood.infrastructure.persistence.repository.order;


import com.postech.payment.fastfood.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
}
