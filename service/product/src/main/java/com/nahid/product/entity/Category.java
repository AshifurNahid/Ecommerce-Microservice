package com.nahid.product.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category extends BaseEntity<Long> {

    @Column(nullable = false, unique = true, length = 100)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "is_active")
    Boolean isActive = true;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    List<Product> products;

}