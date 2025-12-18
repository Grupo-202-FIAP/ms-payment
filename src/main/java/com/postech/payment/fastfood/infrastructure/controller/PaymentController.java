package com.postech.payment.fastfood.infrastructure.controller;

import com.postech.payment.fastfood.application.gateways.LoggerPort;
import com.postech.payment.fastfood.application.mapper.OrderMapper;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.CheckPaymentStatusUseCase;
import com.postech.payment.fastfood.application.usecases.interfaces.payment.GenerateQrCodePaymentUseCase;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.GenerateQrCodeResult;
import com.postech.payment.fastfood.infrastructure.controller.dto.request.OrderRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/qr-code")
    public ResponseEntity<GenerateQrCodeResult> generateQrCode(@RequestBody @Valid OrderRequest orderRequest) {
        logger.info("[Payment] Iniciando geração de QR Code para o pedido id={}", orderRequest.id());
        final GenerateQrCodeResult qrCode = generateQrCodePaymentUseCase.execute(OrderMapper.toDomain(orderRequest));
        logger.info("[Payment] QR Code gerado com sucesso para o pedido id={}", orderRequest.id());
        return ResponseEntity.ok(qrCode);
    }

    @GetMapping("/status")
    public ResponseEntity<String> checkPaymentStatus(@RequestParam("orderId") @NotNull UUID orderId) {
        logger.info("[Payment] Verificando status de pagamento para o pedido id={}", orderId);
        final String status = checkPaymentStatus.execute(orderId);
        logger.info("[Payment] Status de pagamento verificado para o pedido id={}: {}", orderId, status);
        return ResponseEntity.ok(status);
    }

}
