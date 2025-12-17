package com.postech.ms_payment_fastfood.application.gateways;

import java.util.UUID;

public interface PaymentRepositoryPort {

    void save(UUID orderId);
}
