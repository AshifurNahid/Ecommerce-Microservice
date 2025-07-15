package com.nahid.notification.service.impl;

import com.nahid.notification.dto.NotificationDto;
import com.nahid.notification.dto.NotificationResponseDto;
import com.nahid.notification.dto.OrderEventDto;
import com.nahid.notification.dto.PaymentNotificationDto;
import com.nahid.notification.entity.Notification;
import com.nahid.notification.enums.NotificationStatus;
import com.nahid.notification.enums.ReferenceType;
import com.nahid.notification.mapper.NotificationMapper;
import com.nahid.notification.repository.NotificationRepository;
import com.nahid.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationDto createNotification(NotificationDto notificationDto) {
        log.info("Creating notification for customer: {}", notificationDto.getCustomerId());

        Notification notification = notificationMapper.toEntity(notificationDto);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(0);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getId());

        return notificationMapper.toDto(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(UUID id) {
        log.debug("Fetching notification with ID: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));

        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByCustomerId(String customerId) {
        log.debug("Fetching notifications for customer: {}", customerId);

        List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return notifications.stream()
                .map(notificationMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByCustomerId(String customerId, Pageable pageable) {
        log.debug("Fetching paginated notifications for customer: {}", customerId);

        Page<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return notifications.map(notificationMapper::toResponseDto);
    }

    @Override
    public NotificationDto updateNotificationStatus(UUID id, NotificationStatus status) {
        log.info("Updating notification status to {} for ID: {}", status, id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));

        notification.setStatus(status);
        if (status == NotificationStatus.SENT) {
            notification.setSentAt(LocalDateTime.now());
        }

        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification status updated successfully for ID: {}", id);

        return notificationMapper.toDto(updatedNotification);
    }

    @Override
    public void processPaymentNotification(PaymentNotificationDto paymentNotificationDto) {
        log.info("Processing payment notification for payment ID: {}", paymentNotificationDto.getPaymentId());

        try {
            // Check for duplicate notifications
            if (isDuplicateNotification(paymentNotificationDto.getPaymentId(),ReferenceType.PAYMENT)) {
                log.warn("Duplicate payment notification detected for payment ID: {}", paymentNotificationDto.getPaymentId());
                return;
            }

            // Create notification entity
            Notification notification = notificationMapper.paymentDtoToEntity(paymentNotificationDto);

            // Generate message if not provided
            if (notification.getMessage() == null || notification.getMessage().isEmpty()) {
                notification.setMessage(generatePaymentMessage(paymentNotificationDto));
            }

            // Save notification
            Notification savedNotification = notificationRepository.save(notification);

            // Send notification based on type
            switch (notification.getNotificationType()) {
                case SMS -> sendSmsNotification(savedNotification);
                case EMAIL -> sendEmailNotification(savedNotification);
                default -> log.warn("Unsupported notification type: {}", notification.getNotificationType());
            }

            log.info("Payment notification processed successfully for payment ID: {}", paymentNotificationDto.getPaymentId());

        } catch (Exception e) {
            log.error("Error processing payment notification for payment ID: {}. Error: {}",
                    paymentNotificationDto.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process payment notification", e);
        }
    }

    @Override
    public void processOrderNotification(OrderEventDto orderEventDto) {
        log.info("Processing order notification for order ID: {}", orderEventDto.getOrderId());

        try {
            // Check for duplicate notifications
            if (isDuplicateNotification(orderEventDto.getOrderId(), ReferenceType.ORDER)) {
                log.warn("Duplicate order notification detected for order ID: {}", orderEventDto.getOrderId());
                return;
            }

            // Create notification entity
            Notification notification = notificationMapper.orderDtoToEntity(orderEventDto);

            // Save notification
            Notification savedNotification = notificationRepository.save(notification);

            // Send SMS notification (default for orders)
            sendSmsNotification(savedNotification);

            log.info("Order notification processed successfully for order ID: {}", orderEventDto.getOrderId());

        } catch (Exception e) {
            log.error("Error processing order notification for order ID: {}. Error: {}",
                    orderEventDto.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process order notification", e);
        }
    }

    @Override
    public void retryFailedNotifications() {
        log.info("Starting retry process for failed notifications");

        List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(NotificationStatus.FAILED);

        log.info("Found {} failed notifications for retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                log.info("Retrying notification ID: {}", notification.getId());

                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setStatus(NotificationStatus.RETRY);

                // Send notification based on type
                switch (notification.getNotificationType()) {
                    case SMS -> sendSmsNotification(notification);
                    case EMAIL -> sendEmailNotification(notification);
                    default -> log.warn("Unsupported notification type: {}", notification.getNotificationType());
                }

            } catch (Exception e) {
                log.error("Error retrying notification ID: {}. Error: {}",
                        notification.getId(), e.getMessage(), e);

                notification.setErrorMessage(e.getMessage());
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
            }
        }

        log.info("Retry process completed");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getFailedNotifications() {
        log.debug("Fetching failed notifications");

        List<Notification> failedNotifications = notificationRepository
                .findByStatusOrderByCreatedAtDesc(NotificationStatus.FAILED);

        return failedNotifications.stream()
                .map(notificationMapper::toResponseDto)
                .toList();
    }

    @Override
    public void sendSmsNotification(Notification notification) {
        log.info("Sending SMS notification to: {} for notification ID: {}",
                notification.getCustomerPhone(), notification.getId());

        try {
            // Simulate SMS sending - replace with actual SMS service integration
            simulateSmsService(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);

            notificationRepository.save(notification);

            log.info("SMS notification sent successfully for notification ID: {}", notification.getId());

        } catch (Exception e) {
            log.error("Failed to send SMS notification for ID: {}. Error: {}",
                    notification.getId(), e.getMessage(), e);

            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);

            throw new RuntimeException("Failed to send SMS notification", e);
        }
    }

    @Override
    public void sendEmailNotification(Notification notification) {
        log.info("Sending email notification to: {} for notification ID: {}",
                notification.getCustomerEmail(), notification.getId());

        try {
            // Simulate email sending - replace with actual email service integration
            simulateEmailService(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);

            notificationRepository.save(notification);

            log.info("Email notification sent successfully for notification ID: {}", notification.getId());

        } catch (Exception e) {
            log.error("Failed to send email notification for ID: {}. Error: {}",
                    notification.getId(), e.getMessage(), e);

            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);

            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicateNotification(UUID referenceId,ReferenceType referenceType) {
        return notificationRepository.existsByReferenceIdAndReferenceType(referenceId, referenceType);
    }

    private String generatePaymentMessage(PaymentNotificationDto paymentDto) {
        return switch (paymentDto.getStatus()) {
            case COMPLETED -> String.format("Payment of $%.2f %s has been processed successfully. Transaction ID: %s",
                    paymentDto.getAmount(), paymentDto.getCurrency(), paymentDto.getTransactionId());
            case FAILED -> String.format("Payment of $%.2f %s has failed. Please try again.",
                    paymentDto.getAmount(), paymentDto.getCurrency());
            case CANCELLED -> String.format("Payment of $%.2f %s has been cancelled.",
                    paymentDto.getAmount(), paymentDto.getCurrency());
            default -> String.format("Payment status update: %s for amount $%.2f %s",
                    paymentDto.getStatus(), paymentDto.getAmount(), paymentDto.getCurrency());
        };
    }

    private void simulateSmsService(Notification notification) {
        // Simulate SMS service call - replace with actual SMS provider (Twilio, AWS SNS, etc.)
        log.info("SMS Service: Sending SMS to {} - Message: {}",
                notification.getCustomerPhone(), notification.getMessage());

        // Simulate processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate occasional failures for testing
        if (Math.random() < 0.05) { // 5% failure rate
            throw new RuntimeException("SMS service temporarily unavailable");
        }

        log.info("SMS sent successfully to: {}", notification.getCustomerPhone());
    }

    private void simulateEmailService(Notification notification) {
        // Simulate email service call - replace with actual email provider (SendGrid, AWS SES, etc.)
        log.info("Email Service: Sending email to {} - Subject: Notification - Message: {}",
                notification.getCustomerEmail(), notification.getMessage());

        // Simulate processing time
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate occasional failures for testing
        if (Math.random() < 0.03) { // 3% failure rate
            throw new RuntimeException("Email service temporarily unavailable");
        }

        log.info("Email sent successfully to: {}", notification.getCustomerEmail());
    }
}