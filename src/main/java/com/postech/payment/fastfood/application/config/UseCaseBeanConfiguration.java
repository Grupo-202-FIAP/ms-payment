package com.postech.payment.fastfood.application.config;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.gateways.MercadoPagoPort;
import com.postech.payment.fastfood.application.gateways.PaymentRepositoryPort;
import com.postech.payment.fastfood.application.usecases.implementation.payment.GenerateQrCodePaymentUseCaseImpl;
import com.postech.payment.fastfood.application.usecases.implementation.payment.ProcessPaymentNotificationUseCaseImpl;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.ProcessPaymentNotificationUseCase;
import com.postech.payment.fastfood.infrastructure.http.mercadopago.security.MercadoPagoWebhookSignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseBeanConfiguration {
    @Bean
    public GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase(
            MercadoPagoPort mercadoPagoPort,
            LoggerPort loggerPort, PaymentRepositoryPort paymentRepositoryPort) {
        return new GenerateQrCodePaymentUseCaseImpl(mercadoPagoPort, loggerPort, paymentRepositoryPort);
    }

    @Bean
    public ProcessPaymentNotificationUseCase processPaymentNotificationUseCase(
            MercadoPagoWebhookSignatureValidator mercadoPagoWebhookSignatureValidator,
            LoggerPort logger) {
        return new ProcessPaymentNotificationUseCaseImpl(
                mercadoPagoWebhookSignatureValidator,
                logger);
    }


}
