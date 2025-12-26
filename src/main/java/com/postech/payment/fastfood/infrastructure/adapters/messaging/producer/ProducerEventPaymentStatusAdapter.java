package com.postech.payment.fastfood.infrastructure.adapters.messaging.producer;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.PublishEventPaymentStatusPort;
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
    @Value("${spring.cloud.aws.sns.paymentCallbackTopic}")
    private String topicArn;

    public ProducerEventPaymentStatusAdapter(SnsTemplate snsTemplate, LoggerPort logger) {
        this.snsTemplate = snsTemplate;
        this.logger = logger;
    }

    @Override
    public void publish(Object message) {
        try {
            logger.info("[Adapter][SNS] Publicando evento no tópico: {}", topicArn);
            snsTemplate.convertAndSend(topicArn, message);
        } catch (SnsException e) {
            logger.error("[Adapter][SNS] Erro no serviço SNS para o tópico: {}", topicArn, e);
            throw new MessagingException("Falha no serviço de mensageria", e);

        } catch (SdkClientException e) {
            logger.error("[Adapter][SNS] Erro de conectividade com AWS SNS: {}", topicArn, e);
            throw new MessagingException("Falha de conexão com provedor de mensagens", e);
        }
    }
}
