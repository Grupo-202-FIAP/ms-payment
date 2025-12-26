package com.postech.payment.fastfood.application.usecases.ports.input;

import com.postech.payment.fastfood.domain.model.Payment;
import java.util.UUID;

public interface FindPaymentByOrderIdUseCase {
    Payment findPaymentByOrderId(UUID orderId);
}
