package com.postech.payment.fastfood.application.gateways;

import com.postech.payment.fastfood.domain.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(UUID orderId);
}
