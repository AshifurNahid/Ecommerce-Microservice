package com.nahid.product.entity;

import com.nahid.product.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_code", nullable = false, unique = true)
    private String reservationCode;

    @Column(name = "order_reference", nullable = false, unique = true)
    private String orderReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InventoryReservationItem> items = new ArrayList<>();

    public static InventoryReservation createNew(String orderReference) {
        return InventoryReservation.builder()
                .reservationCode(UUID.randomUUID().toString())
                .orderReference(orderReference)
                .status(ReservationStatus.RESERVED)
                .items(new ArrayList<>())
                .build();
    }

    public void addItem(InventoryReservationItem item) {
        item.setReservation(this);
        this.items.add(item);
    }
}
