package com.postech.payment.fastfood.application.ports.input;

import java.util.UUID;

public interface RollbackPaymentUseCase {
    void execute(UUID orderID);
}
