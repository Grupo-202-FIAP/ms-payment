package com.postech.payment.fastfood.infrastructure.persistence.repository.order;

import com.postech.payment.fastfood.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrderEntityRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByIdentifier(String externalReference);
}
