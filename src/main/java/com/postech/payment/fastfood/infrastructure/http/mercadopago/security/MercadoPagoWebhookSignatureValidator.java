package com.postech.payment.fastfood.infrastructure.http.mercadopago.security;

import org.springframework.stereotype.Component;

@Component
public class MercadoPagoWebhookSignatureValidator {

//    private final LoggerPort loggerPort;
//
//    @Value("${mercadoPago.webhook.secretKey}")
//    private String secretKey;
//    @Value("${mercadoPago.webhook.secretKey}")
//    private String mercadoPagoWebhookSecret;
//
//    public MercadoPagoWebhookSignatureValidator(LoggerPort loggerPort) {
//        this.loggerPort = loggerPort;
//    }

  /*
    public boolean verifySignatureOfProvider(String signature, String xRequestId, String dataId) {
        if (isEmptyOrNull(signature) || isEmptyOrNull(xRequestId) || isEmptyOrNull(dataId)) {
            return false;
        }

        try {
            // Faz o split do signature: "ts=123456,v1=hash"
            final String[] signatureParts = signature.split(",");
            if (signatureParts.length != 2) {
                return false;
            }

            // Extrai o timestamp
            final String[] timestampPart = signatureParts[0].split("=");
            if (timestampPart.length != 2) {
                return false;
            }
            final String valueOfTimestamp = timestampPart[1];

            // Extrai a assinatura esperada
            final String[] xSignaturePart = signatureParts[1].split("=");
            if (xSignaturePart.length != 2) {
                return false;
            }
            final String valueOfXSignature = xSignaturePart[1];

            // Monta o template que será assinado
            final String signatureTemplateParsed = String.format("id:%s;request-id:%s;ts:%s;",
                    dataId, xRequestId, valueOfTimestamp);

            // Gera a assinatura usando HMAC-SHA256
            final String cyphedSignature = new HmacUtils("HmacSHA256", mercadoPagoWebhookSecret)
                    .hmacHex(signatureTemplateParsed);

            // Compara as assinaturas
            return valueOfXSignature.equals(cyphedSignature);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
    */

}

