package com.nahid.payment.controller;

import com.nahid.payment.dto.PaymentRequestDto;
import com.nahid.payment.dto.PaymentResponseDto;
import com.nahid.payment.enums.PaymentStatus;
import com.nahid.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> processPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentResponseDto response = paymentService.processPayment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable UUID paymentId) {
        PaymentResponseDto response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable UUID orderId) {
        PaymentResponseDto response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByTransactionId(@PathVariable String transactionId) {
        PaymentResponseDto response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByCustomerId(@PathVariable Long userId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PaymentResponseDto>> getRecentPayments() {
        List<PaymentResponseDto> payments = paymentService.getRecentPayments();
        return ResponseEntity.ok(payments);
    }


    @GetMapping("/User/{userId}/total")
    public ResponseEntity<BigDecimal> getUserTotalPaidAmount(@PathVariable Long userId) {
        BigDecimal totalAmount = paymentService.getUserTotalPaidAmount(userId);
        return ResponseEntity.ok(totalAmount);
    }


    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponseDto> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestParam PaymentStatus status) {
        PaymentResponseDto response = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable UUID paymentId) {
        PaymentResponseDto response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam BigDecimal refundAmount) {
        PaymentResponseDto response = paymentService.refundPayment(paymentId, refundAmount);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponseDto> retryFailedPayment(@PathVariable UUID paymentId) {
        PaymentResponseDto response = paymentService.retryFailedPayment(paymentId);
        return ResponseEntity.ok(response);
    }
}