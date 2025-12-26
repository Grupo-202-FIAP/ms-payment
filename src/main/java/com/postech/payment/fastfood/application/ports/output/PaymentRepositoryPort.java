package com.postech.payment.fastfood.application.ports.output;

import com.postech.payment.fastfood.domain.model.Payment;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {
    void save(Payment payment);

    Optional<Payment> findByOrderId(UUID orderId);
}
