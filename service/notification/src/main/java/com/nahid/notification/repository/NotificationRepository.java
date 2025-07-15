package com.nahid.notification.repository;

import com.nahid.notification.entity.Notification;
import com.nahid.notification.enums.NotificationStatus;
import com.nahid.notification.enums.NotificationType;
import com.nahid.notification.enums.ReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByReferenceIdAndReferenceType(UUID referenceId, ReferenceType referenceType);

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Page<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    List<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    List<Notification> findByNotificationTypeAndStatusOrderByCreatedAtDesc(
            NotificationType notificationType,
            NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < 3 ORDER BY n.createdAt ASC")
    List<Notification> findFailedNotificationsForRetry(@Param("status") NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    long countByStatus(NotificationStatus status);

    long countByNotificationTypeAndStatus(NotificationType notificationType, NotificationStatus status);

    boolean existsByReferenceIdAndReferenceType(UUID referenceId, ReferenceType referenceType);
}
