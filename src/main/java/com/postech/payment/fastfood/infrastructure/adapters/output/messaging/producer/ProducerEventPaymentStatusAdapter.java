package com.postech.payment.fastfood.infrastructure.adapters.output.messaging.producer;

import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sns.model.SnsException;

@Component
public class ProducerEventPaymentStatusAdapter implements PublishEventPaymentStatusPort {
    private final SnsTemplate snsTemplate;
    private final LoggerPort logger;
    private final String topicArn;

    public ProducerEventPaymentStatusAdapter(SnsTemplate snsTemplate, LoggerPort logger,
                                            @Value("${spring.cloud.aws.sns.paymentCallbackTopic}") String topicArn) {
        this.snsTemplate = snsTemplate;
        this.logger = logger;
        this.topicArn = topicArn;
    }

    @Override
    public void publish(Object message) {
        try {
            logger.info("[ADAPTER][SNS] Publishing event to topic: {}", topicArn);
            snsTemplate.convertAndSend(topicArn, message);
        } catch (SnsException e) {
            logger.error("[ADAPTER][SNS] SNS service error for topic: {}", topicArn, e);
            throw new MessagingException("Messaging service failure", e);

        } catch (SdkClientException e) {
            logger.error("[ADAPTER][SNS] AWS SNS connectivity error: {}", topicArn, e);
            throw new MessagingException("Connection failure with message provider", e);
        }
    }
}
