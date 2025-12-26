package com.postech.payment.fastfood.infrastructure.adapters.messaging.producer;

import com.postech.payment.fastfood.application.gateways.PublishEventPaymentStatusPort;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProducerEventPaymentStatusAdapter implements PublishEventPaymentStatusPort {
    private final SnsTemplate snsTemplate;
    @Value("${spring.cloud.aws.sns.paymentCallbackTopic}")
    private String topicArn;

    public ProducerEventPaymentStatusAdapter(SnsTemplate snsTemplate) {
        this.snsTemplate = snsTemplate;
    }

    public void publish(Object message) {
        snsTemplate.convertAndSend(topicArn, message);
    }
}
