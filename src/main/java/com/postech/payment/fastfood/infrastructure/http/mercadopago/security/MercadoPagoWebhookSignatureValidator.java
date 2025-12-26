package com.postech.payment.fastfood.infrastructure.http.mercadopago.security;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoWebhookSignatureValidator {

    private final LoggerPort loggerPort;

    @Value("${mercadoPago.webhook.secretKey}")
    private String secretKey;
    @Value("${mercadoPago.webhook.secretKey}")
    private String mercadoPagoWebhookSecret;

    public MercadoPagoWebhookSignatureValidator(LoggerPort loggerPort) {
        this.loggerPort = loggerPort;
    }

    /**
     * Verifica a assinatura do provedor Mercado Pago
     *
     * @param signature  Header x-signature no formato "ts=123456,v1=hash"
     * @param xRequestId Header x-request-id
     * @param dataId     Query parameter data.id
     * @return true se a assinatura for válida
     */
    public boolean verifySignatureOfProvider(String signature, String xRequestId, String dataId) {
        if (isEmptyOrNull(signature) || isEmptyOrNull(xRequestId) || isEmptyOrNull(dataId)) {
            return false;
        }

        try {
            // Faz o split do signature: "ts=123456,v1=hash"
            String[] signatureParts = signature.split(",");
            if (signatureParts.length != 2) {
                return false;
            }

            // Extrai o timestamp
            String[] timestampPart = signatureParts[0].split("=");
            if (timestampPart.length != 2) {
                return false;
            }
            String valueOfTimestamp = timestampPart[1];

            // Extrai a assinatura esperada
            String[] xSignaturePart = signatureParts[1].split("=");
            if (xSignaturePart.length != 2) {
                return false;
            }
            String valueOfXSignature = xSignaturePart[1];

            // Monta o template que será assinado
            String signatureTemplateParsed = String.format("id:%s;request-id:%s;ts:%s;",
                    dataId, xRequestId, valueOfTimestamp);

            // Gera a assinatura usando HMAC-SHA256
            String cyphedSignature = new HmacUtils("HmacSHA256", mercadoPagoWebhookSecret)
                    .hmacHex(signatureTemplateParsed);

            // Compara as assinaturas
            return valueOfXSignature.equals(cyphedSignature);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se uma String é null ou vazia
     */
    private boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
}