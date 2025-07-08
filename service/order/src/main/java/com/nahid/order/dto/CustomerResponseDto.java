package com.nahid.order.dto;

import com.nahid.order.enums.CustomerStatus;
import com.nahid.order.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDateTime dateOfBirth;
    private Gender gender;
    private List<CustomerAddressDto> addresses;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}