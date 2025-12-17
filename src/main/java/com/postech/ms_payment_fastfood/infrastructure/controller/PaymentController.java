package com.postech.ms_payment_fastfood.infrastructure.controller;


import com.postech.ms_payment_fastfood.application.gateways.LoggerPort;
import com.postech.ms_payment_fastfood.application.usecases.interfaces.payment.CheckPaymentStatusUseCase;
import com.postech.ms_payment_fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.ms_payment_fastfood.domain.Order;
import com.postech.ms_payment_fastfood.infrastructure.controller.dto.request.OrderRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase;
    private final CheckPaymentStatusUseCase checkPaymentStatus;
    private final LoggerPort logger;

    public PaymentController(
            GenerateQrCodePaymentUseCase generateQrCodePaymentUseCase,
            CheckPaymentStatusUseCase checkPaymentStatus,
            LoggerPort logger) {
        this.generateQrCodePaymentUseCase = generateQrCodePaymentUseCase;
        this.checkPaymentStatus = checkPaymentStatus;
        this.logger = logger;
    }

    @PostMapping("/generate-qr-code")
    public ResponseEntity<String> generateQrCode(@RequestBody OrderRequest order) {
        logger.info("[Payment] Iniciando geração de QR Code para o pedido id={}", order.id());
        final String qrCode = generateQrCodePaymentUseCase.execute(order);
        logger.info("[Payment] QR Code gerado com sucesso para o pedido id={}", order.id());
        return ResponseEntity.ok(qrCode);
    }

    @GetMapping("/check-status")
    public ResponseEntity<String> checkPaymentStatus(@RequestParam("orderId") @NotNull UUID orderId) {
        logger.info("[Payment] Verificando status de pagamento para o pedido id={}", orderId);
        final String status = checkPaymentStatus.execute(orderId);
        logger.info("[Payment] Status de pagamento verificado para o pedido id={}: {}", orderId, status);
        return ResponseEntity.ok(status);
    }

}
