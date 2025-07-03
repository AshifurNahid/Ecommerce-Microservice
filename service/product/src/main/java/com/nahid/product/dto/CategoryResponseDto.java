package com.nahid.product.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponseDto {

    Long id;
    String name;
    String description;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}