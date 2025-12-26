package com.postech.payment.fastfood.application.usecases.interfaces.payment;

import com.postech.payment.fastfood.domain.Payment;
import java.util.UUID;

public interface FindPaymentByOrderIdUseCase {
    Payment findPaymentByOrderId(UUID orderId);
}
