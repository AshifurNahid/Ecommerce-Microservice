package com.nahid.order.repository;

import com.nahid.order.entity.Order;
import com.nahid.order.enums.OrderStatus;
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
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId AND o.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") String customerId,
                                    @Param("status") OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);
}