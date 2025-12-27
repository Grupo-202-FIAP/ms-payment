package com.postech.payment.fastfood.infrastructure.adapters.input.controller;

import com.postech.payment.fastfood.application.mapper.PaymentMapper;
import com.postech.payment.fastfood.application.ports.input.FindPaymentByOrderIdUseCase;
import com.postech.payment.fastfood.application.ports.output.LoggerPort;
import com.postech.payment.fastfood.infrastructure.adapters.input.controller.dto.response.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final FindPaymentByOrderIdUseCase findPaymentByOrderIdUseCase;

    public PaymentController(FindPaymentByOrderIdUseCase findPaymentByOrderIdUseCase, LoggerPort logger) {
        this.findPaymentByOrderIdUseCase = findPaymentByOrderIdUseCase;
    }

    @GetMapping("{orderId}")
    public ResponseEntity<PaymentResponse> findPaymentByOrderId(@PathVariable UUID orderId) {
        final PaymentResponse payment = PaymentMapper.toResponse(findPaymentByOrderIdUseCase.findPaymentByOrderId(orderId));
        return ResponseEntity.ok(payment);
    }

}
