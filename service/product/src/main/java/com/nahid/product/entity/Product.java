package com.nahid.product.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false, unique = true, length = 100)
    String sku;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal price;

    @Column(name = "cost_price", precision = 12, scale = 2)
    BigDecimal costPrice;

    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity = 0;

    @Column(name = "min_stock_level")
    Integer minStockLevel = 0;


    @Column(name = "brand")
    String brand;

    @Column(precision = 8, scale = 3)
    BigDecimal weight;

    @Column(name = "is_active")
    Boolean isActive = true;

    @Column(name = "is_featured")
    Boolean isFeatured = false;

    @Column(name = "images_url")
    String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;
}