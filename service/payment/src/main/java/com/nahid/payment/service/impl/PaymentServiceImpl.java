package com.nahid.payment.service.impl;

import com.nahid.payment.dto.PaymentRequestDto;
import com.nahid.payment.dto.PaymentResponseDto;
import com.nahid.payment.entity.Payment;
import com.nahid.payment.enums.PaymentStatus;
import com.nahid.payment.exception.PaymentException;
import com.nahid.payment.exception.PaymentNotFoundException;
import com.nahid.payment.mapper.PaymentMapper;
import com.nahid.payment.producer.PaymentNotificationProducer;
import com.nahid.payment.repository.PaymentRepository;
import com.nahid.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentNotificationProducer notificationProducer;

    @Override
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        log.info("Processing payment for order: {}, customer: {}, amount: {}",
                requestDto.getOrderId(), requestDto.getCustomerId(), requestDto.getAmount());

        // Check if payment already exists for this order
        if (paymentRepository.existsByOrderId(requestDto.getOrderId())) {
            log.warn("Payment already exists for order: {}", requestDto.getOrderId());
            throw new PaymentException("Payment already exists for order: " + requestDto.getOrderId());
        }

        // Create payment entity
        Payment payment = paymentMapper.toEntity(requestDto);

        try {
            // Simulate payment processing (in real scenario, integrate with payment gateway)
            payment.setStatus(PaymentStatus.PROCESSING);
            payment = paymentRepository.save(payment);

            // Simulate payment gateway processing
            boolean paymentSuccess = simulatePaymentGateway(payment);

            if (paymentSuccess) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(generateTransactionId());
                payment.setProcessedAt(LocalDateTime.now());
                payment.setGatewayResponse("Payment processed successfully");

                payment = paymentRepository.save(payment);

                log.info("Payment processed successfully. Payment ID: {}, Transaction ID: {}",
                        payment.getId(), payment.getTransactionId());

                // Send notification to Kafka
                notificationProducer.sendPaymentNotification(payment);

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment gateway declined the transaction");
                payment.setProcessedAt(LocalDateTime.now());

                payment = paymentRepository.save(payment);

                log.error("Payment failed for order: {}, reason: {}",
                        requestDto.getOrderId(), payment.getFailureReason());
            }

        } catch (Exception e) {
            log.error("Error processing payment for order: {}", requestDto.getOrderId(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("System error: " + e.getMessage());
            payment.setProcessedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);
        }

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    public PaymentResponseDto cancelPayment(UUID paymentId) {
        log.info("Cancelling payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot cancel completed payment: " + paymentId);
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setProcessedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        log.info("Payment cancelled successfully. Payment ID: {}", paymentId);

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    public PaymentResponseDto refundPayment(UUID paymentId, BigDecimal refundAmount) {
        log.info("Processing refund for payment: {}, amount: {}", paymentId, refundAmount);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot refund payment that is not completed: " + paymentId);
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount cannot be greater than payment amount");
        }

        // In real scenario, process refund through payment gateway
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        log.info("Payment refunded successfully. Payment ID: {}, Refund Amount: {}", paymentId, refundAmount);

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCustomerTotalPaidAmount(String customerId) {
        log.info("Calculating total paid amount for customer: {}", customerId);

        BigDecimal totalAmount = paymentRepository.getTotalAmountByCustomerAndStatus(
                customerId, PaymentStatus.COMPLETED);

        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getRecentPayments() {
        log.info("Fetching recent payments (last 24 hours)");

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Payment> payments = paymentRepository.findRecentPayments(since);

        return payments.stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }



    @Override
    public PaymentResponseDto retryFailedPayment(UUID paymentId) {
        log.info("Retrying failed payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new PaymentException("Can only retry failed payments: " + paymentId);
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setFailureReason(null);
        payment = paymentRepository.save(payment);

        // Simulate payment gateway processing
        boolean paymentSuccess = simulatePaymentGateway(payment);

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(generateTransactionId());
            payment.setProcessedAt(LocalDateTime.now());
            payment.setGatewayResponse("Payment processed successfully on retry");

            payment = paymentRepository.save(payment);

            log.info("Payment retry successful. Payment ID: {}, Transaction ID: {}",
                    payment.getId(), payment.getTransactionId());

            // Send notification to Kafka
            notificationProducer.sendPaymentNotification(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway declined the transaction on retry");
            payment.setProcessedAt(LocalDateTime.now());

            payment = paymentRepository.save(payment);

            log.error("Payment retry failed for payment: {}", paymentId);
        }

        return paymentMapper.toResponseDto(payment);
    }

    /**
     * Simulate payment gateway processing
     * In real scenario, integrate with actual payment gateway (Stripe, PayPal, etc.)
     */
    private boolean simulatePaymentGateway(Payment payment) {
        try {
            // Simulate processing delay
            Thread.sleep(100);

            // Simulate 95% success rate
            return Math.random() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(UUID paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(UUID orderId) {
        log.info("Fetching payment for order: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByCustomerId(String customerId) {
        log.info("Fetching payments for customer: {}", customerId);

        List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        return payments.stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
        log.info("Fetching payments with status: {}", status);

        List<Payment> payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status);

        return payments.stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment with transaction ID: {}", transactionId);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with transaction ID: " + transactionId));

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    public PaymentResponseDto updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        log.info("Updating payment status. Payment ID: {}, New Status: {}", paymentId, status);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        payment.setStatus(status);
        payment.setProcessedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponseDto(payment);

    }
}