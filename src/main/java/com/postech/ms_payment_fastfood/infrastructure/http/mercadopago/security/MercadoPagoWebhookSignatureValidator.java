package com.postech.ms_payment_fastfood.infrastructure.http.mercadopago.security;


import com.postech.ms_payment_fastfood.application.gateways.LoggerPort;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoWebhookSignatureValidator {

    private final LoggerPort loggerPort;

    @Value("${mercadoPago.webhook.secretKey}")
    private String secretKey;

    public MercadoPagoWebhookSignatureValidator(LoggerPort loggerPort) {
        this.loggerPort = loggerPort;
    }

    public Boolean isValid(String idUrl, String xrequestId, String xsignature) {

        try {
            final String ts = xsignature.split(",")[0].split("=")[1];
            final String expectedSignature = xsignature.split(",")[1].split("=")[1]; // valor do v1

            final String signedTemplate = String.format("id:%s;request-id:%s;ts:%s;", idUrl, xrequestId, ts);

            final String generatedSignature = new HmacUtils("HmacSHA256", secretKey).hmacHex(signedTemplate);

            return generatedSignature.equals(expectedSignature);

        } catch (NullPointerException | IllegalArgumentException e) {
            loggerPort.error("[Webhook][Payment] Error validating signature: {}", e.getMessage());
            return false;
        }
    }

}
