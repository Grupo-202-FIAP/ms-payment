package com.postech.payment.fastfood.infrastructure.config;

import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.application.ports.output.MercadoPagoPort;
import com.postech.payment.fastfood.application.ports.output.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.ports.output.PublishEventPaymentStatusPort;
import com.postech.payment.fastfood.application.usecases.implementation.payment.FindPaymentByOrderIdUseCaseImpl;
import com.postech.payment.fastfood.application.usecases.implementation.payment.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.application.usecases.implementation.payment.ProcessPaymentNotificationUseCaseImpl;
import com.postech.payment.fastfood.application.usecases.ports.input.FindPaymentByOrderIdUseCase;
import com.postech.payment.fastfood.application.usecases.ports.input.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.usecases.ports.input.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseBeanConfiguration {

    @Bean
    public GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase(
            MercadoPagoPort mercadoPagoPort,
            LoggerPort loggerPort, PaymentRepositoryPort paymentRepositoryPort, PublishEventPaymentStatusPort publishEventPaymentStatusPort) {
        return new GenerateQrCodePaymentUseCaseImpl(
                mercadoPagoPort,
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
}
