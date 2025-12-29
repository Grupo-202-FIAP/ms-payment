package com.postech.payment.fastfood.infrastructure.config;

import com.postech.payment.fastfood.application.ports.input.FindPaymentByOrderIdUseCase;
import com.postech.payment.fastfood.application.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.ports.input.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.application.ports.input.RollbackPaymentUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.PaymentPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.domain.services.FindPaymentByOrderIdUseCaseImpl;
import com.postech.payment.fastfood.domain.services.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.domain.services.ProcessPaymentNotificationUseCaseImpl;
import com.postech.payment.fastfood.domain.services.RollbackPaymentUseCaseImpl;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseBeanConfiguration {

    @Bean
    public GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase(
            PaymentPort paymentPort,
            LoggerPort loggerPort, PaymentRepositoryPort paymentRepositoryPort, PublishEventPaymentStatusPort publishEventPaymentStatusPort) {
        return new GenerateQrCodePaymentUseCaseImpl(
                paymentPort,
                loggerPort,
                paymentRepositoryPort,
                publishEventPaymentStatusPort
        );
    }

    @Bean
    public ProcessPaymentNotificationUseCase processPaymentNotificationUseCase(
            MercadoPagoWebhookSignatureValidator mercadoPagoWebhookSignatureValidator,
            LoggerPort logger, PaymentRepositoryPort paymentRepositoryPort, PublishEventPaymentStatusPort publishEventPaymentStatusPort) {
        return new ProcessPaymentNotificationUseCaseImpl(
                mercadoPagoWebhookSignatureValidator, paymentRepositoryPort, publishEventPaymentStatusPort,
                logger);
    }

    @Bean
    public FindPaymentByOrderIdUseCase findPaymentByOrderIdUseCase(
            PaymentRepositoryPort paymentRepositoryPort,
            LoggerPort loggerPort
    ) {
        return new FindPaymentByOrderIdUseCaseImpl(paymentRepositoryPort, loggerPort);
    }

    @Bean
    public RollbackPaymentUseCase rollbackPaymentUseCase (
            PaymentRepositoryPort paymentRepositoryPort
    ) {
        return new RollbackPaymentUseCaseImpl(paymentRepositoryPort);
    }
}
