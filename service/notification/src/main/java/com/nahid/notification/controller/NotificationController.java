package com.nahid.notification.controller;

import com.nahid.notification.dto.NotificationDto;
import com.nahid.notification.dto.NotificationResponseDto;
import com.nahid.notification.entity.Notification;
import com.nahid.notification.enums.NotificationStatus;
import com.nahid.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1//notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable UUID id) {
        log.info("Fetching notification with ID: {}", id);

        try {
            NotificationDto notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            log.error("Notification not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByUserId(
            @PathVariable String userId) {
        log.info("Fetching notifications for user: {}", userId);

        List<NotificationResponseDto> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByuserIdPaginated(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching paginated notifications for user: {}, page: {}, size: {}",
                userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<NotificationDto> updateNotificationStatus(
            @PathVariable UUID id,
            @RequestParam NotificationStatus status) {
        log.info("Updating notification status to {} for ID: {}", status, id);

        try {
            NotificationDto updatedNotification = notificationService.updateNotificationStatus(id, status);
            return ResponseEntity.ok(updatedNotification);
        } catch (RuntimeException e) {
            log.error("Failed to update notification status for ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/failed")
    public ResponseEntity<List<NotificationResponseDto>> getFailedNotifications() {
        log.info("Fetching failed notifications");

        List<NotificationResponseDto> failedNotifications = notificationService.getFailedNotifications();
        return ResponseEntity.ok(failedNotifications);
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        log.info("Triggering retry for failed notifications");

        try {
            notificationService.retryFailedNotifications();
            return ResponseEntity.ok("Failed notifications retry process initiated");
        } catch (Exception e) {
            log.error("Error during retry process: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred during retry process");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification service is healthy");
    }

    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(@RequestBody NotificationDto notificationDto) {
        log.info("Creating manual notification for user: {}", notificationDto.getUserId());

        try {
            NotificationDto createdNotification = notificationService.createNotification(notificationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}