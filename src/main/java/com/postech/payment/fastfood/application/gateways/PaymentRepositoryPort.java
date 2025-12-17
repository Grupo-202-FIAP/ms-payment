package com.postech.payment.fastfood.application.gateways;

import java.util.UUID;

public interface PaymentRepositoryPort {

    void save(UUID orderId);
}
