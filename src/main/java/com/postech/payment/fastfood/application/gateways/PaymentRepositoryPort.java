package com.postech.payment.fastfood.application.gateways;

import com.postech.payment.fastfood.domain.Payment;

public interface PaymentRepositoryPort {
    void save(Payment payment);
}
