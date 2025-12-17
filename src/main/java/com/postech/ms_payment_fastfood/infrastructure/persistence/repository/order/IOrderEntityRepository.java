package com.postech.ms_payment_fastfood.infrastructure.persistence.repository.order;

import com.postech.ms_payment_fastfood.domain.enums.OrderStatus;
import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrderEntityRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByOrderStatus(OrderStatus status);

    @Query("SELECT o FROM OrderEntity o WHERE o.orderStatus != 'COMPLETED' "
            + "AND o.orderStatus IN ('RECEIVED', 'PREPARING', 'READY', 'CANCELLED') "
            + "ORDER BY "
            + "CASE WHEN o.orderStatus = 'READY' THEN 1 "
            + "     WHEN o.orderStatus = 'PREPARING' THEN 2 "
            + "     WHEN o.orderStatus = 'RECEIVED' THEN 3 "
            + "     WHEN o.orderStatus = 'CANCELLED' THEN 4 "
            + "     ELSE 5 END, "
            +
            "o.orderDateTime ASC")
    List<OrderEntity> findOrdersByStatus();

    Optional<OrderEntity> findByIdentifier(String externalReference);
}
