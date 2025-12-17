package com.postech.ms_payment_fastfood.infrastructure.persistence.repository.payment;


import com.postech.ms_payment_fastfood.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPaymentEntityRepository extends JpaRepository<PaymentEntity, UUID> {
}
