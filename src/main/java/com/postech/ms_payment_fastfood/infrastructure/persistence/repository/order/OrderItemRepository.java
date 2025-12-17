package com.postech.ms_payment_fastfood.infrastructure.persistence.repository.order;


import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
}
