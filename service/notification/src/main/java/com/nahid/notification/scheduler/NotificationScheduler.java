package com.nahid.notification.scheduler;

import com.nahid.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Retry failed notifications every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void retryFailedNotifications() {
        log.info("Scheduled retry of failed notifications started");

        try {
            notificationService.retryFailedNotifications();
            log.info("Scheduled retry of failed notifications completed");
        } catch (Exception e) {
            log.error("Error during scheduled retry of failed notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Log notification statistics every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void logNotificationStats() {
        log.info("Logging notification statistics");

        try {
            // You can implement statistics logging here
            // For example, count notifications by status, type, etc.
            log.info("Notification statistics logged successfully");
        } catch (Exception e) {
            log.error("Error logging notification statistics: {}", e.getMessage(), e);
        }
    }
}