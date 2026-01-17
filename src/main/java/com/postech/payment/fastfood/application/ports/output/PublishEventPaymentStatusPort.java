package com.postech.payment.fastfood.application.ports.output;

public interface PublishEventPaymentStatusPort {
    void publish(Object object);
}
