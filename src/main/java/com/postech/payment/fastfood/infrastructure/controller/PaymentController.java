package com.postech.payment.fastfood.infrastructure.controller;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.mapper.PaymentMapper;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GeneratedQrCodeResponse;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.PaymentQrCodeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;
    private final LoggerPort logger;

    public PaymentController(
            GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase,
            LoggerPort logger) {
        this.generateQrCodePaymentUseCase = generateQrCodePaymentUseCase;
        this.logger = logger;
    }

    @PostMapping("/qr-code")
    public ResponseEntity<GeneratedQrCodeResponse> generateQrCode(@RequestBody @Valid PaymentQrCodeRequest paymentQrCodeReques) {
        logger.info("[Payment] Iniciando geração de QR Code para o pedido id={}", paymentQrCodeReques.orderId());
        final GeneratedQrCodeResponse qrCode = generateQrCodePaymentUseCase
                .execute(
                        PaymentMapper.toDomain(paymentQrCodeReques),
                        paymentQrCodeReques.items()
                );
        logger.info(
                "[Payment] QR Code gerado com sucesso para o pedido id={}",
                paymentQrCodeReques.orderId());
        return ResponseEntity.ok(qrCode);
    }
}
